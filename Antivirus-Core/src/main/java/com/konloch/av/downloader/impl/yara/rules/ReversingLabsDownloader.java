package com.konloch.av.downloader.impl.yara.rules;

import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadState;

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
	public void download(DownloadState state) throws IOException, SQLException
	{
		if(state == DownloadState.DAILY)
			downloadUpdate();
	}
	
	@Override
	public DownloadState getState() throws IOException, SQLException
	{
		//every 7 days preform the daily update
		if (System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("reversinglabs.database.age") >= 1000 * 60 * 60 * 24 * 7)
			return DownloadState.DAILY;
		
		return DownloadState.NONE;
	}
	
	private void downloadUpdate() throws IOException, SQLException
	{
		downloadFile("https://github.com/reversinglabs/reversinglabs-yara-rules/archive/refs/heads/develop.zip", "yara/reversinglabs.zip");
		extract("yara/reversinglabs.zip", "yara/ReversingLabs", "yara", "yar");
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("reversinglabs.database.age", System.currentTimeMillis());
	}
}