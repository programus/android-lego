package org.programus.book.mobilelego.motion_rc_vehicle.server.core;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

public class VehicleRobot {
	private RegulatedMotor[] wheelMotors = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C)
	}; 
	
	private SensorModes distanceSensor; 
	private SampleProvider distanceProvider; 
	
	private int distance;
	private float speed;
	
	private static VehicleRobot inst = new VehicleRobot();
	
	private VehicleRobot() {
		final int SAMPLE_COUNT = 5;
		this.distanceSensor = new EV3UltrasonicSensor(SensorPort.S3);
		this.distanceProvider = new MeanFilter(this.distanceSensor.getMode("Distance"), SAMPLE_COUNT);
	}
	
	private float getAverageTachoCount() {
		float avg = 0;
		int t = 0;
		for (RegulatedMotor motor : this.wheelMotors) {
			avg += (motor.getTachoCount() - avg) / ++t;
		}
		return avg;
	}
	
	public static VehicleRobot getInstance() {
		return inst;
	}

	public void forward(float speed, double angle) {
		
	}
	
	public void backword(float speed, double angle) {
		this.forward(-speed, angle);
	}
	
	public int getSpeed() {
		return 0;
	}
	
	public int getRotateSpeed() {
		return 0;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public float getObstacleDistance() {
		float[] samples = new float[this.distanceProvider.sampleSize()];
		this.distanceProvider.fetchSample(samples, 0);
		return samples[0];
	}
}
