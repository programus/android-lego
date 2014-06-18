package org.programus.book.mobilelego.robopet.server.robot;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class RobotBody {
	private static final int HEAD_ROTATE_RANGE = 180;
	EV3LargeRegulatedMotor[] motors = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C),
	};
	
	EV3MediumRegulatedMotor headMotor = new EV3MediumRegulatedMotor(MotorPort.A);
	
	EV3UltrasonicSensor headSensor = new EV3UltrasonicSensor(SensorPort.S3);
	EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	
	private static RobotBody instance = new RobotBody();
	private RobotBody() {
	}
	
	public static RobotBody getInstance() {
		return instance;
	}
	
	public void calibrateHead() {
		SampleProvider light = colorSensor.getRedMode();
		float[] sample = new float[light.sampleSize()];
		int range = HEAD_ROTATE_RANGE;
		headMotor.setSpeed(500);
		headMotor.rotateTo(-(range >> 1), false);
		headMotor.resetTachoCount();
		byte maxBrightness = 0;
		byte prev = 0;
		int startAngle = 0;
		int endAngle = 0;
		headMotor.setSpeed(70);
		headMotor.rotateTo(range, true);
		while (headMotor.isMoving()) {
			light.fetchSample(sample, 0);
			int angle = headMotor.getTachoCount();
			byte brightness = (byte) (sample[0] * 100);
			if (brightness > maxBrightness) {
				maxBrightness = brightness;
				startAngle = angle;
			} else if (prev == maxBrightness && brightness < prev) {
				endAngle = angle;
			} else if (brightness < maxBrightness - 8) {
				break;
			}
			System.out.printf("%d: %d\n", angle, brightness);
			prev = brightness;
		}
		headMotor.setSpeed(200);
		headMotor.rotateTo(((startAngle + endAngle) >> 1), false);
		headMotor.flt();
		headMotor.resetTachoCount();
		colorSensor.setFloodlight(false);
	}
}
