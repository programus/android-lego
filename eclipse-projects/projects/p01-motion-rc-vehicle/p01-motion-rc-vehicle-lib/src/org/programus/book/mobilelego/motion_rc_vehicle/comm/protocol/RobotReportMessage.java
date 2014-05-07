package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	private double speed;
	private float rotateSpeed;
	private double distance;
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public float getRotateSpeed() {
		return rotateSpeed;
	}
	public void setRotateSpeed(float rotateSpeed) {
		this.rotateSpeed = rotateSpeed;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "RobotReportMessage [speed=" + speed + ", rotateSpeed="
				+ rotateSpeed + ", distance=" + distance + "]";
	}
}
