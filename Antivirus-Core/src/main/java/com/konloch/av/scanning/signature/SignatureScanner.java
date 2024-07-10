package com.konloch.av.scanning.signature;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.database.malware.FileSignature;
import com.konloch.av.database.malware.MalwareScanFile;
import com.konloch.av.scanning.MalwareScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * File signatures through sqlite lookup
 *
 * @author Konloch
 * @since 6/21/2024
 */
public class SignatureScanner implements MalwareScanner
{
	@Override
	public String detectAsMalware(MalwareScanFile file)
	{
		if(!AVConstants.ENABLE_SIGNATURE_SCANNING || !AVConstants.STATIC_SCANNING)
			return null;
		
		//NOTE: recursive scanning is handled through the API that calls on this, - it's job is to pass directories down to here
		//we don't need to go any further than just the first folder
		List<FileSignature> fileSignatures;
		if(file.getFile().isDirectory())
		{
			fileSignatures = new ArrayList<>();
			File[] files = file.getFile().listFiles();
			if(files != null)
			{
				for(File subFile : files)
				{
					MalwareScanFile subScanFile = new MalwareScanFile(subFile);
					fileSignatures.addAll(Antivirus.AV.sqLiteDB.getByFileHash(subScanFile.getMD5Hash(),
							subScanFile.getSHA1Hash(), subScanFile.getSHA256Hash(), subScanFile.getSHA512Hash()));
				}
			}
		}
		else
		{
			fileSignatures = Antivirus.AV.sqLiteDB.getByFileHash(file.getMD5Hash(),
				file.getSHA1Hash(), file.getSHA256Hash(), file.getSHA512Hash());
		}
		
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