package org.programus.book.mobilelego.robopet.comm.protocol;

/**
 * 对宠物下达的命令
 * @author programus
 *
 */
public class PetCommand implements NetMessage {
	/**
	 * 宠物命令种类
	 */
	public enum Command {
		/** 前进 */
		Forward,
		/** 后退 */
		Backward,
		/** 左转 */
		TurnLeft,
		/** 右转 */
		TurnRight,
		/** 停止 */
		Stop,
		/** 安静 */
		Calm,
		/** 退出程序 */
		Exit,
		/** 自我关机 */
		Shutdown,
	}
	private static final long serialVersionUID = 7244514397681749979L;

	private Command command;
	private int value;
	
	public PetCommand() {
	}
	
	public PetCommand(Command command, int value) {
		this.command = command;
		this.value = value;
	}
	
	/**
	 * @return 命令种类
	 */
	public Command getCommand() {
		return command;
	}
	/**
	 * @param 命令种类
	 */
	public void setCommand(Command command) {
		this.command = command;
	}
	/**
	 * 参数值在前进、后退时为步数；在转向时为角度，向右转为正值；其它情况会忽略此值。
	 * @return 参数值
	 */
	public int getValue() {
		return value;
	}
	/**
	 * 参数值在前进、后退时为步数；在转向时为角度，向右转为正值；其它情况会忽略此值。
	 * @param 参数值
	 */
	public void setValue(int value) {
		this.value = value;
	}
}
