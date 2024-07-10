package com.konloch.av.downloader.impl.yara.rules;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadFrequency;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Downloads & extracts Yara Rules from Reversing Labs
 *
 * @author Konloch
 * @since 6/26/2024
 */
public class ReversingLabsDownloader extends YaraHubDownloader
{
	@Override
	public String getName()
	{
		return "Reversing Labs (Yara Rules)";
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
		if(!AVConstants.ENABLE_YARA_DATABASE_IMPORT)
			return DownloadFrequency.NONE;
		
		//every 7 days preform the daily update
		if (System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("reversinglabs.database.age") >= 1000 * 60 * 60 * 24 * 7)
			return DownloadFrequency.DAILY;
		
		return DownloadFrequency.NONE;
	}
	
	private void downloadUpdate() throws IOException, SQLException
	{
		downloadFile("https://github.com/reversinglabs/reversinglabs-yara-rules/archive/refs/heads/develop.zip", "yara/reversinglabs.zip");
		extract("yara/reversinglabs.zip", "yara/ReversingLabs", "yara", "yar");
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("reversinglabs.database.age", System.currentTimeMillis());
	}
}