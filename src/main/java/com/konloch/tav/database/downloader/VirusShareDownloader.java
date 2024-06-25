package com.konloch.tav.database.downloader;

import com.konloch.YaraAntivirus;
import com.konloch.tav.scanning.FileSignature;

import java.io.*;
import java.net.*;
import java.sql.SQLException;

/**
 * Downloads & extracts the MD5 hashes from VirusShare.com
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class VirusShareDownloader
{
	public void downloadUpdate() throws SQLException
	{
		int downloadIndex = YaraAntivirus.AV.sqLiteDB.getIntegerConfig("virusshare.database.last.full.download");
		
		//setup db
		YaraAntivirus.AV.sqLiteDB.optimizeDatabase();
		
		while(true)
		{
			//download until error
			try
			{
				String databaseName = "VirusShare_" + String.format("%05d", downloadIndex++) + ".md5";
				downloadFile("https://virusshare.com/hashfiles/" + databaseName, "vshare/" + databaseName);
				loadVSDB("vshare/" + databaseName);
				
				//TODO delete
				//new File(TraditionalAntivirus.TAV.workingDirectory, "vshare/" + databaseName).delete();
			}
			catch (FileNotFoundException e)
			{
				if(downloadIndex > 0)
					YaraAntivirus.AV.sqLiteDB.upsertIntegerConfig("virusshare.database.last.full.download", (downloadIndex - 1));
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
		
		//finalize db
		YaraAntivirus.AV.sqLiteDB.insertAllWaitingSignatures();
		YaraAntivirus.AV.sqLiteDB.resetDatabaseOptimization();
	}
	
	private void downloadFile(String url, String fileName) throws IOException
	{
		File updateFile = new File(YaraAntivirus.AV.workingDirectory, fileName);
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
		
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(YaraAntivirus.AV.workingDirectory, file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if(line.isEmpty() || line.startsWith("#"))
					continue;
				
				FileSignature fileSignature = new FileSignature(line, 0, "2");
				fileSignature.insert();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}