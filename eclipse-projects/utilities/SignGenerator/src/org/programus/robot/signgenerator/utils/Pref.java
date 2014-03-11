package org.programus.robot.signgenerator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 从属性文件中取出数值的类。
 * @author programus
 *
 */
public class Pref {
	private static Pref inst = new Pref();
	private final static String PROP_NAME = "org/programus/robot/signgenerator/prop/Pref.properties";
	
	private final static String EDGE_LEN = "edge.len";
	private final static String DEFAULT_UNIT = "default.unit";
	
	private Properties prop; 
	
	private Pref() {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(PROP_NAME);
		this.prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {
			System.err.println("Error when init Pref from " + PROP_NAME);
			e.printStackTrace();
		}
	}
	
	public static Pref getInstance() {
		return inst;
	}
	
	public int getEdgeLen() {
		String value = this.prop.getProperty(EDGE_LEN, "20");
		return Integer.parseInt(value);
	}
	
	public int getDefaultUnit() {
		String value = this.prop.getProperty(DEFAULT_UNIT, "10");
		return Integer.parseInt(value);
	}
}
