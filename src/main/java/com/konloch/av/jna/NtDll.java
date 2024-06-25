package com.konloch.av.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public interface NtDll extends com.sun.jna.Library
{
	NtDll INSTANCE = Native.load("ntdll", NtDll.class, W32APIOptions.DEFAULT_OPTIONS);
	
	//undocumented - https://ntopcode.wordpress.com/2018/01/16/anatomy-of-the-thread-suspension-mechanism-in-windows-windows-internals/
	int NtSuspendProcess(WinNT.HANDLE ProcessHandle);
	int NtResumeProcess(WinNT.HANDLE ProcessHandle);
}