package com.konloch;

import com.konloch.tav.database.downloader.MalwareBazaarDownloader;
import com.konloch.tav.database.malware.MalwareDatabases;
import com.konloch.tav.database.downloader.ClamAVDownloader;
import com.konloch.tav.database.downloader.VirusShareDownloader;
import com.konloch.tav.database.sqlite.SQLiteDB;
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
	
	public final File workingDirectory = getWorkingDirectory();
	public final SQLiteDB sqLiteDB = new SQLiteDB();
	public final MalwareDatabases malwareDB = new MalwareDatabases();
	public final ClamAVDownloader downloaderCDB = new ClamAVDownloader();
	public final VirusShareDownloader downloaderVS = new VirusShareDownloader();
	public final MalwareBazaarDownloader downloadMB = new MalwareBazaarDownloader();
	
	public void startup()
	{
		try
		{
			//load the sql db
			sqLiteDB.connect();
			sqLiteDB.createNewTable();
			
			//===================
			// VIRUS SHARE
			//===================
			
			//TODO NOTE this is too slow to actually use in production
			// instead we should gather these and distribute them as one massive download
			// this can include clamAV db and then be diffpatched for each update for minimal downloads
			
			if (sqLiteDB.getLongConfig("virusshare.database.age") == 0)
			{
				System.out.println("Preforming initial VirusShare database update (This is over 450 files, please be patient)...");
				downloaderVS.downloadUpdate();
				sqLiteDB.upsertIntegerConfig("virusshare.database.age", System.currentTimeMillis());
			}
			
			//===================
			// MALWARE BAZAAR
			//===================
			
			//every week preform the malware bazaar daily update
			if(System.currentTimeMillis() - sqLiteDB.getLongConfig("malwarebazaar.database.age")>= 1000 * 60 * 60 * 24 * 7)
			{
				System.out.println("Preforming weekly Malware Bazaar database update...");
				downloadMB.downloadUpdate();
				sqLiteDB.upsertIntegerConfig("malwarebazaar.database.age", System.currentTimeMillis());
			}
			
			//===================
			// CLAM ANTIVIRUS
			//===================
			
			//run initial update
			if (sqLiteDB.getLongConfig("clamav.database.main.age") == 0)
			{
				System.out.println("Preforming initial ClamAV database update...");
				downloaderCDB.downloadFullUpdate();
				sqLiteDB.upsertIntegerConfig("clamav.database.main.age", System.currentTimeMillis());
				sqLiteDB.upsertIntegerConfig("clamav.database.daily.age", System.currentTimeMillis());
			}
			
			//every week preform the clamAV daily update
			if(System.currentTimeMillis() - sqLiteDB.getLongConfig("clamav.database.daily.age")>= 1000 * 60 * 60 * 24 * 7)
			{
				//TODO make it every 4 hours
				// + in order to do this we need to support diffpatches and finish the libfreshclam implementation
			
				System.out.println("Preforming ClamAV daily update...");
				downloaderCDB.downloadDailyUpdate();
				sqLiteDB.upsertIntegerConfig("clamav.database.daily.age", System.currentTimeMillis());
			}
			
			//print the db stats
			malwareDB.printDatabaseStatistics();
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
	
	private File getWorkingDirectory()
	{
		if(workingDirectory == null)
		{
			File workingDirectory = new File(System.getProperty("user.home") + File.separator + "TAV.konloch");
			
			if(!workingDirectory.exists())
				workingDirectory.mkdirs();
			
			return workingDirectory;
		}
		
		return workingDirectory;
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
