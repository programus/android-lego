package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 停止运动的行为，机器人劳累时使用
 * @author programus
 *
 */
public class Stop extends AbstractBehavior {
	
	private static int HEALTH_CONSUME = -10;

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Tired;
	}

	@Override
	public void move() {
		// 停止
		this.body.stop(false);
		// 体力开始恢复
		this.param.setHealthConsume(HEALTH_CONSUME);
		while (this.isControlling() && this.takeControl()) {
			this.param.updateStatus();
			this.body.presentMood();
			Thread.yield();
		}
		// 重新开始消耗体力
		this.param.setHealthConsume(-HEALTH_CONSUME);
	}

}
