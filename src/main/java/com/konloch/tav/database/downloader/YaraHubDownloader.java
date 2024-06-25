package com.konloch.tav.database.downloader;

import com.konloch.YaraAntivirus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads & extracts Yara Rules from Yara Hub
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class YaraHubDownloader
{
	public void downloadUpdate() throws IOException
	{
		downloadFile("https://yaraify.abuse.ch/yarahub/yaraify-rules.zip", "yara/yarahub.zip");
		extract("yara/yarahub.zip", "yara/");
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
	
	private void extract(String databaseZipPath, String fileName) throws FileNotFoundException
	{
		File databaseZip = new File(YaraAntivirus.AV.workingDirectory, databaseZipPath);
		File updateFile = new File(YaraAntivirus.AV.workingDirectory, fileName);
		
		if(!updateFile.exists())
			updateFile.mkdirs();
		
		if(!databaseZip.exists())
			throw new FileNotFoundException("Yara Hub Update File Not Found: " + updateFile.getAbsolutePath());
		
		extractDatabase(databaseZip, updateFile);
		
		System.out.println("Deleting " + databaseZip.getAbsolutePath());
		databaseZip.delete();
	}
	
	public static void extractDatabase(File zipFile, File outputFolder)
	{
		try (FileInputStream fis = new FileInputStream(zipFile);
		     ZipInputStream zis = new ZipInputStream(fis))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				File outputFile = new File(outputFolder, entry.getName());
				
				//zipslip
				if(!outputFile.getAbsolutePath().startsWith(outputFolder.getAbsolutePath()))
					continue;
				
				if (entry.isDirectory())
					outputFile.mkdirs();
				else
				{
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