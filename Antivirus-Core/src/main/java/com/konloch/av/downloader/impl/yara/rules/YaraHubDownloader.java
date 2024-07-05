package com.konloch.av.downloader.impl.yara.rules;

import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadState;
import com.konloch.av.downloader.Downloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads & extracts Yara Rules from Yara Hub
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class YaraHubDownloader implements Downloader
{
	@Override
	public void download(DownloadState state) throws IOException, SQLException
	{
		if(state == DownloadState.DAILY)
			downloadUpdate();
	}
	
	@Override
	public DownloadState getState() throws IOException, SQLException
	{
		//every 4 hours preform the daily update
		if (System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("yarahub.database.age") >= 1000 * 60 * 60 * 4)
			return DownloadState.DAILY;
		
		return DownloadState.NONE;
	}
	
	private void downloadUpdate() throws IOException, SQLException
	{
		downloadFile("https://yaraify.abuse.ch/yarahub/yaraify-rules.zip", "yara/yarahub.zip");
		extract("yara/yarahub.zip", "yara/YaraHub", "yara", "yar");
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("yarahub.database.age", System.currentTimeMillis());
	}
	
	public void downloadFile(String url, String fileName) throws IOException
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
	
	public void extract(String databaseZipPath, String fileName, String... expectedFileExtensions) throws FileNotFoundException
	{
		File databaseZip = new File(Antivirus.AV.workingDirectory, databaseZipPath);
		File extractFolder = new File(Antivirus.AV.workingDirectory, fileName);
		
		if(!extractFolder.exists())
			extractFolder.mkdirs();
		
		if(!databaseZip.exists())
			throw new FileNotFoundException("Update File Not Found: " + extractFolder.getAbsolutePath());
		
		extractDatabase(databaseZip, extractFolder, expectedFileExtensions);
		
		System.out.println("Deleting " + databaseZip.getAbsolutePath());
		databaseZip.delete();
	}
	
	public static void extractDatabase(File zipFile, File outputFolder, String... expectedFileExtensions)
	{
		try (FileInputStream fis = new FileInputStream(zipFile); ZipInputStream zis = new ZipInputStream(fis))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				File outputFile = new File(outputFolder, entry.getName());
				
				//zipslip
				if (!outputFile.getAbsolutePath().startsWith(outputFolder.getAbsolutePath()))
					continue;
				
				if (!entry.isDirectory())
				{
					boolean matchingExtension = false;
					for (String extension : expectedFileExtensions)
						if (outputFile.getAbsolutePath().endsWith(extension))
							matchingExtension = true;
					
					if (!matchingExtension) //skip non approved file extensions
						continue;
					
					//make the parent directory if it doesn't exist
					outputFile.getParentFile().mkdirs();
					
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile)))
					{
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zis.read(buffer)) > 0)
						{
							bos.write(buffer, 0, len);
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}