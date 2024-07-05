package com.konloch.av.jna;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class NTProcessUtils
{
	public static WinNT.HANDLE getHandle(int processId)
	{
		return Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_ALL_ACCESS, false, processId);
	}
	
	public static boolean suspendProcess(int processId)
	{
		WinNT.HANDLE processHandle = getHandle(processId);
		if (processHandle == null)
			return false;
		
		int result = NtDll.INSTANCE.NtSuspendProcess(processHandle);
		
		Kernel32.INSTANCE.CloseHandle(processHandle);
		return result == 0;
	}
	
	public static boolean resumeProcess(int processId)
	{
		WinNT.HANDLE processHandle = getHandle(processId);
		if (processHandle == null)
			return false;
		
		int result = NtDll.INSTANCE.NtResumeProcess(processHandle);
		
		Kernel32.INSTANCE.CloseHandle(processHandle);
		return result == 0;
	}
}