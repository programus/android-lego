package org.programus.image.utils;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.programus.image.utils.ui.MainWindow;

public class HistogramMain {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private static void createAndShowGUI() {
		JFrame window = new MainWindow();
		window.setVisible(true);
	}

}
