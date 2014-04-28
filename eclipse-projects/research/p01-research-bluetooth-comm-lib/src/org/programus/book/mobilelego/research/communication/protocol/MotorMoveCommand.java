package org.programus.book.mobilelego.research.communication.protocol;

public class MotorMoveCommand implements NetMessage {
	public enum Command {
		Forward,
		Backword,
		Float,
		Stop,
	}
	private static final long serialVersionUID = -7523347542695340161L;
	
	private Command command;
	private float speed;

	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	@Override
	public String toString() {
		return "MotorMoveCommand [command=" + command + ", speed=" + speed
				+ "]";
	}
}
