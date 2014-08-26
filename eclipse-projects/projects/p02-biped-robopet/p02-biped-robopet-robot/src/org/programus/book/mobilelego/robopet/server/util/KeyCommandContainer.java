package org.programus.book.mobilelego.robopet.server.util;

public class KeyCommandContainer {
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
