package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 开心时的前行模式
 * @author programus
 *
 */
public class HappyForward extends AbstractBehavior {

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Happy;
	}

	@Override
	public void move() {
		// 开心时要快走
		int speed = RobotBody.Speed.RunSpeed.value;
		this.body.forward(speed);
		this.param.setHealthConsume(Math.abs(speed / 100));
		// 太开心了，就要开始向悲伤的方向发展
		this.param.sadden(false);
		while (this.isControlling() && this.takeControl()) {
			Thread.yield();
		}
		this.body.stop(false);
	}

}
