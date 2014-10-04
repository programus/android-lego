package org.programus.book.mobilelego.trafficsign.server.processor;

import lejos.hardware.Sound;

import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator;
import org.programus.book.mobilelego.trafficsign.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.trafficsign.server.net.Server;
import org.programus.book.mobilelego.trafficsign.server.robot.Car;

/**
 * 连接断开信号处理器。
 * @author programus
 *
 */
public class ExitProcessor implements Processor<ExitSignal>{
	
	private Car car;
	
	public ExitProcessor(Car car) {
		this.car = car;
	}

	@Override
	public void process(ExitSignal msg, Communicator communicator) {
		Sound.buzz();
		Server server = Server.getInstance();
		server.close();
		car.close();
		System.exit(0);
	}
}
