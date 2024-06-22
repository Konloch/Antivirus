package com.konloch.clamav.database.signature;

import com.konloch.TraditionalAntivirus;
import com.konloch.tav.scanning.FileSignature;
import com.konloch.tav.scanning.MalwareScanFile;
import com.konloch.tav.database.malware.MalwareSignatureDatabase;
import com.konloch.util.FastStringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
	//TODO replace with SQLite
	private final HashMap<Long, ArrayList<FileSignature>> fileSizeLookup = new HashMap<>();
	
	@Override
	public void load()
	{
		//load main signatures
		loadMDB("main/main.mdb");
		loadHSB("main/main.hsb");
		
		//load daily signatures
		loadMDB("daily/daily.mdb");
		loadHSB("daily/daily.hsb");
		
		System.out.println("Loaded " + NumberFormat.getInstance().format(fileSizeLookup.size()) + " malware signatures into memory");
	}
	
	private void loadMDB(String file)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(TraditionalAntivirus.TAV.db.getWorkingDirectory(), file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] signatureInformation = FastStringUtils.split(line, ":", 3);
				
				if(signatureInformation.length == 3)
				{
					FileSignature fileSignature = new FileSignature(signatureInformation[1], Long.parseLong(signatureInformation[0]), signatureInformation[2]);
					
					if(!fileSizeLookup.containsKey(fileSignature.length))
						fileSizeLookup.put(fileSignature.length, new ArrayList<>());
					
					ArrayList<FileSignature> fileSignatures = fileSizeLookup.get(fileSignature.length);
					fileSignatures.add(fileSignature);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadHSB(String file)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(TraditionalAntivirus.TAV.db.getWorkingDirectory(), file))))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] signatureInformation = FastStringUtils.split(line, ":", 3);
				
				if(signatureInformation.length == 3)
				{
					String length = signatureInformation[1];
					
					if(length.equals("*")) //skip variable length exploits as we lack the ability to hash them correctly
						continue;
					
					FileSignature fileSignature = new FileSignature(signatureInformation[0], Long.parseLong(length), signatureInformation[2]);
					
					if(!fileSizeLookup.containsKey(fileSignature.length))
						fileSizeLookup.put(fileSignature.length, new ArrayList<>());
					
					ArrayList<FileSignature> fileSignatures = fileSizeLookup.get(fileSignature.length);
					fileSignatures.add(fileSignature);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String detectAsMalware(MalwareScanFile file)
	{
		ArrayList<FileSignature> fileSignatures = fileSizeLookup.get(file.getSize());
		
		if(fileSignatures != null)
		{
			for(FileSignature fileSignature : fileSignatures)
				return fileSignature.doesDetectAsMalwareType(file);
		}
		
		return null;
	}
}
