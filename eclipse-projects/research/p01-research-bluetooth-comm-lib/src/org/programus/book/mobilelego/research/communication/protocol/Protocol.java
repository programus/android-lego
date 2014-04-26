package org.programus.book.mobilelego.research.communication.protocol;

public interface Protocol {
	public enum Type {
		Button, 
		Motor, 
		Sensor, 
		Text, 
		Exit;
	}
	
	public enum MotorCommand {
		Forward,
		Backword,
		Float,
		Stop,
		Report
	}

	public Type getType();
}
