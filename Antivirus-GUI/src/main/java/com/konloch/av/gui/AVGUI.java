package com.konloch.av.gui;

import com.konloch.Antivirus;
import com.konloch.av.gui.js.AVQuarantineGUI;
import com.konloch.av.gui.js.AVScannerGUI;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.settings.AVSettings;
import com.konloch.av.gui.js.AVSettingsGUI;
import com.konloch.av.gui.swing.AVFailedToBind;
import com.konloch.av.gui.tray.AVTray;
import com.konloch.av.scanengine.ScanEngine;
import com.konloch.av.utils.SwingUtils;

import java.awt.*;
import java.io.*;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVGUI
{
	public static AVGUI GUI;
	
	public ScanEngine scanEngine;
	
	public AVTray tray;
	public AVSettings avSettings;
	public AVScannerGUI guiScanner;
	public AVSettingsGUI guiSettings;
	public AVQuarantineGUI guiQuarantine;
	
	public void initGUI()
	{
		try
		{
			if(!GraphicsEnvironment.isHeadless())
			{
				//init extended core
				scanEngine = new ScanEngine();
				
				//init gui
				avSettings = new AVSettings();
				guiSettings = new AVSettingsGUI();
				guiScanner = new AVScannerGUI();
				guiQuarantine = new AVQuarantineGUI();
				tray = new AVTray();
				
				//init settings
				avSettings.init();
				
				//start scan engine
				scanEngine.init();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		//set the system look and feel
		SwingUtils.setSystemLookAndFeel();
		
		//bind the AV webserver
		AVWebserver.bind();
		
		//alert bind failures
		if(AVWebserver.FAILED_TO_BIND)
		{
			new AVFailedToBind();
			return;
		}
		
		//CLI check
		boolean CLI = args.length != 0;
		
		//create AV & startup
		Antivirus.AV = new Antivirus();
		Antivirus.AV.startup();
		
		//init GUI
		if(!CLI)
		{
			GUI = new AVGUI();
			GUI.initGUI();
		}
		
		//run AV engine
		Antivirus.AV.run();
		
		//preform scan on CLI or show GUI
		if(CLI)
			Antivirus.AV.scan(args);
		else
			AVGUI.GUI.guiScanner.setVisible(true);
		
		//print the db stats
		Antivirus.AV.sqLiteDB.printDatabaseStatistics();
	}
}