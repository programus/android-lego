package org.programus.book.mobilelego.robopet.server.robot;

public class CommandContainer {
	public static enum KeyCommand {
		Enter,
		Esc,
		Left,
		Right,
		Up,
		Down,
	}
	
	private KeyCommand keyCommand = null;

	public KeyCommand getKeyCommand() {
		return keyCommand;
	}

	public void setKeyCommand(KeyCommand keyCommand) {
		this.keyCommand = keyCommand;
	}
}
