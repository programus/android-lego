package org.programus.book.mobilelego.motion_rc_vehicle.server.processor;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator.Processor;

public class RobotMoveProcessor implements Processor<RobotMoveCommand> {

	@Override
	public void process(RobotMoveCommand msg, Communicator communicator) {
	}

}
