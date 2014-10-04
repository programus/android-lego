package org.programus.book.mobilelego.trafficsign.server.robot;

import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

/**
 * 小车
 * @author programus
 *
 */
public class Car {
	/** 角速度比率 */
	private final static short ANGULAR_RATE = 3;
	
	private int speed;
	
	private BaseRegulatedMotor[] wheelMotors = {
			// 左轮马达
			new EV3LargeRegulatedMotor(MotorPort.B),
			// 右轮马达
			new EV3LargeRegulatedMotor(MotorPort.C)
	}; 
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public int getSpeed() {
		return this.speed;
	}
	
	public void forward() {
		for (RegulatedMotor m : this.wheelMotors) {
			m.setSpeed(speed);
			m.forward();
		}
	}
	
	public void backward() {
		for (RegulatedMotor m : this.wheelMotors) {
			m.setSpeed(speed);
			m.backward();
		}
	}
	
	public void stop() {
		for (RegulatedMotor m : this.wheelMotors) {
			m.stop(false);
		}
	}
	
	public void turn(int angle, boolean immediateReturn) {
		this.stop();
		int ra = angle * ANGULAR_RATE;
		for (RegulatedMotor m : this.wheelMotors) {
			m.rotate(ra, true);
			ra = -ra;
		}
		while (!immediateReturn && this.isMoving()) {
			Thread.yield();
		}
	}
	
	public boolean isMoving() {
		for (RegulatedMotor m : this.wheelMotors) {
			if (m.isMoving()) {
				return true;
			}
		}
		return false;
	}
	
	public void close() {
		for (RegulatedMotor m : this.wheelMotors) {
			m.close();
		}
	}
}
