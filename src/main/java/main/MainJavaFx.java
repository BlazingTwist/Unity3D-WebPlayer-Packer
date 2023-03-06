package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApplicationMemory;
import datatypes.UnityFileDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import reader.Unity3dReader;
import writer.Unity3dWriter;

public class MainJavaFx extends Application {

	private static final String memoryFileName = "memory.json";

	public static ApplicationMemory memory;

	private Stage stage;
	private VBox mainLayout;
	private StackPane dragDropPane;
	private HBox dragActionSelectorPane;

	private VBox unity3dReaderWindow;
	private VBox unity3dReaderWindow_filesVbox;
	private CheckBox unity3dReaderWindow_createDirectoryPerFileCheckBox;
	private HashMap<Pair<File, UnityFileDefinition>, CheckBox> unity3dReaderWindow_fileSelection;

	private FileChooser unpackFileChooser;
	private DirectoryChooser unpackTargetDirectoryChooser;
	private FileChooser repackFileChooser;
	private FileChooser repackTargetFileChooser;

	private List<File> targetFiles;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Runtime.getRuntime().addShutdownHook(new Thread(MainJavaFx::onShutdown));
		loadMemory();

		stage.setTitle("Unity3D Packer");
		mainLayout = new VBox();

		attachMenuBar(mainLayout);
		attachMainDropWindow(mainLayout);
		VBox.setVgrow(dragDropPane, Priority.ALWAYS);
		attachDragDropActionSelectorWindow();
		VBox.setVgrow(dragActionSelectorPane, Priority.ALWAYS);
		attachUnity3dReaderWindow();
		VBox.setVgrow(unity3dReaderWindow, Priority.ALWAYS);

		unpackFileChooser = new FileChooser();
		unpackFileChooser.setTitle("Select files to unpack");
		unpackTargetDirectoryChooser = new DirectoryChooser();
		unpackTargetDirectoryChooser.setTitle("Select directory to unpack to");
		repackFileChooser = new FileChooser();
		repackFileChooser.setTitle("Select files to repack");
		repackTargetFileChooser = new FileChooser();
		repackTargetFileChooser.setTitle("Select file to repack to");

