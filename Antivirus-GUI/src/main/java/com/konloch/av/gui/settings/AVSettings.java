package com.konloch.av.gui.settings;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.downloader.impl.yara.YaraDownloader;
import com.konloch.av.utils.WindowsUtil;

import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class AVSettings
{
	public final Map<String, AVSettingsEntry> settings = new LinkedHashMap<>();
	
	public void init() throws SQLException
	{
		addSettingEntry("Automatic Database Updating", "antivirus.automatic.database.updates",
				(value) -> AVConstants.AUTOMATIC_DATABASE_IMPORTING = (boolean) value.getSource());
		
		addSettingEntry("Static File Scanning (Fast)", "antivirus.static.file.scanning",
				(value) ->
				{
					AVConstants.STATIC_SCANNING = (boolean) value.getSource();
					
					Antivirus.AV.sqLiteDB.countDatabase();
				});
		
		addSettingEntry("Dynamic File Scanning (Slow)", "antivirus.dynamic.file.scanning",
				(value) ->
				{
					AVConstants.DYNAMIC_SCANNING = (boolean) value.getSource();
					
					YaraDownloader.yaraRules = 0;
					
					if(AVConstants.DYNAMIC_SCANNING)
					{
						try
						{
							YaraDownloader.loadYaraFilesIntoSingleFile();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					
					Antivirus.AV.sqLiteDB.updateRuleCount();
				});
		
		if (WindowsUtil.IS_WINDOWS)
		{
			addSettingEntry("Real-time VM Mimic", "antivirus.vm.mimic", (value) ->
			{
				boolean active = (boolean) value.getSource();
				
				if (active)
					Antivirus.AV.mimicVM.enable();
				else
					Antivirus.AV.mimicVM.disable();
			});
			
			if(AVConstants.ENABLE_REALTIME_FILE_PROTECTION)
				addSettingEntry("Real-time File Protection", "antivirus.realtime.file.protection", (value) ->
				{
					boolean active = (boolean) value.getSource();
				});
			
			if(AVConstants.ENABLE_REALTIME_FILE_SCANNING)
				addSettingEntry("Real-time File Scanning", "antivirus.realtime.file.scanning", (value) ->
				{
					boolean active = (boolean) value.getSource();
				});
			
			if(AVConstants.ENABLE_REALTIME_PROCESS_SCANNING)
				addSettingEntry("Real-time Process Scanning", "antivirus.realtime.process.scanning", (value) ->
				{
					boolean active = (boolean) value.getSource();
				});
		}
	}
	
	private void addSettingEntry(String settingName, String configSetting, ChangeListener listener) throws SQLException
	{
		boolean initialValue = Antivirus.AV.sqLiteDB.getBooleanConfig(configSetting);
		
		ChangeListener actualListener = e ->
		{
			listener.stateChanged(e);
			
			try
			{
				Antivirus.AV.sqLiteDB.upsertBooleanConfig(configSetting, (boolean) e.getSource());
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
		};
		
		settings.put(settingName, new AVSettingsEntry(settingName, configSetting, initialValue, actualListener));
	}
}
