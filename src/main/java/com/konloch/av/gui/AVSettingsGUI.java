package com.konloch.av.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Konloch
 * @since 7/3/2024
 */
public class AVSettingsGUI extends JFrame
{
	private JTable table;
	private DefaultTableModel model;
	private final Map<String, ChangeListener> listenerMap = new HashMap<>();
	
	public AVSettingsGUI()
	{
		setLayout(new BorderLayout());
		
		model = new DefaultTableModel(new String[]{"Setting", "Enabled"}, 0)
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
		table = new JTable(model);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		addEntry("Setting 1", false, (value) -> System.out.println("Setting 1 clicked: " + value));
		addEntry("Setting 2", true, (value) -> System.out.println("Setting 2 clicked: " + value));
		addEntry("Setting 3", false, (value) -> System.out.println("Setting 3 clicked: " + value));
		
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	private void addEntry(String settingName, boolean initialValue, ChangeListener listener)
	{
		listenerMap.put(settingName, listener);
		model.addRow(new Object[]{settingName, initialValue});
	}
}
