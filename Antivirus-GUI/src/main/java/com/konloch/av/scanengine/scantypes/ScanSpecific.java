package com.konloch.av.scanengine.scantypes;

import com.konloch.Antivirus;
import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.api.ScannedFile;
import com.konloch.av.scanengine.Scan;
import com.konloch.av.scanengine.ScanEngine;
import com.konloch.util.FastStringUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

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
		
		//TODO prompt for the file / dir to scan, then preform the scan
		if(!AVGUI.GUI.scanEngine.ignoreNextScan)
		{
			latestUpdate = "Waiting for user to select file";
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select a file / directory to scan");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			
			int returnValue = fileChooser.showOpenDialog(AVGUI.GUI.guiScanner);
			if (returnValue == JFileChooser.APPROVE_OPTION)
			{
				File[] selectedFiles = fileChooser.getSelectedFiles();
				
				for(File selectedFile : selectedFiles)
				{
					scanFiles.add(selectedFile);
					
					if(!selectedFile.exists())
						continue;
					
					walk(scanFiles, selectedFile);
					
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
			AVGUI.GUI.scanEngine.ignoreNextScan = false;
			
			for(File selectedFile : AVGUI.GUI.scanEngine.selectedFilesForNextScan)
			{
				walk(scanFiles, selectedFile);
			}
			
			AVGUI.GUI.scanEngine.selectedFilesForNextScan.clear();
			
			//TODO
			preformScan = true;
		}
		
		if(preformScan)
		{
			String malwareType;
			
			AVGUI.GUI.scanEngine.getLatestScan().totalScans = scanFiles.size();
			
			int count = 0;
			for(File scanFile : scanFiles)
			{
				if(!scanFile.exists())
				{
					AVGUI.GUI.scanEngine.getLatestScan().finishedScans++;
					continue;
				}
				
				System.out.println("Scanning: " + scanFile.getAbsolutePath());
				
				progress = (int) (((double) count++ / scanFiles.size()) * 100);
				latestUpdate = "Current File: " + scanFile.getName();
				
				if((malwareType = Antivirus.AV.detectAsMalware(scanFile)) != null)
				{
					boolean detected = false;
					for(String fileLine : malwareType.split("\\r?\\n"))
					{
						if(fileLine.contains(" "))
						{
							String[] detectionInfo = FastStringUtils.split(fileLine, " ");
							File selectedFile = new File(detectionInfo[1]);
							
							if(selectedFile.exists())
							{
								//TODO extract file from malware type
								detectedFiles.add(new ScannedFile(selectedFile.getName(), "Detected...", selectedFile));
								detected = true;
							}
						}
					}
					
					System.out.println("Detected: " + malwareType);
					
					if(!detected)
						detectedFiles.add(new ScannedFile(scanFile.getName(), "Detected...", scanFile));
				}
				
				AVGUI.GUI.scanEngine.getLatestScan().finishedScans++;
			}
			
			
			AVGUI.GUI.scanEngine.getLatestScan().finishedScanAt = System.currentTimeMillis();
			AVGUI.GUI.scanEngine.getLatestScan().scanFinished = true;
			progress = 100;
			latestUpdate = "Scan Complete - " + detectedFiles.size() + " Infection" + (detectedFiles.size() == 1 ? "" : "s") + " Detected";
		}
		
		engine.finished();
	}
	
	private void walk(ArrayList<File> scanFiles, File selectedFile)
	{
		if(selectedFile.isDirectory())
		{
			try
			{
				Files.walkFileTree(selectedFile.toPath(), new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					{
						//scanFiles.add(file.toFile());
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					{
						scanFiles.add(dir.toFile());
						return FileVisitResult.CONTINUE;
					}
				});
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}