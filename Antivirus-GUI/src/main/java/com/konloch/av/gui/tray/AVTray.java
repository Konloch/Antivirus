package com.konloch.av.gui.tray;

import com.konloch.AVConstants;
import com.konloch.av.gui.AVGUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/3/2024
 */
public class AVTray
{
	public SystemTray tray;
	public TrayIcon trayIcon;
	public PopupMenu trayPopup;
	public MenuItem settings;
	public MenuItem scanner;
	public MenuItem quarantine;
	
	public AVTray() throws AWTException, IOException
	{
		tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))), AVConstants.TITLE);
		trayPopup = new PopupMenu();
		
		scanner = new MenuItem("Scanner");
		scanner.addActionListener(e ->
		{
			AVGUI.GUI.guiScanner.setVisible(true);
			AVGUI.GUI.guiScanner.requestFocus();
		});
		trayPopup.add(scanner);
		
		settings = new MenuItem("Settings");
		settings.addActionListener(e ->
		{
			AVGUI.GUI.guiSettings.setVisible(true);
			AVGUI.GUI.guiSettings.requestFocus();
		});
		trayPopup.add(settings);
		
		quarantine = new MenuItem("Quarantine");
		quarantine.addActionListener(e ->
		{
			AVGUI.GUI.guiQuarantine.setVisible(true);
			AVGUI.GUI.guiQuarantine.requestFocus();
		});
		trayPopup.add(quarantine);
		
		MenuItem exitButton = new MenuItem("Exit");
		exitButton.addActionListener(e -> System.exit(0));
		trayPopup.add(exitButton);
		
		trayIcon.setPopupMenu(trayPopup);
		
		tray.add(trayIcon);
	}
	
}
