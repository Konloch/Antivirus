package com.konloch.tav.database.downloader;

import com.konloch.TraditionalAntivirus;

import java.io.*;
import java.net.*;

/**
 * Downloads & extracts the MD5 hashes from VirusShare.com
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class VirusShareDownloader
{
	public void downloadUpdate()
	{
		int downloadIndex = TraditionalAntivirus.TAV.tavDB.getVSLastFullDownload().get();
		
		while(true)
		{
			//download until error
			try
			{
				String databaseName = "VirusShare_" + String.format("%05d", downloadIndex++) + ".md5";
				downloadFile("https://virusshare.com/hashfiles/" + databaseName, "vshare/" + databaseName);
			}
			catch (FileNotFoundException e)
			{
				if(downloadIndex > 0)
					TraditionalAntivirus.TAV.tavDB.getVSLastFullDownload().set(downloadIndex - 1);
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void downloadFile(String url, String fileName) throws IOException
	{
		File updateFile = new File(TraditionalAntivirus.TAV.tavDB.getWorkingDirectory(), fileName);
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
}