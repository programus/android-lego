package org.programus.book.mobilelego.trafficsign.comm.protocol;

/**
 * 识别路标后传给小车的命令
 * @author programus
 *
 */
public class CarCommand implements NetMessage {
	private static final long serialVersionUID = 1435421615318148312L;

	/**
	 * 命令内容
	 */
	public enum Command {
		/** 前进 */
		Forward,
		/** 左转（90°） */
		TurnLeft,
		/** 右转（90°） */
		TurnRight,
		/** 掉头 */
		TurnBack,
		/** 停止 */
		Stop,
		/** 退出 */
		Exit,
		/** 关机 */
		Shutdown,
	}
	
	private Command cmd;
	
	public CarCommand(Command command) {
		this.setCommand(command);
	}
	
	public void setCommand(Command command) {
		this.cmd = command;
	}
	
	public Command getCommand() {
		return this.cmd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CarCommand [cmd=" + cmd + "]";
	}
}
