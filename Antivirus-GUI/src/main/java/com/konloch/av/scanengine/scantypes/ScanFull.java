package com.konloch.av.scanengine.scantypes;

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
		
		//scan full
		ArrayList<File> scanFiles = new ArrayList<>();
		
		for(File root : File.listRoots())
		{
			if(engine.getActiveScan() == null) //scan ended early
				return;
			
			if(!root.exists())
				continue;
			
			scanFiles.add(root);
			
			walk(engine, scanFiles, root);
		}
		
		//scan full computer
		scan(scanFiles);
		
		//signal engine finish
		engine.finished();
	}
}
