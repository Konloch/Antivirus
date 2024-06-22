package com.konloch.clamav.database;

import com.konloch.clamav.database.main.DetectedFileSignatureDatabase;
import com.konloch.tav.database.malware.MalwareScanFile;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class ClamAVDB
{
	public final DetectedFileSignatureDatabase detectedFileSignatureDatabase = new DetectedFileSignatureDatabase();
	//TODO add the rest of the databases
	
	public void loadAllDatabases()
	{
		detectedFileSignatureDatabase.load();
	}
	
	public boolean isDetected(MalwareScanFile file)
	{
		return detectedFileSignatureDatabase.isDetected(file);
	}
}
