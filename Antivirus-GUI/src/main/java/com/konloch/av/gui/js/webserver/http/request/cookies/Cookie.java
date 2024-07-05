package com.konloch.av.gui.js.webserver.http.request.cookies;

/**
 * @author Konloch
 * @since 3/8/2023
 */
public class Cookie
{
	private final String name;
	private final String value;
	private String expires; //the date this cookie is set to expire at
	private boolean httpOnly; //means javascript cannot access this cookie
	private boolean secure; //only accessible VIA https or localhost
	private String domain; //locks the cookie to specific domains, subdomains are included by default
	private String path; //only accessible if the request path starts with the specific path
	private String sameSite; //cross site forgery request protectio
	
	public Cookie(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String getExpires()
	{
		return expires;
	}
	
	public Cookie setExpires(String expires)
	{
		this.expires = expires;
		return this;
	}
	
	public boolean isHttpOnly()
	{
		return httpOnly;
	}
	
	public Cookie setHttpOnly(boolean httpOnly)
	{
		this.httpOnly = httpOnly;
		return this;
	}
	
	public boolean isSecure()
	{
		return secure;
	}
	
	public Cookie setSecure(boolean secure)
	{
		this.secure = secure;
		return this;
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public Cookie setDomain(String domain)
	{
		this.domain = domain;
		return this;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public Cookie setPath(String path)
	{
		this.path = path;
		return this;
	}
	
	public String getSameSite()
	{
		return sameSite;
	}
	
	public Cookie setSameSite(String sameSite)
	{
		this.sameSite = sameSite;
		return this;
	}
}
