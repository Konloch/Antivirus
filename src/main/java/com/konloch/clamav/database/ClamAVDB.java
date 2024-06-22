package com.konloch.clamav.database;

import com.konloch.clamav.database.signature.DetectedFileSignatureDatabase;
import com.konloch.tav.scanning.MalwareScanFile;

/**
 * @author Konloch
 * @since 6/21/2024
 */
public class ClamAVDB
{
	public final DetectedFileSignatureDatabase detectedFileSignatureDatabase = new DetectedFileSignatureDatabase();
	//TODO add the rest of the databases / implement their scanning methods
	
	public void loadAllDatabases()
	{
		detectedFileSignatureDatabase.load();
	}
	
	public String detectAsMalware(MalwareScanFile file)
	{
		return detectedFileSignatureDatabase.detectAsMalware(file);
	}
}
