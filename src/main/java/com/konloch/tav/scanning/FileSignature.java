package com.konloch.tav.scanning;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class FileSignature
{
	public final String hash;
	public final long length;
	public final String malwareType;
	
	public FileSignature(String hash, long length, String malwareType)
	{
		this.hash = hash;
		this.length = length;
		this.malwareType = malwareType;
	}
	
	public String doesDetectAsMalwareType(MalwareScanFile file)
	{
		if(file.getSize() == length)
		{
			file.hash();
			
			if(file.getSHA1Hash().equals(hash) ||
					file.getSHA256Hash().equals(hash) ||
					file.getMD5Hash().equals(hash))
			{
				return malwareType;
			}
		}
		
		return null;
	}
}
