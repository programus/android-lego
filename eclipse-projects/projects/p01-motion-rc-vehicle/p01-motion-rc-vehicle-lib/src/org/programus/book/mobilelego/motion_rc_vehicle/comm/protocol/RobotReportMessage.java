package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	public static final short RATE = 1000;
	
	private short speed;
	private short rotationalSpeed;
	private int distance;
	public double getSpeed() {
		return (double) speed / RATE;
	}
	public void setSpeed(double speed) {
		this.speed = (short) (speed * RATE);
	}
	public double getRotationalSpeed() {
		return (double) rotationalSpeed / RATE;
	}
	public void setRotationalSpeed(double rotationalSpeed) {
		this.rotationalSpeed = (short) (rotationalSpeed * RATE);
	}
	public double getDistance() {
		return (double) distance / RATE;
	}
	public void setDistance(double distance) {
		this.distance = (int) (distance * RATE);
	}
	
	public boolean isSameAs(RobotReportMessage msg) {
		return this.speed == msg.speed && this.rotationalSpeed == msg.rotationalSpeed && this.distance == msg.distance;
	}
	@Override
	public String toString() {
		return "RobotReportMessage [speed=" + speed + ", rotationalSpeed="
				+ rotationalSpeed + ", distance=" + distance + "]";
	}
}
