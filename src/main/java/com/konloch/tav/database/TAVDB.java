package com.konloch.tav.database;

import com.konloch.dynvarmap.DynVarField;
import com.konloch.dynvarmap.DynVarMap;
import com.konloch.dynvarmap.serializer.DynVarSerializer;
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
	
	public DynVarLong getMainDatabaseAge()
	{
		return dynVarMap.getVarLong("database.main.age", 0L);
	}
	
	public DynVarLong getDailyDatabaseAge()
	{
		return dynVarMap.getVarLong("database.daily.age", 0L);
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