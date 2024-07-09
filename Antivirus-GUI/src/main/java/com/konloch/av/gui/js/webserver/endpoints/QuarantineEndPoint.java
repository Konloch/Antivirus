package com.konloch.av.gui.js.webserver.endpoints;

import com.konloch.Antivirus;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.nio.charset.StandardCharsets;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class QuarantineEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		String action = request.getPost().get("action");
		int id;
		
		if(action != null)
		{
			switch(action)
			{
				case "getFiles":
					return AVWebserver.gson.toJson(Antivirus.AV.quarantine.quarantineList)
							.getBytes(StandardCharsets.UTF_8);
				
				case "removeAllFiles":
					Antivirus.AV.quarantine.removeAll();
					break;
				
				case "removeFile":
					id = Integer.parseInt(request.getPost().get("id"));
					Antivirus.AV.quarantine.removeFile(id);
					break;
				
				case "reportFalsePositive":
					id = Integer.parseInt(request.getPost().get("id"));
					
					//TODO ask the user if they want to report it to github or just locally declare it a false positive
					
					Antivirus.AV.quarantine.markFalsePositive(id);
					break;
			}
		}
		
		return "".getBytes(StandardCharsets.UTF_8);
	}
}
