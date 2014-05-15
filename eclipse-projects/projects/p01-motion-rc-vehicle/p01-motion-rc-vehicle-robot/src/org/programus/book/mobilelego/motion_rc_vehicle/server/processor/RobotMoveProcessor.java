package org.programus.book.mobilelego.motion_rc_vehicle.server.processor;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.motion_rc_vehicle.server.core.VehicleRobot;

public class RobotMoveProcessor implements Processor<RobotMoveCommand> {
	private VehicleRobot robot;
	
	public RobotMoveProcessor(VehicleRobot robot) {
		this.robot = robot;
	}

	@Override
	public void process(RobotMoveCommand msg, Communicator communicator) {
		System.out.printf("Processing %s\n", msg.toString());
		float speed = msg.getSpeed();
		double angle = msg.getRotation();
		switch (msg.getCommand()) {
		case Forward:
			robot.forward(speed, angle);
			break;
		case Backward:
			robot.backword(speed, angle);
			break;
		case Float:
			robot.flt();
			break;
		case Stop:
			robot.stop();
			break;
		}
	}

}
