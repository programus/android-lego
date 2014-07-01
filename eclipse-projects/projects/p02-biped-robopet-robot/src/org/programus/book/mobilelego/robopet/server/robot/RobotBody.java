package org.programus.book.mobilelego.robopet.server.robot;

import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * 机器人的躯体，其中包含机器人的肢体行动，没有任何思维指导。
 * @author programus
 *
 */
public class RobotBody {
	/** 机器人头的转动角度范围 */
	private static final int HEAD_ROTATE_RANGE = 180;
	/** 机器人走一步，马达需要转过的角度 */
	private static final int FULL_STEP = 1800;
	/** 机器人走半步，马达需要转过的角度 */
	private static final int HALF_STEP = FULL_STEP >> 1;
	
	/** 与障碍物之间允许的最小距离 */
	public static final float DISTANCE_LIMIT = 0.3f;
	
	/** 转头的马大速度 */
	public enum HeadSpeed {
		FastTurnSpeed(500),
		ScanSpeed(70),
		SlowTurnSpeed(200);
		
		public final int value;
		private HeadSpeed(int value) {
			this.value = value;
		}
	}
	
	/** 步行的马达速度 */
	public enum Speed {
		WalkSpeed(600),
		AlignSpeed(480),
		RunSpeed(800);
		
		public final int value;
		private Speed(int value) {
			this.value = value;
		}
	}
	
	/** 双腿的左右边 */
	public enum Side {
		Left, 
		Right,
	}
	
