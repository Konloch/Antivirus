package com.konloch.av.gui.js.webserver;

import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.nio.charset.StandardCharsets;

import static com.konloch.av.gui.js.webserver.AVWebserver.RANDOM_KEY;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public abstract class AVEndPoint
{
	public byte[] returnPage(ClientBuffer clientBuffer)
	{
		Request request = clientBuffer.request;
		
		String key = request.getGet().get("key");
		
		if(!RANDOM_KEY.equals(key))
		{
			System.out.println("Response rejected: " + request.getPath()  + " using key " + key);
			return "".getBytes(StandardCharsets.UTF_8);
		}
		
		return process(clientBuffer, request);
	}
	
	public abstract byte[] process(ClientBuffer clientBuffer, Request request);
}
