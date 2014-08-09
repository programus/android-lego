package org.programus.book.mobilelego.robopet.server.processors;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.robopet.server.util.CommandManager;

public class ExitProcessor implements Processor<ExitSignal>{
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public void process(ExitSignal msg, Communicator communicator) {
		cmdMgr.clearCommand();
	}
}
