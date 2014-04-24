package org.programus.book.mobilelego.research.communication.net;

import java.io.IOException;
import java.util.UUID;

import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;
import org.programus.book.mobilelego.research.communication.util.Communicator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class SppClient {
	private final static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	public static interface OnConnectedListener {
		void onConnected(Communicator<PhoneMessage, RobotCommand> comm);
		void onFailed(Exception e);
	}
	
	private BluetoothSocket mSocket;
	private OnConnectedListener mConnectedListener;
	private Communicator<PhoneMessage, RobotCommand> mComm = new Communicator<PhoneMessage, RobotCommand>();
	
	public void setOnConnectedListener(OnConnectedListener listener) {
		this.mConnectedListener = listener;
	}
	
	public void connect(final BluetoothDevice device) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
                close();
                try {
					mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
					mSocket.connect();
					mComm.reset(mSocket.getInputStream(), mSocket.getOutputStream());
					if (mConnectedListener != null) {
					    mConnectedListener.onConnected(mComm);
					}
				} catch (Exception e) {
					if (mConnectedListener != null) {
						mConnectedListener.onFailed(e);
					}
				}
			}
		}, "SPP Connect");
		t.start();
	}
	
	public boolean isConnected() {
		return mSocket != null;
	}
	
	public void close() {
		if (mComm.isAvailable()) {
			mComm.close();
		}
		if (mSocket != null) {
			try {
				synchronized (mComm) {
                    mSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSocket = null;
		}
	}
}
