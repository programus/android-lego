package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	private double speed;
	private double rotationalSpeed;
	private double distance;
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getRotationalSpeed() {
		return rotationalSpeed;
	}
	public void setRotationalSpeed(double rotationalSpeed) {
		this.rotationalSpeed = rotationalSpeed;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "RobotReportMessage [speed=" + speed + ", rotationalSpeed="
				+ rotationalSpeed + ", distance=" + distance + "]";
	}
}
