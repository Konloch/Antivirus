package com.konloch.av.downloader;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public interface Downloader
{
	void download(DownloadState state) throws IOException, SQLException;
	DownloadState getState() throws IOException, SQLException;
}
