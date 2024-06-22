package com.konloch;

import com.konloch.clamav.downloader.ClamAVDownloader;
import com.konloch.tav.database.TAVDB;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class TraditionalAntivirus
{
	public static TraditionalAntivirus TAV;
	public final TAVDB db = new TAVDB();
	public final ClamAVDownloader downloaderCDB = new ClamAVDownloader();
	
	public void startup()
	{
		try
		{
			//load the db
			db.load();
			
			//run initial update
			if (db.getMainDatabaseAge().get() == 0)
			{
				System.out.println("Preforming initial database update...");
				downloaderCDB.downloadFullUpdate();
				db.getMainDatabaseAge().set(System.currentTimeMillis());
				db.getDailyDatabaseAge().set(System.currentTimeMillis());
				db.save();
			}
			
			//every week hours preform the daily update
			if(System.currentTimeMillis() - db.getDailyDatabaseAge().get() >= 1000 * 60 * 60 * 24 * 7)
			{
				//TODO make it every 8 hours
				// + in order to do this we need to support diffpatches and finish the libfreshclam implementation
			
				System.out.println("Preforming daily update...");
				downloaderCDB.downloadDailyUpdate();
				db.getDailyDatabaseAge().set(System.currentTimeMillis());
				db.save();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		TAV = new TraditionalAntivirus();
		TAV.startup();
	}
}
