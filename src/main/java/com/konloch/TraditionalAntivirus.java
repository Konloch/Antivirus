package com.konloch;

import com.konloch.clamav.database.ClamAVDB;
import com.konloch.clamav.downloader.ClamAVDownloader;
import com.konloch.tav.database.TAVDB;
import com.konloch.tav.scanning.DetectedSignatureFile;
import com.konloch.tav.scanning.MalwareScanFile;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class TraditionalAntivirus
{
	public static TraditionalAntivirus TAV;
	public final TAVDB db = new TAVDB();
	public final ClamAVDownloader downloaderCDB = new ClamAVDownloader();
	public final ClamAVDB CAVDB = new ClamAVDB();
	
	public void startup()
	{
		try
		{
			//load the db
			db.load();
			
			//run initial update
			if (db.getMainDatabaseAge().get() == 0)
			{
				System.out.println("Preforming initial database update...");
				downloaderCDB.downloadFullUpdate();
				db.getMainDatabaseAge().set(System.currentTimeMillis());
				db.getDailyDatabaseAge().set(System.currentTimeMillis());
				db.save();
			}
			
			//every week hours preform the daily update
			if(System.currentTimeMillis() - db.getDailyDatabaseAge().get() >= 1000 * 60 * 60 * 24 * 7)
			{
				//TODO make it every 4 hours
				// + in order to do this we need to support diffpatches and finish the libfreshclam implementation
			
				System.out.println("Preforming daily update...");
				downloaderCDB.downloadDailyUpdate();
				db.getDailyDatabaseAge().set(System.currentTimeMillis());
				db.save();
			}
			
			System.out.println("Loading malware signatures into memory...");
			
			//load all the Clam Antivirus Databases
			CAVDB.loadAllDatabases();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String detectAsMalware(File file)
	{
		//TODO archive support would go here, it would attempt to unzip, ungzip, tar archive etc as deep as it can go
		// then you would pass the file contents as a byte[] instead of a file, so everything is kept in memory.
		
		MalwareScanFile msf = new MalwareScanFile(file);
		return CAVDB.detectAsMalware(msf);
	}
	
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("Incorrect launch arguments, try passing a file or directory.");
			return;
		}
		
		TAV = new TraditionalAntivirus();
		TAV.startup();
		
		System.out.println("Preforming malware scan...");
		
		String malwareType;
		ArrayList<DetectedSignatureFile> detectedFiles = new ArrayList<>();
		for(String searchFilePath : args)
		{
			File searchFile = new File(searchFilePath);
			
			if(!searchFile.exists())
				continue;
			
			if((malwareType = TAV.detectAsMalware(searchFile)) != null)
			{
				System.out.println("Detection found: " + searchFile.getAbsolutePath() + " is identified as: " + malwareType);
				detectedFiles.add(new DetectedSignatureFile(searchFile, malwareType));
			}
		}
		
		System.out.println("Malware scan completed, found " + detectedFiles.size() + " types of malware");
	}
}
