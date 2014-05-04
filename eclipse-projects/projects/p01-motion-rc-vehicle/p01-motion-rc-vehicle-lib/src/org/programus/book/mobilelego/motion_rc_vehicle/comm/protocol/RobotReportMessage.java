package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	private int speed;
	private int rotateSpeed;
	private int distance;
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public int getRotateSpeed() {
		return rotateSpeed;
	}
	public void setRotateSpeed(int rotateSpeed) {
		this.rotateSpeed = rotateSpeed;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "RobotReportMessage [speed=" + speed + ", rotateSpeed="
				+ rotateSpeed + ", distance=" + distance + "]";
	}
}
