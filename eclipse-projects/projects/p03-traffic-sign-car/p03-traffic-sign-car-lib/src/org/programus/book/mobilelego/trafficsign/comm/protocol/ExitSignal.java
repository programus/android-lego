package org.programus.book.mobilelego.trafficsign.comm.protocol;

/**
 * 退出信号消息
 * @author programus
 */
public class ExitSignal implements NetMessage {
	private static final long serialVersionUID = 668276549465813664L;
	
	private static final ExitSignal instance = new ExitSignal();
	
	private ExitSignal() {
	}
	
	public static ExitSignal getInstance() {
		return instance;
	}
}
