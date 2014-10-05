package org.programus.image.utils.ui;

import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

/**
 * 程序主窗口类。
 * @author programus
 *
 */
public class MainWindow extends JFrame {
	/** 用来做对象持久化识别的常数。由于本类不打算做持久化，可以忽略。 */
	private static final long serialVersionUID = -7815623551771426558L;

	private HistogramPanel mainPanel;
	private ImagePanel imagePanel;
	private BufferedImage image;
	
	private FilenameFilter fileFilter = new FilenameFilter() {
		private final String[] ACCEPTABLE_TYPE = {
			".gif", ".png", ".jpg", ".bmp"
		};
		@Override
		public boolean accept(File dir, String name) {
			name = name.toLowerCase();
			for (String type : this.ACCEPTABLE_TYPE) {
				if (name.endsWith(type)) {
					return true;
				}
			}
			return false;
		}
	};
	
	public MainWindow() {
		super();
		this.initComponents();
		this.initBehavior();
		this.initMenu();
	}
	
	private void initComponents() {
		this.mainPanel = new HistogramPanel();
		this.imagePanel = new ImagePanel();
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Histogram", this.mainPanel);
		tab.addTab("Original Image", imagePanel);
		this.getContentPane().add(tab);
		this.setTitle("Histogram");
		this.pack();
	}
	
	private void initBehavior() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
	}
	
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		menuBar.add(this.getFileMenu());
	}
	
	private JMenu getFileMenu() {
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem item = new JMenuItem("Open");
		menu.add(item);
		item.setMnemonic(KeyEvent.VK_O);
		item.addActionListener(new ActionListener() {
			private String prevFile;
			private String prevDir;
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(MainWindow.this, "Open File", FileDialog.LOAD);
				fd.setFilenameFilter(fileFilter);
				fd.setDirectory(prevDir);
				fd.setFile(prevFile == null ? "*.gif" : prevFile);
				fd.setVisible(true);

				String fname = fd.getFile();
				if (fname != null) {
					String filename = String.format("%s%s%s", fd.getDirectory(), File.separator, fname);
					File file = new File(filename);
					BufferedImage im = null;
					try {
						im = ImageIO.read(file);
						if (im == null) {
                            JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", "File Read Error", "Format error."), "Error", JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", "File Read Error", ex.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
					if (im != null) {
						loadImage(im);
						mainPanel.setImage(image);
						mainPanel.repaint();
						imagePanel.setImage(im);
						imagePanel.repaint();
					}
				}
			}
		});
		
		menu.addSeparator();

		item = new JMenuItem("Exit");
		menu.add(item);
		item.setMnemonic(KeyEvent.VK_X);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		return menu;
	}

	protected void loadImage(BufferedImage im) {
		if (im.getType() != BufferedImage.TYPE_BYTE_GRAY) {
			this.image = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = this.image.createGraphics();
			g.drawImage(im, 0, 0, null);
			g.dispose();
		} else {
			this.image = im;
		}
	}
}