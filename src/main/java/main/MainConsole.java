package main;

import datatypes.UnityFileDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import reader.Unity3dReader;
import writer.Unity3dWriter;

public class MainConsole {

	public static void main(String[] args) {
		if (args == null || args.length == 0 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
			helpCommand();
			return;
		}

		if (args[0].equalsIgnoreCase("pack")) {
			packCommand(args);
			return;
		}

		if (args[0].equalsIgnoreCase("unpack")) {
			unpackCommand(args);
			return;
		}

		if (args[0].equalsIgnoreCase("analyze")) {
			analyzeCommand(args);
			return;
		}

		System.out.println("Unknown command!");
	}

	private static void helpCommand() {
		System.out.println("===== Argument Overview =====");
		System.out.println("['?'] or ['help'] - show this output");
		System.out.println("---");
		System.out.println("['pack', <sourceDirectory>, <webPlayerVersion>, <unityEngineVersion>] - pack all files in sourceDirectory to a 'sourceDirectory.unity3d' bundle");
		System.out.println("['pack', <sourceDirectory>, <targetFile>, <webPlayerVersion>, <unityEngineVersion>] - pack all files in sourceDirectory, save as targetFile");
		System.out.println("---");
		System.out.println("['unpack', <bundleFile>] - unpack the bundled files into a directory of the same name as the bundleFile");
		System.out.println("['unpack', <bundleFile>, <targetDirectory>] - unpack the bundled files into the targetDirectory");
		System.out.println("---");
		System.out.println("['analyze', <bundleFile>] - output information on the bundle and its files");
	}

	private static void packCommand(String[] args) {
		if (args.length < 4 || args.length > 5) {
			System.out.println("Invalid arguments for pack command. Got " + args.length + " arguments, expected 4 or 5.");
			helpCommand();
			return;
		}

		File sourceDirectory = new File(args[1]);
		if (!sourceDirectory.isDirectory()) {
			System.out.println("Specified sourceDirectory is not a directory!");
			return;
		}

		List<File> sourceFiles = Arrays.stream(Objects.requireNonNull(sourceDirectory.listFiles()))
				.filter(file -> !file.isDirectory())
				.collect(Collectors.toList());

		File outputFile;
		String webPlayerVersion;
		String unityEngineVersion;
		if (args.length == 4) {
			outputFile = new File(sourceDirectory.getAbsolutePath() + ".unity3d");
			webPlayerVersion = args[2];
			unityEngineVersion = args[3];
		} else {
			outputFile = new File(args[2]);
			if (outputFile.isDirectory()) {
				outputFile = new File(outputFile.getAbsolutePath() + ".unity3d");
			}
			webPlayerVersion = args[3];
			unityEngineVersion = args[4];
		}
		//noinspection ResultOfMethodCallIgnored
		outputFile.getParentFile().mkdirs();

		System.out.println("Packing SourceFiles:");
		for (File sourceFile : sourceFiles) {
			System.out.println("  - " + sourceFile.getName());
		}
		System.out.println("---");
		System.out.println("Into target file:");
		System.out.println("  - " + outputFile.getAbsolutePath());
		System.out.println("---");
		System.out.println("With");
		System.out.println("  - WebPlayerVersion  : " + webPlayerVersion);
		System.out.println("  - UnityEngineVersion: " + unityEngineVersion);

		try {
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			Unity3dWriter.writeFromFiles(sourceFiles, outputStream, webPlayerVersion, unityEngineVersion);
			outputStream.close();
			System.out.println("Packing succeeded.");
		} catch (IOException e) {
			System.out.println("Encountered an Error while packing: " + e);
			e.printStackTrace();
		}
	}

	private static void unpackCommand(String[] args) {
		// ['unpack', <bundleFile>] - unpack the bundled files into a directory of the same name as the bundleFile
		// ['unpack', <bundleFile>, <targetDirectory>] - unpack the bundled files into the targetDirectory
		if (args.length < 2 || args.length > 3) {
			System.out.println("Invalid arguments for unpack command. Got " + args.length + " arguments, expected 2 or 3.");
			helpCommand();
			return;
		}

		File bundleFile = new File(args[1]);
		if (!bundleFile.exists() || bundleFile.isDirectory()) {
			System.out.println("BundleFile does not exist, cannot unpack. Path:");
			System.out.println(bundleFile.getAbsolutePath());
			return;
		}

		File targetDirectory;
		if (args.length == 2) {
			targetDirectory = new File(bundleFile.getParentFile().getAbsolutePath() + File.separator + bundleFile.getName() + File.separator);
		} else {
			targetDirectory = new File(args[2]);
		}

		//noinspection ResultOfMethodCallIgnored
		targetDirectory.mkdirs();

		List<UnityFileDefinition> bundleFiles;
		try {
			FileInputStream fileInputStream = new FileInputStream(bundleFile);
			Unity3dReader unity3dReader = new Unity3dReader(fileInputStream.readAllBytes());
			bundleFiles = unity3dReader.getFiles();
		} catch (IOException e) {
			System.out.println("Encountered an Error while unpacking: " + e);
			e.printStackTrace();
			return;
		}

		System.out.println("Unpacking " + bundleFiles.size() + " bundled files: ");
		for (UnityFileDefinition file : bundleFiles) {
			System.out.println("  - name: " + file.getName());
		}
		System.out.println("To TargetDirectory: ");
		System.out.println("  - " + targetDirectory.getAbsolutePath());

		try {
			for (UnityFileDefinition file : bundleFiles) {
				String targetPath = targetDirectory.getAbsolutePath() + File.separator + file.getName();
				FileOutputStream outputStream = new FileOutputStream(targetPath);
				outputStream.write(file.getBody());
				outputStream.close();
			}
		} catch (IOException e) {
			System.out.println("Encountered an Error while unpacking: " + e);
			e.printStackTrace();
			return;
		}

		System.out.println("Unpacking succeeded.");
	}

