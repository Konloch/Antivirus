package com.konloch;

import com.konloch.av.database.malware.DetectedSignatureFile;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.database.sql.SQLiteDB;
import com.konloch.av.downloader.impl.yara.YaraDownloader;
import com.konloch.av.mimicvm.MimicVM;
import com.konloch.av.scanning.MalwareScanners;
import com.konloch.av.tasks.UpdateTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class Antivirus
{
	public static Antivirus AV;
	
	public final File workingDirectory = getWorkingDirectory();
	public final SQLiteDB sqLiteDB = new SQLiteDB();
	public final MalwareScanners scanners = new MalwareScanners();
	public final AVFlags flags = new AVFlags();
	public final MimicVM mimicVM = new MimicVM();
	
	public void startup()
	{
		try
		{
			System.out.println("Starting up...");
			
			//load the sql db
			sqLiteDB.connect();
			sqLiteDB.createNewTable();
			sqLiteDB.createInitialSettings();
			
			//start the update task
			new Thread(new UpdateTask(), "Update-Task").start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run() throws IOException, InterruptedException
	{
		//start vm mimic
		try
		{
			if (sqLiteDB.getBooleanConfig("antivirus.vm.mimic", true))
				mimicVM.enable();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//wait for initial updates to finish
		while(!AV.flags.updateFinished)
		{
			Thread.sleep(1);
		}
		
		//write mega yara file
		YaraDownloader.loadYaraFilesIntoSingleFile();
	}
	
	public void scan(String... args)
	{
		long start = System.currentTimeMillis();
		
		System.out.println("Preforming malware scan...");
		
		String malwareType;
		ArrayList<DetectedSignatureFile> detectedFiles = new ArrayList<>();
		for(String searchFilePath : args)
		{
			File searchFile = new File(searchFilePath);
			
			if(!searchFile.exists())
				continue;
			
			System.out.println("Scanning: " + searchFile.getAbsolutePath());
			
			if((malwareType = AV.detectAsMalware(searchFile)) != null)
			{
				System.out.println(malwareType);
				detectedFiles.add(new DetectedSignatureFile(searchFile, malwareType));
			}
		}
		
		long finished = System.currentTimeMillis()-start;
		System.out.println("Malware scan completed, found " + detectedFiles.size() + " types of malware, took: " + finished + " ms");
	}
	
	public String detectAsMalware(File file)
	{
		//TODO archive support would go here, it would attempt to unzip, ungzip, tar archive etc as deep as it can go
		// then you would pass the file contents as a byte[] instead of a file, so everything is kept in memory.
		
		MalwareScanFile msf = new MalwareScanFile(file);
		return scanners.detectAsMalware(msf);
	}
	
	private File getWorkingDirectory()
	{
		if(workingDirectory == null)
		{
			File workingDirectory = new File(System.getProperty("user.home") + File.separator + "Antivirus");
			
			if(!workingDirectory.exists())
				workingDirectory.mkdirs();
			
			return workingDirectory;
		}
		
		return workingDirectory;
	}
}