		this.stage = stage;
		Scene scene = new Scene(mainLayout, 1024, 600);
		stage.setScene(scene);
		stage.show();
	}

	private void attachMenuBar(Pane target) {
		MenuBar menuBar = new MenuBar();
		target.getChildren().add(menuBar);

		Menu fileMenu = new Menu();
		fileMenu.setText("File");
		MenuItem fileMenuUnpackButton = new MenuItem("Unpack ...");
		MenuItem fileMenuRepackButton = new MenuItem("Repack ...");
		fileMenu.getItems().addAll(fileMenuUnpackButton, fileMenuRepackButton);

		fileMenuUnpackButton.setOnAction(e -> onUnpackPressed(true));
		fileMenuRepackButton.setOnAction(e -> onRepackPressed(true));

		Menu configMenu = new Menu();
		configMenu.setText("Settings");
		MenuItem unityWebPlayerVersionItem = new MenuItem("set WebPlayerVersion");
		MenuItem unityEngineVersionItem = new MenuItem("set UnityVersion");
		configMenu.getItems().addAll(unityWebPlayerVersionItem, unityEngineVersionItem);

		TextInputDialog textDialog = new TextInputDialog();
		unityWebPlayerVersionItem.setOnAction(e -> {
			textDialog.setTitle("Set WebPlayer Version");
			textDialog.setHeaderText("Current Version: " + memory.getUnityWebPlayerVersion());
			memory.setUnityWebPlayerVersion(textDialog.showAndWait().orElse(null));
		});
		unityEngineVersionItem.setOnAction(e -> {
			textDialog.setTitle("Set UnityEngine Version");
			textDialog.setHeaderText("Current Version: " + memory.getUnityEngineVersion());
			memory.setUnityEngineVersion(textDialog.showAndWait().orElse(null));
		});

		menuBar.getMenus().addAll(fileMenu, configMenu);
	}

	private void attachMainDropWindow(Pane target) {
		dragDropPane = new StackPane();
		target.getChildren().add(dragDropPane);

		Text text = new Text("Drag files here of use the 'File' menu to start (Un)packing");
		dragDropPane.getChildren().add(text);
		text.setTextAlignment(TextAlignment.CENTER);
		StackPane.setAlignment(text, Pos.CENTER);
		dragDropPane.setOnDragOver(event -> {
			if (event.getGestureSource() != dragDropPane && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			event.consume();
		});
		dragDropPane.setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			boolean success = false;
			if (dragboard.hasFiles()) {
				onDragDroppedFiles(dragboard.getFiles());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}

	private void attachDragDropActionSelectorWindow() {
		dragActionSelectorPane = new HBox();
		dragActionSelectorPane.setAlignment(Pos.CENTER);
		dragActionSelectorPane.setPadding(new Insets(10, 10, 10, 10));

		StackPane pane1 = new StackPane();
		StackPane pane2 = new StackPane();

		ImageView unpackImage = new ImageView(new Image("/javafx/unpack.png", 100, 100, true, true));
		ImageView repackImage = new ImageView(new Image("/javafx/repack.png", 100, 100, true, true));

		VBox unpackBox = new VBox();
		unpackBox.setAlignment(Pos.CENTER);
		Label unpackLabel = new Label("unpack files");
		unpackLabel.setAlignment(Pos.CENTER);
		unpackBox.getChildren().add(unpackLabel);
		Button unpackButton = new Button();
		unpackButton.setGraphic(unpackImage);
		unpackBox.getChildren().add(unpackButton);

		VBox repackBox = new VBox();
		repackBox.setAlignment(Pos.CENTER);
		Label repackLabel = new Label("repack files");
		repackBox.getChildren().add(repackLabel);
		repackLabel.setAlignment(Pos.CENTER);
		Button repackButton = new Button();
		repackButton.setGraphic(repackImage);
		repackBox.getChildren().add(repackButton);

		unpackButton.setOnAction(event -> onUnpackPressed(false));

		repackButton.setOnAction(event -> onRepackPressed(false));

		pane1.getChildren().add(unpackBox);
		pane2.getChildren().add(repackBox);
		StackPane.setAlignment(unpackBox, Pos.CENTER);
		StackPane.setAlignment(repackBox, Pos.CENTER);
		HBox.setHgrow(pane1, Priority.ALWAYS);
		HBox.setHgrow(pane2, Priority.ALWAYS);
		dragActionSelectorPane.getChildren().addAll(pane1, pane2);
	}

	private void attachUnity3dReaderWindow() {
		unity3dReaderWindow = new VBox();
		unity3dReaderWindow.setAlignment(Pos.CENTER);

		HBox headerHBox = new HBox();
		headerHBox.setAlignment(Pos.CENTER_LEFT);
		Button extractSelectionButton = new Button("Extract selected files");
		Button selectAllButton = new Button("Select all files");
		Button selectNoneButton = new Button("Deselect all files");
		unity3dReaderWindow_createDirectoryPerFileCheckBox = new CheckBox("create subDirectories");
		headerHBox.getChildren().addAll(extractSelectionButton, selectAllButton, selectNoneButton, unity3dReaderWindow_createDirectoryPerFileCheckBox);

		extractSelectionButton.setOnAction(event -> unity3dReaderWindow_onExtractFilesPressed());
		selectAllButton.setOnAction(event -> unity3dReaderWindow_onSelectAllPressed());
		selectNoneButton.setOnAction(event -> unity3dReaderWindow_onSelectNonePressed());

		ScrollPane unity3dReaderWindow_filesScrollPane = new ScrollPane();
		unity3dReaderWindow_filesVbox = new VBox();
		unity3dReaderWindow_filesVbox.setSpacing(24);
		unity3dReaderWindow_filesScrollPane.setContent(unity3dReaderWindow_filesVbox);
		unity3dReaderWindow_filesVbox.setAlignment(Pos.CENTER);
		unity3dReaderWindow_filesVbox.setPadding(new Insets(10, 10, 10, 10));

		unity3dReaderWindow.getChildren().addAll(headerHBox, unity3dReaderWindow_filesScrollPane);
	}

	private void onUnpackPressed(boolean openFileSelector) {
		if (openFileSelector) {
			if (memory.getLastUnpackSource() != null) {
				File file = new File(memory.getLastUnpackSource());
				if (file.exists() && file.isDirectory()) {
					unpackFileChooser.setInitialDirectory(file);
				}
			}
			targetFiles = unpackFileChooser.showOpenMultipleDialog(stage);
		}
		if (targetFiles == null) {
			openMainDropWindow();
			return;
		}
		memory.setLastUnpackSource(getDirectoryForMemory(targetFiles));

		Map<File, Unity3dReader> readers = targetFiles.stream()
				.filter(file -> file != null && file.exists())
				.map(file -> {
					try {
						FileInputStream inputStream = new FileInputStream(file);
						return new Pair<>(file, new Unity3dReader(inputStream.readAllBytes()));
					} catch (IOException e) {
						Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open file: '"
								+ file.getAbsolutePath() + "', Error: " + e);
						alert.showAndWait();
						return null;
					}
				}).filter(Objects::nonNull)
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		openUnity3dReaderWindow(readers);
	}

	private void onRepackPressed(boolean openFileSelector) {
		if (openFileSelector) {
			if (memory.getLastRepackSource() != null) {
				File file = new File(memory.getLastRepackSource());
				if (file.exists() && file.isDirectory()) {
					repackFileChooser.setInitialDirectory(file);
				}
			}
			targetFiles = repackFileChooser.showOpenMultipleDialog(stage);
		}
		if (targetFiles == null) {
			openMainDropWindow();
			return;
		}
		memory.setLastRepackSource(getDirectoryForMemory(targetFiles));

		if (memory.getLastRepackTarget() != null) {
			File file = new File(memory.getLastRepackTarget());
			if (file.isDirectory()) {
				repackTargetFileChooser.setInitialDirectory(file);
			} else {
				repackTargetFileChooser.setInitialDirectory(file.getParentFile());
				repackTargetFileChooser.setInitialFileName(file.getName());
			}
		}
		File repackTarget = repackTargetFileChooser.showSaveDialog(stage);
		if (repackTarget == null) {
			repackTarget = new File(memory.getLastRepackTarget());
		} else {
			memory.setLastRepackTarget(repackTarget.getAbsolutePath());
		}
		//noinspection ResultOfMethodCallIgnored
		repackTarget.getParentFile().mkdirs();

		List<File> fullTargetFileList = new ArrayList<>();
		for (File targetFile : targetFiles) {
			if (targetFile.isDirectory()) {
				try {
					fullTargetFileList.addAll(
							Files.walk(targetFile.toPath(), FileVisitOption.FOLLOW_LINKS)
									.filter(Files::isRegularFile)
									.map(Path::toFile)
									.collect(Collectors.toList()));
				} catch (IOException e) {
					Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to resolve directory, exception: " + e);
					alert.showAndWait();
				}
			} else {
				fullTargetFileList.add(targetFile);
			}
		}

		try {
			if (!repackTarget.exists()) {
				//noinspection ResultOfMethodCallIgnored
				repackTarget.createNewFile();
			}
			FileOutputStream outputStream = new FileOutputStream(repackTarget);
			Unity3dWriter.writeFromFiles(fullTargetFileList, outputStream, memory.getUnityWebPlayerVersion(), memory.getUnityEngineVersion());
			outputStream.close();
			Alert alert = new Alert(Alert.AlertType.INFORMATION, "Repacking complete.");
			alert.show();
		} catch (IOException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR, "Error during repacking: " + e);
			alert.showAndWait();
		}
		openMainDropWindow();
	}

	private void onDragDroppedFiles(List<File> files) {
		targetFiles = files;
		openDragActionSelectorWindow();
	}

	private void unity3dReaderWindow_onExtractFilesPressed() {
		if (unity3dReaderWindow_fileSelection == null) {
			return;
		}

		if (memory.getLastUnpackTarget() != null) {
			File file = new File(memory.getLastUnpackTarget());
			if (file.isDirectory()) {
				unpackTargetDirectoryChooser.setInitialDirectory(file);
			} else {
				unpackTargetDirectoryChooser.setInitialDirectory(file.getParentFile());
			}
		}
		File unpackTargetDirectory = unpackTargetDirectoryChooser.showDialog(stage);
		if (unpackTargetDirectory == null) {
			unpackTargetDirectory = new File(memory.getLastUnpackTarget());
		} else {
			memory.setLastUnpackTarget(unpackTargetDirectory.getAbsolutePath());
		}
		if (!unpackTargetDirectory.isDirectory() || !unpackTargetDirectory.exists()) {
			//noinspection ResultOfMethodCallIgnored
			unpackTargetDirectory.mkdirs();
		}

		int extractionCount = 0;
		for (Pair<File, UnityFileDefinition> key : unity3dReaderWindow_fileSelection.keySet()) {
			CheckBox box = unity3dReaderWindow_fileSelection.get(key);
			if (!box.isSelected()) {
				continue;
			}

			String targetDirectory = unpackTargetDirectory.getAbsolutePath();
			if (unity3dReaderWindow_createDirectoryPerFileCheckBox.isSelected()) {
				targetDirectory = targetDirectory + File.separator + key.getKey().getName();
			}
			File targetDirectoryFile = new File(targetDirectory);
			if (!targetDirectoryFile.exists() || !targetDirectoryFile.isDirectory()) {
				//noinspection ResultOfMethodCallIgnored
				targetDirectoryFile.mkdirs();
			}

			UnityFileDefinition fileDefinition = key.getValue();
			String targetFilePath = targetDirectory + File.separator + fileDefinition.getName();
			try {
				FileOutputStream outputStream = new FileOutputStream(targetFilePath);
				outputStream.write(fileDefinition.getBody());
				outputStream.close();
				extractionCount++;
			} catch (IOException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to extract file: " + targetFilePath + ", Error: " + e);
				alert.showAndWait();
			}
		}

		Alert alert = new Alert(Alert.AlertType.INFORMATION, "Finished Extracting " + extractionCount + " files.");
		alert.show();
		openMainDropWindow();
	}

	private void unity3dReaderWindow_onSelectAllPressed() {
		if (unity3dReaderWindow_fileSelection == null) {
			return;
		}

		for (CheckBox value : unity3dReaderWindow_fileSelection.values()) {
			value.setSelected(true);
		}
	}

	private void unity3dReaderWindow_onSelectNonePressed() {
		if (unity3dReaderWindow_fileSelection == null) {
			return;
		}

		for (CheckBox value : unity3dReaderWindow_fileSelection.values()) {
			value.setSelected(false);
		}
	}

	private void openMainDropWindow() {
		mainLayout.getChildren().remove(1);
		mainLayout.getChildren().add(dragDropPane);
	}

	private void openDragActionSelectorWindow() {
		mainLayout.getChildren().remove(1);
		mainLayout.getChildren().add(dragActionSelectorPane);
	}

	private void openUnity3dReaderWindow(Map<File, Unity3dReader> readers) {
		unity3dReaderWindow_filesVbox.getChildren().clear();
		unity3dReaderWindow_fileSelection = new HashMap<>();
		for (File readerFile : readers.keySet()) {
			Unity3dReader reader = readers.get(readerFile);
			VBox readerBox = new VBox();
			readerBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, BorderStroke.THIN)));
			readerBox.setAlignment(Pos.CENTER);

			Label fileNameLabel = new Label(readerFile.getName());

			GridPane headerInfoPane = new GridPane();
			headerInfoPane.setPadding(new Insets(10, 10, 0, 10));
			headerInfoPane.setVgap(8);
			headerInfoPane.setHgap(16);
			headerInfoPane.addColumn(0,
					new Label("BuildVersion: " + reader.getBuildVersion()),
					new Label("WebPlayerVersion: " + reader.getWebPlayerVersion()),
					new Label("UnityEngineVersion: " + reader.getUnityEngineVersion()));
			headerInfoPane.addColumn(1,
					new Label("fileHeaderSize: " + reader.getHeaderSize()),
					new Label("compressedFileSize: " + reader.getCompressedFileSize()),
					new Label("compressedContentSize: " + reader.getCompressedBodySize()));
			headerInfoPane.addColumn(2,
					new Label("decompressedHeaderSize: " + reader.getContentHeaderSize()),
					new Label("decompressedBodySize: " + reader.getDecompressedBodySize()));
			headerInfoPane.addColumn(3,
					new Label("unknown_1: " + reader.getUnknown1()),
					new Label("unknown_2: " + reader.getUnknown2()));

			GridPane contentInfoPane = new GridPane();
			contentInfoPane.setPadding(new Insets(10, 10, 10, 20));
			contentInfoPane.setVgap(8);
			contentInfoPane.setHgap(16);
			int rowIndex = 0;
			for (UnityFileDefinition file : reader.getFiles()) {
				CheckBox checkBox = new CheckBox();
				checkBox.setSelected(true);
				contentInfoPane.addRow(rowIndex,
						checkBox,
						new Label(file.getName()),
						new Label("Offset: " + file.getFileContentOffset()),
						new Label("Size: " + file.getFileSize()));
				unity3dReaderWindow_fileSelection.put(new Pair<>(readerFile, file), checkBox);
				rowIndex++;
			}

			readerBox.getChildren().addAll(fileNameLabel, headerInfoPane, contentInfoPane);
			unity3dReaderWindow_filesVbox.getChildren().add(readerBox);
		}
		mainLayout.getChildren().remove(1);
		mainLayout.getChildren().add(unity3dReaderWindow);
	}

	private String getDirectoryForMemory(List<File> files) {
		if (files == null || files.size() == 0) {
			return null;
		}
		File file = files.get(0);
		if (file == null || !file.exists()) {
			return null;
		}
		if (file.isDirectory()) {
			return file.getAbsolutePath();
		}
		return file.getParentFile().getAbsolutePath();
	}

	private static void loadMemory() {
		String executableDir = System.getProperty("user.dir");
		File memoryFile = new File(executableDir + File.separator + memoryFileName);
		if (!memoryFile.exists()) {
			memory = new ApplicationMemory();
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				memory = mapper.readValue(memoryFile, ApplicationMemory.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void onShutdown() {
		String executableDir = System.getProperty("user.dir");
		System.out.println("Exe dir is: " + executableDir);
		try {
			FileWriter writer = new FileWriter(executableDir + File.separator + memoryFileName, false);
			ObjectMapper mapper = new ObjectMapper();
			writer.write(mapper.writeValueAsString(memory));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
