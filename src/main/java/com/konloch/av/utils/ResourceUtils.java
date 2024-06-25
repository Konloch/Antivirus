package com.konloch.av.utils;

import java.io.*;

/**
 * @author Konloch
 * @since 6/24/2024
 */
public class ResourceUtils
{
	public static byte[] readBytesFromFile(String filePath) throws IOException
	{
		InputStream is = ResourceUtils.class.getResourceAsStream(filePath);
		
		if (is == null)
			throw new FileNotFoundException("File not found: " + filePath);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024]; // Adjust buffer size as needed
		
		while ((nRead = is.read(data, 0, data.length)) != -1)
			buffer.write(data, 0, nRead);
		
		buffer.flush();
		byte[] bytesArray = buffer.toByteArray();
		
		is.close();
		
		return bytesArray;
	}
}
