package com.konloch.av.gui.js.webserver.api;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class ScannedFile
{
	private String name;
	private String status;
	
	public ScannedFile(String name, String status) {
		this.name = name;
		this.status = status;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
