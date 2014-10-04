package org.programus.book.mobilelego.trafficsign.mobile.imagerecognition;

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
	/** 交通信号的边长 */
	public final static byte SIGN_EDGE_LEN = 20;
	
	/** 横坐标掩码，由于使用一个short类型存储坐标，后8位为横坐标，所以掩码为0xff */
	private final static int X_MASK = 0xff;
	/** 纵坐标位移数，由于使用一个short类型存储坐标，前八位为纵坐标，所以位移数为8 */
	private final static int Y_SHIFT = 8;
	
	/** 所有黑色点的坐标数据，主要用来比较和生成hash code。 */
	private short[] data;
	/** 所存储的点数 */
	private int size;
	/** 绘图用的点坐标值 */
	private float[] points;
	
	/** 绘图用的Paint对象 */
	private Paint paint = new Paint();
	
	/**
	 * 构造函数
	 * @param data 点数据
	 */
	public TrafficSign(short... data) {
		this();
		this.size = Math.min(data.length, this.data.length);
		System.arraycopy(data, 0, this.data, 0, size);
		int i = 0;
		for (short d : data) {
			this.points[i++] = d & X_MASK;
			this.points[i++] = d >> Y_SHIFT;
			if (i > size << 1) {
				break;
			}
		}
	}
	
	/** 构造函数 */
	public TrafficSign() {
		this.reset();
	}
	
	/** 重置数据 */
	public void reset() {
		int len = SIGN_EDGE_LEN * SIGN_EDGE_LEN;
		if (this.data == null || this.data.length < len) {
			this.data = new short[len];
			this.points = new float[len << 1];
		}
		Arrays.fill(data, (short)0xffff);
		this.size = 0;
	}
	
	/**
	 * 追加新点。目前函数只支持按顺序追加点，且不能追加重复的点。
	 * @param x 新点的横坐标
	 * @param y 新点的纵坐标
	 * @return 成功追加返回true，点数达到上限则返回false
	 */
	public boolean addPoint(int x, int y) {
		boolean result = size < data.length;
		if (result) {
			this.points[size << 1] = x;
			this.points[(size << 1) + 1] = y;
			this.data[size++] = (short)((y << 8) | x);
		}
		return result;
	}
	
	/**
	 * 绘制路标，为了在路标没有被检测的时候可以显示之前检测到的过时路标，第二个参数为指定是否为过时路标。
	 * @param canvas 画布
	 * @param isOutOfDate 是否过时
	 */
	public void draw(Canvas canvas, boolean isOutOfDate) {
		// 设置绘图用的Paint对象
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.BLACK);
		// 背景色白色
		canvas.drawColor(Color.WHITE);
		// 保存画布状态
		canvas.save();
		// 放大绘图内容
		canvas.scale((float)canvas.getWidth() / SIGN_EDGE_LEN, (float)canvas.getHeight() / SIGN_EDGE_LEN);
		// 移动绘图原点以保证图片绘制位置
		canvas.translate(.5f, .5f);
		// 绘制所有的点，由于前面做了放大，每个点都会成为一个正方形
		canvas.drawPoints(points, 0, size << 1, paint);
		// 回复画布状态
		canvas.restore();
		if (isOutOfDate) {
			// 对过时路标，加上一层暗红色遮罩
			canvas.drawColor(0xff7f0000, PorterDuff.Mode.DARKEN);
		}
		// 设置绘图颜色
		paint.setColor(Color.GREEN);
		// 绘制图片边框
		canvas.drawRect(0, 0, canvas.getWidth() - 1, canvas.getHeight() - 1, paint);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof TrafficSign) {
			TrafficSign sign = (TrafficSign) obj;
			result = sign.size == this.size;
			if (result) {
				for (int i = 0; i < size; i++) {
					if (sign.data[i] != this.data[i]) {
						result = false;
						break;
					}
				}
			}
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < this.size; i++) {
			hash = (hash << 5) - hash + this.data[i];
		}
		return hash;
	}
}
