package com.konloch.av.scanengine.scantypes;

import com.konloch.av.scanengine.Scan;
import com.konloch.av.scanengine.ScanEngine;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Konloch
 * @since 7/9/2024
 */
public class ScanFull extends ScanSpecific
{
	@Override
	public void preformScan(ScanEngine engine)
	{
		System.out.println("Preforming full computer file scan...");
		
		latestUpdate = "Indexing files to be scanned...";
		
		ArrayList<File> scanFiles = new ArrayList<>();
		for(File root : File.listRoots())
		{
			if(!root.exists())
				continue;
			
			scanFiles.add(root);
			
			walk(scanFiles, root);
		}
		
		latestUpdate = "Indexed " + scanFiles.size();
		System.out.println("Indexed " + scanFiles.size());
		
		//scan full computer
		scan(scanFiles);
		
		engine.finished();
	}
}
