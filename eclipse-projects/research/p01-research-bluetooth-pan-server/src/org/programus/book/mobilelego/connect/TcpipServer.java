package org.programus.book.mobilelego.connect;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Sound;


public class TcpipServer {
	private final static int PORT = 9988;
	
	public static void main(String[] args) {
		ServerSocket server = null;
		Socket socket = null;
		InputStream in = null;
		try {
			server = new ServerSocket(PORT);	// 建立服务器
			socket = server.accept();

			in = socket.getInputStream();
			int data = in.read();							// 读取一个字节数据
			
			if (data == 1) {
				// 如果收到数字1
				Sound.beep();								// 发出“哔——”
			} else {
				Sound.buzz();								// 发出“bu——”
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();						// 断开输入流
				} catch (IOException e) {
				}
			}
			if (socket != null) {
                try {
					socket.close();				// 断开网络连接
				} catch (IOException e) {
				}
			}
			if (server != null) {
                try {
                    server.close();				// 关闭服务器
				} catch (IOException e) {
				}
			}
		}
		
	}
}
