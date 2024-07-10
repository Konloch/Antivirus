package com.konloch.av.gui.js.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.konloch.av.gui.js.webserver.http.HTTPdLib;

import java.io.IOException;
import java.util.Random;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVWebserver
{
	public static HTTPdLib webserver;
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static boolean FAILED_TO_BIND = true;
	public static int PORT;
	public static final String STATIC_CONTENT_KEY = "/" + generateRandomPath();
	public static final String DYNAMIC_API_KEY = generateRandomPath();
	
	public static void bind()
	{
		for(PORT = 20050; PORT < 65535; PORT++)
		{
			try
			{
				webserver = new HTTPdLib(PORT, new AVRequestListener());
				webserver.start();
				
				FAILED_TO_BIND = false;
				
				System.out.println("AVWebserver bound on http://localhost:" + webserver.getServer().getPort() + STATIC_CONTENT_KEY + "?key=" + DYNAMIC_API_KEY);
				break;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static String generateRandomPath()
	{
		Random random = new Random();
		char[] charset = "abcdefhijklmpqrstuvwxyzABCDEFHIJKLMQRSTUVWXYZ0123456789".toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 64; i++)
			sb.append(charset[random.nextInt(charset.length)]);
		
		return sb.toString();
	}
}
