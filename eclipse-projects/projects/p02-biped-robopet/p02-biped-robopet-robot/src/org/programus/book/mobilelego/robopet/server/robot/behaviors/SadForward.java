package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 情绪低落时的前行模式
 * @author programus
 *
 */
public class SadForward extends AbstractBehavior {

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Sad;
	}

	@Override
	public void move() {
		// 情绪低落慢步走
		int speed = RobotBody.Speed.AlignSpeed.value;
		this.body.forward(speed);
		this.param.setHealthConsume(Math.abs(speed / 100));
		// 情绪低落时向高兴方向发展
		this.param.please();
		while (this.isControlling() && this.takeControl()) {
			Thread.yield();
		}
		this.body.stop(false);
	}

}
