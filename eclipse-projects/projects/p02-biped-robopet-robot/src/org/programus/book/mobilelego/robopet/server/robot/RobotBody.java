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

public class RobotBody {
	private static final int HEAD_ROTATE_RANGE = 180;
	private static final int FULL_STEP = 1800;
	private static final int HALF_STEP = FULL_STEP >> 1;
	
	public enum Speed {
		WalkSpeed(600),
		AlignSpeed(480),
		RunSpeed(800);
		
		public final int value;
		private Speed(int value) {
			this.value = value;
		}
	}
	
	public enum Side {
		Left, 
		Right,
	}
	
	private EV3LargeRegulatedMotor[] legs = {
			new EV3LargeRegulatedMotor(MotorPort.B),
			new EV3LargeRegulatedMotor(MotorPort.C),
	};
	
	private EV3MediumRegulatedMotor headMotor = new EV3MediumRegulatedMotor(MotorPort.A);
	
	private EV3UltrasonicSensor headSensor = new EV3UltrasonicSensor(SensorPort.S3);
	private EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	
	private static RobotBody instance = new RobotBody();
	private RobotBody() {
		for (BaseRegulatedMotor m : legs) {
			m.resetTachoCount();
		}
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
	
	public boolean isLegAligned(Side side) {
		return this.legs[side.ordinal()].getTachoCount() % FULL_STEP == 0;
	}
	
	public boolean isLegsAligned() {
		return this.isLegAligned(Side.Left) && this.isLegAligned(Side.Right);
	}
	
	private void realignLeg(Side side) {
		BaseRegulatedMotor motor = this.legs[side.ordinal()];
		int current = motor.getTachoCount();
		int delta = current % FULL_STEP;
		int target = current - delta + ((Math.abs(delta) < HALF_STEP) ? 0 : delta > 0 ? FULL_STEP : -FULL_STEP);
		motor.rotateTo(target, true);
	}
	
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
	
	private void realignLegs() {
		this.realignLegs(false);
	}
	
	public void forward(int speed) {
		this.realignLegs();
		for (Side side : Side.values()) {
			RegulatedMotor m = this.legs[side.ordinal()];
			m.setSpeed(speed);
			m.forward();
		}
	}
	
	public void backward(int speed) {
		this.realignLegs();
		for (Side side : Side.values()) {
			RegulatedMotor m = this.legs[side.ordinal()];
			m.setSpeed(speed);
			m.backward();
		}
	}
	
	public void stop(boolean immediateReturn) {
		this.realignLegs(immediateReturn);
	}
	
	public void turn(int speed, Side side) {
		this.realignLegs();
		for (Side s : Side.values()) {
			RegulatedMotor m = this.legs[s.ordinal()];
			m.setSpeed(s.ordinal() == side.ordinal() ? speed * 9 / 10 : speed);
			m.forward();
		}
	}
	
	public int getSpeed() {
		return Math.max(this.legs[Side.Left.ordinal()].getSpeed(), this.legs[Side.Right.ordinal()].getSpeed());
	}
}
