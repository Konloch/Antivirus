package com.konloch.av.gui.settings;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class AVSettingsEntry
{
	public final String name;
	public final String configSetting;
	public boolean value;
	public final ChangeListener onChange;
	
	public AVSettingsEntry(String name, String configSetting, boolean value, ChangeListener onChange)
	{
		this.name = name;
		this.configSetting = configSetting;
		this.value = value;
		this.onChange = onChange;
	}
}
