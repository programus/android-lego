package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import java.io.IOException;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.server.net.Server;
import org.programus.book.mobilelego.robopet.server.robot.CommandContainer;

public class ExitProgram extends AbstractBehavior {
	private CommandContainer cc;
	
	public ExitProgram(CommandContainer cc) {
		this.cc = cc;
	}

	@Override
	public boolean takeControl() {
		return cc.getKeyCommand() == CommandContainer.KeyCommand.Esc;
	}

	@Override
	public void move() {
		System.out.println("Exit...");
		cc.setKeyCommand(null);
		Server server = Server.getInstance();
		Communicator comm = server.getCommunicator();
		if (comm.isAvailable()) {
			server.getCommunicator().send(ExitSignal.getInstance());
		}
		server.close();
		this.body.stop(false);
		try {
			this.param.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.body.close();
		System.exit(0);
	}

}
