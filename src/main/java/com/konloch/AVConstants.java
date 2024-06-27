package com.konloch;

import com.konloch.av.downloader.Downloader;
import com.konloch.av.downloader.impl.signatures.ClamAVDownloader;
import com.konloch.av.downloader.impl.signatures.MalwareBazaarDownloader;
import com.konloch.av.downloader.impl.signatures.VirusShareDownloader;
import com.konloch.av.downloader.impl.yara.YaraDownloader;
import com.konloch.av.downloader.impl.yara.rules.YaraHubDownloader;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class AVConstants
{
	public static boolean ENABLE_SIGNATURE_SCANNING_DATABASES_IMPORT = false;
	public static boolean ENABLE_SIGNATURE_SCANNING = false;
	
	public static final Downloader[] DOWNLOADERS = new Downloader[]
	{
			new VirusShareDownloader(),
			new MalwareBazaarDownloader(),
			new ClamAVDownloader(),
			new YaraHubDownloader(),
			new YaraDownloader(),
	};
}