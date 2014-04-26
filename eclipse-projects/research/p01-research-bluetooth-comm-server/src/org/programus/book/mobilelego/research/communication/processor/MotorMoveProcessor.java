package org.programus.book.mobilelego.research.communication.processor;

import lejos.hardware.motor.BaseRegulatedMotor;

import org.programus.book.mobilelego.research.communication.protocol.MotorMoveCommand;
import org.programus.book.mobilelego.research.communication.protocol.MotorMoveCommand.Command;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorMoveProcessor<M extends BaseRegulatedMotor> implements Processor<MotorMoveCommand> {
	private M motor;
	
	public MotorMoveProcessor(M motor) {
		this.motor = motor;
	}
	
	@Override
	public void process(MotorMoveCommand cmd, Communicator communicator) {
		System.out.println(String.format("Process command: %s", cmd.toString()));
		Command command = cmd.getCommand();
		float speed = cmd.getSpeed();
		motor.setSpeed(speed);
		switch(command) {
		case Forward:
			motor.forward();
			break;
		case Backword:
			motor.backward();
			break;
		case Float:
			motor.flt(true);
			break;
		case Stop:
			motor.stop();
			break;
		}
	}
}
