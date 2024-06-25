package com.konloch.av.downloader.impl;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.downloader.DownloadState;
import com.konloch.av.downloader.Downloader;
import com.konloch.av.utils.HashUtils;
import com.konloch.util.FastStringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

/**
 * Downloads & extracts the Clam AV Database from clamav.net
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class ClamAVDownloader implements Downloader
{
	@Override
	public void download(DownloadState state) throws IOException, SQLException
	{
		switch(state)
		{
			case INITIAL:
				downloadFullUpdate();
				break;
			
			case DAILY:
				downloadDailyUpdate();
				break;
		}
	}
	
	@Override
	public DownloadState getState() throws IOException, SQLException
	{
		if(!AVConstants.ENABLE_SIGNATURE_SCANNING_DATABASES_IMPORT)
			return DownloadState.NONE;
		
		if (Antivirus.AV.sqLiteDB.getLongConfig("clamav.database.main.age") == 0)
			return DownloadState.INITIAL;
		
		//TODO make it every 4 hours
		// + in order to do this we need to support diffpatches and finish the libfreshclam implementation
		if(System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("clamav.database.daily.age") >= 1000 * 60 * 60 * 24 * 7)
			return DownloadState.DAILY;
		
		return DownloadState.NONE;
	}
	
	private void downloadFullUpdate() throws IOException, SQLException
	{
		downloadFile("https://database.clamav.net/main.cvd", "main.cvd");
		extractDatabase(new File(Antivirus.AV.workingDirectory, "main"),
				new File(Antivirus.AV.workingDirectory, "main.cvd"));
		
		//setup db
		Antivirus.AV.sqLiteDB.optimizeDatabase();
		
		loadMDB("main/main.mdb");
		loadHSB("main/main.hsb");
		
		//finalize db
		Antivirus.AV.sqLiteDB.insertAllWaitingSignatures();
		Antivirus.AV.sqLiteDB.resetDatabaseOptimization();
		
		//delete the main folder
		new File(Antivirus.AV.workingDirectory, "main").delete();
		
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("clamav.database.main.age", System.currentTimeMillis());
		
		downloadDailyUpdate();
	}
	
	private void downloadDailyUpdate() throws IOException, SQLException
	{
		downloadFile("http://database.clamav.net/daily.cvd", "daily.cvd");
		extractDatabase(new File(Antivirus.AV.workingDirectory, "daily"),
				new File(Antivirus.AV.workingDirectory, "daily.cvd"));
		
		//setup db
		Antivirus.AV.sqLiteDB.optimizeDatabase();
		
		loadMDB("daily/daily.mdb");
		loadHSB("daily/daily.hsb");
		
		//finalize db
		Antivirus.AV.sqLiteDB.insertAllWaitingSignatures();
		Antivirus.AV.sqLiteDB.resetDatabaseOptimization();
		
		//delete the daily folder
		new File(Antivirus.AV.workingDirectory, "daily").delete();
		
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("clamav.database.daily.age", System.currentTimeMillis());
	}
	
	private void downloadFile(String url, String fileName) throws IOException
	{
		File updateFile = new File(Antivirus.AV.workingDirectory, fileName);
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
					
					//zipslip
					if(!outputFile.getAbsolutePath().startsWith(databaseFolder.getAbsolutePath()))
						continue;
					
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
	
	private void loadMDB(String file)
	{
		System.out.println("Inserting " + file + " into SQLite db...");
		
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(Antivirus.AV.workingDirectory, file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] signatureInformation = FastStringUtils.split(line, ":", 3);
				
				if(signatureInformation.length == 3)
				{
					FileSignature fileSignature = new FileSignature(signatureInformation[1], Long.parseLong(signatureInformation[0]), signatureInformation[2]);
					fileSignature.insert();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadHSB(String file)
	{
		System.out.println("Inserting " + file + " into SQLite db...");
		
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(Antivirus.AV.workingDirectory, file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] signatureInformation = FastStringUtils.split(line, ":", 3);
				
				if(signatureInformation.length == 3)
				{
					String length = signatureInformation[1];
					FileSignature fileSignature = new FileSignature(signatureInformation[0], length.equals("*") ? 0 : Long.parseLong(length), signatureInformation[2]);
					fileSignature.insert();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}