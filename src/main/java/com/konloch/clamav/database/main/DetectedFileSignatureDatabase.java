package com.konloch.clamav.database.main;

import com.konloch.tav.database.malware.MalwareScanFile;
import com.konloch.tav.database.malware.MalwareSignatureDatabase;

/**
 * .hdb: Stores MD5 or SHA1 hashes of entire malicious files
 *
 * NOTE: This database loads from two files, .hdb and .mdb
 * .mdb: Similar to .hdb, it stores MD5 hashes of malicious files.
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class DetectedFileSignatureDatabase implements MalwareSignatureDatabase
{
	@Override
	public void load()
	{
		//TODO
	}
	
	@Override
	public boolean isDetected(MalwareScanFile file)
	{
		//TODO
		return false;
	}
}
