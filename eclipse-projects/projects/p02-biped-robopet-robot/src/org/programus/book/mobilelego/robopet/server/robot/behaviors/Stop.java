package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 停止运动的行为，机器人劳累时使用
 * @author programus
 *
 */
public class Stop extends AbstractBehavior {
	
	private static int HEALTH_CONSUME = -1;

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Tired;
	}

	@Override
	public void move() {
		this.body.stop(false);
		this.param.setHealthConsume(HEALTH_CONSUME);
		while (this.isControlling()) {
			Thread.yield();
		}
		this.param.setHealthConsume(-HEALTH_CONSUME);
	}

}
