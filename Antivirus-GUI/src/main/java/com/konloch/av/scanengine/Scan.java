package com.konloch.av.scanengine;

import com.konloch.av.gui.js.webserver.api.ScannedFile;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public abstract class Scan
{
	public int progress = 0;
	public String latestUpdate = "Initializing scan...";
	public final long started = System.currentTimeMillis();
	public long finishedScanAt;
	public boolean scanFinished;
	public final ArrayList<ScannedFile> detectedFiles = new ArrayList<>();
	public int totalScans = 0;
	public int finishedScans = 0;
	
	public abstract void preformScan(ScanEngine engine);
	
	public String getDuration()
	{
		long scanDuration = (scanFinished ? finishedScanAt : System.currentTimeMillis()) - started;
		long seconds = scanDuration / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		
		minutes %= 60;
		seconds %= 60;
		
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	public String getEstimation()
	{
		long duration = estimateTotalTimeLeft();
		long seconds = duration / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		
		minutes %= 60;
		seconds %= 60;
		
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	public long estimateTotalTimeLeft()
	{
		if (finishedScans == 0)
			return 0;
		
		long averageScanTime = (System.currentTimeMillis() - started) / finishedScans;
		long scansLeft = totalScans - finishedScans;
		
		return averageScanTime * scansLeft;
	}
}
