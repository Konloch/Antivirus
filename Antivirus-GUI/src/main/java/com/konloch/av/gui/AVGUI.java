package com.konloch.av.gui;

import com.konloch.Antivirus;
import com.konloch.av.gui.swing.AVSettingsGUI;
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
	public AVSettingsGUI guiSettings;
	
	public void initGUI()
	{
		try
		{
			if(!GraphicsEnvironment.isHeadless())
			{
				guiSettings = new AVSettingsGUI();
				tray = new AVTray();
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
	}
}