	private static void analyzeCommand(String[] args) {
		// ['analyze', <bundleFile>] - output information on the bundle and its files
		if (args.length != 2) {
			System.out.println("Invalid arguments for analyze command. Got " + args.length + " arguments, expected 2.");
			helpCommand();
			return;
		}

		File bundleFile = new File(args[1]);
		if (!bundleFile.exists() || bundleFile.isDirectory()) {
			System.out.println("BundleFile does not exist, cannot analyze. Path:");
			System.out.println(bundleFile.getAbsolutePath());
			return;
		}

		try {
			FileInputStream fileInputStream = new FileInputStream(bundleFile);
			Unity3dReader reader = new Unity3dReader(fileInputStream.readAllBytes());
			System.out.println("Completed analyzing file: " + bundleFile.getAbsolutePath());
			System.out.println("---- Version Info ----");
			System.out.println("  - BuildVersion      : " + reader.getBuildVersion());
			System.out.println("  - WebPlayerVersion  : " + reader.getWebPlayerVersion());
			System.out.println("  - UnityEngineVersion: " + reader.getUnityEngineVersion());
			System.out.println("---- BundleFile (Compressed) info ----");
			System.out.println("  - Header Size: " + reader.getHeaderSize());
			System.out.println("  - File Size  : " + reader.getCompressedFileSize());
			System.out.println("  - Data Size  : " + reader.getCompressedBodySize());
			System.out.println("---- BundleContent (Decompressed) info ----");
			System.out.println("  - Header Size: " + reader.getContentHeaderSize());
			System.out.println("  - Total Size : " + reader.getContentHeaderSize() + reader.getDecompressedBodySize());
			System.out.println("  - Data Size  : " + reader.getDecompressedBodySize());
			System.out.println("---- Unknown fields ----");
			System.out.println("  - Unknown 1: " + reader.getUnknown1());
			System.out.println("  - Unknown 2: " + reader.getUnknown2());
			System.out.println("---- Bundled Files ----");
			List<List<String>> table = tableBuilder(reader.getFiles(),
					UnityFileDefinition::getName,
					file -> "" + file.getFileContentOffset(),
					file -> "" + file.getFileSize());
			for (List<String> row : table) {
				System.out.println("  - Name: " + row.get(0) + "    Offset: " + row.get(1) + "    Size: " + row.get(2));
			}
			System.out.println("---- End of Output ----");
		} catch (IOException e) {
			System.out.println("Encountered an Error while analyzing: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @param data          List of Objects to gather data from, one per row to create
	 * @param columnGetters List of Functions, one function per column to create
	 * @param <T>           Type of the Objects from data
	 * @return a List of rows, with columns padded to the same charCount
	 */
	private static <T> List<List<String>> tableBuilder(List<T> data, Function<T, String>... columnGetters) {
		List<List<String>> columnList = new ArrayList<>();
		if (columnGetters.length == 0) {
			return columnList;
		}

		for (Function<T, String> columnGetter : columnGetters) {
			columnList.add(data.stream()
					.map(columnGetter)
					.collect(Collectors.toList()));
		}

		List<Integer> maxColumnWidths = columnList.stream()
				.map(column -> column.stream().max(Comparator.comparingInt(String::length)).orElse("").length())
				.collect(Collectors.toList());

		List<List<String>> paddedColumnList = new ArrayList<>();
		for (int i = 0; i < maxColumnWidths.size(); i++) {
			int targetColumnWidth = maxColumnWidths.get(i);
			List<String> column = columnList.get(i);
			paddedColumnList.add(column.stream()
					.map(value -> {
						int valueLength = value.length();
						if (valueLength >= targetColumnWidth) {
							return value;
						}
						return value + " ".repeat(targetColumnWidth - valueLength);
					}).collect(Collectors.toList()));
		}

		int rowCount = paddedColumnList.get(0).size();
		List<List<String>> rowList = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			int finalRowIndex = rowIndex;
			rowList.add(paddedColumnList.stream()
					.map(column -> column.get(finalRowIndex))
					.collect(Collectors.toList()));
		}
		return rowList;
	}
}
