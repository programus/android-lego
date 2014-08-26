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
		// 生气时快步走
		int speed = RobotBody.Speed.RunSpeed.value;
		this.body.forward(speed);
		// 走路消耗体力与速度有关
		this.param.setHealthConsume(Math.abs(speed / 100));
		// 生气会让宠物悲伤一点
		this.param.sadden(false);
		while (this.isControlling() && this.takeControl()) {
			Thread.yield();
		}
		this.body.stop(false);
	}

}
