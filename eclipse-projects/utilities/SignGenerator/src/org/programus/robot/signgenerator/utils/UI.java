package org.programus.robot.signgenerator.utils;

import java.util.ResourceBundle;

/**
 * 从属性文件中取出数值的类。
 * @author programus
 *
 */
public class UI {
	private static UI inst = new UI();
	private final static String BUNDLE_NAME = "org.programus.robot.signgenerator.prop.ui";
	
	private ResourceBundle bundle;
	
	private UI() {
		this.bundle = ResourceBundle.getBundle(BUNDLE_NAME);

	}
	
	public static UI getInstance() {
		return inst;
	}
	
	public String getText(String key) {
		return this.bundle.getString(key);
	}
}
