package org.programus.book.mobilelego.motion_rc_vehicle.server.core;

import java.util.Timer;
import java.util.TimerTask;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotReportMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

public class RobotReporter {
	private static final int ITERVAL = 500;
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
		RobotReportMessage msg = new RobotReportMessage();
		int distance = robot.getDistance();
		short speed = robot.getSpeed();
		short rotationSpeed = robot.getRotationSpeed();
        msg.setDistance(distance);
        msg.setSpeed(speed);
        msg.setRotationSpeed(rotationSpeed);
		if (prevMsg == null || !msg.isSameAs(prevMsg)) {
			communicator.send(msg);
		}
		return msg;
	}
	
	private void sendMaxReport() {
		RobotReportMessage msg = new RobotReportMessage();
		msg.setDistance(-1);
		msg.setSpeed(robot.getMaxSpeed());
		msg.setRotationSpeed(robot.getMaxRotationSpeed());
		communicator.send(msg);
	}
	
	public void startReporting() {
		// 首先向手机端发送速度和转速上限值
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
