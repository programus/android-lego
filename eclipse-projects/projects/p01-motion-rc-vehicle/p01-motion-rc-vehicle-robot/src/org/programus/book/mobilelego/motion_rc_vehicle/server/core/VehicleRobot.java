package org.programus.book.mobilelego.motion_rc_vehicle.server.core;

import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * 被遥控的机器人
 * @author programus
 *
 */
public class VehicleRobot {
	/** 车轮直径，用以计算速度和里程，单位：mm */
	private final static short WHEEL_DIAMETER = 36;
	/** 角速度比率 */
	private final static short ANGULAR_RATE = 5;
	
	/** 圆周率近似值 */
	private final static int PI = 3;

	private BaseRegulatedMotor[] wheelMotors = {
			// 左轮马达
			new EV3LargeRegulatedMotor(MotorPort.B),
			// 右轮马达
			new EV3LargeRegulatedMotor(MotorPort.C)
	}; 
	
	private EV3UltrasonicSensor distanceSensor; 
	/** 超声传感器取样器 */
	private SampleProvider distanceProvider; 
	
	/** 马达转速上限 */
	private int speedLimit;
	/** 前次转角值 */
	private int prevTachoCount;
	/** 里程，单位：mm */
	private int distance;
	/** 速度，单位：mm/s */
	private int speed;
	
	private static VehicleRobot inst = new VehicleRobot();
	
	private VehicleRobot() {
		this.distanceSensor = new EV3UltrasonicSensor(SensorPort.S3);
		this.distanceProvider = this.distanceSensor.getDistanceMode();
		EV3 ev3 = LocalEV3.get();
		speedLimit = (int) (ev3.getPower().getVoltage() * 100);
	}
	
	public static VehicleRobot getInstance() {
		return inst;
	}
	
	/**
	 * 取得左右轮的转角总和
	 * @return 左右轮的转角总和
	 */
	private int getTotalTachoCount() {
		int sum = 0;
		for (RegulatedMotor motor: this.wheelMotors) {
			sum += motor.getTachoCount();
		}
		return sum;
	}
	
	private int getDistanceFromTotalTachoCount(int tachoCount) {
		return tachoCount * WHEEL_DIAMETER * PI / 720;
	}
	
	private synchronized void updateDistance() {
		int tachoCount = getTotalTachoCount();
		this.distance += this.getDistanceFromTotalTachoCount(Math.abs(tachoCount - prevTachoCount));
		prevTachoCount = tachoCount;
	}
	
	/**
	 * 调整引擎角速度，保证引擎角速度不超过上限
	 * @param speed 引擎角速度
	 * @return 调整后角速度
	 */
	private int adjustSpeed(int speed) {
		if (Math.abs(speed) > speedLimit) {
			speed = speed > 0 ? speedLimit : -speedLimit;
		}
		return speed;
	}
	
	/**
	 * 取得数字符号。正数返回1，负数返回-1，0返回0
	 * @param num 数字
	 * @return 正数返回1，负数返回-1，0返回0
	 */
	private int signum(int num) {
		return num == 0 ? 0 : (num > 0) ? 1 : -1;
	}

	/**
	 * 机器人前进
	 * @param speed 前进时的平均引擎角速度，为负数时机器人后退
	 * @param angle 机器人转弯角度，数值来自遥控手机，单位为度
	 */
	public void forward(int speed, int angle) {
		if (signum(speed) != signum(this.speed)) {
			// 若驶向相反方向，则更新距离值，
			// 以免距离值被反向运动中和
			updateDistance();
		}
		
		// 保证速度不超过上限
		speed = adjustSpeed(speed);
		
		// 计算转弯时的两轮角速度与平均角速度之间的差
		int dv = angle * ANGULAR_RATE;
		if (speed < 0) {
			// 倒车时，差值取相反值
			dv = -dv;
		}
		// 粗求两轮引擎的角速度，结果可能超过引擎速度上限
		int[] speeds = { speed + dv, speed - dv};
		// 循环计算两轮引擎实际角速度
		for (int i = 0; i < speeds.length; i++) {
			int x = speeds[i];
			int adv = Math.abs(dv) << 1;
			if (Math.abs(x) > speedLimit) {
				// 如果粗算角速度值超过速度上限
				// 当前引擎速度设为上限
				speeds[i] = x > 0 ? speedLimit : -speedLimit;
				// 另外一个引擎的速度做出相应处理
				speeds[(~i) & 0x01] = x > 0 ? speedLimit - adv : -speedLimit + adv;
				break;
			}
		}

		// 将计算出的速度设置到马达上
		for (int i = 0; i < wheelMotors.length; i++) {
			BaseRegulatedMotor motor = wheelMotors[i];
			int sp = speeds[i];
			motor.setSpeed(sp);
			int currSpeed = motor.getRotationSpeed();
			// 仅在速度方向发生改变时，重新调用forward()或backward()方法
			if (sp > 0 && currSpeed <= 0) {
				motor.forward();
			} else if (sp < 0 && currSpeed >= 0){
				motor.backward();
			}
		}
	}
	
	public void backword(int speed, int angle) {
		this.forward(-speed, angle);
	}
	
	public void flt() {
		for (BaseRegulatedMotor motor : wheelMotors) {
			motor.flt(true);
		}
	}
	
	public void stop() {
		for (BaseRegulatedMotor motor : wheelMotors) {
			motor.stop(true);
		}
	}
	
	/**
	 * 根据马达角速度计算速度值
	 * @param rotationSpeed 马达角速度
	 * @return 机器人行进速度值
	 */
	private short getSpeed(short rotationSpeed) {
		return (short) (rotationSpeed * PI * WHEEL_DIAMETER / 360);
	}
	
	/**
	 * 返回机器人行进速度值，负值代表后退。单位：mm/s
	 * @return 机器人行进速度值
	 */
	public short getSpeed() {
		return this.getSpeed(this.getRotationSpeed());
	}
	
	/**
	 * 返回机器人的最大行进速度，数值由马达最大角速度计算而来。单位：mm/s
	 * @return 机器人的最大行进速度
	 */
	public short getMaxSpeed() {
		return this.getSpeed(this.getMaxRotationSpeed());
	}
	
	/**
	 * 返回引擎平均角速度。单位：度/s
	 * @return 引擎平均角速度
	 */
	public short getRotationSpeed() {
		return (short)((wheelMotors[0].getRotationSpeed() + wheelMotors[1].getRotationSpeed()) >> 1);
	}
	
	/**
	 * 返回最大引擎角速度，数值取自马达速度上限。单位：度/s
	 * @return 最大引擎角速度
	 */
	public short getMaxRotationSpeed() {
		return (short)(speedLimit);
	}
	
	/**
	 * 返回总里程，从本次启动程序开始计算。单位：mm
	 * @return 总里程
	 */
	public int getDistance() {
		this.updateDistance();
		return distance;
	}
	
	/**
	 * 返回障碍物距离。单位：m
	 * @return 障碍物距离
	 */
	public float getObstacleDistance() {
		float[] samples = new float[this.distanceProvider.sampleSize()];
		this.distanceProvider.fetchSample(samples, 0);
		return samples[0];
	}
	
	/**
	 * 释放机器人所用资源。
	 */
	public void release() {
		for (BaseRegulatedMotor motor : this.wheelMotors) {
			motor.close();
		}
		this.distanceSensor.close();
	}
}
