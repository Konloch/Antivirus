package com.konloch.av.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class ProcessUtils
{
	public static int getProcessIdByName(String processName)
	{
		WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
		
		try
		{
			Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();
			if (!Kernel32.INSTANCE.Process32First(snapshot, processEntry))
				return -1;
			
			do
			{
				String currentProcessName = Native.toString(processEntry.szExeFile);
				if (currentProcessName.equalsIgnoreCase(processName))
					return processEntry.th32ProcessID.intValue();
			} while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));
			
			return -1;
		}
		finally
		{
			Kernel32.INSTANCE.CloseHandle(snapshot);
		}
	}
}