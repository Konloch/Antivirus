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
			//create MessageDigest instance for MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			//add input string bytes to digest
			md.update(input);
			
			//get the hash's bytes
			byte[] hashBytes = md.digest();
			
			//convert hash bytes to hex format
			StringBuilder sb = new StringBuilder();
			for (byte b : hashBytes)
			{
				sb.append(String.format("%02x", b));
			}
			
			return sb.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException("MD5 algorithm not found", e);
		}
	}
}
