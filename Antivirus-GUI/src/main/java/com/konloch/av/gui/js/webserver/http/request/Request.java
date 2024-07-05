package com.konloch.av.gui.js.webserver.http.request;

import com.konloch.av.gui.js.webserver.http.request.cookies.Cookie;

import java.io.File;
import java.util.HashMap;

/**
 * A request represents the incoming webserver request, and the out-going webserver response
 *
 * @author Konloch
 * @since 3/1/2023
 */
public class Request
{
	private final String remoteIP;
	private final RequestType method;
	private final String path;
	private final String version;
	private final HashMap<String, String> requestHeaders;
	private final HashMap<String, String> responseHeaders;
	private final HashMap<String, String> requestCookies;
	private final HashMap<String, Cookie> responseCookies;
	private final HashMap<String, String> get;
	private final HashMap<String, String> post;
	private int returnCode = 200;
	private String contentType = "text/plain; charset=utf-8";
	private File[] uploads;
	
	public Request(String remoteIP, RequestType method, String path, String version,
	               HashMap<String, String> requestHeaders, HashMap<String, String> cookies,
	               HashMap<String, String> get, HashMap<String, String> post)
	{
		this.remoteIP = remoteIP;
		this.path = path;
		this.method = method;
		this.version = version;
		this.requestHeaders = requestHeaders;
		this.get = get;
		this.post = post;
		this.responseHeaders = new HashMap<>();
		this.requestCookies = cookies;
		this.responseCookies = new HashMap<>();
	}
	
	public String getRemoteIP()
	{
		return remoteIP;
	}
	
	public String getInitialHeader()
	{
		return getRequestHeaders().get(":");
	}
	
	public String getHost()
	{
		return getRequestHeaders().get("Host");
	}
	
	public String getUserAgent()
	{
		return getRequestHeaders().get("User-Agent");
	}
	
	public RequestType getMethod()
	{
		return method;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public HashMap<String, String> getRequestHeaders()
	{
		return requestHeaders;
	}
	
	public HashMap<String, String> getResponseHeaders()
	{
		return responseHeaders;
	}
	
	public HashMap<String, String> getRequestCookies()
	{
		return requestCookies;
	}
	
	public HashMap<String, Cookie> getResponseCookies()
	{
		return responseCookies;
	}
	
	public int getReturnCode()
	{
		return returnCode;
	}
	
	public void setReturnCode(int returnCode)
	{
		this.returnCode = returnCode;
	}
	
	public String getContentType()
	{
		return contentType;
	}
	
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	public File[] getUploads()
	{
		return uploads;
	}
	
	public void setUploads(File... uploads)
	{
		this.uploads = uploads;
	}
	
	public HashMap<String, String> getGet()
	{
		return get;
	}
	
	public HashMap<String, String> getPost()
	{
		return post;
	}
	
	public enum RequestType
	{
		UNKNOWN,
		GET,
		POST;
		
		public static RequestType from(String method)
		{
			for(RequestType type : values())
			{
				if (type == UNKNOWN)
					continue;
				
				if (method.equals(type.name()))
					return type;
			}
			
			return UNKNOWN;
		}
	}
}