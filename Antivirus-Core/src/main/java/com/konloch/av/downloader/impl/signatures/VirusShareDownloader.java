package com.konloch.av.downloader.impl.signatures;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.downloader.DownloadFrequency;
import com.konloch.av.downloader.Downloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import static com.konloch.AVConstants.SIGNATURE_IDENTIFIER_VIRUSSHARE_SUBMISSION;

/**
 * Downloads & extracts the MD5 hashes from VirusShare.com
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class VirusShareDownloader implements Downloader
{
	@Override
	public String getName()
	{
		return "Virus Share (File Signatures)";
	}
	
	@Override
	public void download(DownloadFrequency state) throws IOException, SQLException
	{
		if(state == DownloadFrequency.DAILY)
			downloadUpdate();
	}
	
	@Override
	public DownloadFrequency getState() throws IOException, SQLException
	{
		if(!AVConstants.ENABLE_SIGNATURE_SCANNING_DATABASES_IMPORT)
			return DownloadFrequency.NONE;
		
		//every 14 days preform a download
		if(System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("virusshare.database.age")>= 1000 * 60 * 60 * 24 * 14)
			return DownloadFrequency.DAILY;
		
		return DownloadFrequency.NONE;
	}
	
	private void downloadUpdate() throws SQLException
	{
		int downloadIndex = Antivirus.AV.sqLiteDB.getIntegerConfig("virusshare.database.last.full.download");
		
		//setup db
		Antivirus.AV.sqLiteDB.optimizeDatabase();
		
		while(true)
		{
			//download until error
			try
			{
				String databaseName = "VirusShare_" + String.format("%05d", downloadIndex++) + ".md5";
				downloadFile("https://virusshare.com/hashfiles/" + databaseName, "vshare/" + databaseName);
				loadVSDB("vshare/" + databaseName);
				
				//delete
				new File(Antivirus.AV.workingDirectory, "vshare/" + databaseName).delete();
			}
			catch (FileNotFoundException e)
			{
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
		
		if(downloadIndex > 0)
			Antivirus.AV.sqLiteDB.upsertIntegerConfig("virusshare.database.last.full.download", (downloadIndex - 1));
		
		//delete
		new File(Antivirus.AV.workingDirectory, "vshare").delete();
		
		//finalize db
		Antivirus.AV.sqLiteDB.insertAllWaitingSignatures();
		Antivirus.AV.sqLiteDB.resetDatabaseOptimization();
		
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("virusshare.database.age", System.currentTimeMillis());
	}
	
	private void downloadFile(String url, String fileName) throws IOException
	{
		File updateFile = new File(Antivirus.AV.workingDirectory, fileName);
		updateFile.getParentFile().mkdirs(); //make folder dir incase it's not there
		
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		
		System.out.println("Downloading " + url + " to " + updateFile.getAbsolutePath());
		
		try (InputStream inputStream = connection.getInputStream();
		     FileOutputStream fileOutputStream = new FileOutputStream(updateFile))
		{
			byte[] buffer = new byte[4096];
			int bytesRead;
			
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		}
		finally
		{
			connection.disconnect();
		}
	}
	
	private void loadVSDB(String file)
	{
		System.out.println("Inserting " + file + " into SQLite db...");
		
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(Antivirus.AV.workingDirectory, file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if(line.isEmpty() || line.startsWith("#"))
					continue;
				
				FileSignature fileSignature = new FileSignature(line, 0, SIGNATURE_IDENTIFIER_VIRUSSHARE_SUBMISSION);
				fileSignature.insert();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}