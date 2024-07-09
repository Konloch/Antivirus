package com.konloch.av.gui.js.webserver.endpoints.settings;

import com.google.gson.reflect.TypeToken;
import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;
import com.konloch.av.gui.settings.AVSettingsEntry;

import javax.swing.event.ChangeEvent;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class SettingsChangeEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		String settingsChangeJson = new String(clientBuffer.bodyBuffer.toByteArray(), StandardCharsets.UTF_8);
		Map<String, Boolean> settingsMap = AVWebserver.gson.fromJson(settingsChangeJson, new TypeToken<Map<String, Boolean>>(){}.getType());
		
		if(settingsMap == null)
			return "".getBytes(StandardCharsets.UTF_8);
		
		for (Map.Entry<String, Boolean> entry : settingsMap.entrySet())
		{
			String settingName = entry.getKey();
			boolean settingValue = entry.getValue();
			
			AVSettingsEntry settingsEntry = AVGUI.GUI.avSettings.settings.get(settingName);
			
			if(settingsEntry == null)
				continue;
			
			settingsEntry.value = settingValue;
			settingsEntry.onChange.stateChanged(new ChangeEvent(settingValue));
			
			System.out.println("Setting: " + settingName + " = " + settingValue);
		}
		
		return "".getBytes(StandardCharsets.UTF_8);
	}
}
