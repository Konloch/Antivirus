package com.konloch.av.gui.js.webserver.endpoints.scan;

import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.nio.charset.StandardCharsets;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class ScanFullEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		AVGUI.GUI.scanEngine.scanGUIStage = 1;
		//TODO full computer scan
		return "".getBytes(StandardCharsets.UTF_8);
	}
}
