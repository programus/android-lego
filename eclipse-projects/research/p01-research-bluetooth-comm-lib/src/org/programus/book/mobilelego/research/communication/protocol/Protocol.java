package org.programus.book.mobilelego.research.communication.protocol;

public interface Protocol {
	public enum Type {
		Button, 
		Motor, 
		Sensor, 
		Text, 
		Exit;
	}

	public Type getType();
}
