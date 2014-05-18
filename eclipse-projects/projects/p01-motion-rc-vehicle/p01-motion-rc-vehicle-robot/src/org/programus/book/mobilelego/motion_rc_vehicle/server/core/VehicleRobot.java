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

public class VehicleRobot {
	private final static short WHEEL_DIAMETER = 36;
	private final static short AXLE_TRACK = 110;
	
	private final static int PI = 3;

	private BaseRegulatedMotor[] wheelMotors = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C)
	}; 
	
	private EV3UltrasonicSensor distanceSensor; 
	private SampleProvider distanceProvider; 
	
	private int speedLimit;
	private int prevTachoCount;
	private int distance;
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
	
	private void updateDistance() {
		int tachoCount = getTotalTachoCount();
		this.distance += this.getDistanceFromTotalTachoCount(Math.abs(tachoCount - prevTachoCount));
		prevTachoCount = tachoCount;
	}
	
	private int adjustSpeed(int speed) {
		if (Math.abs(speed) > speedLimit) {
			speed = speed > 0 ? speedLimit : -speedLimit;
		}
		return speed;
	}
	
	private int signum(int num) {
		return num == 0 ? 0 : (num > 0) ? 1 : -1;
	}

	public void forward(int speed, int angle) {
		long t = System.currentTimeMillis();
		if (signum(speed) != signum(this.speed)) {
			updateDistance();
		}
		
		speed = adjustSpeed(speed);
		
		int dv = angle * AXLE_TRACK / WHEEL_DIAMETER * signum(speed);
		int baseSpeed = speed;
		int[] speeds = { baseSpeed + dv, baseSpeed - dv};
		for (int i = 0; i < speeds.length; i++) {
			int x = speeds[i];
			int adv = Math.abs(dv);
			if (Math.abs(x) > speedLimit) {
				speeds[i] = x > 0 ? speedLimit : -speedLimit;
				speeds[(~i) & 0x01] = x > 0 ? speedLimit - adv : -speedLimit + adv;
			}
		}

		for (int i = 0; i < wheelMotors.length; i++) {
			BaseRegulatedMotor motor = wheelMotors[i];
			int sp = speeds[i];
			motor.setSpeed(sp);
			int currSpeed = motor.getRotationSpeed();
			if (sp > 0 && currSpeed <= 0) {
				motor.forward();
			} else if (sp < 0 && currSpeed >= 0){
				motor.backward();
			}
		}
		System.out.println(System.currentTimeMillis() - t);
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
	
	private short getSpeed(short rotationSpeed) {
		return (short) (rotationSpeed * PI * WHEEL_DIAMETER / 360);
	}
	
	public short getSpeed() {
		return this.getSpeed(this.getRotationSpeed());
	}
	
	public short getMaxSpeed() {
		return this.getSpeed(this.getMaxRotationSpeed());
	}
	
	public short getRotationSpeed() {
		return (short)((wheelMotors[0].getRotationSpeed() + wheelMotors[1].getRotationSpeed()) >> 1);
	}
	
	public short getMaxRotationSpeed() {
		return (short)(speedLimit);
	}
	
	public int getDistance() {
		this.updateDistance();
		return distance;
	}
	
	public float getObstacleDistance() {
		float[] samples = new float[this.distanceProvider.sampleSize()];
		this.distanceProvider.fetchSample(samples, 0);
		return samples[0];
	}
	
	public void release() {
		for (BaseRegulatedMotor motor : this.wheelMotors) {
			motor.close();
		}
		this.distanceSensor.close();
	}
}
