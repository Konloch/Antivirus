package com.konloch.av.downloader;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public interface Downloader
{
	void download(DownloadFrequency state) throws IOException, SQLException;
	
	DownloadFrequency getState() throws IOException, SQLException;
	
	String getName();
}
