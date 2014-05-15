package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

public class ObstacleInforMessage implements NetMessage {
	private static final long serialVersionUID = 5173579547303936055L;
	
	public static enum Type {
		Safe((short)800),
		Warning((short)400),
		Danger((short)100);
		
		private final short value;
		Type(short mm) {
			this.value = mm;
		}
	}

	private Type type;
	private short distance;
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
	public short getDistance() {
		return distance;
	}
	public void setDistance(short distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "ObstacleInforMessage [type=" + type + ", distance=" + distance
				+ "]";
	}
}
