package com.konloch.av.gui.js.webserver.api;

import java.util.List;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class ScanProgress
{
	public int progress;
	public List<ScannedFile> scannedFiles;
	public String currentFile;
	public int duration;
	public int remaining;
	
	public ScanProgress(int progress, List<ScannedFile> scannedFiles, String currentFile, int duration, int remaining)
	{
		this.progress = progress;
		this.scannedFiles = scannedFiles;
		this.currentFile = currentFile;
		this.duration = duration;
		this.remaining = remaining;
	}
}
