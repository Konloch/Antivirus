package com.konloch.av.tasks;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadState;
import com.konloch.av.downloader.Downloader;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class UpdateTask implements Runnable
{
	
	@Override
	public void run()
	{
		while(Antivirus.AV.flags.running)
		{
			for(Downloader downloader : AVConstants.DOWNLOADERS)
			{
				try
				{
					DownloadState state = downloader.getState();
					
					if(state != DownloadState.NONE)
						downloader.download(state);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					Antivirus.AV.flags.updating = false;
					Antivirus.AV.flags.updateFinished = true;
				}
			}
			
			//print the db stats
			Antivirus.AV.sqLiteDB.printDatabaseStatistics();
			
			//wait for an hour
			try
			{
				Thread.sleep(1000 * 60 * 60);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
