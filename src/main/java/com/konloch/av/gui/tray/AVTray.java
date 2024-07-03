package com.konloch.av.gui.tray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
 * @author Konloch
 * @since 7/3/2024
 */
public class AVTray
{
	public SystemTray tray;
	public TrayIcon trayIcon;
	public PopupMenu trayPopup;
	public MenuItem toggleButton;
	
	public AVTray() throws AWTException, IOException
	{
		tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(ImageIO.read(AVTray.class.getResourceAsStream("/img/icon.png")), "Kon's Antivirus");
		trayPopup = new PopupMenu();
		
		toggleButton = new MenuItem("Settings");
		toggleButton.addActionListener(e ->
		{
		
		});
		trayPopup.add(toggleButton);
		
		MenuItem exitButton = new MenuItem("Exit");
		exitButton.addActionListener(e -> System.exit(0));
		trayPopup.add(exitButton);
		
		trayIcon.setPopupMenu(trayPopup);
		
		tray.add(trayIcon);
	}
	
}
