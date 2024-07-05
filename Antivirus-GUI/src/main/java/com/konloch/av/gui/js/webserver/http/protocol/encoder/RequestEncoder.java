package com.konloch.av.gui.js.webserver.http.protocol.encoder;

import com.konloch.av.gui.js.webserver.http.HTTPdLib;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Konloch
 * @since 3/1/2023
 */
public class RequestEncoder
{
	private final HTTPdLib server;
	
	public RequestEncoder(HTTPdLib server)
	{
		this.server = server;
	}
	
	public byte[] generateResponse(Request request, byte[] message)
	{
		HashMap<String, String> headers = request.getResponseHeaders();
		HashSet<String> sentHeaders = new HashSet<>();
		StringBuilder header = new StringBuilder();
		
		//default version and status code
		if(!headers.containsKey(":"))
			headers.put(":", "HTTP/1.1 " + request.getReturnCode());
		
		//default content-type
		if(!headers.containsKey("Content-Type"))
			headers.put("Content-Type", request.getContentType());
		
		//default date (now)
		if(!headers.containsKey("Date"))
			headers.put("Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
		
		//send version
		sentHeaders.add(":");
		header.append(headers.get(":")).append("\n");
		
		//send content-type
		sentHeaders.add("Content-Type");
		header.append("Content-Type: ").append(headers.get("Content-Type")).append("\n");
		
		//send date
		sentHeaders.add("Date");
		header.append("Date: ").append(headers.get("Date")).append("\n");
		
		//send content-length
		sentHeaders.add("Content-Length");
		if(headers.containsKey("Content-Length"))
			header.append("Content-Length: ").append(headers.get("Content-Length")).append("\n");
		
		//send any user supplied headers
		for(String headerName : headers.keySet())
		{
			//skip all headers that have already been crafted
			if(sentHeaders.contains(headerName))
				continue;
			
			header.append(headerName).append(": ").append(headers.get(headerName)).append("\n");
		}
		
		//TODO
		// Cache Control / ETag
		// XSS-Protection / CORS
		// write cookies
		
		//send EOF so the web client can parse the message body
		header.append("\n");
		
		//create the response output stream
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		
		//build the response
		try
		{
			response.write(header.toString().getBytes(StandardCharsets.UTF_8));
			response.write(message);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//return the response as a byte array
		return response.toByteArray();
	}
}