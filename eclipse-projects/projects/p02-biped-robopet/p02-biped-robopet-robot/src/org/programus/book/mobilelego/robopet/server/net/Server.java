package org.programus.book.mobilelego.robopet.server.net;

import java.io.IOException;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.net.OnConnectedListener;

public class Server {
	private static Server instance = new Server();
	private BTConnector connector;
	private Communicator communicator; 
	private NXTConnection conn; 
	private OnConnectedListener onConnectedListener;

	private Server() {
		this.communicator = new Communicator();
	}
	
	public static Server getInstance() {
		return instance;
	}

	public void start() {
		if (!this.isStarted()) {
			this.connector = new BTConnector();
			conn = connector.waitForConnection(0, NXTConnection.RAW);
			try {
				this.communicator.reset(conn.openInputStream(), conn.openOutputStream());
				this.communicator.clearProcessor(null);
				if (this.onConnectedListener != null) {
					this.onConnectedListener.onConnected(communicator);
				}
			} catch (IOException e) {
				if (this.onConnectedListener != null) {
					this.onConnectedListener.onFailed(e);
				}
			}
		}
	}
	
	public void close() {
		if (this.communicator.isAvailable()) {
			this.communicator.close();
		}
		if (this.conn != null) {
			try {
				synchronized (this.communicator) {
					this.conn.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.conn = null;
		}
		if (this.connector != null) {
			this.connector.close();
			this.connector = null;
		}
	}
	
	public boolean isStarted() {
		return conn != null;
	}
	
	public Communicator getCommunicator() {
		return this.communicator;
	}

	public OnConnectedListener getOnConnectedListener() {
		return onConnectedListener;
	}

	public void setOnConnectedListener(OnConnectedListener onConnectedListener) {
		this.onConnectedListener = onConnectedListener;
	}
}
