package com.konloch.av.scanengine.scantypes;

import com.konloch.av.scanengine.Scan;
import com.konloch.av.scanengine.ScanEngine;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class ScanQuick extends Scan
{
	@Override
	public void preformScan(ScanEngine engine)
	{
		latestUpdate = "Quick Scanning is currently not available";
		
		//TODO scan quick
	}
}
