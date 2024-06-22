package com.konloch.tav.utils;

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
	
	public static String bytesToHex(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
