package com.konloch.av.gui.js.webserver.endpoints.settings;

import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;
import com.konloch.av.gui.settings.AVSettingsEntry;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class SettingsStatusEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		Map<String, Boolean> settings = new LinkedHashMap<>();
		for(AVSettingsEntry entry : AVGUI.GUI.avSettings.settings.values())
			settings.put(entry.name, entry.value);
		
		String jsonString = AVWebserver.gson.toJson(settings);
		return jsonString.getBytes(StandardCharsets.UTF_8);
	}
}
