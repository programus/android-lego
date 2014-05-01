package org.programus.book.mobilelego.research.communication.protocol;

/**
 * 控制马达运转的命令
 * @author programus
 *
 */
public class MotorMoveCommand implements NetMessage {
	/**
	 * 命令种类枚举
	 * @author programus
	 *
	 */
	public enum Command {
		/** 前进 */
		Forward,
		/** 后退 */
		Backword,
		/** 切断动力、惯性滑行 */
		Float,
		/** 停止 */
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
