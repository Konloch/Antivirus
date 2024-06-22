package com.konloch.tav.scanning;

import com.konloch.TraditionalAntivirus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
	
	public void insert()
	{
		TraditionalAntivirus.TAV.sqLiteDB.insertSignature(this);
	}
	
	public void update() throws SQLException
	{
		String updateSQL = "UPDATE signatures SET length = ?, identifier = ? WHERE hash = ?";
		try (PreparedStatement pstmt = TraditionalAntivirus.TAV.sqLiteDB.getConnection().prepareStatement(updateSQL))
		{
			pstmt.setLong(1, length);
			pstmt.setString(2, malwareType);
			pstmt.setString(3, hash);
			pstmt.executeUpdate();
		}
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
