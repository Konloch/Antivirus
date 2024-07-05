package com.konloch.av.gui.js.webserver.http.protocol.decoder;

import java.util.HashMap;

/**
 * @author Konloch
 * @since 3/8/2023
 */
public class MultiPartDecoder
{
	private final byte[] multipart;
	private HashMap<String, MultiPart> parts = new HashMap<>();
	
	public MultiPartDecoder(byte[] multipart)
	{
		this.multipart = multipart;
		parse();
	}
	
	private void parse()
	{
		//TODO
	}
	
	public MultiPart get(String name)
	{
		return parts.get(name);
	}
}
