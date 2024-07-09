package com.konloch.av.gui.js.webserver;

import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.api.ScanProgress;
import com.konloch.av.gui.js.webserver.endpoints.QuarantineEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.scan.ScanFullEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.scan.ScanQuickEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.scan.ScanStatusEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.scan.ScanStopEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.settings.SettingsChangeEndPoint;
import com.konloch.av.gui.js.webserver.endpoints.settings.SettingsStatusEndPoint;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;
import com.konloch.av.gui.js.webserver.http.request.RequestListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author Konloch
 * @since 7/4/2024
 */
public class AVRequestListener implements RequestListener
{
	private final HashMap<String, AVEndPoint> endPoints = new HashMap<>();
	
	{
		endPoints.put("/api/settings/status", new SettingsStatusEndPoint());
		endPoints.put("/api/settings/change", new SettingsChangeEndPoint());
		endPoints.put("/api/quarantine", new QuarantineEndPoint());
		endPoints.put("/api/scan/quick", new ScanQuickEndPoint());
		endPoints.put("/api/scan/full", new ScanFullEndPoint());
		endPoints.put("/api/scan/stop", new ScanStopEndPoint());
		endPoints.put("/api/scan/status", new ScanStatusEndPoint());
	}
	
	@Override
	public byte[] request(ClientBuffer clientBuffer)
	{
		Request request = clientBuffer.request;
		
		AVEndPoint endPoint = endPoints.get(request.getPath());
		
		if(endPoint != null)
			return endPoint.returnPage(clientBuffer);
		
		String path = request.getPath();
		String lowerCasePath = path.toLowerCase();
		
		//block all requests that do not start with the random path identifier
		//   + .css and .js are whitelisted to allow the web-app to work
		if (!(lowerCasePath.endsWith(".css") || lowerCasePath.endsWith(".js"))
				&& !path.startsWith(AVWebserver.RANDOM_PATH_IDENTIFIER))
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
