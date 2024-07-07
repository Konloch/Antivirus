package com.konloch.av.quarantine;

/**
 * @author Konloch
 * @since 7/6/2024
 */
public class FileQuarantine
{
	public int id;
	public final String path;
	public final String name;
	public final String reason;
	public final String currentPath;
	
	public FileQuarantine(int id, String path, String name, String reason, String currentPath)
	{
		this.id = id;
		this.path = path;
		this.name = name;
		this.reason = reason;
		this.currentPath = currentPath;
	}
}
