package com.konloch.av.gui.swing;

import com.konloch.AVConstants;
import com.konloch.av.gui.tray.AVTray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class AVFailedToBind extends JFrame
{
	public AVFailedToBind() throws IOException
	{
		setTitle("Critical Issue");
		setIconImage(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel label = new JLabel("<html><center>Antivirus failed to boot the webserver<br>This means we cannot continue to boot into the GUI<br>Please post an issue on the Github</center></html>");
		label.setHorizontalAlignment(JLabel.CENTER);
		
		JButton okButton = new JButton("Acknowledged");
		okButton.addActionListener(e -> System.exit(0));
		
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(okButton);
		
		getContentPane().add(panel);
		
		setAlwaysOnTop(true);
		setSize(300, 115);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
}
