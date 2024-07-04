package com.konloch.av.gui.tray;

import com.konloch.Antivirus;

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
	public MenuItem toggleButton;
	
	public AVTray() throws AWTException, IOException
	{
		tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))), "Konloch's Antivirus");
		trayPopup = new PopupMenu();
		
		toggleButton = new MenuItem("Settings");
		toggleButton.addActionListener(e ->
		{
			Antivirus.AV.guiSettings.setVisible(true);
			Antivirus.AV.guiSettings.requestFocus();
		});
		trayPopup.add(toggleButton);
		
		MenuItem exitButton = new MenuItem("Exit");
		exitButton.addActionListener(e -> System.exit(0));
		trayPopup.add(exitButton);
		
		trayIcon.setPopupMenu(trayPopup);
		
		tray.add(trayIcon);
	}
	
}
