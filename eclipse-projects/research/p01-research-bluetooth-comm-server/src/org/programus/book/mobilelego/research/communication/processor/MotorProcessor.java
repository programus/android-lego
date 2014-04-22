package org.programus.book.mobilelego.research.communication.processor;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorProcessor implements Processor<RobotCommand, PhoneMessage> {
	private static enum Command {
		Forward,
		Float,
		Stop,
		Report
	}
	
	private EV3LargeRegulatedMotor motor = new EV3LargeRegulatedMotor(MotorPort.B);
	
	private Timer timer = new Timer("Reporting Timer", true);
	private TimerTask task = null;

	@Override
	public void process(RobotCommand cmd, Communicator<RobotCommand, PhoneMessage> communicator) {
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
			if (((int) speed) == 0) {
				this.stopReportTask();
			} else {
				this.startReportTask(communicator);
			}
			break;
		}
	}
	
	private void startReportTask(final Communicator<RobotCommand, PhoneMessage> communicator) {
		if (task != null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    PhoneMessage msg = new PhoneMessage();
                    msg.setType(PhoneMessage.Type.Motor);
                    msg.setIntValue(motor.getTachoCount());
                    communicator.send(msg);
                }
            };
            
            timer.schedule(task, 0, 100);
		}
	}
	
	private void stopReportTask() {
		if (task != null) {
            task.cancel();
            task = null;
            timer.purge();
		}
	}
}
