package org.programus.book.mobilelego.research.connect;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;


public class TcpipServer {
	private final static int PORT = 9988;
	
	public static void main(String[] args) {
        // 取得GraphicsLCD实例
        GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
        // 设置为小字体
        g.setFont(Font.getSmallFont());
		ServerSocket server = null;
		Socket socket = null;
		InputStream in = null;
		try {
			server = new ServerSocket(PORT);	// 建立服务器
			g.clear();							// 清屏
            // 在屏幕左上角显示文字
            g.drawString("waiting connectoin...", 0, 0, 
        		GraphicsLCD.LEFT | GraphicsLCD.TOP);
			socket = server.accept();

			in = socket.getInputStream();
			int data = in.read();				// 读取一个字节数据
			
			if (data == 1) {
				// 如果收到数字1
				Sound.beep();					// 发出“哔--”
			} else {
				Sound.buzz();					// 发出“咘--”
			}
		} catch (IOException e) {
			g.clear();							// 清屏
			g.drawString(e.getMessage(), 0, 0, 
                GraphicsLCD.LEFT | GraphicsLCD.TOP);
			Button.waitForAnyPress();			// 等待任意按键
		} finally {
			if (in != null) {
				try {
					in.close();					// 断开输入流
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