	/** 双腿的马达 */
	private final EV3LargeRegulatedMotor[] legs = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C),
	};
	
	/** 头颈的马达 */
	private final EV3MediumRegulatedMotor headMotor = new EV3MediumRegulatedMotor(MotorPort.A);
	/** 取障碍物距离用的采样器 */
	private final SampleProvider distanceProvider;
	/** 头部距离传感器 */
	private final EV3UltrasonicSensor headSensor = new EV3UltrasonicSensor(SensorPort.S3);
	/** 头部校对用的颜色传感器 */
	private final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	
	/** 单例模式的实例 */
	private static RobotBody instance = new RobotBody();
	/** 单例模式，构造函数为私有 */
	private RobotBody() {
		this.distanceProvider = headSensor.getDistanceMode();
		for (BaseRegulatedMotor m : legs) {
			m.resetTachoCount();
		}
	}
	
	/** 单例模式，取得实例的函数 */
	public static RobotBody getInstance() {
		return instance;
	}
	
	/**
	 * 矫正头部方位
	 */
	public void calibrateHead() {
		// 开启颜色传感器的反光检测模式
		SampleProvider light = colorSensor.getRedMode();
		// 预备取样容器变量
		float[] sample = new float[light.sampleSize()];
		int range = HEAD_ROTATE_RANGE;
		// 头部反向转动
		headMotor.setSpeed(HeadSpeed.FastTurnSpeed.value);
		headMotor.rotateTo(-(range >> 1), false);
		headMotor.resetTachoCount();
		
		byte maxBrightness = 0;
		byte prev = 0;
		int startAngle = 0;
		int endAngle = 0;
		// 转动头部
		headMotor.setSpeed(HeadSpeed.ScanSpeed.value);
		headMotor.rotateTo(range, true);
		while (headMotor.isMoving()) {
			// 头部转动时，不断采集颜色传感器数值
			light.fetchSample(sample, 0);
			int angle = headMotor.getTachoCount();
			byte brightness = (byte) (sample[0] * 100);
			if (brightness > maxBrightness) {
				// 计算目前为止的最大值
				maxBrightness = brightness;
				// 亮度值在上升，覆盖记录进入角度
				startAngle = angle;
			} else if (prev == maxBrightness && brightness < prev) {
				// 当前亮度小于前次亮度，且前次亮度是最大值时，
				// 说明头部已经转过颜色传感器，记录离开角度
				endAngle = angle;
			} else if (brightness < maxBrightness - 8) {
				// 亮度明显低于最大亮度，则头部早已完全转过。
				break;
			}
			// 记录当前亮度值为下次循环中的前次亮度
			prev = brightness;
		}
		headMotor.setSpeed(HeadSpeed.SlowTurnSpeed.value);
		// 转至进入/离开颜色传感器区的中心点，作为前方角度。
		headMotor.rotateTo(((startAngle + endAngle) >> 1), false);
		headMotor.resetTachoCount();
		colorSensor.setFloodlight(false);
	}
	
	/**
	 * 判断一侧腿是否回到初始位置
	 * @param side 
	 * @return 当腿在初始位置时返回true
	 */
	public boolean isLegAligned(Side side) {
		return this.legs[side.ordinal()].getTachoCount() % FULL_STEP == 0;
	}
	
	/**
	 * 判断双腿是否都在初始位置
	 * @return 当双腿都在初始位置时返回true
	 */
	public boolean isLegsAligned() {
		return this.isLegAligned(Side.Left) && this.isLegAligned(Side.Right);
	}
	
	/**
	 * 重置单侧腿，使其回到初始位置
	 * @param side
	 */
	private void realignLeg(Side side) {
		BaseRegulatedMotor motor = this.legs[side.ordinal()];
		int current = motor.getTachoCount();
		int delta = current % FULL_STEP;
		int target = current - delta + ((Math.abs(delta) < HALF_STEP) ? 0 : delta > 0 ? FULL_STEP : -FULL_STEP);
		motor.rotateTo(target, true);
	}
	
	/**
	 * 重置双腿，使其回到初始位置
	 * @param immediateReturn true时不等待双腿归位，函数立即返回
	 */
	private void realignLegs(boolean immediateReturn) {
		for (Side side : Side.values()) {
			this.realignLeg(side);
		}
		if (!immediateReturn) {
			for (Side side : Side.values()) {
				this.legs[side.ordinal()].waitComplete();
			}
		}
	}
	
	/**
	 * 重置双腿，使其回到初始位置，并等待双腿归位完毕
	 */
	private void realignLegs() {
		this.realignLegs(false);
	}
	
	/**
	 * 前进
	 * @param speed 马达速度
	 */
	public void forward(int speed) {
		this.realignLegs();
		for (Side side : Side.values()) {
			RegulatedMotor m = this.legs[side.ordinal()];
			m.setSpeed(speed);
			m.forward();
		}
	}
	
	/**
	 * 后退
	 * @param speed 马达速度
	 */
	public void backward(int speed) {
		this.realignLegs();
		for (Side side : Side.values()) {
			RegulatedMotor m = this.legs[side.ordinal()];
			m.setSpeed(speed);
			m.backward();
		}
	}
	
	/**
	 * 停止，并重置双腿位置
	 * @param immediateReturn true时不等待双腿重置，立即返回
	 */
	public void stop(boolean immediateReturn) {
		this.realignLegs(immediateReturn);
	}
	
	/**
	 * 转向
	 * @param speed 主马达速度
	 * @param side 转向方向
	 */
	public void turn(int speed, Side side) {
		this.realignLegs();
		for (Side s : Side.values()) {
			RegulatedMotor m = this.legs[s.ordinal()];
			m.setSpeed(s.ordinal() == side.ordinal() ? speed * 9 / 10 : speed);
			m.forward();
		}
	}
	
	/**
	 * 取得主马达速度
	 * @return 主马达速度
	 */
	public int getSpeed() {
		return Math.max(this.legs[Side.Left.ordinal()].getSpeed(), this.legs[Side.Right.ordinal()].getSpeed());
	}
	
	/**
	 * 取得障碍物距离
	 * @return 障碍物距离
	 */
	public float getObstacleDistance() {
		float[] samples = new float[this.distanceProvider.sampleSize()];
		this.distanceProvider.fetchSample(samples, 0);
		return samples[0];
	}
	
	/**
	 * 检测是否接近障碍物
	 * @return true则为已接近障碍物
	 */
	public boolean isObstacleNear() {
		return this.getObstacleDistance() < DISTANCE_LIMIT;
	}
	
	/**
	 * 限制头部转角
	 * @param angle 原始头部转角
	 * @return 限制修正后的转角
	 */
	private int limitAngle(int angle) {
		if (angle < -90) {
			angle = -90;
		}
		if (angle > 90) {
			angle = 90;
		}
		return angle;
	}
	
	/**
	 * 转头
	 * @param speed 转头速度
	 * @param fromAngle 转头开始角度，如果开始角度为Integer.MAX_VALUE或Integer.MIN_VALUE，则已当前角度为初始角度，否则会首先快速转至初始角度
	 * @param toAngle 转头结束角度
	 * @param immediateReturn true时，立即返回，不等待转头结束
	 */
	public void turnHead(HeadSpeed speed, int fromAngle, int toAngle, boolean immediateReturn) {
		if (fromAngle != Integer.MAX_VALUE && fromAngle != Integer.MIN_VALUE) {
			this.headMotor.setSpeed(HeadSpeed.FastTurnSpeed.value);
			this.headMotor.rotateTo(limitAngle(fromAngle), false);
		}
		this.headMotor.setSpeed(speed.value);
		this.headMotor.rotateTo(limitAngle(toAngle), immediateReturn);
	}
	
	/**
	 * 检测头部是否仍在转动
	 * @return 头部转动时返回true
	 */
	public boolean isHeadTurning() {
		return this.headMotor.isMoving();
	}
	
	/**
	 * 取得头部转角
	 * @return
	 */
	public int getHeadTurnAngle() {
		return this.headMotor.getTachoCount();
	}
	
	/**
	 * 打开/关闭眼睛灯光
	 * @param on true - 打开；false - 关闭
	 */
	public void turnEyeLight(boolean on) {
		if (on) {
			this.headSensor.enable();
		} else {
			this.headSensor.disable();
		}
	}
}
