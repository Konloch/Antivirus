package com.konloch.av.scanning.signature;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.scanning.MalwareScanner;

import java.util.List;

/**
 * .hdb: Stores MD5 or SHA1 hashes of entire malicious files
 *
 * NOTE: This database loads from two files, .hdb and .mdb
 * .mdb: Similar to .hdb, it stores MD5 hashes of malicious files.
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class StaticLengthSignatureScanner implements MalwareScanner
{
	@Override
	public String detectAsMalware(MalwareScanFile file)
	{
		if(!AVConstants.ENABLE_SIGNATURE_SCANNING)
			return null;
		
		List<FileSignature> fileSignatures = Antivirus.AV.sqLiteDB.getByFileSize(file.getSize());
		
		if(fileSignatures != null)
		{
			for(FileSignature fileSignature : fileSignatures)
			{
				String hash = fileSignature.doesDetectAsMalwareType(file);
				
				if(hash != null)
					return hash;
			}
		}
		
		return null;
	}
}
