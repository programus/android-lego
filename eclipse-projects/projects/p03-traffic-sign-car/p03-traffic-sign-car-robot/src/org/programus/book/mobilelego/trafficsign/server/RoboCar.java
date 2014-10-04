package org.programus.book.mobilelego.trafficsign.server;

import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import org.programus.book.mobilelego.trafficsign.comm.protocol.CarCommand;
import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.net.OnConnectedListener;
import org.programus.book.mobilelego.trafficsign.server.net.Server;
import org.programus.book.mobilelego.trafficsign.server.processor.CommandProcessor;
import org.programus.book.mobilelego.trafficsign.server.processor.ExitProcessor;
import org.programus.book.mobilelego.trafficsign.server.robot.Car;

public class RoboCar {

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
		final Car car = new Car();
		// 创建命令处理员对象
		final CommandProcessor cmdProcessor = new CommandProcessor(car);
		// 创建退出信号处理员对象
		final ExitProcessor exitProcessor = new ExitProcessor(car);
		// 取得服务器对象
		Server server = Server.getInstance();
		server.setOnConnectedListener(new OnConnectedListener() {
			@Override
			public void onConnected(Communicator comm) {
				// 服务器连接成功，向通讯员追加处理员
				comm.addProcessor(CarCommand.class, cmdProcessor);
				comm.addProcessor(ExitSignal.class, exitProcessor);
				// 通知已连接
				promptConnected();
			}

			@Override
			public void onFailed(Exception e) {
				// 服务器连接失败
				Sound.buzz();
				// 关闭机器人
				car.close();
				// 打出错误信息
				e.printStackTrace(System.out);
				// 退出程序
				System.exit(0);
			}
		});
		// 提示服务器等待连接
		promptWait();
		// 启动服务器，等待连接
		server.start();
	}
}
