package org.programus.book.mobilelego.motion_rc_vehicle.server.net;

import java.io.IOException;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

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
            this.communicator = new Communicator(conn.openInputStream(), conn.openOutputStream());
		}
		return this.communicator;
	}
}
