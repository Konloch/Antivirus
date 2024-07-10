package com.konloch.av.tasks;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.downloader.DownloadFrequency;
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
		boolean fullyDownloaded = false;
		boolean announcedFullyDownloaded = false;
		
		while(Antivirus.AV.flags.running)
		{
			try
			{
				boolean hasHadToDownload = false;
				if(AVConstants.AUTOMATIC_DATABASE_IMPORTING)
				{
					for (Downloader downloader : AVConstants.DOWNLOADERS)
					{
						try
						{
							DownloadFrequency state = downloader.getState();
							
							if (state != DownloadFrequency.NONE)
							{
								announcedFullyDownloaded = false; //reset the announcement on a download prompt
								hasHadToDownload = true;
								Antivirus.AV.softwareStatus.status = "Downloading " + downloader.getName();
								downloader.download(state);
							}
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
					
					if(!fullyDownloaded)
						fullyDownloaded = !hasHadToDownload;
					
					if(fullyDownloaded && !announcedFullyDownloaded)
					{
						announcedFullyDownloaded = true;
						
						//print the db stats
						Antivirus.AV.sqLiteDB.countDatabase();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				//wait a second
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
