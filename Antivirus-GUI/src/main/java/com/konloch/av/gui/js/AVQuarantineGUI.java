package com.konloch.av.gui.js;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.utils.DisableJavaFXWebViewSelection;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.tray.AVTray;
import javafx.application.Platform;
import javafx.concurrent.Worker;
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
public class AVQuarantineGUI extends JFrame
{
	public WebView webView;
	
	public AVQuarantineGUI() throws IOException
	{
		setTitle(AVConstants.TITLE + " Quarantine " + AVConstants.VERSION);
		setIconImage(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JFXPanel jfxPanel = new JFXPanel();
		getContentPane().add(jfxPanel);
		
		Platform.runLater(() ->
		{
			webView = new WebView();
			webView.getEngine().setJavaScriptEnabled(true);
			webView.setContextMenuEnabled(false);
			
			DisableJavaFXWebViewSelection disableJavaFXWebViewSelection = new DisableJavaFXWebViewSelection(jfxPanel, webView.getEventDispatcher());
			webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
			{
				if(newValue.equals(Worker.State.SUCCEEDED))
					webView.setEventDispatcher(disableJavaFXWebViewSelection);
			});
			
			reload();
			
			jfxPanel.setScene(new Scene(webView));
		});
		
		setSize(1920/3, 460);
		setLocationRelativeTo(null);
	}
	
	public void reload()
	{
		webView.getEngine().load("http://localhost:" + AVWebserver.PORT + AVWebserver.RANDOM_PATH_IDENTIFIER + "/quarantine.html?key=" + AVWebserver.RANDOM_KEY);
	}
	
	public static void main(String[] args) throws IOException
	{
		AVWebserver.bind();
		
		Antivirus.AV = new Antivirus();
		Antivirus.AV.startup();
		
		AVGUI.GUI = new AVGUI();
		AVGUI.GUI.initGUI();
		
		if(!AVWebserver.FAILED_TO_BIND)
		{
			new AVQuarantineGUI().setVisible(true);
		}
	}
}