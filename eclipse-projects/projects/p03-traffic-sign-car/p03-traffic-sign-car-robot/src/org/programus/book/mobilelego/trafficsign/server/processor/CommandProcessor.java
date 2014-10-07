package org.programus.book.mobilelego.trafficsign.server.processor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.LCDOutputStream;
import lejos.hardware.lcd.TextLCD;

import org.programus.book.mobilelego.trafficsign.comm.protocol.CarCommand;
import org.programus.book.mobilelego.trafficsign.comm.protocol.CommandCompletedMessage;
import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.trafficsign.server.net.Server;
import org.programus.book.mobilelego.trafficsign.server.robot.Car;

public class CommandProcessor implements Processor<CarCommand> {
	/** 命令对应的声音文件 */
	private static final File[] SND_FILES = {
		new File("forward.wav"),
		new File("turnLeft.wav"),
		new File("turnRight.wav"),
		new File("turnBack.wav"),
		new File("stop.wav"),
		new File("exit.wav"),
		new File("shutdown.wav"),
	};
	/** 系统的关机命令 */
	private static final String SHUTDOWN_CMD = "init 0";
	private Car car;
	private PrintStream out;
	
	public CommandProcessor(Car car) {
		this.car = car;
		TextLCD lcd = LocalEV3.get().getTextLCD(Font.getSmallFont());
		lcd.clear();
		out = new PrintStream(new LCDOutputStream(lcd));
	}

	@Override
	public void process(CarCommand msg, Communicator communicator) {
		CarCommand.Command cmd = msg.getCommand();
		File sndFile = SND_FILES[cmd.ordinal()];
		if (sndFile.exists()) {
			Sound.playSample(SND_FILES[cmd.ordinal()], Sound.VOL_MAX);
		}
		out.println(cmd);
		switch (cmd) {
		case Forward:
			car.forward();
			break;
		case TurnLeft:
			car.turn(-90, false);
			car.forward();
			break;
		case TurnRight:
			car.turn(90, false);
			car.forward();
			break;
		case TurnBack:
			car.turn(180, false);
			car.forward();
			break;
		case Stop:
			car.stop();
			break;
		case Exit:
			exit(communicator);
			break;
		case Shutdown:
			shutdown(communicator);
			break;
		}
		// 命令执行完毕
		communicator.send(CommandCompletedMessage.getInstance());
	}

	private void closeCommunication(Communicator communicator) {
		communicator.send(ExitSignal.getInstance());
		Server server = Server.getInstance();
		if (server.isStarted()) {
			server.close();
		}
	}
	
	private void exit(Communicator communicator) {
		this.closeCommunication(communicator);
		Sound.buzz();
		System.exit(0);
	}
	
	private void shutdown(Communicator communicator) {
		this.closeCommunication(communicator);
		Sound.buzz();
		try {
			Runtime.getRuntime().exec(SHUTDOWN_CMD);
		} catch (IOException e) {
			// 与leJOS源代码一样，忽略
		}
	}
}
