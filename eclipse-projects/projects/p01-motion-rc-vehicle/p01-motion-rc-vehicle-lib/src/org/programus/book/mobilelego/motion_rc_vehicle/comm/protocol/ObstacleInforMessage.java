package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class ObstacleInforMessage implements NetMessage {
	private static final long serialVersionUID = 5173579547303936055L;
	
	public static enum Type {
		Safe(800),
		Warning(400),
		Danger(100);
		
		private final int value;
		Type(int mm) {
			this.value = mm;
		}
	}

	private Type type;
	private float distance;
	public Type getType() {
		if (this.distance < Type.Danger.value) {
			type = Type.Danger;
		} else if (this.distance < Type.Warning.value) {
			type = Type.Warning;
		} else {
			type = Type.Safe;
		}
		return type;
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
