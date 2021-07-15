package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DataStreamUtils {

	public static String readNullTerminatedString(DataInputStream inputStream) throws IOException {
		ArrayList<Byte> bytes = new ArrayList<>();
		byte b;
		while ((b = inputStream.readByte()) != 0x00) {
			bytes.add(b);
		}
		byte[] byteArray = new byte[bytes.size()];
		int i = 0;
		for (Byte aByte : bytes) {
			byteArray[i++] = aByte;
		}
		return new String(byteArray, StandardCharsets.UTF_8);
	}

	public static void writeNullTerminatedString(DataOutputStream outputStream, String text) throws IOException {
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		outputStream.write(bytes);
		outputStream.write(0x00);
	}
}
