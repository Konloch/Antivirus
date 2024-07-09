package com.konloch.av.gui.js.webserver.endpoints.scan;

import com.konloch.av.gui.AVGUI;
import com.konloch.av.gui.js.webserver.AVEndPoint;
import com.konloch.av.gui.js.webserver.AVWebserver;
import com.konloch.av.gui.js.webserver.api.ScanProgress;
import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;
import com.konloch.av.gui.js.webserver.http.request.Request;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class ScanStatusEndPoint extends AVEndPoint
{
	@Override
	public byte[] process(ClientBuffer clientBuffer, Request request)
	{
		if(AVGUI.GUI != null && AVGUI.GUI.scanEngine != null && AVGUI.GUI.scanEngine.getLatestScan() != null)
		{
			String duration = "Scan Duration: " + AVGUI.GUI.scanEngine.getLatestScan().getDuration();
			String estimation;
			
			if(AVGUI.GUI.scanEngine.getLatestScan().estimateTotalTimeLeft() == 0)
			{
				if (AVGUI.GUI.scanEngine.getLatestScan().scanFinished)
					estimation = "Scan Finished";
				else
					estimation = "Calculating Remainder...";
			}
			else
				estimation = "Remaining: " + AVGUI.GUI.scanEngine.getLatestScan().getEstimation();
			
			ScanProgress progress = new ScanProgress(AVGUI.GUI.scanEngine.getLatestScan().progress,
					AVGUI.GUI.scanEngine.getLatestScan().detectedFiles,
					AVGUI.GUI.scanEngine.getLatestScan().latestUpdate,
					duration,
					estimation);
			return AVWebserver.gson.toJson(progress)
					.getBytes(StandardCharsets.UTF_8);
		}
		else
		{
			//TODO detect state and alert based on that
			ScanProgress progress = new ScanProgress(0,
					new ArrayList<>(),
					"Waiting for scanner engine to start...",
					"",
					"");
			String jsonString = AVWebserver.gson.toJson(progress);
			return jsonString.getBytes(StandardCharsets.UTF_8);
		}
	}
}
