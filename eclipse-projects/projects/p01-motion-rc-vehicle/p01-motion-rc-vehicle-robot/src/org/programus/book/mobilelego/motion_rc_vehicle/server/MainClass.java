package org.programus.book.mobilelego.motion_rc_vehicle.server;

import java.io.IOException;

import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.server.core.ObstacleMonitor;
import org.programus.book.mobilelego.motion_rc_vehicle.server.core.VehicleRobot;
import org.programus.book.mobilelego.motion_rc_vehicle.server.net.Server;
import org.programus.book.mobilelego.motion_rc_vehicle.server.processor.RobotMoveProcessor;

public class MainClass {
	private static EV3 ev3 = LocalEV3.get();
    private static GraphicsLCD g = ev3.getGraphicsLCD();
    private static LED led = ev3.getLED();
    
    static {
    	g.setFont(Font.getSmallFont());
    }

	private static void promptWait() {
		g.clear();
		g.drawString("Waiting connection...", 0, 0, GraphicsLCD.LEFT | GraphicsLCD.TOP);
		led.setPattern(6);
	}
	
	private static void promptConnected() {
		g.clear();
		g.drawString("Connected!", 0, 0, GraphicsLCD.LEFT | GraphicsLCD.TOP);
		led.setPattern(1);
	}

	public static void main(String[] args) {
		promptWait();
		VehicleRobot robot = VehicleRobot.getInstance();
		Server server = Server.getInstance();
		server.start();
		promptConnected();
		try {
			Communicator communicator = server.getCommunicator();
			communicator.addProcessor(RobotMoveCommand.class, new RobotMoveProcessor(robot));
			ObstacleMonitor obsMonitor = new ObstacleMonitor(robot, communicator);
			obsMonitor.startReporting();
		} catch (IOException e) {
			Sound.buzz();
			e.printStackTrace();
		}
	}
}
