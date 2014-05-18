package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	private short speed;
	private short rotationSpeed;
	private int distance;
	public short getSpeed() {
		return speed;
	}
	public void setSpeed(short speed) {
		this.speed = speed;
	}
	public short getRotationSpeed() {
		return rotationSpeed;
	}
	public void setRotationSpeed(short rotationSpeed) {
		this.rotationSpeed = rotationSpeed;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	public boolean isSameAs(RobotReportMessage msg) {
		return this.speed == msg.speed && this.rotationSpeed == msg.rotationSpeed && this.distance == msg.distance;
	}
	@Override
	public String toString() {
		return "RobotReportMessage [speed=" + speed + ", rotationSpeed="
				+ rotationSpeed + ", distance=" + distance + "]";
	}
}
