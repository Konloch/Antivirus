package com.konloch.av.database.sql;

import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.downloader.impl.yara.YaraDownloader;

import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konloch
 * @since 6/22/2024
 */
public class SQLiteDB
{
	private Connection connection;
	private final ArrayList<FileSignature> signatureAddQueue = new ArrayList<>();
	
	public void connect() throws SQLException
	{
		connection = DriverManager.getConnection("jdbc:sqlite:" + new File(Antivirus.AV.workingDirectory, "db.sqlite").getAbsolutePath());
	}
	
	public void optimizeDatabase()
	{
		try (Statement stmt = connection.createStatement())
		{
			stmt.execute("PRAGMA synchronous = OFF;");
			stmt.execute("PRAGMA journal_mode = MEMORY;");
			stmt.execute("PRAGMA cache_size = 100000;");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void resetDatabaseOptimization()
	{
		try (Statement stmt = connection.createStatement())
		{
			stmt.execute("PRAGMA synchronous = FULL;");
			stmt.execute("PRAGMA journal_mode = DELETE;");
			stmt.execute("PRAGMA cache_size = -2000;");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void createNewTable() throws SQLException
	{
		try (Statement statement = connection.createStatement())
		{
			statement.execute("CREATE TABLE IF NOT EXISTS signatures (\n"
					+ "    hash TEXT PRIMARY KEY,\n"
					+ "    length INTEGER NOT NULL,\n"
					+ "    identifier TEXT NOT NULL\n"
					+ ");");
		}
		
		try (Statement statement = connection.createStatement())
		{
			statement.execute("CREATE TABLE IF NOT EXISTS config_integer (\n"
					+ "    key TEXT PRIMARY KEY,\n"
					+ "    value INTEGER NOT NULL\n"
					+ ");");
		}
		
		try (Statement statement = connection.createStatement())
		{
			statement.execute("CREATE TABLE IF NOT EXISTS config_text (\n"
					+ "    key TEXT PRIMARY KEY,\n"
					+ "    value TEXT NOT NULL\n"
					+ ");");
		}
	}
	
	public void insertSignature(FileSignature fileSignature)
	{
		signatureAddQueue.add(fileSignature);
		
		if(signatureAddQueue.size() >= 100000)
			insertAllWaitingSignatures();
	}
	
	public void insertAllWaitingSignatures()
	{
		if(signatureAddQueue.isEmpty())
			return;
		
		insertFileSignatures(signatureAddQueue);
		signatureAddQueue.clear();
	}
	
	public void insertFileSignatures(List<FileSignature> fileSignatures)
	{
		String query = "INSERT OR REPLACE INTO signatures (hash, length, identifier) VALUES (?, ?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			connection.setAutoCommit(false);
			
			for (FileSignature fileSignature : fileSignatures)
			{
				pstmt.setString(1, fileSignature.hash);
				pstmt.setLong(2, fileSignature.length);
				pstmt.setString(3, fileSignature.malwareType);
				pstmt.addBatch();
			}
			
			pstmt.executeBatch();
			connection.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			
			try
			{
				//roll back transaction on error
				connection.rollback();
			}
			catch (SQLException rollbackEx)
			{
				rollbackEx.printStackTrace();
			}
		}
		finally
		{
			try
			{
				connection.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public List<FileSignature> getByFileSize(long fileSize)
	{
		List<FileSignature> fileSignatures = new ArrayList<>();
		
		String query = "SELECT hash, length, identifier FROM signatures WHERE length = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setLong(1, fileSize);
			try (ResultSet rs = pstmt.executeQuery())
			{
				while (rs.next())
				{
					String hash = rs.getString("hash");
					int length = rs.getInt("length");
					String identifier = rs.getString("identifier");
					FileSignature fileSignature = new FileSignature(hash, length, identifier);
					fileSignatures.add(fileSignature);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return fileSignatures;
	}
	
	public List<FileSignature> getByFileHash(String... fileHash)
	{
		List<FileSignature> fileSignatures = new ArrayList<>();
		
		StringBuilder query = new StringBuilder("SELECT hash, length, identifier FROM signatures WHERE  hash IN (");
		for (int i = 0; i < fileHash.length; i++) {
			query.append("?");
			if (i < fileHash.length - 1) {
				query.append(", ");
			}
		}
		query.append(")");
		
		try (PreparedStatement pstmt = connection.prepareStatement(query.toString()))
		{
			for (int i = 0; i < fileHash.length; i++)
				pstmt.setString(i + 1, fileHash[i]);
			
			try (ResultSet rs = pstmt.executeQuery())
			{
				while (rs.next())
				{
					String hash = rs.getString("hash");
					int length = rs.getInt("length");
					String identifier = rs.getString("identifier");
					FileSignature fileSignature = new FileSignature(hash, length, identifier);
					fileSignatures.add(fileSignature);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return fileSignatures;
	}
	
	public long countFileSignatures()
	{
		String query = "SELECT COUNT(*) AS totalCount FROM signatures";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			try (ResultSet rs = pstmt.executeQuery())
			{
				if (rs.next())
					return rs.getLong("totalCount");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public Integer getIntegerConfig(String key) throws SQLException
	{
		return getIntegerConfig(key, 0);
	}
	
	public Integer getIntegerConfig(String key, int defaultValue) throws SQLException
	{
		String query = "SELECT value FROM config_integer WHERE key = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			try (ResultSet rs = pstmt.executeQuery())
			{
				if (rs.next())
					return rs.getInt("value");
			}
		}
		
		upsertIntegerConfig(key, defaultValue);
		
		return defaultValue;
	}
	
	public Long getLongConfig(String key) throws SQLException
	{
		return getLongConfig(key, 0L);
	}
	
	public Long getLongConfig(String key, long defaultValue) throws SQLException
	{
		String query = "SELECT value FROM config_integer WHERE key = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			try (ResultSet rs = pstmt.executeQuery())
			{
				if (rs.next())
					return rs.getLong("value");
			}
		}
		
		upsertIntegerConfig(key, defaultValue);
		
		return defaultValue;
	}
	
	public String getStringConfig(String key) throws SQLException
	{
		return getStringConfig(key, "");
	}
	
	public String getStringConfig(String key, String defaultValue) throws SQLException
	{
		String query = "SELECT value FROM config_text WHERE key = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			try (ResultSet rs = pstmt.executeQuery())
			{
				if (rs.next())
					return rs.getString("value");
			}
		}
		
		upsertStringConfig(key, defaultValue);
		
		return defaultValue;
	}
	
	public Boolean getBooleanConfig(String key) throws SQLException
	{
		return getBooleanConfig(key, false);
	}
	
	public Boolean getBooleanConfig(String key, boolean defaultValue) throws SQLException
	{
		return Boolean.parseBoolean(getStringConfig(key, String.valueOf(defaultValue)));
	}
	
	public void upsertIntegerConfig(String key, int value) throws SQLException
	{
		String query = "INSERT OR REPLACE INTO config_integer (key, value) VALUES (?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			pstmt.setInt(2, value);
			pstmt.executeUpdate();
		}
	}
	
	public void upsertIntegerConfig(String key, long value) throws SQLException
	{
		String query = "INSERT OR REPLACE INTO config_integer (key, value) VALUES (?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			pstmt.setLong(2, value);
			pstmt.executeUpdate();
		}
	}
	
	public void upsertStringConfig(String key, String value) throws SQLException
	{
		String query = "INSERT OR REPLACE INTO config_text (key, value) VALUES (?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query))
		{
			pstmt.setString(1, key);
			pstmt.setString(2, value);
			pstmt.executeUpdate();
		}
	}
	
	public void upsertBooleanConfig(String key, boolean value) throws SQLException
	{
		upsertStringConfig(key, String.valueOf(value));
	}
	
	public void printDatabaseStatistics()
	{
		System.out.println("Counted " + NumberFormat.getInstance().format(Antivirus.AV.sqLiteDB.countFileSignatures()) + " malware signatures & " + NumberFormat.getInstance().format(YaraDownloader.yaraRules) + " yara rules.");
	}
	
	public void close()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Connection getConnection()
	{
		return connection;
	}
}
