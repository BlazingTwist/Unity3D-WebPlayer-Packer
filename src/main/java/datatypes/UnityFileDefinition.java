package datatypes;

public class UnityFileDefinition {
	private String name;
	private int fileContentOffset;
	private int fileSize;
	private byte[] body;

	public UnityFileDefinition() {
	}

	public String getName() {
		return name;
	}

	public UnityFileDefinition setName(String name) {
		this.name = name;
		return this;
	}

	public int getFileContentOffset() {
		return fileContentOffset;
	}

	public UnityFileDefinition setFileContentOffset(int fileContentOffset) {
		this.fileContentOffset = fileContentOffset;
		return this;
	}

	public int getFileSize() {
		return fileSize;
	}

	public UnityFileDefinition setFileSize(int fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public byte[] getBody() {
		return body;
	}

	public UnityFileDefinition setBody(byte[] body) {
		this.body = body;
		return this;
	}
}
