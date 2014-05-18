package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

/**
 * 控制机器人移动的命令
 * @author programus
 *
 */
public class RobotMoveCommand implements NetMessage {
	/**
	 * 命令种类枚举
	 * @author programus
	 *
	 */
	public enum Command {
		/** 前进 */
		Forward,
		/** 后退 */
		Backward,
		/** 切断动力、惯性滑行 */
		Float,
		/** 停止，禁止转向 */
		Stop,
	}
	private static final long serialVersionUID = -7523347542695340161L;
	
	private Command command;
	/** 机器人行进速度 */
	private short speed;
	/** 机器人转向角度 */
	private short rotation;

	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public short getSpeed() {
		return speed;
	}
	public void setSpeed(short speed) {
		this.speed = speed;
	}
	public short getRotation() {
		return rotation;
	}
	public void setRotation(short rotation) {
		this.rotation = rotation;
	}
	@Override
	public String toString() {
		return "RobotMoveCommand [command=" + command + ", speed=" + speed
				+ ", rotation=" + rotation + "]";
	}
}
