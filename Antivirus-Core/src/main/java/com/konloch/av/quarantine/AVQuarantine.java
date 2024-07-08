package com.konloch.av.quarantine;

import com.konloch.Antivirus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konloch
 * @since 7/6/2024
 */
public class AVQuarantine
{
	public List<FileQuarantine> quarantineList = new ArrayList<>();
	
	public void init()
	{
		quarantineList.addAll(Antivirus.AV.sqLiteDB.loadQuarantinedFiles());
	}
	
	public FileQuarantine quarantineFile(File file)
	{
		return quarantineFile(file, "");
	}
	
	public FileQuarantine quarantineFile(File file, String reason)
	{
		if(!file.exists())
		{
			//TODO some kind of error should be thrown / logged, something got to the file first
			return null;
		}
		
		//AVConstants.QUARANTINE_WONT_REMOVE_UNTIL_APPROVED
		
		//TODO move file & rename
		FileQuarantine quarantine = new FileQuarantine(quarantineList.size(), file.getAbsolutePath(), file.getName(), reason, file.getAbsolutePath());
	
		Antivirus.AV.sqLiteDB.upsertFileQuarantine(quarantine);
		
		quarantineList.add(quarantine);
		
		return quarantine;
	}
	
	public boolean markFalsePositive(int id)
	{
		//TODO move file back to where it came from
		
		Antivirus.AV.sqLiteDB.removeFromQuarantine(id);
		quarantineList.removeIf(fQ -> fQ == null || fQ.id == id);
		
		//TODO
		return false;
	}
	
	public boolean removeFile(int id)
	{
		//TODO delete from the quarantine folder
		
		Antivirus.AV.sqLiteDB.removeFromQuarantine(id);
		quarantineList.removeIf(fQ -> fQ == null || fQ.id == id);
		
		return false;
	}
	
	public void removeAll()
	{
		Antivirus.AV.quarantine.quarantineList.removeIf(fileQuarantine ->
		{
			if(fileQuarantine == null)
				return true;
			
			Antivirus.AV.sqLiteDB.removeFromQuarantine(fileQuarantine.id);
			
			return true;
		});
	}
	
}
