package org.programus.book.mobilelego.research.communication.net;

import java.io.IOException;
import java.util.UUID;

import org.programus.book.mobilelego.research.communication.util.Communicator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * 基于SPP链接的客户端类
 * @author programus
 */
public class SppClient {
	private final static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	/**
	 * 当建立连接时回调用的接口
	 */
	public static interface OnConnectedListener {
		/**
		 * 正常建立连接时将调用此函数
		 * @param comm 建立连接后创建的通讯员对象
		 */
		void onConnected(Communicator comm);
		/**
		 * 连接失败时调用此函数
		 * @param e 失败时的例外
		 */
		void onFailed(Exception e);
	}
	
	private BluetoothSocket mSocket;
	private OnConnectedListener mConnectedListener;
	private Communicator mComm = new Communicator();
	
	/**
	 * 设置回调接口
	 * @param listener 回调接口
	 */
	public void setOnConnectedListener(OnConnectedListener listener) {
		this.mConnectedListener = listener;
	}
	
	/**
	 * 与指定的蓝牙设备进行SPP连接
	 * @param device 指定的蓝牙设备
	 */
	public void connect(final BluetoothDevice device) {
		// 为防止界面阻塞，在新线程中进行连接
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// 关闭现有连接（如果有的话）
                close();
                try {
					mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
					mSocket.connect();
					// 重设通讯员
					mComm.reset(mSocket.getInputStream(), mSocket.getOutputStream());
					mComm.clearProcessor(null);
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
	
	/**
	 * 返回连接是否已建立
	 * @return 连接已建立时返回true
	 */
	public boolean isConnected() {
		return mSocket != null;
	}
	
	/**
	 * 关闭连接
	 */
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
