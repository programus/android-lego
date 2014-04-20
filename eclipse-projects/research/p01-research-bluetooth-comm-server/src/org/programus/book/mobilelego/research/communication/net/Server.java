package org.programus.book.mobilelego.research.communication.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

import org.programus.book.mobilelego.research.communication.Communicator;

public class Server {
	private static Server instance = new Server();
	private Communicator communicator; 
	private NXTConnection conn; 

	private Server() {}
	
	public static Server getInstance() {
		return instance;
	}

	public void start() {
		BTConnector connector = new BTConnector();
		conn = connector.waitForConnection(0, NXTConnection.RAW);
	}
	
	public boolean isStarted() {
		return conn != null;
	}
	
	public Communicator getCommunicator() throws IOException {
		if (this.communicator == null) {
            this.communicator = new Communicator(new ObjectInputStream(conn.openInputStream()), new ObjectOutputStream(conn.openOutputStream()));
		}
		return this.communicator;
	}
}
