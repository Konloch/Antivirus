package com.konloch.av.mimicvm;

import com.konloch.Antivirus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konloch
 * @since 6/20/2024
 */
public class MimicVM
{
	private final List<Process> processPool = new ArrayList<>();
	
	public MimicVM()
	{
		//add shutdown hook to disable all active processes
		Runtime.getRuntime().addShutdownHook(new Thread(this::disable));
	}
	
	//extract BlankProcess.exe to the various files, then run them
	public void enable()
	{
		if(!processPool.isEmpty())
			return;
		
		try
		{
			String[] processList;
			try (InputStream inputStream = MimicVM.class.getResourceAsStream("/res/text/FakeProcesses.txt"))
			{
				int bytesRead;
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				while ((bytesRead = inputStream.read(buffer)) != -1)
					outputStream.write(buffer, 0, bytesRead);
				
				processList = outputStream.toString("UTF-8").split("\\r?\\n");
			}
			
			for (String fakeProcessName : processList)
			{
				if (fakeProcessName.isEmpty() || fakeProcessName.startsWith("#"))
					continue;
				
				File fakeProcessFolder = new File(Antivirus.AV.workingDirectory, "mimic");
				File fakeProcessFile = new File(fakeProcessFolder, fakeProcessName);
				
				if(!fakeProcessFolder.exists())
					fakeProcessFolder.mkdirs();
				
				//creates a fake process by copying BlankProcess.exe
				if (!fakeProcessFile.exists())
					createFakeProcess(fakeProcessFile);
				
				//run BlankProcess.exe under the new name
				Process process;
				try
				{
					process = new ProcessBuilder(fakeProcessFile.getAbsolutePath()).start();
					processPool.add(process);
				}
				catch (IOException e)
				{
					System.err.println("Error running process: " + e.getMessage());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//kill each process
	public void disable()
	{
		for (Process process : processPool)
			process.destroy();
		
		processPool.clear();
	}
	
	private void createFakeProcess(File fakeProcessFile) throws IOException
	{
		try (InputStream inputStream = MimicVM.class.getResourceAsStream("/res/binary/BlankProcess.exe");
		     OutputStream outputStream = new FileOutputStream(fakeProcessFile))
		{
			int bytesRead;
			byte[] buffer = new byte[1024];
			while ((bytesRead = inputStream.read(buffer))!= -1)
				outputStream.write(buffer, 0, bytesRead);
		}
	}
}
