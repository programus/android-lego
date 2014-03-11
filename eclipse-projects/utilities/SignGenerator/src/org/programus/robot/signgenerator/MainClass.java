package org.programus.robot.signgenerator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.programus.robot.signgenerator.ui.MainWindow;

/**
 * 路标生成器的入口类。
 * @author programus
 *
 */
public class MainClass {
	
	/**
	 * 程序入口主函数。程序启动时，首先执行此函数。
	 * @param args 从命令行传入的参数。
	 */
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
