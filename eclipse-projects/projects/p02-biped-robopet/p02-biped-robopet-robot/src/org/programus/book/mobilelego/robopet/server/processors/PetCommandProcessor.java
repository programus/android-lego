/**
 * 
 */
package org.programus.book.mobilelego.robopet.server.processors;

import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.robopet.server.util.CommandManager;

/**
 * 宠物命令处理器
 * @author programus
 *
 */
public class PetCommandProcessor implements Processor<PetCommand> {
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public void process(PetCommand msg, Communicator communicator) {
		// 收到宠物命令后，投入命令管理器中
		cmdMgr.putCommand(msg);
	}

}
