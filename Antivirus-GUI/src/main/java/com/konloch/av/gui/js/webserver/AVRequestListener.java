package com.konloch.av.gui.js.webserver;

import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;
import com.konloch.av.gui.js.webserver.http.request.RequestListener;
import com.konloch.av.gui.settings.AVSettingsEntry;

import javax.swing.event.ChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVRequestListener implements RequestListener
{
	@Override
	public byte[] request(ClientBuffer clientBuffer)
	{
		Request request = clientBuffer.request;
		switch (request.getPath())
		{
			case "/api/settings/status":
			{
				Map<String, Boolean> settings = new LinkedHashMap<>();
				for(AVSettingsEntry entry : AVGUI.GUI.avSettings.settings.values())
					settings.put(entry.name, entry.value);
				
				String jsonString = AVWebserver.gson.toJson(settings);
				return jsonString.getBytes(StandardCharsets.UTF_8);
			}
			
			case "/api/settings/change":
			{
				String settingsChangeJson = new String(clientBuffer.bodyBuffer.toByteArray(), StandardCharsets.UTF_8);
				Map<String, Boolean> settingsMap = AVWebserver.gson.fromJson(settingsChangeJson, Map.class);
				
				if(settingsMap == null)
					return "".getBytes(StandardCharsets.UTF_8);
				
				for (Map.Entry<String, Boolean> entry : settingsMap.entrySet())
				{
					String settingName = entry.getKey();
					boolean settingValue = entry.getValue();
					
					AVSettingsEntry settingsEntry = AVGUI.GUI.avSettings.settings.get(settingName);
					settingsEntry.value = settingValue;
					settingsEntry.onChange.stateChanged(new ChangeEvent(settingValue));
					
					System.out.println("Setting: " + settingName + " = " + settingValue);
				}
				
				return "".getBytes(StandardCharsets.UTF_8);
			}
			
			case "/api/scan/quick":
				return "".getBytes(StandardCharsets.UTF_8);
				
			case "/api/scan/full":
				return "".getBytes(StandardCharsets.UTF_8);
				
			case "/api/scan/specific":
				return "".getBytes(StandardCharsets.UTF_8);
			
			case "/api/scan/status":
				return ("{\n" + "  \"progress\": 50,\n" + "  \"scannedFiles\": [\n" + "    {\n" + "      \"name\": \"file1.txt\",\n" + "      \"status\": \"Quarantined\"\n" + "    },\n" + "    {\n" + "      \"name\": \"file2.exe\",\n" + "      \"status\": \"Clean\"\n" + "    }\n" + "  ],\n" + "  \"currentFile\": \"file3.docx\",\n" + "  \"duration\": 120,\n" + "  \"remaining\": 30\n" + "}").getBytes(StandardCharsets.UTF_8);
			
			default:
			{
				String path = request.getPath();
				String lowerCasePath = path.toLowerCase();
				
				//block all requests that do not start with the random path identifier
				//   + .css and .js are whitelisted to allow the web-app to work
				if (!(lowerCasePath.endsWith(".css") || lowerCasePath.endsWith(".js")) && !path.startsWith(AVWebserver.RANDOM_PATH_IDENTIFIER))
				{
					request.setReturnCode(404);
					return "Error 404 file not found".getBytes(StandardCharsets.UTF_8);
				}
				
				//remove the random path
				if (path.startsWith(AVWebserver.RANDOM_PATH_IDENTIFIER))
					path = path.substring(AVWebserver.RANDOM_PATH_IDENTIFIER.length());
				
				//guess content type
				request.setContentType(guessType(path));
				
				//normalize the path to minimize traversal attacks
				Path basePath = Paths.get("/res/js");
				Path fullPath = basePath.resolve(path);
				String normalizedPath = fullPath.normalize().toString().replace("\\", "/");
				
				//load path as local resource
				InputStream inputStream = AVRequestListener.class.getResourceAsStream("/res/js" + normalizedPath);
				if (inputStream != null)
				{
					try
					{
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = inputStream.read(buffer)) != -1)
							bos.write(buffer, 0, bytesRead);
						
						return bos.toByteArray();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				
				request.setReturnCode(404);
				return "".getBytes(StandardCharsets.UTF_8);
			}
		}
	}
	
	public static String guessType(String requestPath)
	{
		if (requestPath.endsWith(".html") || requestPath.endsWith(".htm"))
			return "text/html";
		else if (requestPath.endsWith(".css"))
			return "text/css";
		else if (requestPath.endsWith(".js"))
			return "application/javascript";
		else if (requestPath.endsWith(".png"))
			return "image/png";
		else if (requestPath.endsWith(".ico"))
			return "image/x-icon";
		else if (requestPath.endsWith(".jpg") || requestPath.endsWith(".jpeg"))
			return "image/jpeg";
		else if (requestPath.endsWith(".gif"))
			return "image/gif";
		else
			// Default to plain text if the file type is unknown
			return "text/plain";
	}
}
