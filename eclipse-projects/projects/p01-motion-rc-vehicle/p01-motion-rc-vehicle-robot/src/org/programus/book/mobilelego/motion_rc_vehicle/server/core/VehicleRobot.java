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
import lejos.robotics.filter.MeanFilter;

public class VehicleRobot {
	private final static float WHEEL_DIAMETER = 0.036f;
	private final static float AXLE_TRACK = 0.11f;

	private BaseRegulatedMotor[] wheelMotors = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C)
	}; 
	
	private EV3UltrasonicSensor distanceSensor; 
	private SampleProvider distanceProvider; 
	
	private float speedLimit;
	private double prevTachoCount;
	private double distance;
	private float speed;
	
	private static VehicleRobot inst = new VehicleRobot();
	
	private VehicleRobot() {
		final int SAMPLE_COUNT = 5;
		this.distanceSensor = new EV3UltrasonicSensor(SensorPort.S3);
		this.distanceProvider = new MeanFilter(this.distanceSensor.getMode("Distance"), SAMPLE_COUNT);
		EV3 ev3 = LocalEV3.get();
		speedLimit = ev3.getPower().getVoltage() * 100;
	}
	
	public static VehicleRobot getInstance() {
		return inst;
	}
	
	private double getAverageTachoCount() {
		double avg = 0;
		int t = 0;
		for (RegulatedMotor motor : this.wheelMotors) {
			avg += (motor.getTachoCount() - avg) / ++t;
		}
		return avg;
	}
	
	private void updateDistance() {
		double tachoCount = getAverageTachoCount();
		this.distance += Math.abs(tachoCount - prevTachoCount) * Math.PI * WHEEL_DIAMETER / 360;
		prevTachoCount = tachoCount;
	}
	
	private float adjustSpeed(float speed) {
		if (Math.abs(speed) > speedLimit) {
			speed = speed > 0 ? speedLimit : -speedLimit;
		}
		return speed;
	}

	public void forward(float speed, double angle) {
		if (Math.signum(speed) != Math.signum(this.speed)) {
			updateDistance();
		}
		
		speed = adjustSpeed(speed);
		
		float dv = (float) Math.toDegrees(angle * AXLE_TRACK / WHEEL_DIAMETER);
		float baseSpeed = Math.abs(speed);
		float[] speeds = { baseSpeed + dv, baseSpeed - dv};
		System.out.printf("speed before: %.3f, %.3f/%.3f\n", speed, speeds[0], speeds[1]);
		for (int i = 0; i < speeds.length; i++) {
			float x = speeds[i];
			float adv = Math.abs(dv);
			if (Math.abs(x) > speedLimit) {
				speeds[i] = x > 0 ? speedLimit : -speedLimit;
				speeds[~i] = x > 0 ? speedLimit - adv : -speedLimit + adv;
			}
		}
		System.out.printf("speed after: %.3f, %.3f/%.3f\n", speed, speeds[0], speeds[1]);

		for (int i = 0; i < wheelMotors.length; i++) {
			BaseRegulatedMotor motor = wheelMotors[i];
			float sp = speeds[i];
			motor.setSpeed(sp);
			int currSpeed = motor.getRotationSpeed();
			System.out.printf("motor[%d] - curr: %d, set: %.1f\n", i, currSpeed, sp);
			if (sp > 0 && currSpeed <= 0) {
				motor.forward();
			} else if (sp < 0 && currSpeed >= 0){
				motor.backward();
			}
		}
	}
	
	public void backword(float speed, double angle) {
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
	
	private double getSpeed(double rotationSpeed) {
		return rotationSpeed * Math.PI * WHEEL_DIAMETER;
	}
	
	public double getSpeed() {
		return this.getSpeed(this.getRotationalSpeed());
	}
	
	public double getMaxSpeed() {
		return this.getSpeed(this.getMaxRotationSpeed());
	}
	
	public double getRotationalSpeed() {
		return (wheelMotors[0].getRotationSpeed() + wheelMotors[1].getRotationSpeed()) / 2. / 360;
	}
	
	public double getMaxRotationSpeed() {
		return speedLimit / 360;
	}
	
	public double getDistance() {
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
