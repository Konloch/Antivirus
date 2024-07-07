import com.konloch.Antivirus;

import java.io.File;

/**
 * @author Konloch
 * @since 7/6/2024
 */
public class TestQuarantine
{
	public static void main(String[] args)
	{
		Antivirus.AV = new Antivirus();
		Antivirus.AV.startup();
		
		System.out.println("Upserting...");
		
		for(int i = 0; i < 5; i++)
			Antivirus.AV.quarantine.quarantineFile(new File("test.txt"), "Test");
		
		System.out.println("Done");
	}
}
