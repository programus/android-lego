package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import lejos.robotics.subsumption.Behavior;

import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.robot.RobotParam;

/**
 * 抽象行为方法。对Behavior进行了适当的封装。
 * @author programus
 *
 */
public abstract class AbstractBehavior implements Behavior {
	/** 为所有行为类准备机器人躯体，方便调用 */
	protected final RobotBody body = RobotBody.getInstance();
	/** 为所有行为类准备机器人参数，方便调用 */
	protected final RobotParam param = body.getParam();
	/** 存储此行为是否仍有控制权 */
	private boolean controlling;
	
	/**
	 * 返回此行为是否仍有控制权
	 * @return 有控制权时返回true
	 */
	protected boolean isControlling() {
		return this.controlling;
	}

	/**
	 * 对行动方法进行封装，默认取得控制权
	 */
	@Override
	public void action() {
		this.controlling = true;
		this.body.presentMood();
		System.out.printf("=== %s got control ===\n", this.getClass().getSimpleName());
		this.move();
		System.out.printf("=== %s completed move ===\n", this.getClass().getSimpleName());
	}

	/**
	 * 对压制控制权方法进行封装，标记此行为得到压制
	 */
	@Override
	public void suppress() {
		System.out.printf("=== Suppressed %s ===\n", this.getClass().getSimpleName());
		this.controlling = false;
	}
	
	/**
	 * 子类中需要实现的行动方法，其中需写清楚机器人的行为。
	 */
	public abstract void move();
}
