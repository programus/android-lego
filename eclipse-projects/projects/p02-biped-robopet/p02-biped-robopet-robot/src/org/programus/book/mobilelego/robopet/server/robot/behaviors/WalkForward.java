package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;


/**
 * 向前行进
 * @author programus
 *
 */
public class WalkForward extends AbstractBehavior {

	/**
	 * 除其他行为模式外的行为模式，优先级最低，为常态行为模式。
	 */
	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void move() {
		// 走路
		int speed = RobotBody.Speed.WalkSpeed.value; 
		this.body.forward(speed);
		this.param.setHealthConsume(Math.abs(speed / 100));
		// 走路很开心
		this.param.please();
		while (this.isControlling() && this.takeControl()) {
			Thread.yield();
		}
		this.body.stop(false);
	}

}
