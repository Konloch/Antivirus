package com.konloch.av.gui.js.webserver.http;

import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;
import com.konloch.av.gui.js.webserver.http.request.RequestBuilder;
import com.konloch.av.gui.js.webserver.http.request.RequestListener;
import com.konloch.socket.SocketClient;
import com.konloch.socket.SocketServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A minified HTTP 1.1 compliant webserver.
 *
 * @author Konloch
 * @since 2/28/2023
 */
public class HTTPdLib
{
	private final RequestBuilder requestBuilder;
	private final SocketServer server;
	private final HashMap<Long, ClientBuffer> connected = new HashMap<>();
	private final HashMap<String, AtomicLong> simultaneousConnectionMap = new HashMap<>();
	private int maximumSimultaneousConnections = 10;
	private int maximumHeaderParameterCount = 100;
	private int maximumHeaderParameterSize = 1024 * 1024;
	private int maximumCookieCount = 100;
	
	public HTTPdLib(int port, RequestListener requestListener) throws IOException
	{
		this(port, 1, requestListener);
	}
	
	public HTTPdLib(int port, int threadPool, RequestListener requestListener) throws IOException
	{
		requestBuilder = new RequestBuilder(this);
		
		server = new SocketServer(port, threadPool,
		
		//setup the request filter
		client ->
		{
			AtomicLong simultaneousConnections = simultaneousConnectionMap.get(client.getRemoteAddress());
			
			//only allow X simultaneous connections
			if(simultaneousConnections != null)
				return simultaneousConnections.incrementAndGet() <= maximumSimultaneousConnections;
			else
			{
				//no other simultaneous connections
				simultaneousConnectionMap.put(client.getRemoteAddress(), new AtomicLong(1));
				return true;
			}
		},
		
		//process the client IO
		client ->
		{
			ClientBuffer buffer = getBuffer(client);
			
			switch(client.getState())
			{
				//signal we want to start reading into the buffer
				case 0:
					//signal that we want to start reading and to fill up the buffer
					client.setInputRead(true);
					
					//advance to stage 1
					client.setState(1);
					break;
				
				//wait until the stream has signalled the buffer has reached the end
				case 1:
					//when the buffer is full advance to stage 2
					if(!client.isInputRead())
						client.setState(2);
					break;
					
				//read the buffer and look for EOF, if we haven't reached it yet, go back to state 0
				case 2:
					//skip empty buffer
					if(client.getInputBuffer().size() == 0)
						break;
					
					//get the bytes written
					byte[] bytes = client.getInputBuffer().toByteArray();
					
					//reset the input buffer
					client.getInputBuffer().reset();
					
					try
					{
						buffer.writeHeader(bytes);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					if(buffer.hasReachedEOL)
						client.setState(3);
					
					break;
					
				case 3:
					//TODO
					// at some point the headers will have to be processed and we will have to verify it its a post request
					// if it's a post request, we will need to download X bytes and then process them
					// this same piece of code is how we handle multi-form uploads and single form uploads
					// keep-alive is also handled in that same chunk of code
					
					if(buffer.request == null)
						buffer.request = requestBuilder.build(client, buffer);
					
					if(buffer.request != null)
					{
						if(buffer.request.getMethod() == Request.RequestType.POST)
						{
							client.setState(10);
							break;
						}
						
						client.setOutputBufferCache(requestBuilder.getEncoder().generateResponse(buffer.request, requestListener.request(buffer)));
					}
					
					client.setState(100);
					break;
					
				//read post request
				case 10:
					//get the bytes written
					bytes = client.getInputBuffer().toByteArray();
					
					//reset the input buffer
					client.getInputBuffer().reset();
					
					//TODO
					// before multi parts, the body data should be saved for post requests
					// look for multi parts, if they exist, parse them, log, and save the bytes to file
					// if it was previously in a multi part parse, it should continue writing to that last file
					
					try
					{
						buffer.writeBody(bytes);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					if(!client.isInputRead())
					{
						if(buffer.request != null)
							client.setOutputBufferCache(requestBuilder.getEncoder().generateResponse(buffer.request,
									requestListener.request(buffer)));
						
						client.setState(100);
					}
					break;
					
				//wait for the data to be sent
				case 100:
					if(client.getOutputBuffer().size() == 0 &&
							System.currentTimeMillis() - client.getLastNetworkActivityWrite() >= 100)
					{
						try
						{
							client.getSocket().close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					break;
			}
		},
		
		//on client disconnect remove the cached data
		client ->
		{
			connected.remove(client.getUID());
			
			AtomicLong simultaneousConnections = simultaneousConnectionMap.get(client.getRemoteAddress());
			if(simultaneousConnections != null && simultaneousConnections.decrementAndGet() <= 0)
				simultaneousConnectionMap.remove(client.getRemoteAddress());
		});
	}
	
	public ClientBuffer getBuffer(SocketClient client)
	{
		if(!connected.containsKey(client.getUID()))
			connected.put(client.getUID(), new ClientBuffer());
		
		return connected.get(client.getUID());
	}
	
	public void start()
	{
		server.start();
	}
	
	public void stop()
	{
		server.stopSocketServer();
	}
	
	public SocketServer getServer()
	{
		return server;
	}
	
	public int getMaximumSimultaneousConnections()
	{
		return maximumSimultaneousConnections;
	}
	
	public void setMaximumSimultaneousConnections(int maximumSimultaneousConnections)
	{
		this.maximumSimultaneousConnections = maximumSimultaneousConnections;
	}
	
	public int getMaximumHeaderParameterCount()
	{
		return maximumHeaderParameterCount;
	}
	
	public HTTPdLib setMaximumHeaderParameterCount(int maximumHeaderParameterCount)
	{
		this.maximumHeaderParameterCount = maximumHeaderParameterCount;
		return this;
	}
	
	public int getMaximumHeaderParameterSize()
	{
		return maximumHeaderParameterSize;
	}
	
	public void setMaximumHeaderParameterSize(int maximumHeaderParameterSize)
	{
		this.maximumHeaderParameterSize = maximumHeaderParameterSize;
	}
	
	public int getMaximumCookieCount()
	{
		return maximumCookieCount;
	}
	
	public void setMaximumCookieCount(int maximumCookieCount)
	{
		this.maximumCookieCount = maximumCookieCount;
	}
}
