package com.konloch.av.gui.js.webserver.api;

import java.io.File;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class ScannedFile
{
	public String name;
	public String path;
	public String status;
	public transient File file;
	
	public ScannedFile(String name, String path, String status, File file)
	{
		this.name = name;
		this.path = path;
		this.status = status;
		this.file = file;
	}
}
