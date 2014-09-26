package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.util.Arrays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

/**
 * 存储交通信号的类
 * @author programus
 *
 */
public class TrafficSign {
	public final static byte SIGN_EDGE_LEN = 20;
	
	private short[] data;
	private int size;
	private float[] points;
	
	private Paint paint = new Paint();
	
	public TrafficSign(short... data) {
		this();
		System.arraycopy(data, 0, this.data, 0, this.data.length);
	}
	
	public TrafficSign() {
		this.reset();
	}
	
	public void reset() {
		int len = SIGN_EDGE_LEN * SIGN_EDGE_LEN;
		if (this.data == null || this.data.length < len) {
			this.data = new short[len];
			this.points = new float[len << 1];
		} else {
			Arrays.fill(data, (short)0xffff);
		}
		this.size = 0;
	}
	
	public void addPoint(int x, int y) {
		this.points[size << 1] = x;
		this.points[(size << 1) + 1] = y;
		this.data[size++] = (short)((x << 8) | y);
	}
	
	public void draw(Canvas canvas, boolean disabled) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.BLACK);
		canvas.drawColor(Color.WHITE);
		canvas.save();
		canvas.scale((float)canvas.getWidth() / SIGN_EDGE_LEN, (float)canvas.getHeight() / SIGN_EDGE_LEN);
		canvas.drawPoints(points, 0, size << 1, paint);
		canvas.restore();
		if (disabled) {
			canvas.drawColor(0xff7f0000, PorterDuff.Mode.DARKEN);
		}
		paint.setColor(Color.GREEN);
		canvas.drawRect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1, paint);
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof TrafficSign) {
			TrafficSign sign = (TrafficSign) obj;
			result = sign.size == this.size && Arrays.equals(this.data, sign.data);
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < this.size; i++) {
			hash = (hash << 5) - hash + this.data[i];
		}
		
		return hash;
	}
}
