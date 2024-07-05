package com.konloch.av.scanengine;

import com.konloch.Antivirus;
import com.konloch.av.scanengine.scantypes.ScanSpecific;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class ScanEngine
{
	public boolean ignoreNextScan;
	public int scanGUIStage = 0;
	public final List<File> selectedFilesForNextScan = new ArrayList<>();
	private Scan activeScan;
	private Scan latestScan;
	
	private final Thread scanThread = new Thread(() ->
	{
		while(true)
		{
			try
			{
				Thread.sleep(1);
				
				if (Antivirus.AV.flags.updateFinished && activeScan != null)
				{
					activeScan.preformScan(this);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}, "ScanEngine");
	
	public void init()
	{
		scanThread.start();
	}
	
	public void scanSpecific()
	{
		if(activeScan != null)
			return;
		
		latestScan = activeScan = new ScanSpecific();
	}
	
	public void finished()
	{
		activeScan = null;
	}
	
	public Scan getLatestScan()
	{
		return latestScan;
	}
}
