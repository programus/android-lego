package org.programus.book.mobilelego.research.communication.processor;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import org.programus.book.mobilelego.research.communication.Communicator;
import org.programus.book.mobilelego.research.communication.Communicator.RobotCmdProcessor;
import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;

public class MotorProcessor implements RobotCmdProcessor {
	private static enum Command {
		Forward,
		Float,
		Stop,
		Report
	}
	
	private EV3LargeRegulatedMotor motor = new EV3LargeRegulatedMotor(MotorPort.B);

	@Override
	public void process(RobotCommand cmd, Communicator communicator) {
		int cmdIndex = cmd.getIntValue();
		float speed = cmd.getFloatValue();
		motor.setSpeed(speed);
		switch(Command.values()[cmdIndex]) {
		case Forward:
			motor.forward();
			break;
		case Float:
			motor.flt(true);
			break;
		case Stop:
			motor.stop();
			break;
		case Report:
			PhoneMessage msg = new PhoneMessage();
			msg.setType(PhoneMessage.Type.Motor);
			msg.setIntValue(motor.getTachoCount());
			communicator.sendPhoneMsg(msg);
			break;
		}
	}

}
