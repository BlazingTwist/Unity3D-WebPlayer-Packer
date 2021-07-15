import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;

public class LzmaTest {

	/*@Test
	public void test_lzma() throws IOException {
		String input;
		{
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("testfile.txt");
			assert resourceAsStream != null;
			byte[] inBytes = resourceAsStream.readAllBytes();
			input = new String(inBytes, StandardCharsets.UTF_8);

			FileOutputStream fileOutputStream = new FileOutputStream("D:\\Arbeit\\IntelliJ_projects\\Unity3D_ModdingTool\\src\\main\\resources\\resultfile.txt", false);
			LZMAOutputStream outputStream = new LZMAOutputStream(fileOutputStream, new LZMA2Options(), -1);
			outputStream.write(inBytes);
			outputStream.finish();

			resourceAsStream.close();
			fileOutputStream.close();
		}

		String output;
		{
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("resultfile.txt");
			assert resourceAsStream != null;

			LZMAInputStream lzmaInputStream = new LZMAInputStream(resourceAsStream);
			output = new String(lzmaInputStream.readAllBytes(), StandardCharsets.UTF_8);

			resourceAsStream.close();
		}

		Assertions.assertEquals(input, output);
	}*/

	/*@Test
	public void test_unpack() throws IOException {
		FileInputStream fileInputStream = new FileInputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.unity3d");
		byte[] bytes = fileInputStream.readAllBytes();
		Unity3dReader unity3dReader = new Unity3dReader(bytes);
		String targetPath = "D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\UnpackTest\\";
		for (UnityFileDefinition file : unity3dReader.getFiles()) {
			FileOutputStream fileOutputStream = new FileOutputStream(targetPath + file.getName(), false);
			fileOutputStream.write(file.getBody());
		}
		System.out.println("Success");
	}

	@Test
	public void test_repack() throws IOException {
		List<File> files = Files.walk(Paths.get("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\UnpackTest"), 1, FileVisitOption.FOLLOW_LINKS)
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.collect(Collectors.toList());
		File outputFile = new File("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMainRepack.unity3d");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		Unity3dWriter.writeFromFiles(files, outputStream, "3.x.x", "3.5.6f4");
		outputStream.close();
		System.out.println("Success");
	}

	@Test
	public void test_create_decompressed_body() throws IOException {
		{
			FileInputStream fileInputStream = new FileInputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.immediate");
			byte[] bytes = fileInputStream.readAllBytes();
			Unity3dReader unity3dReader = new Unity3dReader(bytes);
			FileOutputStream outputStream = new FileOutputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.immediate.decompressed");
			outputStream.write(unity3dReader.getDecompressedBody());
		}
		{
			FileInputStream fileInputStream = new FileInputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.unity3d");
			byte[] bytes = fileInputStream.readAllBytes();
			Unity3dReader unity3dReader = new Unity3dReader(bytes);
			FileOutputStream outputStream = new FileOutputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.decompressed");
			outputStream.write(unity3dReader.getDecompressedBody());
		}
		System.out.println("Success");
	}

	@Test
	public void test_immediate_repack() throws IOException {
		FileInputStream fileInputStream = new FileInputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.unity3d");
		byte[] bytes = fileInputStream.readAllBytes();
		Unity3dReader unity3dReader = new Unity3dReader(bytes);

		FileOutputStream outputStream = new FileOutputStream("D:\\Tools\\UNITY MODDING\\QuickBMS\\SuperSecret\\SSMain.immediate");
		Unity3dWriter.writeFromFileDefinition(unity3dReader.getFiles(), outputStream, "3.x.x", "3.5.6f4");
		System.out.println("Success");
	}*/
}
