package org.programus.book.mobilelego.research.communication.protocol;

import java.io.Serializable;

public class PhoneMessage implements Protocol, Serializable {
	private static final long serialVersionUID = 2028887184636945050L;
	private Type type;
	private int intValue;
	private float floatValue;
	private float[] arrayValue;
	private String stringValue;
	@Override
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getIntValue() {
		return intValue;
	}
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	public float getFloatValue() {
		return floatValue;
	}
	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}
	public float[] getArrayValue() {
		return arrayValue;
	}
	public void setArrayValue(float[] arrayValue) {
		this.arrayValue = arrayValue;
	}
	public String getStringValue() {
		return stringValue;
	}
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
}
