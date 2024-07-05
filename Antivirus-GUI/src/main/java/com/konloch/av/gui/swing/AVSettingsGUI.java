package com.konloch.av.gui.swing;

import com.konloch.AVConstants;
import com.konloch.Antivirus;
import com.konloch.av.gui.tray.AVTray;
import com.konloch.av.utils.WindowsUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/3/2024
 */
public class AVSettingsGUI extends JFrame
{
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();
	private final DefaultTableModel model = new DefaultTableModel(new String[]{"Setting", "Active"}, 0)
	{
		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			Class<?> clazz = String.class;
			switch (columnIndex)
			{
				case 1:
					clazz = Boolean.class;
					break;
			}
			return clazz;
		}
		
		@Override
		public boolean isCellEditable(int row, int column)
		{
			return column == 1;
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column)
		{
			super.setValueAt(aValue, row, column);
			
			ChangeListener listener = listenerMap.get((String) getValueAt(row, 0));
			listener.stateChanged(new ChangeEvent(aValue));
		}
	};
	
	public AVSettingsGUI() throws IOException, SQLException
	{
		setTitle(AVConstants.TITLE + " " + AVConstants.VERSION);
		setIconImage(ImageIO.read(Objects.requireNonNull(AVTray.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JTable table = new JTable(model);
		
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		addSettingEntry("Automatic Database Updating", "antivirus.automatic.database.updates", (value) -> System.out.println("Automatic database updating is now toggled: " + value.getSource()));
		
		if(WindowsUtil.IS_WINDOWS)
		{
			addSettingEntry("VM Mimic", "antivirus.vm.mimic", (value) ->
			{
				boolean active = (boolean) value.getSource();
				
				if(active)
					Antivirus.AV.mimicVM.enable();
				else
					Antivirus.AV.mimicVM.disable();
			});
			
			addSettingEntry("Real-time File Protection", "antivirus.realtime.file.protection", (value) ->
			{
				boolean active = (boolean) value.getSource();
			});
			
			addSettingEntry("Real-time File Scanning", "antivirus.realtime.file.scanning", (value) ->
			{
				boolean active = (boolean) value.getSource();
			});
			
			addSettingEntry("Real-time Process Scanning", "antivirus.realtime.process.scanning", (value) ->
			{
				boolean active = (boolean) value.getSource();
			});
		}
		
		pack();
		setLocationRelativeTo(null);
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
		
		listenerMap.put(settingName, actualListener);
		model.addRow(new Object[]{settingName, initialValue});
	}
}