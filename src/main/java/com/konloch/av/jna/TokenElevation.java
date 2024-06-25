package com.konloch.av.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Konloch
 * @since 6/25/2024
 */
public class TokenElevation extends Structure
{
	public int TokenIsElevated;
	
	@Override
	protected List<String> getFieldOrder()
	{
		return Collections.singletonList("TokenIsElevated");
	}
	
	public static class ByReference extends TokenElevation implements Structure.ByReference {}
	
	public static class ByValue extends TokenElevation implements Structure.ByValue {}
}
