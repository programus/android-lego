package org.programus.book.mobilelego.motion_rc_vehicle.server.core;

import java.util.Timer;
import java.util.TimerTask;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotReportMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

public class RobotReporter {
	private static final int ITERVAL = 100;
	private VehicleRobot robot;
	private Communicator communicator;
	
	private Timer timer = new Timer("robot reporter", true);
	private TimerTask task; 
	
	public RobotReporter(VehicleRobot robot, Communicator communicator) {
		this.robot = robot;
		this.communicator = communicator;
		this.communicator.addProcessor(ExitSignal.class, new Communicator.Processor<ExitSignal>() {
			@Override
			public void process(ExitSignal msg, Communicator communicator) {
				stopReporting();
			}
		});
	}
	
	private RobotReportMessage sendReport(RobotReportMessage prevMsg) {
		RobotReportMessage msg = prevMsg;
		double distance = robot.getDistance();
		double speed = robot.getSpeed();
		double rotationalSpeed = robot.getRotationalSpeed();
		if (msg == null || distance != prevMsg.getDistance() || speed != prevMsg.getSpeed() || rotationalSpeed != prevMsg.getRotationalSpeed()) {
			msg = new RobotReportMessage();
			msg.setDistance(distance);
			msg.setSpeed(speed);
			msg.setRotationalSpeed(rotationalSpeed);
			communicator.send(msg);
		}
		return msg;
	}
	
	private void sendMaxReport() {
		RobotReportMessage msg = new RobotReportMessage();
		msg.setDistance(0);
		msg.setSpeed(robot.getMaxSpeed());
		msg.setRotationalSpeed(robot.getMaxRotationSpeed());
		communicator.send(msg);
	}
	
	public void startReporting() {
		sendMaxReport();
		if (task == null) {
			task = new TimerTask() {
				private RobotReportMessage prevMsg;
				@Override
				public void run() {
					prevMsg = sendReport(prevMsg);
				}
			};
			timer.schedule(task, 0, ITERVAL);
		}
	}
	
	public void stopReporting() {
		if (task != null) {
			task.cancel();
			task = null;
			timer.purge();
		}
	}
}
