package org.programus.book.mobilelego.research.communication.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class SppClient {
	private final static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	public static interface OnConnectedListener {
		void onConnected(InputStream input, OutputStream output);
		void onFailed(Exception e);
	}
	
	private BluetoothSocket socket;
	private OnConnectedListener onConnectedListener;
	
	public void setOnConnectedListener(OnConnectedListener listener) {
		this.onConnectedListener = listener;
	}
	
	public void connect(final BluetoothDevice device) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
                close();
                try {
					socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
					socket.connect();
					
					if (onConnectedListener != null) {
					    onConnectedListener.onConnected(socket.getInputStream(), socket.getOutputStream());
					}
				} catch (Exception e) {
					if (onConnectedListener != null) {
						onConnectedListener.onFailed(e);
					}
				}
			}
		}, "SPP Connect");
		t.start();
	}
	
	public boolean isConnected() {
		return socket != null;
	}
	
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
		}
	}
}
