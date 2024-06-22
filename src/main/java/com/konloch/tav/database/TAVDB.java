package com.konloch.tav.database;

import com.konloch.dynvarmap.DynVarMap;
import com.konloch.dynvarmap.serializer.DynVarSerializer;
import com.konloch.dynvarmap.vars.DynVarInteger;
import com.konloch.dynvarmap.vars.DynVarLong;

import java.io.File;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class TAVDB
{
	private final File workingDirectory = getWorkingDirectory();
	private final File dbFile = new File(workingDirectory, "TAV.db");
	private final DynVarMap dynVarMap = new DynVarMap();
	private final DynVarSerializer serializer = new DynVarSerializer(dbFile, dynVarMap);
	
	public File getWorkingDirectory()
	{
		if(workingDirectory == null)
		{
			File workingDirectory = new File(System.getProperty("user.home") + File.separator + "TAV.konloch");
			
			if(!workingDirectory.exists())
				workingDirectory.mkdirs();
			
			return workingDirectory;
		}
		
		return workingDirectory;
	}
	
	public DynVarLong getCAVMainDatabaseAge()
	{
		return dynVarMap.getVarLong("clamav.database.main.age", 0L);
	}
	
	public DynVarLong getCAVDailyDatabaseAge()
	{
		return dynVarMap.getVarLong("clamav.database.daily.age", 0L);
	}
	
	public DynVarLong getVSDatabaseAge()
	{
		return dynVarMap.getVarLong("virusshare.database.age", 0L);
	}
	
	public DynVarInteger getVSLastFullDownload()
	{
		return dynVarMap.getVarInt("virusshare.database.last.full.download", 0);
	}
	
	public void load()
	{
		if(dbFile.exists())
			serializer.load();
	}
	
	public void save()
	{
		serializer.save();
	}
}