package org.programus.book.mobilelego.trafficsign.server.net;

import java.io.IOException;

import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.net.OnConnectedListener;

/**
 * 服务器
 * @author programus
 *
 */
public class Server {
	/**
	 * 为单例模式准备的预备对象
	 */
	private static Server instance = new Server();
	
	/** 连接蓝牙的连接器 */
	private BTConnector connector;
	/** 通讯员对象 */
	private Communicator communicator; 
	/** 与客户端之间的蓝牙连接 */
	private NXTConnection conn; 
	/** 连接建立后的监听器 */
	private OnConnectedListener onConnectedListener;

	private Server() {
		this.communicator = new Communicator();
	}
	
	public static Server getInstance() {
		return instance;
	}

	/**
	 * 启动服务器
	 */
	public void start() {
		if (!this.isStarted()) {
			this.connector = new BTConnector();
			// 监听等待客户端连接
			conn = connector.waitForConnection(0, NXTConnection.RAW);
			try {
				// 连接成功后，重置通讯员
				this.communicator.reset(conn.openInputStream(), conn.openOutputStream());
				this.communicator.clearProcessor(null);
				if (this.onConnectedListener != null) {
					// 调用连接成功时的回调函数
					this.onConnectedListener.onConnected(communicator);
				}
			} catch (IOException e) {
				if (this.onConnectedListener != null) {
					// 调用连接失败时的回调函数
					this.onConnectedListener.onFailed(e);
				}
			}
		}
	}
	
	/**
	 * 关闭服务器
	 */
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
//			this.connector.close();
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
