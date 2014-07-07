package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;

import lejos.robotics.subsumption.Behavior;

public abstract class AbstractBehavior implements Behavior {
	protected final RobotBody body = RobotBody.getInstance();
	private boolean running;
	
	protected boolean isRunning() {
		return this.running;
	}

	@Override
	public void action() {
		this.running = true;
		this.move();
	}

	@Override
	public void suppress() {
		this.running = false;
	}
	
	public abstract void move();
}
