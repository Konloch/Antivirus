package com.konloch.av.utils;

import javax.swing.*;

/**
 * @author Konloch
 * @since 7/5/2024
 */
public class SwingUtils
{
	public static void setSystemLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
