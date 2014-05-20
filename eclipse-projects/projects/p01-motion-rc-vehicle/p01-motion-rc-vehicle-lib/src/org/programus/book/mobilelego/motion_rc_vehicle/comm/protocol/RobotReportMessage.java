package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

/**
 * 机器人数据报告消息
 * @author programus
 */
public class RobotReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	/** 机器人行进速度，单位：mm/s */
	private short speed;
	/** 机器人引擎转速，单位：度/s */
	private short rotationSpeed;
	/** 机器人行进总里程，单位：mm 
	 * (里程从每次程序运行时开始重新从零计算) 
	 */
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
