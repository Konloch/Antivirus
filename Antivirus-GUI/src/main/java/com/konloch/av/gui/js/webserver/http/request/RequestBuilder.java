package com.konloch.av.gui.js.webserver.http.request;

import com.konloch.av.gui.js.webserver.http.HTTPdLib;
import com.konloch.av.gui.js.webserver.http.protocol.decoder.RequestDecoder;
import com.konloch.av.gui.js.webserver.http.protocol.encoder.RequestEncoder;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.socket.SocketClient;
import com.konloch.util.FastStringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author Konloch
 * @since 3/1/2023
 */
public class RequestBuilder
{
	private final RequestDecoder decoder;
	private final RequestEncoder encoder;
	
	public RequestBuilder(HTTPdLib server)
	{
		this.decoder = new RequestDecoder(server);
		this.encoder = new RequestEncoder(server);
	}
	
	public Request build(SocketClient client, ClientBuffer buffer)
	{
		final byte[] request = buffer.headerBuffer.toByteArray();
		
		//decode the initial request parameters
		final HashMap<String, String> parameters = decoder.decodeHeaders(request);
		HashMap<String, String> cookies;
		HashMap<String, String> post;
		HashMap<String, String> get;
		
		final String header = parameters.get(":");
		
		//malformed request
		if(header == null)
			return null;
		
		//split the header parameters
		final String[] headerParams = FastStringUtils.split(header, " ", 3);
		
		//only return the request if it's valid
		if(headerParams.length == 3)
		{
			String method = headerParams[0];
			String path = headerParams[1];
			String version = headerParams[2];
			
			final Request.RequestType methodType = Request.RequestType.from(method);
			
			//decode GET
			final int getIndex = path.indexOf('?');
			if(getIndex != -1)
			{
				final String getData = path.substring(getIndex);
				get = decoder.decodeParameters(getData.substring(1)); //substring the '?'
				
				//truncate path
				path = path.substring(0, getIndex);
			}
			else
				get = new HashMap<>();
			
			// decode POST
			if(methodType == Request.RequestType.POST)
				post = decoder.decodeParameters(new String(buffer.bodyBuffer.toByteArray(), StandardCharsets.UTF_8));
			else
				post = new HashMap<>();
			
			//decode cookies
			if(parameters.containsKey("Cookie"))
				cookies = decoder.decodeCookies(parameters.get("Cookie"));
			else
				cookies = new HashMap<>();
			
			return new Request(client.getRemoteAddress(), methodType, path, version, parameters, cookies, get, post);
		}
		
		return null;
	}
	
	public RequestDecoder getDecoder()
	{
		return decoder;
	}
	
	public RequestEncoder getEncoder()
	{
		return encoder;
	}
}