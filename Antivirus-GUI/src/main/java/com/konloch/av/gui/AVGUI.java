package com.konloch.av.gui;

import com.konloch.Antivirus;
import com.konloch.av.gui.js.AVScannerGUI;
import com.konloch.av.gui.settings.AVSettings;
import com.konloch.av.gui.js.AVSettingsGUI;
import com.konloch.av.gui.tray.AVTray;

import java.awt.*;
import java.io.IOException;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVGUI
{
	public static AVGUI GUI;
	
	public AVTray tray;
	public AVSettings avSettings;
	public AVScannerGUI guiScanner;
	public AVSettingsGUI guiSettings;
	
	public void initGUI()
	{
		try
		{
			if(!GraphicsEnvironment.isHeadless())
			{
				avSettings = new AVSettings();
				guiSettings = new AVSettingsGUI();
				guiScanner = new AVScannerGUI();
				tray = new AVTray();
				
				//init settings
				avSettings.init();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		boolean CLI = args.length != 0;
		Antivirus.AV = new Antivirus();
		Antivirus.AV.startup();
		
		if(!CLI)
		{
			GUI = new AVGUI();
			GUI.initGUI();
		}
		
		Antivirus.AV.run();
		
		if(CLI)
			Antivirus.AV.scan(args);
		else
			AVGUI.GUI.guiScanner.setVisible(true);
		
		//print the db stats
		Antivirus.AV.sqLiteDB.printDatabaseStatistics();
	}
}