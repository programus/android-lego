package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.util.Arrays;

import android.graphics.Canvas;
import android.graphics.Point;

public class TrafficSign {
	public static class Size {
		public final int width;
		public final int height;
		
		public Size(int w, int h) {
			this.width = w;
			this.height = h;
		}
		
		public int getIndex(int x, int y) {
			return x + y * width;
		}
		
		public int getIndex(Point p) {
			return this.getIndex(p.x, p.y);
		}
		
		@Override
		public boolean equals(Object obj) {
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            return width == s.width && height == s.height;
		}
        @Override
        public int hashCode() {
            return width * 32713 + height;
        }
	}
	
	public enum Rotation {
		Degree0,
		Degree90,
		Degree180,
		Degree270
	}
	
	public final static int SIGN_EDGE_LEN = 20;
	private static final int BLOCK_SIZE = 10;
	private static final int CORNER_COUNT = 4;
	/**
	 * 识别标记的模式。按宽度 黑x1，白x1，黑x3，白x1，黑x1 的模式。
	 */
	private static final int[] PATTERN = {1, 1, 3, 1, 1};
	/**
	 * 识别标记的总宽度（单位：一个单元宽度）
	 */
	private static final double PATTERN_SIZE = 7;
	/**
	 * 检查标记时的宽度容错比例。
	 */
	private double mVariance = 0.3;
	
	private int mMinUnit = 1;
	
	private byte[] mRawBuffer;
	private Rotation mRotation;
	private int[] mMonoBuffer;
	private int[] mSignRows;
	
	private int[] mHistogram;
	private int[] mStateCount;
	
	private Size mImageSize;
	
	private final Size mSignSize = new Size(SIGN_EDGE_LEN, SIGN_EDGE_LEN);
	
	private boolean mSignDetected;
	
	public TrafficSign() {
		this.mSignRows = new int[this.mSignSize.height];
		this.mHistogram = new int[0x100];
		this.mStateCount = new int[PATTERN.length];
	}
	
	public void updateRawBuffer(byte[] raw, Rotation rotation) {
		this.mRawBuffer = raw;
		this.mRotation = rotation;
	}
	
	public void setImageSize(int w, int h) {
		this.mImageSize = new Size(w, h);
		int size = w * h;
		if (this.mMonoBuffer == null || size < this.mMonoBuffer.length) {
			this.mMonoBuffer = new int[size];
		}
	}
	
	public void setMinUnit(int minUnit) {
		this.mMinUnit = minUnit;
	}
	
	public boolean detectTrafficSign() {
		int threshold = this.getThresholdAndTransportData();
		this.convertMono(threshold);
		return this.mSignDetected;
	}
	
	public boolean isSignDetected() {
		return this.mSignDetected;
	}
	
	private void convertMono(int threshold) {
		for (int i = 0; i < this.mMonoBuffer.length; i++) {
			this.mMonoBuffer[i] = this.mMonoBuffer[i] > threshold ? 0xffffff : 0;
		}
	}
	
	private int getThresholdAndTransportData() {
		int w = this.mImageSize.width;
		int h = this.mImageSize.height;
		int wh = w * h;
		long sum = 0;
//		int wl = 0;
//		int m = (this.mHistogram.length - 1) >> 1;
//		for (int y = 0; y < h; y++) {
//			int wy = y * w;
//			int xh = 0;
//			for (int x = 0; x < w; x++) {
//				int index = x + wy;
//				int oi = index;
//				switch (this.mRotation) {
//				case Degree0:
//					break;
//				case Degree90:
//					oi = y + wh - h - xh;
//					break;
//				case Degree180:
//					oi = wh - x - wy - 1;
//					break;
//				case Degree270:
//					oi = h - 1 - y + xh;
//					break;
//				}
//				xh += h;
//				int value = 0xff & this.mRawBuffer[oi];
//				this.mMonoBuffer[index] = colorFromGs(value);
//				histogram[value]++;
//				if (value <= m) {
//					wl++;
//				} else {
//					wr++;
//				}
//			}
//		}
		for (int i = 0; i < wh; i++) {
			int value = 0xff & this.mRawBuffer[i];
			sum += value;
			this.mMonoBuffer[i] = colorFromGs(value);
			this.mHistogram[value]++;
//			if (value <= m) {
//				wl++;
//			}
		}
//		return this.getThreshold(this.mHistogram, m, wl, wh - wl);
		return this.getThreshold(this.mHistogram, wh, sum);
	}
	
	/**
	 * 平衡柱状图算法计算黑白分割阈值
	 * @param histogram 灰度值柱状图信息
	 * @param m 初始中点
	 * @param wl 左边重量
	 * @param wr 右边重量
	 * @return 阈值
	 */
	private int getThreshold(int[] histogram, int m, int wl, int wr) {
		int s = 0;
		int e = histogram.length - 1;
		while (s <= e) {
			if (wr > wl) {
				wr -= histogram[e--];
				if (((s + e) >> 1) < m) {
					wr += histogram[m];
					wl -= histogram[m--];
				}
			} else {
				wl -= histogram[s++];
				if (((s + e) >> 1) > m) {
					wl += histogram[++m];
					wr -= histogram[m];
				}
			}
		}
		Arrays.fill(histogram, 0);
		return colorFromGs(m);
	}
	
	/**
	 * 大津算法计算黑白分割阈值
	 * @param histogram 灰度值柱状图信息
	 * @param total 总像素数
	 * @param sum 灰度总值
	 * @return 阈值
	 */
	private int getThreshold(int[] histogram, int total, long sum) {
		long sumB = 0;
		long wB = 0;
		long wF = 0;
		double mB = 0;
		double mF = 0;
		double max = 0;
		double between = 0;
		int threshold1 = 0;
		int threshold2 = 0;
		for (int i = 0; i < histogram.length; i++) {
			wB += histogram[i];
			int h = histogram[i];
			histogram[i] = 0;
			if (wB == 0) {
				continue;
			}
			wF = total - wB;
			if (wF <= 0) {
				for (int j = i + 1; j < histogram.length; j++) {
					histogram[j] = 0;
				}
				break;
			}
			sumB += h * i;
			mB = (double) sumB / wB;
			mF = (double) (sum - sumB) / wF;
			double d = mB - mF;
			between = wB * wF * d * d;
			if (between >= max) {
				threshold1 = i;
				if (between > max) {
					threshold2 = i;
					max = between;
				}
			}
		}
		
		return colorFromGs((threshold1 + threshold2) >> 1);
	}
	
	private int colorFromGs(int gs) {
		return (gs << 16) | (gs << 8) | gs;
	}
	
	public void drawMonoImage(Canvas canvas) {
		int w = this.mImageSize.width;
		int h = this.mImageSize.height;
		int tx = 0;
		int ty = 0;
		int rotate = 0;
		switch (this.mRotation) {
		case Degree0:
			break;
		case Degree90:
			tx = w;
			rotate = 90;
			w = this.mImageSize.height;
			h = this.mImageSize.width;
			break;
		case Degree180:
			tx = w;
			ty = h;
			rotate = 180;
			break;
		case Degree270:
			ty = h;
			rotate = 270;
			w = this.mImageSize.height;
			h = this.mImageSize.width;
			break;
		}
		canvas.save();
		canvas.translate(tx, ty);
		canvas.rotate(rotate);
		canvas.drawBitmap(mMonoBuffer, 0, w, 0.f, 0.f, w, h, false, null);
		canvas.restore();
	}
	
	public void drawDetectedSign(Canvas canvas) {
		
	}
}
