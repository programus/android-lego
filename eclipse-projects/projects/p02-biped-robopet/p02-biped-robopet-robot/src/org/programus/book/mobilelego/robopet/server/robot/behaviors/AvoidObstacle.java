package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;

public class AvoidObstacle extends AbstractBehavior {
	private static int HEALTH_CONSUME = 1;

	@Override
	public boolean takeControl() {
		return this.body.isObstacleNear();
	}

	@Override
	public void move() {
		System.out.println("Begin avoid obstacle");
		this.param.annoy();
		this.param.sadden(true);
		this.body.stop(false);
		this.param.setHealthConsume(HEALTH_CONSUME);
		this.body.turnHead(RobotBody.HeadSpeed.ScanSpeed, -90, 90, true);
		float maxDistance = 0;
		int angle = -90;
		while (this.body.isHeadTurning()) {
			float distance = this.body.getObstacleDistance();
			int currAngle = this.body.getHeadTurnAngle();
			System.out.printf("%d: %.2f/%.2f\n", currAngle, distance, maxDistance);
			if (distance > maxDistance) {
				maxDistance = distance;
				angle = currAngle;
				if (maxDistance == Float.POSITIVE_INFINITY) {
					break;
				}
			}
		}
		this.body.turnHead(RobotBody.HeadSpeed.FastTurnSpeed, Integer.MIN_VALUE, 0, true);
		this.body.turn(RobotBody.Speed.RunSpeed.value, angle, false);
		
		this.body.stop(false);
	}

}
