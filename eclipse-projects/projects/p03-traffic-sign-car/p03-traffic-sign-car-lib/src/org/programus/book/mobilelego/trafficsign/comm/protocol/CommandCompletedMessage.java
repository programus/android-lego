package org.programus.book.mobilelego.trafficsign.comm.protocol;

/**
 * 小车完成命令后的回报消息
 * @author programus
 *
 */
public class CommandCompletedMessage implements NetMessage {
	private static final long serialVersionUID = 2653792304106497134L;
	
	private static CommandCompletedMessage inst = new CommandCompletedMessage();
	
	private CommandCompletedMessage() {	}
	
	public static CommandCompletedMessage getInstance() {
		return inst;
	}
}
