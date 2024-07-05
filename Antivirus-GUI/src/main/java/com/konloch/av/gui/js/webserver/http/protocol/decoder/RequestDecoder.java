package com.konloch.av.gui.js.webserver.http.protocol.decoder;

import com.konloch.av.gui.js.webserver.http.HTTPdLib;

import java.util.HashMap;

/**
 * @author Konloch
 * @since 3/1/2023
 */
public class RequestDecoder
{
	private final HTTPdLib server;
	
	public RequestDecoder(HTTPdLib server)
	{
		this.server = server;
	}
	
	/**
	 * Decode the headers from a raw web request, this will respect the security limits set on the webserver
	 *
	 * @param buffer any byte array as the buffer containing the request data
	 * @return a HashMap String, String key-value pair containing the headers
	 */
	public HashMap<String, String> decodeHeaders(byte[] buffer)
	{
		HashMap<String, String> parameters = new HashMap<>();
		
		StringBuilder key = new StringBuilder(" :");
		StringBuilder value = new StringBuilder();
		boolean keyFlag = false;
		int parameterCreationCount = 0;
		for(byte b : buffer)
		{
			char c = (char) b;
			
			//verify ascii only
			//TODO may want to just stop the request entirely and throw a 500
			if(!isAscii(c))
				continue;
			
			//looking for key
			if(keyFlag)
			{
				if(c == ':')
					keyFlag = false;
				else
					key.append(c);
			}
			
			//end of value
			else if(c == '\n' || c == '\r')
			{
				if(parameterCreationCount++ >= server.getMaximumHeaderParameterCount())
					return parameters;
				
				if(key.length() > 0 && value.length() > 0)
					parameters.put(key.substring(1), value.toString());
				
				key = new StringBuilder();
				value = new StringBuilder();
				keyFlag = true;
			}
			
			//looking for value
			else if(value.length() < server.getMaximumHeaderParameterSize())
				value.append(c);
		}
		
		return parameters;
	}
	
	/**
	 * Decode request parameters, this will respect the security limits set on the webserver
	 *
	 * @param rawParameters any String representing the raw parameters to be parsed
	 * @return a HashMap String, String key-value pair containing the parsed parameters
	 */
	public HashMap<String, String> decodeParameters(String rawParameters)
	{
		if (rawParameters == null || rawParameters.isEmpty())
			return new HashMap<>();
		
		HashMap<String, String> parameters = new HashMap<>();
		
		int start = 0;
		int end = 0;
		
		while (end < rawParameters.length())
		{
			//find the position of the next '&' character
			end = rawParameters.indexOf('&', start);
			if (end == -1)
			{
				//if no '&' is found, set 'end' to the end of the string
				end = rawParameters.length();
			}
			
			//extract the current parameter substring
			String paramPair = rawParameters.substring(start, end);
			
			//split the parameter into key and value
			int equalsIndex = paramPair.indexOf('=');
			if (equalsIndex != -1)
			{
				String key = paramPair.substring(0, equalsIndex).trim();
				String value = paramPair.substring(equalsIndex + 1).trim();
				
				
				//verify ascii only
				//TODO may want to just stop the request entirely and throw a 500
				if(isAscii(key) && isAscii(value))
				{
					//TODO add additional checks here, like maximum size.
					parameters.put(key, value);
				}
			}
			
			//move 'start' to the character after the current '&'
			start = end + 1;
		}
		
		return parameters;
	}
	
	/**
	 * Decode sent cookies, this will respect the security limits set on the webserver
	 *
	 * @param rawCookies any String to represent the cookie header
	 * @return a HashMap String, String key-value pair containing the cookies
	 */
	public HashMap<String, String> decodeCookies(String rawCookies)
	{
		HashMap<String, String> cookies = new HashMap<>();
		
		if(rawCookies == null || rawCookies.isEmpty())
			return cookies;
		
		char[] chars = rawCookies.toCharArray();
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean keyFlag = true;
		for(char c : chars)
		{
			//looking for the key
			if(keyFlag)
			{
				//skip all spaces in the key
				if(c == ' ')
					continue;
				
				//no longer a key when the '=' character is found
				if(c == '=')
					keyFlag = false;
				else
					key.append(c);
			}
			
			//looking for value
			else
			{
				//end of value search
				if(c == ';')
				{
					if(key.length() > 0 && value.length() > 0)
						cookies.put(key.toString(), value.toString());
						
					key = new StringBuilder();
					value = new StringBuilder();
					keyFlag = true;
					
					if(cookies.size() >= server.getMaximumCookieCount())
						break;
				}
				else
					value.append(c);
			}
		}
		
		//add the last key / value in the buffer as long as it's valid
		if(!keyFlag && cookies.size() < server.getMaximumCookieCount())
		{
			if(key.length() > 0 && value.length() > 0)
				cookies.put(key.toString(), value.toString());
		}
		
		return cookies;
	}
	
	
	/**
	 * return if all characters are ascii
	 *
	 * @param s any string
	 * @return true if the character is ascii
	 */
	public static boolean isAscii(String s)
	{
		char[] arr = s.toCharArray();
		for(char c : arr)
		{
			if(!isAscii(c))
				return false;
		}
		
		return true;
	}
	
	/**
	 * A very fast O(1) lookup table to return if a character is ascii
	 *
	 * @param c any character
	 * @return true if the character is ascii
	 */
	public static boolean isAscii(char c)
	{
		switch(c)
		{
			//symbols
			case ' ':
			case '!':
			case '@':
			case '#':
			case '$':
			case '%':
			case '^':
			case '&':
			case '*':
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '+':
			case '=':
			case '-':
			case '_':
			case '`':
			case '~':
			case ';':
			case ':':
			case '"':
			case '\'':
			case '?':
			case '<':
			case '>':
			case ',':
			case '.':
			case '\\':
			case '/':
			case '|':
			case '\r':
			case '\n':
			
			//numbers
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			
			//alphabet lowercase
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
			
			//alphabet uppercase
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
				return true;
		}
		
		return false;
	}
}
