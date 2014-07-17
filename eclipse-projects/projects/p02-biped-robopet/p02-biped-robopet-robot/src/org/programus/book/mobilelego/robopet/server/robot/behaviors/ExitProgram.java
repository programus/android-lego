package org.programus.book.mobilelego.robopet.server.robot.behaviors;

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
		this.body.stop(false);
		this.body.close();
		System.exit(0);
	}

}
