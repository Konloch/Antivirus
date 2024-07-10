package com.konloch.av.scanengine;

import com.konloch.Antivirus;
import com.konloch.av.scanengine.scantypes.ScanFull;
import com.konloch.av.scanengine.scantypes.ScanQuick;
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
	public boolean dontPromptForNextScan;
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
				
				if (activeScan != null)
				{
					if(Antivirus.AV.flags.updateFinished)
						activeScan.preformScan(this);
					else
						activeScan.latestUpdate = "Waiting for database update to finish...";
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
	
	public void scanQuick()
	{
		if(activeScan != null)
			return;
		
		latestScan = activeScan = new ScanQuick();
	}
	
	public void scanFull()
	{
		if(activeScan != null)
			return;
		
		latestScan = activeScan = new ScanFull();
	}
	
	public void scanSpecific()
	{
		if(activeScan != null)
			return;
		
		latestScan = activeScan = new ScanSpecific();
	}
	
	public void scanStop()
	{
		activeScan = null;
		System.out.println("STOPPED SCAN");
	}
	
	public void finished()
	{
		activeScan = null;
	}
	
	public Scan getActiveScan()
	{
		return activeScan;
	}
	
	public Scan getLatestScan()
	{
		return latestScan;
	}
}
