package com.konloch.av.quarantine;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.MalwareScanFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
		quarantineList.removeIf(fQ ->
		{
			boolean match = fQ == null || fQ.id == id;
			
			if(match && fQ != null)
			{
				MalwareScanFile file = new MalwareScanFile(new File(fQ.path));
				Antivirus.AV.sqLiteDB.insertWhitelistFileSignature(file.getSHA512Hash());
			}
			return match;
		});
		
		return false;
	}
	
	//TODO this function should be on a background thread so we can continue the deletion attempts over and over until we get a successful delete
	private void remove(FileQuarantine fileQuarantine) throws IOException
	{
		if(AVConstants.QUARANTINE_WONT_REMOVE_UNTIL_APPROVED)
		{
			File quarantinedFile = new File(fileQuarantine.path);
			
			if(quarantinedFile.exists())
			{
				//TODO could look for all active processes, if the path is the same, kill it before we try to delete
				Files.delete(quarantinedFile.toPath());
			}
			else //log the win I guess, probably another AV caught it before we could react
				System.out.println("Quarantined File: " + quarantinedFile.getAbsolutePath() + " no longer exists");
		}
		else
		{
			//TODO delete from the quarantine folder
		}
	}
	
	public boolean removeFile(int id)
	{
		Antivirus.AV.sqLiteDB.removeFromQuarantine(id);
		quarantineList.removeIf(fQ ->
		{
			boolean match = fQ == null || fQ.id == id;
			
			if(match && fQ != null)
			{
				try
				{
					remove(fQ);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return false;
				}
			}
			
			return match;
		});
		
		return false;
	}
	
	public void removeAll()
	{
		Antivirus.AV.quarantine.quarantineList.removeIf(fileQuarantine ->
		{
			if(fileQuarantine == null)
				return true;
			
			try
			{
				remove(fileQuarantine);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			
			Antivirus.AV.sqLiteDB.removeFromQuarantine(fileQuarantine.id);
			
			return true;
		});
	}
	
}
