package org.programus.book.mobilelego.motion_rc_vehicle.server.core;

import java.util.Timer;
import java.util.TimerTask;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ObstacleInforMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

/**
 * 障碍物监测器
 * @author programus
 *
 */
public class ObstacleMonitor {
	/** 监测间隔，单位：毫秒 */
	private static final int ITERVAL = 200;
	private VehicleRobot robot;
	private Communicator communicator;
	
	private Timer timer = new Timer("distance reporter", true);
	private TimerTask task; 
	
	public ObstacleMonitor(VehicleRobot robot, Communicator communicator) {
		this.robot = robot;
		this.communicator = communicator;
		this.communicator.addProcessor(ExitSignal.class, new Communicator.Processor<ExitSignal>() {
			@Override
			public void process(ExitSignal msg, Communicator communicator) {
				stopReporting();
			}
		});
	}
	
	private ObstacleInforMessage sendReport(ObstacleInforMessage prevMsg) {
		ObstacleInforMessage msg = prevMsg;
		float distance = robot.getObstacleDistance();
		if (msg == null || distance != prevMsg.getDistance()) {
			msg = new ObstacleInforMessage();
			msg.setDistance(distance);
			communicator.send(msg);
		}
		return msg;
	}
	
	public void startReporting() {
		if (task == null) {
			task = new TimerTask() {
				private ObstacleInforMessage prevMsg;
				@Override
				public void run() {
					prevMsg = sendReport(prevMsg);
					if (ObstacleInforMessage.Type.Danger.equals(prevMsg.getType()) && robot.getRotationSpeed() > 0) {
						robot.stop();
					}
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
