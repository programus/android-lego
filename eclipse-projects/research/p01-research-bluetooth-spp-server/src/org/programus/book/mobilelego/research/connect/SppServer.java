package org.programus.book.mobilelego.research.connect;

import java.io.IOException;
import java.io.InputStream;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

public class SppServer {
	/**
	 * 程序入口函数
	 * @param args 命令行参数（未使用）
	 */
	public static void main(String[] args) {
        // 取得GraphicsLCD实例
        GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
        // 设置为小字体
        g.setFont(Font.getSmallFont());
        // 新建基于SPP的蓝牙连接器
        BTConnector connector = new BTConnector();
        // 在屏幕左上角显示文字
        g.drawString("waiting connection...", 0, 0, 
            GraphicsLCD.LEFT | GraphicsLCD.TOP);
        // 等待连接
        NXTConnection conn = 
            connector.waitForConnection(0, NXTConnection.RAW);
		if (conn != null) {
			// 连接成功的情况
            InputStream in = null;
			in = conn.openInputStream();
			try {
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
			}
			finally {
				if (in != null) {
					try {
						in.close();					// 断开输入流
					} catch (IOException e) { }
				}				
				try {
					conn.close();
				} catch (IOException e) { }
			}
		} else {
			g.clear();
			g.drawString("Connect failed", 0, 0, 
                GraphicsLCD.LEFT | GraphicsLCD.TOP);
            Button.waitForAnyPress();				// 等待任意按键
		}
	}
}
