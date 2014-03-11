package org.programus.robot.signgenerator.ui;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.programus.robot.signgenerator.model.SignData;
import org.programus.robot.signgenerator.utils.UI;
import org.programus.robot.signgenerator.vis.SignLoader;
import org.programus.robot.signgenerator.vis.SignPainter;

/**
 * 程序主窗口类。
 * @author programus
 *
 */
public class MainWindow extends JFrame {
	/** 用来做对象持久化识别的常数。由于本类不打算做持久化，可以忽略。 */
	private static final long serialVersionUID = -7815623551771426558L;

	private SignCanvasPanel mainPanel;
	private SignData data;
	private SignPainter painter;
	private UI ui = UI.getInstance();
	
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
		this.mainPanel = new SignCanvasPanel();
		this.data = this.mainPanel.getData();
		this.painter = new SignPainter(this.mainPanel.getPainter());
		this.getContentPane().add(this.mainPanel);
		this.setTitle(ui.getText("app.name"));
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
		menuBar.add(this.getEditMenu());
	}
	
	private JMenu getFileMenu() {
		JMenu menu = new JMenu(ui.getText("menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		
		JMenu submenu = new JMenu(ui.getText("menu.import"));
		menu.setMnemonic(KeyEvent.VK_I);
		menu.add(submenu);
		
		JMenuItem item = new JMenuItem(ui.getText("menu.image"));
		submenu.add(item);
		item.setMnemonic(KeyEvent.VK_M);
		item.addActionListener(new ActionListener() {
			private String prevFile;
			private String prevDir;
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(MainWindow.this, ui.getText("title.saveImage"), FileDialog.LOAD);
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
                            JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", ui.getText("message.readError"), ui.getText("message.formatError")), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", ui.getText("message.readError"), ex.getMessage()), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
					if (im != null) {
						SignLoader loader = new SignLoader();
						if (loader.loadImage(im, data)) {
							mainPanel.repaint();
						} else {
							JOptionPane.showMessageDialog(MainWindow.this, ui.getText("message.loadError"), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		
		submenu = new JMenu(ui.getText("menu.export"));
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(submenu);

		item = new JMenuItem(ui.getText("menu.image"));
		submenu.add(item);
		item.setMnemonic(KeyEvent.VK_M);
		item.addActionListener(new ActionListener() {
			private String prevFile;
			private String prevDir;
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(MainWindow.this, ui.getText("title.saveImage"), FileDialog.SAVE);
				fd.setFilenameFilter(fileFilter);
				fd.setDirectory(prevDir);
				fd.setFile(prevFile == null ? "*.gif" : prevFile);
				fd.setVisible(true);

				String fname = fd.getFile();
				if (fname != null) {
					int size = 0;
					while (size <= 0) {
                        String v = JOptionPane.showInputDialog(MainWindow.this, ui.getText("message.size"), 1024);
                        try {
							size = Integer.parseInt(v);
						} catch (NumberFormatException ex) {
						}
					}
					painter.setCanvasSize(new Dimension(size, size));
					BufferedImage image = painter.getImage();
					String filename = String.format("%s%s%s", fd.getDirectory(), File.separator, fname);
					String format = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
					if ("png/gif".indexOf(format) < 0) {
						filename += ".gif";
						format = "gif";
					}
					File file = new File(filename);
					try {
						if (!ImageIO.write(image, format, file)) {
                            JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s: %s", ui.getText("message.saveError"), ui.getText("message.formatError"), format), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", ui.getText("message.saveError"), ex.getMessage()), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
		});
		
		item = new JMenuItem(ui.getText("menu.code"));
		submenu.add(item);
		item.setMnemonic(KeyEvent.VK_D);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CodeDialog dialog = new CodeDialog(data);
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		});
		
		menu.addSeparator();

		item = new JMenuItem(ui.getText("menu.print"));
		menu.add(item);
		item.setMnemonic(KeyEvent.VK_P);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK, true));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrinterJob pj = PrinterJob.getPrinterJob();
				pj.setJobName(ui.getText("print.jobName"));
				pj.setPrintable(painter);
				if (pj.printDialog()) {
					try {
						pj.print();
					} catch (PrinterException ex) {
						JOptionPane.showMessageDialog(MainWindow.this, String.format("%s%n%s", ui.getText("message.printError"), ex.getMessage()), ui.getText("title.error"), JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
		});
		
		menu.addSeparator();

		item = new JMenuItem(ui.getText("menu.exit"));
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
	
	private JMenu getEditMenu() {
		JMenu menu = new JMenu(ui.getText("menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem(ui.getText("menu.grid"));
		menu.add(cbMenuItem);
		cbMenuItem.setMnemonic(KeyEvent.VK_G);
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		cbMenuItem.setState(mainPanel.isShowGrid());
		cbMenuItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mainPanel.setShowGrid(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		JMenuItem menuItem = new JMenuItem(ui.getText("menu.clear"));
		menu.add(menuItem);
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK, true));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainPanel.clear();
			}
		});
		return menu;
	}
}
