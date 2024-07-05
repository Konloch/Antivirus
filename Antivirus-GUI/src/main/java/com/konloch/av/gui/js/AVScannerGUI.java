package com.konloch.av.gui.js;

import com.konloch.AVConstants;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.tray.AVTray;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVScannerGUI extends JFrame
{
	public AVScannerGUI() throws IOException
	{
		setTitle(AVConstants.TITLE + " Scanner " + AVConstants.VERSION);
		setIconImage(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JFXPanel jfxPanel = new JFXPanel();
		getContentPane().add(jfxPanel);
		
		Platform.runLater(() ->
		{
			WebView webView = new WebView();
			webView.getEngine().load("http://localhost:" + AVWebserver.PORT + AVWebserver.RANDOM_PATH_IDENTIFIER + "/scanner.html");
			jfxPanel.setScene(new Scene(webView));
		});
		
		setSize(1920/3, 1080/3);
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) throws IOException
	{
		AVWebserver.bind();
		
		if(!AVWebserver.FAILED_TO_BIND)
		{
			new AVScannerGUI().setVisible(true);
		}
	}
}