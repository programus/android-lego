package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class ExitSignal implements NetMessage {
	private static final long serialVersionUID = 668276549465813664L;
	
	private static final ExitSignal instance = new ExitSignal();
	
	private ExitSignal() {
	}
	
	public static ExitSignal getInstance() {
		return instance;
	}
}