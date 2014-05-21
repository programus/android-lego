package org.programus.book.mobilelego.motion_rc_vehicle.server;

import java.io.IOException;

import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.server.core.ObstacleMonitor;
import org.programus.book.mobilelego.motion_rc_vehicle.server.core.RobotReporter;
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
		// 取得机器人对象
		final VehicleRobot robot = VehicleRobot.getInstance();
		// 取得服务器对象
		Server server = Server.getInstance();
		// 提示服务器等待连接
		promptWait();
		// 启动服务器，等待连接
		server.start();
		// 通知已连接
		promptConnected();
		try {
			// 取得通讯员对象
			Communicator communicator = server.getCommunicator();
			// 追加机器人移动命令处理员
			communicator.addProcessor(RobotMoveCommand.class, new RobotMoveProcessor(robot));
			// 追加退出命令处理员
			communicator.addProcessor(ExitSignal.class, new Communicator.Processor<ExitSignal>() {
				@Override
				public void process(ExitSignal msg, Communicator communicator) {
					// 退出时释放机器人资源
					robot.release();
				}
			});
			// 创建障碍物监视器并启动
			ObstacleMonitor obsMonitor = new ObstacleMonitor(robot, communicator);
			obsMonitor.startReporting();
			// 创建机器人状态报告器并启动
			RobotReporter reporter = new RobotReporter(robot, communicator);
			reporter.startReporting();
		} catch (IOException e) {
			Sound.buzz();
			e.printStackTrace();
		}
	}
}
