package com.konloch.clamav.downloader;

import com.konloch.TraditionalAntivirus;
import com.konloch.tav.utils.HashUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.net.*;
import java.util.zip.GZIPInputStream;

/**
 * Downloads & extracts the Clam AV Database from clamav.net
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class ClamAVDownloader
{
	public void downloadFullUpdate() throws IOException
	{
		downloadFile("https://database.clamav.net/main.cvd", "main.cvd");
		updateDatabase("main", "main.cvd");
		downloadFile("https://database.clamav.net/daily.cvd", "daily.cvd");
		updateDatabase("daily", "daily.cvd");
		downloadFile("https://database.clamav.net/bytecode.cvd", "bytecode.cvd");
		updateDatabase("bytecode", "bytecode.cvd");
	}
	
	public void downloadDailyUpdate() throws IOException
	{
		downloadFile("http://database.clamav.net/daily.cvd", "daily.cvd");
		updateDatabase("daily", "daily.cvd");
	}
	
	private void downloadFile(String url, String fileName) throws IOException
	{
		File updateFile = new File(TraditionalAntivirus.TAV.db.getWorkingDirectory(), fileName);
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "clamav/1.3.1 (Identifier: " + generateUniqueString() + ")");
		
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
	
	private String generateUniqueString()
	{
		StringBuilder sb = new StringBuilder(System.getProperty("user.home"));
		
		try
		{
			InetAddress localHost = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(localHost);
			byte[] mac = network.getHardwareAddress();
			
			for (int i = 0; i < mac.length; i++)
			{
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
		}
		catch (UnknownHostException | SocketException e)
		{
			e.printStackTrace();
		}
		
		return HashUtils.getMD5Hash(sb.toString());
	}
	
	private void updateDatabase(String databaseName, String fileName) throws FileNotFoundException
	{
		File databaseFolder = new File(TraditionalAntivirus.TAV.db.getWorkingDirectory(), databaseName);
		File updateFile = new File(TraditionalAntivirus.TAV.db.getWorkingDirectory(), fileName);
		
		if(!databaseFolder.exists())
			databaseFolder.mkdirs();
		
		if(!updateFile.exists())
			throw new FileNotFoundException("Database Update File Not Found: " + updateFile.getAbsolutePath());
		
		extractDatabase(databaseFolder, updateFile);
		
		System.out.println("Deleting " + updateFile.getAbsolutePath());
		updateFile.delete();
	}
	
	private void extractDatabase(File databaseFolder, File updateFile)
	{
		int cavHeaderSize = 0x200; //skip versioning / comments / etc
		
		try (FileInputStream fis = new FileInputStream(updateFile);
		     BufferedInputStream bis = new BufferedInputStream(fis))
		{
			//skip the header
			if (bis.skip(cavHeaderSize) != cavHeaderSize)
				throw new IOException("Failed to skip the required number of bytes");
			
			//wrap the remaining stream in a GZIPInputStream, then decompress the tar archive from that tream
			try (GZIPInputStream gzis = new GZIPInputStream(bis);
			     TarArchiveInputStream tais = new TarArchiveInputStream(gzis))
			{
				//process the tar entries
				TarArchiveEntry entry;
				while ((entry = tais.getNextTarEntry()) != null)
				{
					File outputFile = new File(databaseFolder, entry.getName());
					
					//ensure parent exists before writing
					outputFile.getParentFile().mkdirs();
					
					System.out.println("Extracting " + entry.getName() + " to " + outputFile.getAbsolutePath() + "...");
					
					try (FileOutputStream fos = new FileOutputStream(outputFile))
					{
						byte[] buffer = new byte[1024];
						int len;
						while ((len = tais.read(buffer)) > 0)
						{
							fos.write(buffer, 0, len);
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