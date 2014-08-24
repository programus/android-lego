package org.programus.book.mobilelego.robopet.server.util;

import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;

/**
 * 命令容器
 * @author programus
 *
 */
public class CommandManager {
	private static CommandManager inst = new CommandManager();
	
	private PetCommand cmdWaiting;
	private PetCommand cmdProcessing;
	
	private CommandManager() {
	}
	
	public static CommandManager getInstance() {
		return inst;
	}
	
	public synchronized void putCommand(PetCommand command) {
		this.cmdWaiting = command;
	}
	
	public PetCommand peekCommand() {
		return this.cmdWaiting;
	}
	
	public PetCommand peekProcessingCommand() {
		return this.cmdProcessing;
	}
	
	public synchronized PetCommand getCommandToProcess() {
		if (this.cmdWaiting != null) {
			this.cmdProcessing = this.cmdWaiting;
		}
		PetCommand cmd = this.cmdWaiting;
		this.cmdWaiting = null;
		return cmd;
	}
	
	public synchronized void clearCommand() {
		this.cmdProcessing = null;
		this.cmdWaiting = null;
	}
	
	public boolean hasCommandWaiting() {
		return this.cmdWaiting != null;
	}
	
	public boolean hasCommandProcessing() {
		return this.cmdProcessing != null;
	}
	
	public synchronized void finishProcess() {
		this.cmdProcessing = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CommandManager [cmdWaiting=" + cmdWaiting + ", cmdProcessing="
				+ cmdProcessing + "]";
	}
}
