package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class ObstacleInforMessage implements NetMessage {
	private static final long serialVersionUID = 5173579547303936055L;
	
	public static enum Type {
		Safe,
		Warning,
		Danger
	}

	private Type type;
	private float distance;
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "ObstacleInforMessage [type=" + type + ", distance=" + distance
				+ "]";
	}
}
