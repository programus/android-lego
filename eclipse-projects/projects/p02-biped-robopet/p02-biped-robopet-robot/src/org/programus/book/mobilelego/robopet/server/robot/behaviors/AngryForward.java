package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 生气时的前行模式
 * @author programus
 *
 */
public class AngryForward extends AbstractBehavior {

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Angry;
	}

	@Override
	public void move() {
		int speed = RobotBody.Speed.RunSpeed.value;
		this.body.forward(speed);
		this.param.setHealthConsume(speed / 100);
		this.param.sadden();
		while (this.isControlling()) {
			Thread.yield();
		}
		this.body.stop(false);
	}

}
