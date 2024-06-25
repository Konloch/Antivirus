package com.konloch.tav.utils;

import com.konloch.disklib.DiskReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class HashUtils
{
	public static String getMD5Hash(String input)
	{
		return getMD5Hash(input.getBytes(StandardCharsets.UTF_8));
	}
	
	public static String getMD5Hash(byte[] input)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			md.update(input);
			
			return bytesToHex(md.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException("MD5 algorithm not found", e);
		}
	}
	
	public static boolean doesFileMatchBytes(File file, byte[] bytes) throws IOException
	{
		//TODO this is kind of silly but the binaries are tiny so it's not really an issue to have it loaded into memory twice at the same time
		// with this in mind - be careful if you're going to use this function outside of where it's already applied
		
		byte[] contents = DiskReader.readBytes(file);
		
		if(contents.length != bytes.length)
			return false;
		
		for(int i = 0; i < contents.length; i++)
		{
			if(bytes[i] != contents[i])
				return false;
		}
		
		return true;
	}
	
	public static String bytesToHex(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
