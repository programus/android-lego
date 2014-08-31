package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;

/**
 * 规避障碍物行为
 * @author programus
 *
 */
public class AvoidObstacle extends AbstractBehavior {
	private static int HEALTH_CONSUME = 1;

	@Override
	public boolean takeControl() {
		return this.body.isObstacleNear();
	}

	@Override
	public void move() {
		// 让机器人更愤怒
		this.param.annoy();
		// 让机器人更悲伤
		this.param.sadden(true);
		this.body.stop(false);
		// 固定的体力消耗
		this.param.setHealthConsume(HEALTH_CONSUME);
		// 转头扫描以确定规避方向
		this.body.turnHead(RobotBody.HeadSpeed.ScanSpeed, -90, 90, true);
		float maxDistance = 0;
		int angle = -90;
		while (this.body.isHeadTurning()) {
			float distance = this.body.getObstacleDistance();
			int currAngle = this.body.getHeadTurnAngle();
			if (distance > maxDistance) {
				// 寻找最远障碍物距离
				maxDistance = distance;
				// 记录最远障碍距离时的角度
				angle = currAngle;
				if (maxDistance == Float.POSITIVE_INFINITY) {
					break;
				}
			}
		}
		// 头部转回前方
		this.body.turnHead(RobotBody.HeadSpeed.FastTurnSpeed, Integer.MIN_VALUE, 0, true);
		// 转向规避方向
		this.body.turn(RobotBody.Speed.RunSpeed.value, angle, true);
		while (this.isControlling() && this.body.isMoving()) {
			Thread.yield();
		}
		
		this.body.stop(false);
	}

}
