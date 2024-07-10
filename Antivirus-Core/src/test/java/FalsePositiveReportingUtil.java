import com.konloch.av.database.malware.MalwareScanFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Konloch
 * @since 7/10/2024
 */
public class FalsePositiveReportingUtil extends JFrame
{
	private JTextArea textArea;
	
	public FalsePositiveReportingUtil() throws IOException
	{
		setTitle("False Positive Reporting Util");
		setIconImage(ImageIO.read(Objects.requireNonNull(FalsePositiveReportingUtil.class.getResourceAsStream("/res/img/icon.png"))));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textArea = new JTextArea(20, 40);
		textArea.setText("Drag & Drop Files For Their Checksum");
		textArea.setEditable(false);
		
		getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		
		new DropTarget(textArea, new DropTargetAdapter()
		{
			@Override
			public void drop(DropTargetDropEvent dtde)
			{
				try
				{
					Transferable tr = dtde.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (DataFlavor flavor : flavors)
					{
						if (flavor.isFlavorJavaFileListType())
						{
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							java.util.List<File> files = (java.util.List<File>) tr.getTransferData(flavor);
							
							SwingUtilities.invokeLater(()-> textArea.setText(""));
							
							for (File file : files)
								processFile(file);
							
							dtde.dropComplete(true);
							return;
						}
					}
				}
				catch (IOException | UnsupportedFlavorException e)
				{
					e.printStackTrace();
				}
			}
		});
		
		pack();
		setVisible(true);
	}
	
	private void processFile(File file)
	{
		MalwareScanFile scanFile = new MalwareScanFile(file);
		
		SwingUtilities.invokeLater(()->
		{
			try
			{
				textArea.getDocument().insertString(textArea.getDocument().getLength(), "File Path: " + file.getAbsolutePath() + "\n", null);
				textArea.getDocument().insertString(textArea.getDocument().getLength(), "File MD5: " + scanFile.getMD5Hash() + "\n", null);
				textArea.getDocument().insertString(textArea.getDocument().getLength(), "File SHA-1: " + scanFile.getSHA1Hash() + "\n", null);
				textArea.getDocument().insertString(textArea.getDocument().getLength(), "File SHA-256: " + scanFile.getSHA256Hash() + "\n", null);
				textArea.getDocument().insertString(textArea.getDocument().getLength(), "File SHA-512: " + scanFile.getSHA512Hash() + "\n\n", null);
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public static void main(String[] args) throws IOException
	{
		new FalsePositiveReportingUtil();
	}
}
