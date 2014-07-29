/**
 * 
 */
package org.programus.book.mobilelego.robopet.server.processors;

import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.comm.util.Communicator.Processor;

/**
 * @author programus
 *
 */
public class PetCommandProcessor implements Processor<PetCommand> {

	@Override
	public void process(PetCommand msg, Communicator communicator) {
	}

}
