package utils;

import java.nio.charset.StandardCharsets;

public class OffsetBasedReader {

	private final byte[] bytes;
	private final int byteCount;
	private int bytePointer;

	public OffsetBasedReader(byte[] bytes) {
		this.bytes = bytes;
		this.byteCount = bytes.length;
		this.bytePointer = 0;
	}

	public byte readByte() {
		return bytes[bytePointer++];
	}

	public int readInt() {
		int ch1 = readByte() & 0xff;
		int ch2 = readByte() & 0xff;
		int ch3 = readByte() & 0xff;
		int ch4 = readByte() & 0xff;
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
	}

	public String readString() {
		int stringOffset = bytePointer;
		int stringLength = 0;
		while (readByte() != 0x00) {
			stringLength++;
		}
		return new String(bytes, stringOffset, stringLength, StandardCharsets.UTF_8);
	}

	public int readBytes(byte[] buffer){
		if(bytePointer >= byteCount){
			return -1;
		}

		int readBytes = 0;
		for(; readBytes < buffer.length && bytePointer < byteCount; readBytes++){
			buffer[readBytes] = readByte();
		}
		return readBytes;
	}

	public void goToOffset(int offset) {
		bytePointer = offset;
	}

	public void addOffset(int offset) {
		bytePointer = Math.max(0, bytePointer + offset);
	}
}
