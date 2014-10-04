package org.programus.book.mobilelego.trafficsign.server.processor;

import java.io.IOException;

import lejos.hardware.Sound;

import org.programus.book.mobilelego.trafficsign.comm.protocol.CarCommand;
import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.trafficsign.server.net.Server;
import org.programus.book.mobilelego.trafficsign.server.robot.Car;

public class CommandProcessor implements Processor<CarCommand> {
	/** 系统的关机命令 */
	private static final String SHUTDOWN_CMD = "init 0";
	private Car car;
	
	public CommandProcessor(Car car) {
		this.car = car;
	}

	@Override
	public void process(CarCommand msg, Communicator communicator) {
		switch (msg.getCommand()) {
		case Forward:
			car.forward();
			break;
		case TurnLeft:
			car.turn(-90, false);
			break;
		case TurnRight:
			car.turn(90, false);
			break;
		case TurnBack:
			car.turn(180, false);
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
