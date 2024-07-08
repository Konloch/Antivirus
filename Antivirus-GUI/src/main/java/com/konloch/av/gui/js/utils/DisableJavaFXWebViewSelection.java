package com.konloch.av.gui.js.utils;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javax.swing.*;

/**
 * @author Konloch
 * @author https://stackoverflow.com/a/27042293
 * @since 7/5/2024
 */
public class DisableJavaFXWebViewSelection implements EventDispatcher
{
	private final JComponent component;
	private final EventDispatcher oldDispatcher;
	
	public DisableJavaFXWebViewSelection(JComponent component, EventDispatcher oldDispatcher)
	{
		this.component = component;
		this.oldDispatcher = oldDispatcher;
	}
	
	@Override
	public Event dispatchEvent(Event event, EventDispatchChain tail)
	{
		if (event instanceof MouseEvent)
		{
			MouseEvent m = (MouseEvent) event;
			
			//disable selection
			if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
			{
				//TODO this needs to be able to detect if it's on a webview scrollbar
				if(m.getX() < component.getWidth()-10)
					event.consume();
			}
			
			//disable double click selection
			if(m.getClickCount() > 1)
				event.consume();
		}
		
		if (event instanceof KeyEvent && event.getEventType().equals(KeyEvent.KEY_PRESSED))
		{
			KeyEvent k = (KeyEvent)event;
			
			//disable ctrl + a
			try
			{
				if (k.isControlDown() && (int) k.getText().toCharArray()[0] == 1)
					event.consume();
			}
			catch (Exception e)
			{
				//ignore
			}
		}
		
		return oldDispatcher.dispatchEvent(event, tail);
	}
}
