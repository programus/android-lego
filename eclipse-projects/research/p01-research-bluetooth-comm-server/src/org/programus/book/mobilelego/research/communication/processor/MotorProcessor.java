package org.programus.book.mobilelego.research.communication.processor;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.Protocol;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorProcessor implements Processor<RobotCommand, PhoneMessage> {
	private EV3LargeRegulatedMotor motor = new EV3LargeRegulatedMotor(MotorPort.B);
	
	private Timer timer = new Timer("Reporting Timer", true);
	private TimerTask task = null;

	@Override
	public void process(RobotCommand cmd, Communicator<RobotCommand, PhoneMessage> communicator) {
		System.out.println(String.format("Process command: %s", cmd.toString()));
		int cmdIndex = cmd.getIntValue();
		float speed = cmd.getFloatValue();
		motor.setSpeed(speed);
		switch(Protocol.MotorCommand.values()[cmdIndex]) {
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
		case Report:
			if (((int) speed) == 0) {
				System.out.println("stop report.");
				this.stopReportTask();
			} else {
				System.out.println("start report.");
				this.startReportTask(communicator);
			}
			break;
		}
	}
	
	private void sendReport(Communicator<RobotCommand, PhoneMessage> communicator, int value) {
        PhoneMessage msg = new PhoneMessage();
        msg.setType(PhoneMessage.Type.Motor);
        msg.setIntValue(value);
        communicator.send(msg);
	}
	
	private void startReportTask(final Communicator<RobotCommand, PhoneMessage> communicator) {
		final int tachoCount = motor.getTachoCount();
		sendReport(communicator, tachoCount);
		if (task == null) {
            task = new TimerTask() {
            	private int oldCount = tachoCount;
                @Override
                public void run() {
                	int tachoCount = motor.getTachoCount();
                	if (tachoCount != oldCount) {
                        System.out.printf("report: %d\n", motor.getTachoCount());
                		sendReport(communicator, tachoCount);
                		oldCount = tachoCount;
                	}
                	
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
