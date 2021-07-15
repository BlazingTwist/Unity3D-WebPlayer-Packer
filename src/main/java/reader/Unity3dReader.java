package reader;

import datatypes.UnityFileDefinition;
import static utils.DataStreamUtils.readNullTerminatedString;
import utils.OffsetBasedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import org.tukaani.xz.LZMAInputStream;

public class Unity3dReader {

	private final int buildVersion;
	private final String webPlayerVersion;
	private final String unityEngineVersion;
	private final int compressedFileSize;
	private final int headerSize;
	private final int unknown1;
	private final int unknown2;
	private final int compressedBodySize;
	private final int decompressedBodySize;
	private final int repeatCompressedFileSize;
	private final int contentHeaderSize;
	private final byte[] compressedBody;
	private final byte[] decompressedBody;
	private final ArrayList<UnityFileDefinition> files = new ArrayList<>();

	public Unity3dReader(byte[] file) throws IOException {
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(file));
		String unityWebString = readNullTerminatedString(inputStream);
		if (!unityWebString.equals("UnityWeb")) {
			throw new IllegalArgumentException("Cannot read file! Expected type: 'UnityWeb', actual type: " + unityWebString);
		}

		buildVersion = inputStream.readInt();
		webPlayerVersion = readNullTerminatedString(inputStream);
		unityEngineVersion = readNullTerminatedString(inputStream);
		compressedFileSize = inputStream.readInt();
		headerSize = inputStream.readInt();
		unknown1 = inputStream.readInt();
		unknown2 = inputStream.readInt();
		compressedBodySize = inputStream.readInt();
		decompressedBodySize = inputStream.readInt();
		repeatCompressedFileSize = inputStream.readInt();
		contentHeaderSize = inputStream.readInt();
		inputStream.readByte(); // consume final null-byte of header
		compressedBody = inputStream.readAllBytes();

		LZMAInputStream lzmaInputStream = new LZMAInputStream(new ByteArrayInputStream(compressedBody));

		decompressedBody = lzmaInputStream.readAllBytes();
		OffsetBasedReader decompressedBodyReader = new OffsetBasedReader(decompressedBody);
		int expectedFileCount = decompressedBodyReader.readInt();
		for (int i = 0; i < expectedFileCount; i++) {
			UnityFileDefinition unityFile = new UnityFileDefinition();
			files.add(unityFile);
			unityFile.setName(decompressedBodyReader.readString());
			unityFile.setFileContentOffset(decompressedBodyReader.readInt());
			unityFile.setFileSize(decompressedBodyReader.readInt());
		}
		decompressedBodyReader.readByte(); // consume null-byte separating content-header and content-data
		// reached end of content-header, remaining body is concatenated files
		for (UnityFileDefinition unityFileDefinition : files) {
			decompressedBodyReader.goToOffset(unityFileDefinition.getFileContentOffset());
			int fileSize = unityFileDefinition.getFileSize();
			byte[] fileBuffer = new byte[fileSize];
			int readBytes = decompressedBodyReader.readBytes(fileBuffer);
			if(readBytes != fileSize){
				throw new EOFException("Unable to read to end of sub-file: " + unityFileDefinition.getName());
			}
			unityFileDefinition.setBody(fileBuffer);
		}

		inputStream.close();
		lzmaInputStream.close();
	}

	public int getBuildVersion() {
		return buildVersion;
	}

	public String getWebPlayerVersion() {
		return webPlayerVersion;
	}

	public String getUnityEngineVersion() {
		return unityEngineVersion;
	}

	public int getCompressedFileSize() {
		return compressedFileSize;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public int getUnknown1() {
		return unknown1;
	}

	public int getUnknown2() {
		return unknown2;
	}

	public int getCompressedBodySize() {
		return compressedBodySize;
	}

	public int getDecompressedBodySize() {
		return decompressedBodySize;
	}

	public int getRepeatCompressedFileSize() {
		return repeatCompressedFileSize;
	}

	public int getContentHeaderSize() {
		return contentHeaderSize;
	}

	public byte[] getCompressedBody() {
		return compressedBody;
	}

	public byte[] getDecompressedBody() {
		return decompressedBody;
	}

	public ArrayList<UnityFileDefinition> getFiles() {
		return files;
	}
}
