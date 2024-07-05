package com.konloch.av.downloader.impl.yara.rules;

import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadState;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Downloads & extracts Yara Rules from DefenderYara
 *
 * @author Konloch
 * @since 6/26/2024
 */
public class DefenderYaraDownloader extends YaraHubDownloader
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
		//TODO disabled due to scan speed
		
		//every week preform the daily update
		//if (System.currentTimeMillis() - Antivirus.AV.sqLiteDB.getLongConfig("defender-yara.database.age") >= 1000 * 60 * 60 * 24 * 7)
		//	return DownloadState.DAILY;
		
		return DownloadState.NONE;
	}
	
	private void downloadUpdate() throws IOException, SQLException
	{
		downloadFile("https://github.com/roadwy/DefenderYara/archive/refs/heads/main.zip", "yara/defender-yara.zip");
		extract("yara/defender-yara.zip", "yara/DefenderYara", "yara", "yar");
		Antivirus.AV.sqLiteDB.upsertIntegerConfig("defender-yara.database.age", System.currentTimeMillis());
	}
}