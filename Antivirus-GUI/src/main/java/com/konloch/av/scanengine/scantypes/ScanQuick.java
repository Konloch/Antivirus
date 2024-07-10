package com.konloch.av.scanengine.scantypes;

import com.konloch.av.jna.AdminCheck;
import com.konloch.av.scanengine.ScanEngine;
import com.konloch.process.EasyProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.prefs.Preferences;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class ScanQuick extends ScanSpecific
{
	public final HashSet<ActiveProcess> processes = new HashSet<>(); //TODO move to api
	
	@Override
	public void preformScan(ScanEngine engine)
	{
		System.out.println("Preforming quick computer file scan...");
		
		latestUpdate = "Indexing files to be scanned...";
		
		//scan quick
		ArrayList<File> scanFiles = new ArrayList<>();
		processes.clear();
		
		//scan all active processes
		//TODO move to API
		try
		{
			EasyProcess process = EasyProcess.from(new ProcessBuilder("wmic", "process", "get", "ExecutablePath,ProcessID"));
			process.waitFor();
			
			HashSet<ActiveProcess> processes = new HashSet<>();
			for(String out : process.out)
			{
				if(out.trim().isEmpty())
					continue;
				
				String[] parts = out.split("\\s+");
				String path = parts[0];
				String pid = parts[1];
				
				processes.add(new ActiveProcess(path, pid));
			}
			
			for(ActiveProcess activeProcess: processes)
			{
				File processFile = new File(activeProcess.path);
				File parent = processFile.getParentFile();
				
				if(processFile.exists())
					scanFiles.add(processFile);
				else if(parent != null && parent.exists())
					scanFiles.add(parent);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//check windows vista+ start menu startup
		// TODO move to API
		for(File root : File.listRoots())
		{
			try
			{
				File[] users = new File(root, "Users").listFiles();
				
				if (users != null)
				{
					for (File userDir : users)
					{
						try
						{
							File startupDir = new File(userDir, "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup");
							
							if (startupDir.exists())
								scanFiles.add(startupDir);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		//read registry for startup
		// TODO move to API
		try
		{
			if(AdminCheck.isCurrentUserAdmin())
			{
				Preferences root = Preferences.systemRoot();
				Preferences runKey = root.node("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run");
				String[] values = runKey.keys();
				
				for (String value : values)
				{
					String val = runKey.get(value, "");
					boolean enabled = val.length() > 0;
					System.out.println(value + ": " + (enabled ? "Enabled" : "Disabled"));
				}
			}
			else
			{
				EasyProcess process = EasyProcess.from(new ProcessBuilder("reg", "query", "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run\\"));
				
				process.waitFor();
				
				for(String line : process.out)
				{
					//TODO reg_sz is probably not the only type compatible, multi-string & binary should be checked
					if(line.trim().isEmpty() || !line.contains("REG_SZ"))
						continue;
					
					String path = extractPath(line);
					String fullPath = extractFullPath(line); //TODO hash & compare in file signature db
					File file = new File(path);
					File parent = file.getParentFile();
					
					if(file.exists())
						scanFiles.add(file);
					else if(parent != null && parent.exists())
						scanFiles.add(parent);
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		
		//scan full computer
		scan(scanFiles);
		
		//TODO on detection, the activeProcess list should be polled & process should be terminated on a loop until the file can be deleted
		// this part may have to wait for the quarantine until realtime quarantine is possible
		
		//signal engine finish
		engine.finished();
	}
	
	public static String extractPath(String entry)
	{
		StringBuilder path = new StringBuilder();
		int index = entry.indexOf("REG_SZ") + 7;
		String entryPath = entry.substring(index).trim();
		boolean wholePath = entryPath.startsWith("\"");
		boolean inQuotes = false;
		
		for (char c : entryPath.toCharArray())
		{
			if (c == '"')
				inQuotes = !inQuotes;
			else if (wholePath && c == ' ' && !inQuotes)
				break;
			else
				path.append(c);
		}
		
		return decodeUrlEncodedCharacters(path.toString());
	}
	
	public static String extractFullPath(String entry)
	{
		StringBuilder path = new StringBuilder();
		int index = entry.indexOf("REG_SZ") + 7;
		String entryPath = entry.substring(index).trim();
		
		for (char c : entryPath.toCharArray())
			path.append(c);
		
		return decodeUrlEncodedCharacters(path.toString());
	}
	
	public static String decodeUrlEncodedCharacters(String path)
	{
		try
		{
			path = java.net.URLDecoder.decode(path, "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return path;
	}
}
