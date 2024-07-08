package com.konloch;

import com.konloch.av.downloader.Downloader;
import com.konloch.av.downloader.impl.signatures.ClamAVDownloader;
import com.konloch.av.downloader.impl.signatures.MalwareBazaarDownloader;
import com.konloch.av.downloader.impl.signatures.VirusShareDownloader;
import com.konloch.av.downloader.impl.yara.YaraDownloader;
import com.konloch.av.downloader.impl.yara.rules.DefenderYaraDownloader;
import com.konloch.av.downloader.impl.yara.rules.Neo23x0Downloader;
import com.konloch.av.downloader.impl.yara.rules.ReversingLabsDownloader;
import com.konloch.av.downloader.impl.yara.rules.YaraHubDownloader;
import com.konloch.av.utils.WindowsUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class AVConstants
{
	public static final String VERSION = getVersion(Antivirus.class.getPackage().getImplementationVersion());
	public static boolean DEV_MODE = false;
	public static String TITLE = "Antivirus+";
	
	public static boolean STATIC_SCANNING = false;
	public static boolean DYNAMIC_SCANNING = true;
	
	public static boolean ENABLE_YARA_SCANNING = WindowsUtil.IS_WINDOWS;
	public static boolean ENABLE_YARA_COMPILING = WindowsUtil.IS_WINDOWS; //TODO this can be a security concern
	public static boolean ENABLE_YARA_DATABASE_IMPORT = WindowsUtil.IS_WINDOWS;
	public static boolean ENABLE_SIGNATURE_SCANNING_DATABASES_IMPORT = true;
	public static boolean ENABLE_SIGNATURE_SCANNING = true;
	public static boolean ENABLE_REALTIME_FILE_PROTECTION = false;
	public static boolean ENABLE_REALTIME_FILE_SCANNING = false;
	public static boolean ENABLE_REALTIME_PROCESS_SCANNING = false;
	
	public static final String SIGNATURE_IDENTIFIER_VIRUSSHARE_SUBMISSION = "2";
	public static final String SIGNATURE_IDENTIFIER_MALWAREBAZAAR_SUBMISSION = "3";
	
	public static final ArrayList<Downloader> DOWNLOADERS = new ArrayList<>(Arrays.asList(
			new VirusShareDownloader(),
			new MalwareBazaarDownloader(),
			new ClamAVDownloader(),
			new DefenderYaraDownloader(),
			new YaraHubDownloader(),
			new ReversingLabsDownloader(),
			new Neo23x0Downloader(),
			new YaraDownloader()
	));
	
	public static String getVersion(String mavenVersion)
	{
		if(mavenVersion == null)
		{
			DEV_MODE = true;
			return "Developer Mode";
		}
		
		return "v" + mavenVersion;
	}
}