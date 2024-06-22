package com.konloch;

import com.konloch.tav.database.downloader.MalwareBazaarDownloader;
import com.konloch.tav.database.malware.MalwareDatabases;
import com.konloch.tav.database.downloader.ClamAVDownloader;
import com.konloch.tav.database.downloader.VirusShareDownloader;
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
	
	public final TAVDB tavDB = new TAVDB();
	public final MalwareDatabases malwareDB = new MalwareDatabases();
	public final ClamAVDownloader downloaderCDB = new ClamAVDownloader();
	public final VirusShareDownloader downloaderVS = new VirusShareDownloader();
	public final MalwareBazaarDownloader downloadMB = new MalwareBazaarDownloader();
	
	public void startup()
	{
		try
		{
			//load the db
			tavDB.load();
			
			//run initial update
			if (tavDB.getCAVMainDatabaseAge().get() == 0)
			{
				System.out.println("Preforming initial ClamAV database update...");
				downloaderCDB.downloadFullUpdate();
				tavDB.getCAVMainDatabaseAge().set(System.currentTimeMillis());
				tavDB.getCAVDailyDatabaseAge().set(System.currentTimeMillis());
				tavDB.save();
			}
			
			//every week preform the clamAV daily update
			if(System.currentTimeMillis() - tavDB.getCAVDailyDatabaseAge().get() >= 1000 * 60 * 60 * 24 * 7)
			{
				//TODO make it every 4 hours
				// + in order to do this we need to support diffpatches and finish the libfreshclam implementation
			
				System.out.println("Preforming ClamAV daily update...");
				downloaderCDB.downloadDailyUpdate();
				tavDB.getCAVDailyDatabaseAge().set(System.currentTimeMillis());
				tavDB.save();
			}
			
			//TODO NOTE this is too slow to actually use in production
			// instead we should gather these and distribute them as one massive download
			// this can include clamAV db and then be diffpatched for each update for minimal downloads
			
			if (tavDB.getVSDatabaseAge().get() == 0)
			{
				System.out.println("Preforming initial VirusShare database update (This is over 450 files, please be patient)...");
				downloaderVS.downloadUpdate();
				tavDB.getVSDatabaseAge().set(System.currentTimeMillis());
				tavDB.save();
			}
			
			//every week preform the malware bazaar daily update
			if(System.currentTimeMillis() - tavDB.getMBDatabaseAge().get() >= 1000 * 60 * 60 * 24 * 7)
			{
				System.out.println("Preforming weekly Malware Bazaar database update...");
				downloadMB.downloadUpdate();
				tavDB.getMBDatabaseAge().set(System.currentTimeMillis());
				tavDB.save();
			}
			
			System.out.println("Loading malware signatures into memory...");
			
			//load all the Clam Antivirus Databases
			malwareDB.loadAllDatabases();
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
		return malwareDB.detectAsMalware(msf);
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
