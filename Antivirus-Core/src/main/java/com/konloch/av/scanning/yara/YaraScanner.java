package com.konloch.av.scanning.yara;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.downloader.impl.yara.YaraDownloader;
import com.konloch.av.scanning.MalwareScanner;
import com.konloch.av.utils.WindowsUtil;
import com.konloch.util.FastStringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class YaraScanner implements MalwareScanner
{
	public static final HashSet<String> rulesWithErrors = new HashSet<>();
	
	@Override
	public String detectAsMalware(MalwareScanFile file)
	{
		if(!AVConstants.ENABLE_YARA_SCANNING || !AVConstants.DYNAMIC_SCANNING)
			return null;
		
		String architecture = System.getProperty("os.arch");
		String arch;
		
		if(!WindowsUtil.IS_WINDOWS)
			throw new RuntimeException("This is currently windows only - YaraX might be a solution, open a ticket and let us know you need it.");
		
		if (architecture.equals("x86") || architecture.equals("i386") || architecture.equals("i686"))
			arch = "32";
		else if (architecture.equals("amd64") || architecture.equals("x86_64"))
			arch = "64";
		else
			throw new RuntimeException("Only 32bit & 64bit are supported, cannot support: " + architecture);
		
		//TODO ideally we would compile the yara files and reuse them each scan
		File yaraLocalFile = new File(Antivirus.AV.workingDirectory, "yara" + arch + ".exe");
		File yaraRuleFile = new File(Antivirus.AV.workingDirectory, "yara-rules.yar");
		File yaracRuleFile = new File(Antivirus.AV.workingDirectory, "yara-rules.yarc");
		
		if(!yaraLocalFile.exists())
			throw new RuntimeException("File not found: " + yaraLocalFile.getAbsolutePath());
		
		try
		{
			//setup commands
			List<String> command = new ArrayList<>();
			command.add(yaraLocalFile.getAbsolutePath());
			
			//TODO some kind of verification would be smart so they can't just RCE the compiled yara file
			// an easy solution is once the yara file is compiled, read it for the hash, then before this runs, read to verify the checksum
			if(AVConstants.ENABLE_YARA_COMPILING && yaracRuleFile.exists())
			{
				command.add("-c");
				command.add(yaracRuleFile.getAbsolutePath());
			}
			else
				command.add(yaraRuleFile.getAbsolutePath());
			
			command.add(file.getFile().getAbsolutePath());
			
			//create process builder
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(Antivirus.AV.workingDirectory);
			Process process = pb.start();
			
			//wait for the process to complete
			int exitCode = process.waitFor();
			//System.out.println("\t+ yara.exe exited with code: " + exitCode);
			
			//read the results
			ArrayList<String> results = readInputStream(process.getInputStream());
			if(!results.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				for (String s : results)
					sb.append(s).append("\n");
				
				return sb.toString();
			}
			
			//read the errors
			ArrayList<String> err = readInputStream(process.getErrorStream());
			for(String errorMessage : err)
			{
				if(errorMessage.startsWith("error:"))
				{
					//System.out.println(errorMessage);
					
					if(errorMessage.contains("in "))
					{
						String rule = FastStringUtils.split(errorMessage, "\"")[1].trim();
						String reason = FastStringUtils.split(errorMessage, "):")[1].trim();
						
						System.out.println("Skipping rule: '" + rule + "', reason: '" + reason + "'");
						
						//add rule to known "skip" rules list
						rulesWithErrors.add(rule);
						
						//rebuild the yara mega-rules
						YaraDownloader.loadYaraFilesIntoSingleFile();
						
						//retry...
						return detectAsMalware(file);
					}
				}
				
				//ignore warnings
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static ArrayList<String> readInputStream(InputStream inputStream) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		ArrayList<String> lines = new ArrayList<>();
		
		String line;
		while ((line = reader.readLine()) != null)
			lines.add(line);
		
		reader.close();
		
		return lines;
	}
}
