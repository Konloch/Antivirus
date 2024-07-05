package com.konloch.av.gui.js.webserver.http.client;

import com.konloch.av.gui.js.webserver.http.request.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The client buffer represents any incoming socket connection.
 *
 * Until the request has reached the EOL terminator it won't be treated as a http request.
 *
 * @author Konloch
 * @since 3/1/2023
 */
public class ClientBuffer
{
	public final ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
	public final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
	public boolean hasReachedEOL;
	public Request request;
	
	public void writeHeader(byte[] bytes) throws IOException
	{
		int EOLIndex = getEOL(bytes);
		
		if(EOLIndex > 0)
		{
			headerBuffer.write(bytes, 0, EOLIndex);
			bodyBuffer.write(bytes, EOLIndex, bytes.length - EOLIndex);
		}
		else
		{
			headerBuffer.write(bytes);
			
			//requests that have the EOL sent in chunks need to be handled by processing the entire buffer
			if(!hasReachedEOL)
				getEOL(headerBuffer.toByteArray());
		}
	}
	
	public void writeBody(byte[] bytes) throws IOException
	{
		bodyBuffer.write(bytes);
	}
	
	private int getEOL(byte[] bytes)
	{
		int returnCarriageCount = 0;
		boolean returnCarriage = false;
		
		int EOLIndex = 0;
		for(byte b : bytes)
		{
			if(!hasReachedEOL)
				EOLIndex++;
			
			char c = (char) b;
			if(c == '\n' || c == '\r')
			{
				if(returnCarriage)
				{
					if (returnCarriageCount++ >= 2)
						hasReachedEOL = true;
				}
				else
					returnCarriage = true;
			}
			else if(returnCarriage)
			{
				returnCarriage = false;
				returnCarriageCount = 0;
			}
		}
		
		return EOLIndex;
	}
}
