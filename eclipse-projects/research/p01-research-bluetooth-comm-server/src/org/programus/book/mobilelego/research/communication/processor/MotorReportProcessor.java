package org.programus.book.mobilelego.research.communication.processor;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.motor.BaseRegulatedMotor;

import org.programus.book.mobilelego.research.communication.protocol.MotorReportCommand;
import org.programus.book.mobilelego.research.communication.protocol.MotorReportMessage;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorReportProcessor<M extends BaseRegulatedMotor> implements Processor<MotorReportCommand> {
	private M motor;
	
	private Timer timer = new Timer("Reporting Timer", true);
	private TimerTask task = null;
	
	public MotorReportProcessor(M motor) {
		this.motor = motor;
	}

	private MotorReportMessage sendReport(Communicator communicator, MotorReportMessage prevMsg) {
		MotorReportMessage msg = prevMsg;
		int speed = motor.getRotationSpeed();
		int tachoCount = motor.getTachoCount();
		if (msg == null || speed != prevMsg.getSpeed() || tachoCount != prevMsg.getTachoCount()) {
			msg = new MotorReportMessage();
			msg.setSpeed(speed);
			msg.setTachoCount(tachoCount);
            communicator.send(msg);
		}
		
		return msg;
	}
	
	private void startReportTask(final Communicator communicator) {
        final MotorReportMessage msg = new MotorReportMessage();
		sendReport(communicator, null);
		if (task == null) {
            task = new TimerTask() {
                MotorReportMessage prevMsg = msg;
                @Override
                public void run() {
                    prevMsg = sendReport(communicator, prevMsg);
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

	@Override
	public void process(MotorReportCommand msg, Communicator communicator) {
		if (msg.isReportOn()) {
			this.startReportTask(communicator);
		} else {
			this.stopReportTask();
		}
	}
}
