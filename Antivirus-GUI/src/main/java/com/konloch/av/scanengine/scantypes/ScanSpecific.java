package com.konloch.av.scanengine.scantypes;

import com.konloch.Antivirus;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.api.ScannedFile;
import com.konloch.av.scanengine.Scan;
import com.konloch.av.scanengine.ScanEngine;
import com.konloch.util.FastStringUtils;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class ScanSpecific extends Scan
{
	@Override
	public void preformScan(ScanEngine engine)
	{
		System.out.println("Preforming specific file scan...");
		
		boolean preformScan;
		ArrayList<File> scanFiles = new ArrayList<>();
		
		//prompt for the file / dir to scan, then preform the scan
		if(!AVGUI.GUI.scanEngine.dontPromptForNextScan)
		{
			latestUpdate = "Waiting for user to select file";
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select a file / directory to scan");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			
			int returnValue = fileChooser.showOpenDialog(AVGUI.GUI.guiScanner);
			if (returnValue == JFileChooser.APPROVE_OPTION)
			{
				File[] selectedFiles = fileChooser.getSelectedFiles();
				
				latestUpdate = "Indexing files to be scanned...";
				
				for(File selectedFile : selectedFiles)
				{
					if(!selectedFile.exists())
						continue;
					
					scanFiles.add(selectedFile);
					
					walk(engine, scanFiles, selectedFile);
					
					System.out.println("Selected file or directory: " + selectedFile.getAbsolutePath());
				}
				
				preformScan = true;
			}
			else
			{
				progress = 100;
				latestUpdate = "Scan cancelled";
				preformScan = false;
			}
		}
		else
		{
			AVGUI.GUI.scanEngine.dontPromptForNextScan = false;
			
			latestUpdate = "Indexing files to be scanned...";
			
			for(File selectedFile : AVGUI.GUI.scanEngine.selectedFilesForNextScan)
			{
				if(!selectedFile.exists())
					continue;
				
				scanFiles.add(selectedFile);
				
				walk(engine, scanFiles, selectedFile);
			}
			
			AVGUI.GUI.scanEngine.selectedFilesForNextScan.clear();
			
			preformScan = true;
		}
		
		if(preformScan)
			scan(scanFiles);
		
		engine.finished();
	}
	
	public void scan(ArrayList<File> scanFiles)
	{
		String malwareType;
		
		AVGUI.GUI.scanEngine.getLatestScan().totalScans = scanFiles.size();
		
		int count = 0;
		for(File scanFile : scanFiles)
		{
			//check for scan cancelling
			if(AVGUI.GUI.scanEngine.getActiveScan() == null)
				break;
			
			if(!scanFile.exists())
			{
				AVGUI.GUI.scanEngine.getLatestScan().finishedScans++;
				continue;
			}
			
			progress = (int) (((double) count++ / scanFiles.size()) * 100);
			latestUpdate = "Current File: " + scanFile.getName();
			
			if((malwareType = Antivirus.AV.detectAsMalware(scanFile)) != null)
			{
				boolean detected = false;
				
				//extract file from malware type
				for(String fileLine : malwareType.split("\\r?\\n"))
				{
					if(fileLine.contains(" "))
					{
						String[] detectionInfo = FastStringUtils.split(fileLine, " ", 2);
						File detectedFile = new File(detectionInfo[1]);
						
						if(detectedFile.exists())
						{
							MalwareScanFile detectedScanFile = new MalwareScanFile(detectedFile);
							
							//verify the file isn't whitelisted
							if(!detectedScanFile.isWhitelisted())
							{
								System.out.println("Detected: " + malwareType);
								Antivirus.AV.quarantine.quarantineFile(detectedFile, detectionInfo[0]);
								detectedFiles.add(new ScannedFile(detectedFile.getName(), detectedFile.getAbsolutePath(), "Detected...", detectedFile));
								detected = true;
							}
						}
					}
				}
				
				//detected as file being scanned directly
				if(!detected)
				{
					MalwareScanFile detectedScanFile = new MalwareScanFile(scanFile);
					
					//verify the file isn't whitelisted
					if(!detectedScanFile.isWhitelisted())
					{
						System.out.println("Detected: " + malwareType);
						detectedFiles.add(new ScannedFile(scanFile.getName(), scanFile.getAbsolutePath(), "Detected...", scanFile));
					}
				}
			}
			
			AVGUI.GUI.scanEngine.getLatestScan().finishedScans++;
		}
		
		AVGUI.GUI.scanEngine.getLatestScan().finishedScanAt = System.currentTimeMillis();
		AVGUI.GUI.scanEngine.getLatestScan().scanFinished = true;
		progress = 100;
		latestUpdate = "Scan Complete - " + detectedFiles.size() + " Infection" + (detectedFiles.size() == 1 ? "" : "s") + " Detected";
	}
	
	public void walk(ScanEngine engine, ArrayList<File> scanFiles, File selectedFile)
	{
		if(selectedFile.isDirectory())
		{
			try
			{
				try (Stream<Path> stream = Files.walk(selectedFile.toPath()))
				{
					Iterator<Path> iterator = stream.iterator();
					
					while (iterator.hasNext())
					{
						try
						{
							Path path = iterator.next();
							
							if (engine.getActiveScan() == null) //scan stopped
								break;
							
							if (Files.isDirectory(path))
								scanFiles.add(path.toFile());
						}
						catch (Throwable e)
						{
							e.printStackTrace();
						}
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}
