package com.konloch.av.scanning.signature;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.scanning.MalwareScanner;

import java.util.List;

/**
 * Handle variable length signatures through static file lookup
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class VariableLengthSignatureScanner implements MalwareScanner
{
	@Override
	public String detectAsMalware(MalwareScanFile file)
	{
		if(!AVConstants.ENABLE_SIGNATURE_SCANNING || !AVConstants.STATIC_SCANNING)
			return null;
		
		List<FileSignature> fileSignatures = Antivirus.AV.sqLiteDB.getByFileHash(file.getMD5Hash(),
				file.getSHA1Hash(), file.getSHA256Hash());
		
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
