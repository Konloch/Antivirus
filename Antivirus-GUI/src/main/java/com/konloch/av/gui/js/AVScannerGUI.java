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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVScannerGUI extends JFrame
{
	public WebView webView;
	
	public AVScannerGUI() throws IOException
	{
		setTitle(AVConstants.TITLE + " Scanner " + AVConstants.VERSION);
		setIconImage(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JFXPanel jfxPanel = new JFXPanel();
		getContentPane().add(jfxPanel);
		
		Platform.runLater(() ->
		{
			webView = new WebView();
			webView.getEngine().setJavaScriptEnabled(true);
			webView.setContextMenuEnabled(false);
			
			DisableJavaFXWebViewSelection disableJavaFXWebViewSelection = new DisableJavaFXWebViewSelection(webView.getEventDispatcher());
			webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
			{
				if(newValue.equals(Worker.State.SUCCEEDED))
					webView.setEventDispatcher(disableJavaFXWebViewSelection);
			});
			
			reload();
			
			jfxPanel.setScene(new Scene(webView));
			webView.setOnDragOver(this::dragOver);
			webView.setOnDragDropped(this::dragDropped);
		});
		
		setSize(1920/3, 460);
		setLocationRelativeTo(null);
	}
	
	public void reload()
	{
		webView.getEngine().load("http://localhost:" + AVWebserver.PORT + AVWebserver.RANDOM_PATH_IDENTIFIER + "/scanner.html");
	}
	
	private void dragOver(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		if (db.hasFiles())
			e.acceptTransferModes(TransferMode.COPY);
		else
			e.consume();
	}
	
	private void dragDropped(DragEvent e)
	{
		boolean success = false;
		
		if (e.getDragboard().hasFiles())
		{
			success = true;
			
			AVGUI.GUI.scanEngine.selectedFilesForNextScan.clear();
			
			for(File file :  e.getDragboard().getFiles())
			{
				AVGUI.GUI.scanEngine.selectedFilesForNextScan.add(file);
				System.out.println("Dragged and dropped file: " + file.getAbsolutePath());
			}
		}
		
		e.setDropCompleted(success);
		e.consume();
		
		if(success)
		{
			AVGUI.GUI.scanEngine.dontPromptForNextScan = true;
			boolean stageTwo = AVGUI.GUI.scanEngine.scanGUIStage == 1;
			
			if(stageTwo) //press back on scanner stage 2
			{
				String script = "document.querySelector(\"#__next > div > div > button\").click();" +
								"var observer = new MutationObserver(function(mutations) {" +
								"   mutations.forEach(function(mutation) {" +
								"       if (mutation.type === 'childList') {" +
								"           document.querySelector(\"#__next > div > div > button:nth-child(3)\").click();" +
								"           observer.disconnect();" +
								"       }" +
								"   });" +
								"});" +
								"observer.observe(document.querySelector(\"#__next > div > div\"), { childList: true });";
				webView.getEngine().executeScript(script);
			}
			else //on scanner stage 1, press the 3rd option
			{
				webView.getEngine().executeScript("document.querySelector(\"#__next > div > div > button:nth-child(3)\").click();");
			}
			
			AVGUI.GUI.scanEngine.scanGUIStage = 1;
		}
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
			new AVScannerGUI().setVisible(true);
		}
	}
}