package org.programus.book.mobilelego.research.communication.protocol;

public class MotorReportMessage implements NetMessage {
	private static final long serialVersionUID = -8702695106516789834L;
	
	private int tachoCount;
	private int speed;

	public int getTachoCount() {
		return tachoCount;
	}

	public void setTachoCount(int tachoCount) {
		this.tachoCount = tachoCount;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "MotorReportMessage [tachoCount=" + tachoCount + ", speed="
				+ speed + "]";
	}
}
