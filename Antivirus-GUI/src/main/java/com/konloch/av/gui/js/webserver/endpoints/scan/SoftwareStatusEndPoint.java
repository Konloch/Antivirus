package com.konloch.av.gui.js.webserver.endpoints.scan;

import com.konloch.Antivirus;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.nio.charset.StandardCharsets;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class SoftwareStatusEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		String jsonString = AVWebserver.gson.toJson(Antivirus.AV.softwareStatus);
		return jsonString.getBytes(StandardCharsets.UTF_8);
	}
}
