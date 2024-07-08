package com.konloch.av.downloader.impl.yara.rules;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadState;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Downloads & extracts Yara Rules from Neo23x0
 *
 * @author Konloch
 * @since 6/26/2024
 */
public class Neo23x0Downloader extends YaraHubDownloader
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
		if(!AVConstants.ENABLE_YARA_DATABASE_IMPORT)
			return DownloadState.NONE;
		
		//TODO disabled due to scan speed
		
		//every week preform the daily update
		//if (System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("neo23x0.database.age") >= 1000 * 60 * 60 * 24 * 7)
		//	return DownloadState.DAILY;
		
		return DownloadState.NONE;
	}
	
	private void downloadUpdate() throws IOException, SQLException
	{
		downloadFile("https://github.com/Neo23x0/signature-base/archive/refs/heads/master.zip", "yara/neo23x0.zip");
		extract("yara/neo23x0.zip", "yara/Neo23x0", "yara", "yar");
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("neo23x0.database.age", System.currentTimeMillis());
	}
}