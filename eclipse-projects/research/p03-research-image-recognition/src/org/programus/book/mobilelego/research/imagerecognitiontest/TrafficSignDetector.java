package org.programus.book.mobilelego.research.imagerecognitiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * 路标检测器。
 * 
 * @author programus
 *
 */
public class TrafficSignDetector {
	/**
	 * 用来存储大小的类
	 */
	public static class Size {
		public final int width;
		public final int height;
		
		public Size(int w, int h) {
			this.width = w;
			this.height = h;
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
	
	/**
	 * 检测时图片的旋转角度。角度应该跟着预览方向变化。
	 *
	 */
	public enum Rotation {
		Degree0,
		Degree90,
		Degree180,
		Degree270
	}
	
	/** 图片识别时，边角识别模式的个数 */
	private static final int CORNER_COUNT = 4;
	/** 无透明度信息颜色的掩码 */
	private static final int COLOR_MASK = 0xffffff;
	/** 识别标记的模式。按宽度 黑x1，白x1，黑x3，白x1，黑x1 的模式。 */
	private static final int[] PATTERN = {1, 1, 3, 1, 1};
	/** 识别标记的总宽度（单位：一个单元宽度） */
	private static final int PATTERN_SIZE = 7;
	/** 检查标记时的宽度容错比例。 */
	private float mVariance = 0.3f;
	/** 最小识别单位长度 */
	private int mMinUnit = 1;
	
	/** 存储原始图片数据的数组 */
	private byte[] mRawBuffer;
	/** 
	 * 存储处理中图片（包含处理数据）数据的数组。
	 * 数组中最初保存由原始数据而来的灰度值，处理中使用到的点转为黑白数值
	 */
	private int[] mInfoBuffer;
	/** 旋转信息 */
	private Rotation mRotation;
	
	/** 黑白分割阈值 */
	private int mThreshold;
	/** 为寻找黑白分割阈值而准备的柱状图数组 */
	private int[] mHistogram;
	
	/** 四个角识别模式的坐标 */
	private List<Point> mCorners;
	/** 标准路标中，四个定位点的坐标位置 */
	private float mCornerSrcPoints[];
	/** 存储四个角坐标的数组，计算映射用，中间变量 */
	private float mCornerDstPoints[];
	/** 映射用矩阵 */
	private Matrix mMatrix;
	/** 路标坐标映射源的点，即标准路标中的点，中间变量 */
	private float mSignSrcPoints[];
	/** 路标坐标映射目标的店，即实际图片中的点，中间变量 */
	private float mSignDstPoints[];
	/** 定义数组用以存储标记图形X方向的5个状态(黑、白、宽黑、白、黑)中的像素数，中间变量 */
	private int[] mStateCountX;
	/** 定义数组用以存储标记图形Y方向的5个状态(黑、白、宽黑、白、黑)中的像素数，中间变量 */
	private int[] mStateCountY;
	
	/** 绘图所需的Paint对象 */
	private Paint mPaint;
	
	/** 待处理图片大小，以实际角度下的数值为准 */
	private Size mImageSize;
	
	/** 路标大小 */
	private final Size mSignSize = new Size(TrafficSign.SIGN_EDGE_LEN, TrafficSign.SIGN_EDGE_LEN);
	
	/** 
	 * 检测出的路标对象。
	 * 未检测出路标时，不清空此对象，将保留上次检测出的内容。
	 */
	private TrafficSign mDetectedSign;
	/** 是否检测到路标 */
	private boolean mDetected;
	
	/**
	 * 构造函数
	 */
	public TrafficSignDetector() {
		// 柱状图是针对所有灰度值的，灰度值为16进制0x00-0xff，共计0x100个数值
		this.mHistogram = new int[0x100];
		this.mCorners = new ArrayList<Point>(CORNER_COUNT);
		this.mCornerSrcPoints = this.getCornerSrcPoints();
		this.mCornerDstPoints = new float[this.mCornerSrcPoints.length];
		this.mMatrix = new Matrix();
		this.mSignSrcPoints = new float[(this.mSignSize.height * this.mSignSize.width) << 1];
		this.initSignSrcPoints();
		this.mSignDstPoints = new float[this.mSignSrcPoints.length];
		this.mStateCountX = new int[PATTERN.length];
		this.mStateCountY = new int[PATTERN.length];
		this.mPaint = new Paint();
		this.mPaint.setStyle(Paint.Style.STROKE);
		this.mPaint.setStrokeWidth(1);
	}
	
	/**
	 * 更新原始图片数据
	 * @param raw 原始图片数据
	 */
	public void updateRawBuffer(byte[] raw) {
		this.mRawBuffer = raw;
	}
	
	/**
	 * 设置旋转角度
	 * @param rotation 旋转角度
	 */
	public void setRotation(Rotation rotation) {
		this.mRotation = rotation;
	}
	
	/**
	 * 设置图片大小
	 * @param w
	 * @param h
	 */
	public void setImageSize(int w, int h) {
		this.mImageSize = new Size(w, h);
		// 根据图片大小重设图片信息存储区大小
		int size = w * h;
		if (this.mInfoBuffer == null || size < this.mInfoBuffer.length) {
			this.mInfoBuffer = new int[size];
		}
	}
	
	/**
	 * 设置最小检测单元大小
	 * @param minUnit
	 */
	public void setMinUnit(int minUnit) {
		this.mMinUnit = minUnit;
	}
	
	/**
	 * 设置路标对象，此对象在检测到信息时予以更新。若未指定或指定为null，在检测到路标时会自动创建新对象。
	 * @param sign
	 */
	public void setSign(TrafficSign sign) {
		this.mDetectedSign = sign;
	}
	
	/**
	 * 检测路标，检测结果可以通过{@link #isSignDetected()}和{{@link #getDetectedSign()}函数取得。
	 */
	public void detectTrafficSign() {
		// 计算阈值
		this.mThreshold = this.getThresholdAndTransportData();
		// 检测图像定位模式
		this.mDetected = this.detectCorner();
		if (this.mDetected) {
			// 找到图像定位模式时，计算路标图片映射用的矩阵
			Matrix matrix = this.getMatrix(mCorners);
			// 填充路标对象数据
			this.fillSignData(matrix);
		}
	}
	
	/**
	 * 取得路标
	 * @return
	 */
	public TrafficSign getDetectedSign() {
		return this.mDetectedSign;
	}
	
	/**
	 * 返回是否检测到路标
	 * @return
	 */
	public boolean isSignDetected() {
		return this.mDetected;
	}
	
	/**
	 * 初始化标准路标图形上的所有点
	 */
	private void initSignSrcPoints() {
		int i = 0;
		int lx = this.mSignSize.width;
		int ly = this.mSignSize.height;
		for (int y = 0; y < ly; y++) {
			for (int x = 0; x < lx; x++) {
				this.mSignSrcPoints[i++] = x;
				this.mSignSrcPoints[i++] = y;
			}
		}
	}
	
	/**
	 * 返回标准路标中定位点坐标
	 * @return 定位点坐标
	 */
	private float[] getCornerSrcPoints() {
		// 两个定位点之间的距离
		float len = (TrafficSign.SIGN_EDGE_LEN + PATTERN_SIZE);
		// 定位点偏离路标图形边缘的距离
		float offset = (PATTERN_SIZE + 1) / 2.f;
		return new float[]{
			-offset, -offset, 				// 左上角
			len - offset, -offset,			// 右上角
			-offset, len - offset,			// 左下角
			len - offset, len - offset		// 右下角
		};
	}
	
	/**
	 * 填充路标数据
	 * @param matrix 映射用矩阵
	 */
	private void fillSignData(Matrix matrix) {
		// 根据情况创建新的路标实例或重置路标数据
		if (this.mDetectedSign == null) {
			this.mDetectedSign = new TrafficSign();
		} else {
			this.mDetectedSign.reset();
		}
		// 使用矩阵映射：标准路标中的点->实际图片中的点
		matrix.mapPoints(mSignDstPoints, mSignSrcPoints);
		// 循环所有点，检查亮度值
		int base = 0;
		for (int sy = 0; sy < this.mSignSize.height; sy++) {
			for (int sx = 0; sx < this.mSignSize.width; sx++) {
				int index = sx + base;
				// 取得实际图片中的坐标值
				int x = Math.round(this.mSignDstPoints[index << 1]);
				int y = Math.round(this.mSignDstPoints[(index << 1) + 1]);
				// 根据坐标值计算实际图片中的数据索引
				int monoIndex = x + y * ((this.mRotation.ordinal() & 0x01) == 0 ? this.mImageSize.width : this.mImageSize.height);
				if (this.isDark(monoIndex)) {
					// 如果所在点是暗色，则加入路标数据中
					this.mDetectedSign.addPoint(sx, sy);
				}
			}
			base += this.mSignSize.width;
		}
	}
	
	/**
	 * 判断指定索引的点是否为暗色（即转换成纯黑白图时，为黑色），同时将对应的点设置为黑白色。
	 * @param index 图片数据索引
	 * @return 为暗色时返回<code>true</code>
	 */
	private boolean isDark(int index) {
		boolean result = false;
		if (index >= 0 && index < this.mInfoBuffer.length) {
			// 检查对应点的亮度是否低于阈值
			result = this.mInfoBuffer[index] < this.mThreshold;
			// 将对应点转换为黑白色，以供用户通过屏幕调试
			this.mInfoBuffer[index] = COLOR_MASK & (result ? Color.BLACK : Color.WHITE);
		}
		return result;
	}
	
	/**
	 * 检测定位点。检测到的坐标将存储在{@link #mCorners}中，数值基于原始相机朝向，与旋转参数无关。
	 * @return 检测到时为<code>true</code>
	 */
	private boolean detectCorner() {
		// 根据相机朝向，调整w、h数值
		int w = this.mImageSize.width;
		int h = this.mImageSize.height;
		if ((this.mRotation.ordinal() & 0x01) == 1) {
			w = this.mImageSize.height;
			h = this.mImageSize.width;
		}
		
		// 重新检测前，清空旧数据
		this.mCorners.clear();
		int currentState = 0;
		int scanStep = this.mMinUnit;
		// 上面前面跳过的部分：大小为标记图形大小的一半
		int minScanSize = this.mMinUnit * (PATTERN_SIZE >> 1);
		// 图形的最小可能尺寸
		int minSignSize = this.mMinUnit * ((PATTERN_SIZE >> 1) + TrafficSign.SIGN_EDGE_LEN);

		// 以scanStep为间隔，扫描各行
		for (int y = minScanSize; y < h; y += scanStep) {
			// 一行开始，初始化所有状态的像素数
			Arrays.fill(mStateCountX, 0);
			// 将状态恢复到状态0（黑）
			currentState = 0;
			int base = y * w;
			// 同一Y坐标上找到的标记点数
			int foundCount = 0;
			for (int x = 0; x < w; x++) {
				// 计算并取出纯黑白颜色。
				int index = x + base;
				if (this.isDark(index)) {
					// 当前颜色为黑
					if ((currentState & 0x01) == 1) {
						// 奇数状态：我们正在计算白色像素数
						// 因此状态需要前进一步
						currentState++;
					}
					mStateCountX[currentState]++;
				} else {
					// 当前颜色为白
					if ((currentState & 0x01) == 1) {
						// 奇数状态：我们正在计算白色像素数
						mStateCountX[currentState]++;
					} else {
						// 偶梳状态：我们正在计算黑色像素数
						if (currentState == 4) {
							// 发现 黑白黑白黑 之后的白色，颜色模式匹配
							// 检查颜色宽度比例并试图获取模式中心点
							Point p = this.getPatternPoint(this.mInfoBuffer, w, h, x, y, mStateCountX, this.mCorners);
							if (p != null) {
								// 找到一个点
								this.mCorners.add(p);
								foundCount++;
								// 推测下一个点的Y坐标，跳过无需扫描的部分
								y = this.guessY(mCorners, mStateCountX, y, h);
								// 已经找到全部四个点，则结束查找
								if (this.mCorners.size() >= CORNER_COUNT) {
									break;
								}
								// 同一Y坐标下找到两个点，跳入下一个Y坐标进行查找
								if (foundCount >= 2) {
									break;
								}
							} else {
								// 如果比例不符，跳过前一黑一白部分，重新计算
								currentState = 3;
								System.arraycopy(mStateCountX, 2, mStateCountX, 0, 3);
								mStateCountX[3] = 1;
								mStateCountX[4] = 0;
								continue;
							}
							// 或许已找到一个，查找下一个，
							// 恢复各种状态值。
							Arrays.fill(mStateCountX, 0);
							currentState = 0;
						} else {
							// 当前颜色与正在计算的颜色不同，
							// 状态向前推移，并追加像素数。
							mStateCountX[++currentState]++;
						}
					}
				}
				
				// 当前行剩下的宽度已不足以容纳一个可能的定位点图形
				if (foundCount <= 0 && currentState < 3 && x + minScanSize > w) {
					break;
				}
			}
			// 当前图形已没有足够的大小容纳一个可能的路标图形
			if (this.mCorners.size() < 2 && y + minSignSize > h) {
				break;
			}
		}
		
		return this.mCorners.size() == CORNER_COUNT;
	}
	
	/**
	 * 推测下一个定位点的纵坐标值
	 * @param corners 已找到的定位点
	 * @param stateCount 当前的状态数据
	 * @param y 当前的纵坐标
	 * @param h 纵轴方向的高度
	 * @return 推测出的下一个定位点的纵坐标值
	 */
	private int guessY(List<Point> corners, int[] stateCount, int y, int h) {
		if (corners.size() >= CORNER_COUNT) {
			// 如果已经找到了所有定位点，让纵坐标达到最大高度值，以跳出循环
			y = h;
		} else if (corners.size() >= (CORNER_COUNT >> 1)) {
			// 如果已经找到了一半定位点，下一个点与第一个点的纵坐标差应约等于第二个点和第一个点的横坐标差
			Point pa = corners.get(0);
			// 计算前两点的横坐标差
			int d = Math.abs(corners.get(1).x - pa.x);
			// 新纵坐标为第一个点的纵坐标加上差，为保证不出现漏扫，退回相应的容错大小
			int ny = pa.y + d - (int) ((stateCount[0] + stateCount[1]) * (1 + this.mVariance));
			// 如果新纵坐标出现后退，则维持现状
			if (ny > y) {
				y = ny;
			}
		}
		
		return y;
	}
	
	/**
	 * 计算并取得映射用矩阵
	 * @param corners 所有定位点
	 * @return 映射用矩阵
	 */
	private Matrix getMatrix(List<Point> corners) {
		// 标准路径中的四个定位点坐标，按左上、右上、左下、右下的顺序排序
		float[] src = this.mCornerSrcPoints;
		// 实际图片中的四个定位点坐标
		float[] dst = this.getArrangedCornerPoints(mCornerDstPoints);
		
		// 通过四个定位点来确定矩阵数据
		mMatrix.setPolyToPoly(src, 0, dst, 0, corners.size());
		return mMatrix;
	}
	
	/**
	 * 取得排列好的定位点坐标数据，数据顺序是左上、右上、左下、右下
	 * @param points 用以存储排序后坐标数据的数组
	 * @return 排列好的定位点坐标数据
	 */
	private float[] getArrangedCornerPoints(float[] points) {
		// 由于扫描是基于纵坐标由小到大进行，因此扫描结束后，
		// 四个点是以纵坐标由小到大排序的。坐标是基于原始相机镜头方向的
		
		// 对四个点进行两次循环，
		for (int i = 0; i < CORNER_COUNT; i += 2) {
			// 每次循环取两个点比较它们的横坐标值，
			// 横坐标较小的点向前排
			Point pa = mCorners.get(i);
			Point pb = mCorners.get(i + 1);
			if (pa.x > pb.x) {
				pa = mCorners.get(i + 1);
				pb = mCorners.get(i);
			}
			// 根据旋转角度，计算对应的坐标点数组索引
			int ia = i;
			int ib = i + 1;
			switch (mRotation) {
			case Degree0:
				break;
			case Degree90:
				ia = ~(i >> 1) & 0x01;
				ib = ia + 2;
				break;
			case Degree180:
				ib = ~i & 0x02;
				ia = ib + 1;
				break;
			case Degree270:
				ib = i >> 1;
				ia = ib + 2;
				break;
			}
			// 将坐标点赋值到数组中对应的元素
			points[ia << 1] = pa.x;
			points[(ia << 1) + 1] = pa.y;
			points[ib << 1] = pb.x;
			points[(ib << 1) + 1] = pb.y;
		}
		return points;
	}
	
	/**
	 * 判断颜色模式的宽度模式，并在模式匹配成功后计算识别标记图形的中心点，并对中心点有效性进行验证。
	 * @param im 图片
	 * @param x 当前扫描的点的X坐标
	 * @param y 当前扫描的点的Y坐标
	 * @param stateCount 各个颜色状态的像素数
	 * @param list 已找到的点的列表
	 * @return 如果宽度匹配成功，所得点有效，则返回该点，否则返回<code>null</code>
	 */
	private Point getPatternPoint(int[] data, int w, int h, int x, int y, int[] stateCount, List<Point> list) {
		// 计算匹配图形的总像素数
		int totalFinderSize = 0;
		for (int count : stateCount) {
			if (count <= 0) {
				// 如果某颜色的像素数为0，则直接返回
				return null;
			}
			totalFinderSize += count;
		}
		
		if (totalFinderSize < PATTERN_SIZE * this.mMinUnit) {
			// 如果总像素数小于识别图像的最低允许像素数，则直接宣告失败返回
			return null;
		}
		
		// 计算单元宽度
		float mSize = (float)totalFinderSize / PATTERN_SIZE;
		// 计算允许容错宽度
		float maxVar = mSize * this.mVariance;
		
		// 检查各个颜色宽度
		for (int i = 0; i < stateCount.length; i++) {
			if (Math.abs(mSize * PATTERN[i] - stateCount[i]) >= maxVar * PATTERN[i]) {
				// 如果颜色宽度超出许可范围，返回
				return null;
			}
		}
		
		// 计算中心点X坐标。中心点为当前扫描点向回移动半个识别图形宽度
		int px = (int) (x - totalFinderSize / 2);
		// 检查Y轴方向上的模式匹配
		Arrays.fill(this.mStateCountY, 0);
		// 从当前扫描坐标向上下检查的最大范围
		int sizeLimit = (int) (mSize * 3 + maxVar + 1);
		// 向下检查得到的标记图形下边界
		int yd = this.fillStateCountY(w, h, px, y, mStateCountY, 1, sizeLimit);
		// 向上检查得到的标记图形上边界
		int yu = this.fillStateCountY(w, h, px, y, mStateCountY, -1, sizeLimit);
		if (yd >= 0 && yu >= 0 && yd > yu) {
            // 检查各个颜色宽度
            for (int i = 0; i < mStateCountY.length; i++) {
                if (Math.abs(mSize * PATTERN[i] - mStateCountY[i]) >= maxVar * PATTERN[i]) {
                    // 如果颜色宽度超出许可范围，返回
                    return null;
                }
            }
            // 计算中心点Y坐标
			int py = (yu + yd) / 2;
			Point p = new Point(px, py);
			if (this.isCirclePattern(w, h, px, py, mSize) && this.isValidPoint(p, list, totalFinderSize, yd - yu)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * 检查定位点模式是否符合规范。判断依据为取得固定的抽样点检查颜色，以确定确实为圆形模式。
	 * @param w 图像宽度
	 * @param h 图像高度
	 * @param x 定位点横坐标
	 * @param y 定位点纵坐标
	 * @param size 整个模式的大小
	 * @return
	 */
	private boolean isCirclePattern(int w, int h, int x, int y, float size) {
		// 考察点与中心点的距离
		// 分别为中心黑圆内、白环中央、黑环中央
		float[] distances = {
			1, 2, 3
		};
		// 考察点的坐标值比例，以勾三股四弦五取8个点
		float[] xs = {
			0.8f, 0.6f, 
			-0.6f, -0.8f,
			-0.8f, -0.6f,
			0.6f, 0.8f
		};
		float[] ys = {
			0.6f, 0.8f,
			0.8f, 0.6f,
			-0.6f, -0.8f,
			-0.8f, -0.6f
		};
		
		// 不同距离上的黑白颜色不同，提前定义变量存储正确的颜色
		boolean isBlack = true;
		for (float distance : distances) {
			// 对每个距离进行检查
			for (int i = 0; i < xs.length; i++) {
				// 循环检测每个距离上的8个点
				float cx = x + distance * size * xs[i];
				float cy = y + distance * size * ys[i];
				if ((this.isDark(Math.round(cx) + Math.round(cy) * w)) != isBlack) {
					return false;
				}
			}
			isBlack = !isBlack;
		}
		
		return true;
	}
	
	/**
	 * 检查指定点是否有效。有时临近的点都会符合模式匹配结果，此函数用以判断发现的点是否与已找到的点过分接近。
	 * @param p 待检查点
	 * @param list 已找到的点的列表
	 * @param limitX X坐标的最近允许距离
	 * @param limitY Y坐标的最近允许距离
	 * @return 如果距离足够，判为有效点，返回<code>true</code>，否则返回<code>false</code>
	 */
	private boolean isValidPoint(Point p, List<Point> list, int limitX, int limitY) {
		for (Point ep : list) {
			int dx = Math.abs(ep.x - p.x);
			int dy = Math.abs(ep.y - p.y);
			if (dy < limitY && dx < limitX) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 填充纵坐标方向上的状态数据。坐标系基于相机原始坐标方向。
	 * @param w 图像宽度
	 * @param h 图像高度
	 * @param cx 疑似识别点横坐标
	 * @param cy 疑似识别点纵坐标
	 * @param stateCountY 有待填充数值的纵坐标状态数据数组
	 * @param dir 方向，-1为向上，1为向下
	 * @param sizeLimit 扫描距离极限
	 * @return 识别模式的边界处的纵坐标
	 */
	private int fillStateCountY(int w, int h, int cx, int cy, int[] stateCountY, int dir, int sizeLimit) {
		int currentState = 2;
		for (int y = cy; y < w && y >= 0; y += dir) {
			if (this.isDark(cx + y * w)) {
				// 当前颜色黑色
				if ((currentState & 0x01) == 1) {
                    // 奇数状态：我们正在计算白色像素数
                    // 因此状态需要变化
                    currentState += dir;
				}
				stateCountY[currentState]++;
			} else {
				// 当前颜色白色
				if ((currentState & 0x01) == 1) {
                    // 奇数状态：我们正在计算白色像素数
                    stateCountY[currentState]++;
                } else {
                    // 偶梳状态：我们正在计算黑色像素数
                	switch (currentState) {
                	case 2:
                		currentState += dir;
                		stateCountY[currentState]++;
                		break;
                	case 4:
                	case 0:
                		return y;
                	}
				}
			}
			if (stateCountY[currentState] > sizeLimit) {
				break;
			}
		}
		
		return -1;
	}
	
	/**
	 * 复制原始数据到图形信息区，并计算阈值
	 * @return 阈值
	 */
	private int getThresholdAndTransportData() {
		int w = this.mImageSize.width;
		int h = this.mImageSize.height;
		int wh = w * h;
		long sum = 0;
//		int wl = 0;
//		int m = (this.mHistogram.length - 1) >> 1;
		for (int i = 0; i < wh; i++) {
			int value = 0xff & this.mRawBuffer[i];
			sum += value;
			this.mInfoBuffer[i] = colorFromGs(value);
			this.mHistogram[value]++;
//			if (value <= m) {
//				wl++;
//			}
		}
//		return this.getThreshold(this.mHistogram, m, wl, wh - wl);
		return this.getThreshold(this.mHistogram, wh, sum);
	}
	
//	/**
//	 * 平衡柱状图算法计算黑白分割阈值
//	 * @param histogram 灰度值柱状图信息
//	 * @param m 初始中点
//	 * @param wl 左边重量
//	 * @param wr 右边重量
//	 * @return 阈值
//	 */
//	private int getThreshold(int[] histogram, int m, int wl, int wr) {
//		int s = 0;
//		int e = histogram.length - 1;
//		while (s <= e) {
//			if (wr > wl) {
//				wr -= histogram[e--];
//				if (((s + e) >> 1) < m) {
//					wr += histogram[m];
//					wl -= histogram[m--];
//				}
//			} else {
//				wl -= histogram[s++];
//				if (((s + e) >> 1) > m) {
//					wl += histogram[++m];
//					wr -= histogram[m];
//				}
//			}
//		}
//		Arrays.fill(histogram, 0);
//		return colorFromGs(m);
//	}
	
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
	
	public void drawInfoImage(Canvas canvas) {
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
		canvas.drawBitmap(mInfoBuffer, 0, w, 0.f, 0.f, w, h, false, null);
		
		int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, 0xffa0a0ff};
		for (int i = 0; this.isSignDetected() && i < this.mCornerDstPoints.length; i += 2) {
			mPaint.setColor(colors[i >> 1 % colors.length]);
			float x = this.mCornerDstPoints[i];
			float y = this.mCornerDstPoints[i + 1];
			canvas.drawCircle(this.mCornerDstPoints[i], this.mCornerDstPoints[i + 1], 10, mPaint);
			canvas.drawLine(x, y - 5, x, y + 5, mPaint);
			canvas.drawLine(x - 5, y, x + 5, y, mPaint);
//			System.out.printf("Draw: (%.0f,%.0f) - %x\n", x, y, mPaint.getColor());
		}
		
		canvas.restore();
	}
}
