package org.programus.book.mobilelego.research.communication;

import java.io.IOException;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import org.programus.book.mobilelego.research.communication.net.Server;
import org.programus.book.mobilelego.research.communication.processor.MotorProcessor;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;

public class CommServer {

	public static void main(String[] args) {
        // 取得GraphicsLCD实例
        GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
        // 设置为小字体
        g.setFont(Font.getSmallFont());
        g.clear();							// 清屏
        // 在屏幕左上角显示文字
        g.drawString("waiting connection...", 0, 0, 
            GraphicsLCD.LEFT | GraphicsLCD.TOP);
		Server server = Server.getInstance();
		server.start();
		g.clear();
        // 在屏幕左上角显示文字
        g.drawString("connected!", 0, 0, 
            GraphicsLCD.LEFT | GraphicsLCD.TOP);
		try {
			Communicator communicator = server.getCommunicator();
			communicator.addRobotCmdProcessor(RobotCommand.Type.Motor, new MotorProcessor());
		} catch (IOException e) {
			Sound.buzz();
		}
	}

}
