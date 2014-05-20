package org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol;

/**
 * 障碍物信息消息
 * 用以向遥控手机端通报障碍物信息
 * @author programus
 *
 */
public class ObstacleInforMessage implements NetMessage {
	private static final long serialVersionUID = 5173579547303936055L;
	
	public static enum Type {
		Safe((short)800),
		Warning((short)400),
		Danger((short)200),
		Unknown((short)0);
		
		private final short value;
		Type(short mm) {
			this.value = mm;
		}
	}

	private Type type;
	/** 障碍物距离，单位：mm */
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

	/**
	 * 取得浮点类型距离值
	 * @return 距离值，单位：mm
	 */
	public float getFloatDistanceInMm() {
		float result = this.distance;
		// 对非常规数值进行转换，与setDistance(float)中的处理对应
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
			// 正无穷大，转为-1
			this.distance = -1;
		} else if (Float.NEGATIVE_INFINITY == distance) {
			// 负无穷大，转为-2
			this.distance = -2;
		} else if (Float.isNaN(distance)) {
			// 非合法数字，转为-3
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
