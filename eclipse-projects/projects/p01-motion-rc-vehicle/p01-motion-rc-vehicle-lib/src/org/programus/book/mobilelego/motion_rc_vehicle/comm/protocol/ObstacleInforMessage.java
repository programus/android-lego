package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class ObstacleInforMessage implements NetMessage {
	private static final long serialVersionUID = 5173579547303936055L;
	
	public static enum Type {
		Safe((short)800),
		Warning((short)400),
		Danger((short)100),
		Unknown((short)0);
		
		private final short value;
		Type(short mm) {
			this.value = mm;
		}
	}

	private Type type;
	private int distance;
	public Type getType() {
		if (this.distance < Type.Unknown.value) {
			type = Type.Unknown;
		} else if (this.distance < Type.Danger.value) {
			type = Type.Danger;
		} else if (this.distance < Type.Warning.value) {
			type = Type.Warning;
		} else {
			type = Type.Safe;
		}
		return type;
	}
	public int getDistance() {
		return distance;
	}
	public float getFloatDistanceInMm() {
		float result = this.distance;
		switch (this.distance) {
		case -1:
			result = Float.POSITIVE_INFINITY;
			break;
		case -2:
			result = Float.NEGATIVE_INFINITY;
			break;
		case -3:
			result = Float.NaN;
			break;
		}
		return result;
	}
	public void setDistance(float distance) {
		if (Float.POSITIVE_INFINITY == distance) {
			this.distance = -1;
		} else if (Float.NEGATIVE_INFINITY == distance) {
			this.distance = -2;
		} else if (Float.isNaN(distance)) {
			this.distance = -3;
		} else {
			this.distance = (int) (distance * 1000);
		}
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "ObstacleInforMessage [type=" + this.getType() + ", distance=" + distance
				+ "]";
	}
}
