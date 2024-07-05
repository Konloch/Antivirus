package com.konloch.av.gui.js.webserver.http.protocol.decoder;

/**
 * @author Konloch
 * @since 3/8/2023
 */
public class MultiPart
{
	private final String name;
	private final String fileName;
	private final byte[] value;
	
	public MultiPart(String name, String fileName, byte[] value)
	{
		this.name = name;
		this.fileName = fileName;
		this.value = value;
	}
	
	public void getValue()
	{
		//TODO parse and return
	}
}
