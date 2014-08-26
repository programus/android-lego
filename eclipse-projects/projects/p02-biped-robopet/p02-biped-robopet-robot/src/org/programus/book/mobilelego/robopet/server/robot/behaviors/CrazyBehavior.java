package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.robot.RobotBody.HeadSpeed;
import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 疯狂时的前行模式
 * @author programus
 *
 */
public class CrazyBehavior extends AbstractBehavior {
	private final static int MOVE_TYPE = 4;
	private final static int MAX_MOVE_STEPS = 5;
	private final static int MAX_TURN_ANGLE = 180;
	private final static int MIN_INTERVAL = 500;
	private final static int INTERVAL_INC = 1500;
	
	/** 为疯狂的随机行为准备随机数生成器 */
	private Random rand = new Random();

	@Override
	public boolean takeControl() {
		return this.param.getMood() == RobotParam.Mood.Crazy;
	}

	@Override
	public void move() {
		// 疯狂时会狂奔
		int speed = RobotBody.Speed.RunSpeed.value;
		this.param.setHealthConsume(Math.abs(speed / 100));
		// 疯狂让宠物情绪低落
		this.param.sadden(false);
		// 疯狂时的随机行为
		switch (this.rand.nextInt(MOVE_TYPE)) {
		case 0:
			this.body.forward(speed, this.rand.nextInt(MAX_MOVE_STEPS), true);
			break;
		case 1:
			this.body.backward(speed, this.rand.nextInt(MAX_MOVE_STEPS), true);
			break;
		case 2:
			this.body.turn(speed, this.rand.nextInt(MAX_TURN_ANGLE), true);
			break;
		case 3:
			this.body.turn(speed, -this.rand.nextInt(MAX_TURN_ANGLE), true);
			break;
		}
		// 在另一个线程中不断更新情绪表现（疯狂时的情绪表现就是不稳定的）
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				body.presentMood();
			}
		};
		
		timer.schedule(task, 0, MIN_INTERVAL + rand.nextInt(INTERVAL_INC));
		// 疯狂时会转头
		this.body.turnHead(HeadSpeed.FastTurnSpeed, -90, 90, false);
		this.body.turnHead(HeadSpeed.FastTurnSpeed, -90, 90, false);
		this.body.turnHead(HeadSpeed.FastTurnSpeed, Integer.MAX_VALUE, 0, false);
		while (this.isControlling() && this.body.isMoving() && this.takeControl()) {
			Thread.yield();
		}
		task.cancel();
		timer.purge();
		this.body.stop(false);
	}

}
