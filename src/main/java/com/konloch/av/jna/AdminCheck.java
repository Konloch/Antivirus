package com.konloch.av.jna;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class AdminCheck
{
	public static boolean isCurrentUserAdmin()
	{
		try
		{
			WinNT.HANDLEByReference tokenHandle = new WinNT.HANDLEByReference();
			if (!Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), WinNT.TOKEN_QUERY, tokenHandle))
				throw new RuntimeException("OpenProcessToken failed. Error: " + Kernel32.INSTANCE.GetLastError());
			
			TokenElevation elevation = new TokenElevation();
			IntByReference size = new IntByReference(elevation.size());
			if (!Advapi32.INSTANCE.GetTokenInformation(tokenHandle.getValue(), WinNT.TOKEN_INFORMATION_CLASS.TokenElevation, elevation, elevation.size(), size))
				throw new RuntimeException("GetTokenInformation failed. Error: " + Kernel32.INSTANCE.GetLastError());
			
			return elevation.TokenIsElevated != 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
