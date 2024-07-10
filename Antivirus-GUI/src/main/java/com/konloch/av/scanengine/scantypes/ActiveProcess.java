package com.konloch.av.scanengine.scantypes;

import java.util.Objects;

/**
 * @author Konloch
 * @since 7/10/2024
 */
public class ActiveProcess
{
	public final String path;
	public final String pid;
	
	public ActiveProcess(String path, String pid)
	{
		this.path = path;
		this.pid = pid;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		ActiveProcess that = (ActiveProcess) o;
		
		return Objects.equals(path, that.path);
	}
	
	@Override
	public int hashCode()
	{
		return path != null ? path.hashCode() : 0;
	}
}
