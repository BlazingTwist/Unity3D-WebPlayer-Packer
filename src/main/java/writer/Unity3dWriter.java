package writer;

import datatypes.UnityFileDefinition;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;
import static utils.DataStreamUtils.writeNullTerminatedString;

public class Unity3dWriter {

	private Unity3dWriter() {
	}

	public static void writeFromFileDefinition(List<UnityFileDefinition> files, OutputStream outputStream, String webPlayerVersion, String unityEngineVersion) throws IOException {
		int decompressedContentSize = 0;
		for (UnityFileDefinition file : files) {
			if(decompressedContentSize == 0){
				decompressedContentSize = file.getFileContentOffset();
			}
			file.setFileContentOffset(decompressedContentSize);
			decompressedContentSize += file.getFileSize();
		}
		writeUnity3d(files, outputStream, webPlayerVersion, unityEngineVersion, decompressedContentSize);
	}

	public static void writeFromFiles(List<File> files, OutputStream outputStream, String webPlayerVersion, String unityEngineVersion) throws IOException {
		List<UnityFileDefinition> fileDefinitions = new ArrayList<>();
		int fileOffset = 4; // fileCount takes up 4 bytes
		for (File file : files) {
			UnityFileDefinition fileDefinition = new UnityFileDefinition();
			fileDefinitions.add(fileDefinition);
			fileDefinition.setName(file.getName());
			fileDefinition.setFileSize((int) file.length());
			FileInputStream fileInputStream = new FileInputStream(file);
			fileDefinition.setBody(fileInputStream.readAllBytes());
			fileInputStream.close();

			fileOffset += fileDefinition.getName().length() + 9; // nullTermination + fileSize + fileOffset
		}
		fileOffset += 1; // header ends on null

		// determine file offsets
		for (UnityFileDefinition fileDefinition : fileDefinitions) {
			fileDefinition.setFileContentOffset(fileOffset);
			fileOffset += fileDefinition.getFileSize();
		}

		// value of fileOffset now is last offset + last fileSize -> equivalent to total fileSize
		writeUnity3d(fileDefinitions, outputStream, webPlayerVersion, unityEngineVersion, fileOffset);
	}

	private static void writeUnity3d(List<UnityFileDefinition> files, OutputStream outputStream, String webPlayerVersion,
									 String unityEngineVersion, int decompressedContentSize) throws IOException {
		ByteArrayOutputStream decompressedBodyByteArrayStream = new ByteArrayOutputStream(decompressedContentSize);
		DataOutputStream decompressedBodyStream = new DataOutputStream(decompressedBodyByteArrayStream);
		decompressedBodyStream.writeInt(files.size());
		for (UnityFileDefinition fileDefinition : files) {
			writeNullTerminatedString(decompressedBodyStream, fileDefinition.getName());
			decompressedBodyStream.writeInt(fileDefinition.getFileContentOffset());
			decompressedBodyStream.writeInt(fileDefinition.getFileSize());
		}
		decompressedBodyStream.writeByte(0x00); // null separator between header and values
		// append file bodies
		for (UnityFileDefinition fileDefinition : files) {
			decompressedBodyStream.write(fileDefinition.getBody());
		}

		ByteArrayOutputStream compressedBodyStream = new ByteArrayOutputStream();
		LZMAOutputStream lzmaOutputStream = new LZMAOutputStream(compressedBodyStream, new LZMA2Options(), decompressedContentSize);
		lzmaOutputStream.write(decompressedBodyByteArrayStream.toByteArray());
		lzmaOutputStream.finish();

		byte[] compressedBody = compressedBodyStream.toByteArray();
		DataOutputStream fileOutputStream = new DataOutputStream(outputStream);
		writeNullTerminatedString(fileOutputStream, "UnityWeb");
		fileOutputStream.writeInt(3);
		writeNullTerminatedString(fileOutputStream, webPlayerVersion);
		writeNullTerminatedString(fileOutputStream, unityEngineVersion);
		int headerSize = 9 + 4 + webPlayerVersion.length() + 1 + unityEngineVersion.length() + 1
				+ 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 1;
		fileOutputStream.writeInt(headerSize + compressedBody.length); // compressedFileSize
		fileOutputStream.writeInt(headerSize); // headerSize
		fileOutputStream.writeInt(1);
		fileOutputStream.writeInt(1);
		fileOutputStream.writeInt(compressedBody.length); // compressedBodySize
		fileOutputStream.writeInt(decompressedContentSize); // decompressedBodySize
		fileOutputStream.writeInt(headerSize + compressedBody.length); // repeatCompressedFileSize
		fileOutputStream.writeInt(files.get(0).getFileContentOffset()); // contentHeaderSize
		fileOutputStream.writeByte(0x00); // header separator
		fileOutputStream.write(compressedBody);

		decompressedBodyByteArrayStream.close();
		compressedBodyStream.close();
	}
}

