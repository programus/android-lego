package org.programus.robot.signgenerator.vis;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.programus.robot.signgenerator.model.SignData;

/**
 * 加载图片的类
 * @author programus
 *
 */
public class SignLoader {
	/**
	 * 检查标记时的宽度容错比例。
	 */
	private double variance = 0.3;
	/**
	 * 识别标记的模式。按宽度 黑x1，白x1，黑x3，白x1，黑x1 的模式。
	 */
	private static final int[] PATTERN = {1, 1, 3, 1, 1};
	/**
	 * 识别标记的总宽度（单位：一个单元宽度）
	 */
	private static final double PATTERN_SIZE = 7;
	/**
	 * 检查某单元块颜色时，扫描的范围比例。
	 */
	private static final double CHECK_RANGE = 0.5;
	/**
	 * 检查单元块颜色时，扫描后，所属颜色的最低百分比。
	 */
	private static final double RATE = 0.9;
	
	/**
	 * 加载图片函数
	 * @param im 所加载的图片
	 * @param data 加载图片后写入数据的内部存储数据
	 * @return 加载成功时返回<code>true</code>，失败返回<code>false</code>
	 */
	public boolean loadImage(BufferedImage im, SignData data) {
		boolean success = false;
		// 根据模式匹配，找到四个识别标记的中心点
		List<Point> corners = this.findPattern(this.getMonochromeImage(im), 0, 0);
		if (corners.size() == 4) {
			// 如果恰好找到4个点，说明是我们可以识别的图片
			// 首先对点按左上、右上、左下、右下的顺序进行排序。
			Collections.sort(corners, new Comparator<Point>() {
				@Override
				public int compare(Point o1, Point o2) {
					int dy = o1.y - o2.y;
					return dy == 0 ? o1.x - o2.x : dy;
				}
				
			});
			
			// 根据四个点的位置，计算单元宽度
			Point2D.Double blockSize = new Point2D.Double();
			blockSize.x = ((corners.get(1).x - corners.get(0).x) + (corners.get(3).x - corners.get(2).x)) / 2.0 / (data.getEdgeLen() + PATTERN_SIZE);
			blockSize.y = ((corners.get(2).y - corners.get(0).y) + (corners.get(3).y - corners.get(1).y)) / 2.0 / (data.getEdgeLen() + PATTERN_SIZE);
			// 取左上角点为基准点，进行扫描
			Point base = corners.get(0);
			// 预定义一个点，用以存储每个单元块的中心点
            Point2D.Double center = new Point2D.Double();
            // 循环扫描图片中心数据部分
			for (int row = 0; row < data.getEdgeLen(); row++) {
				center.y = base.y + blockSize.y * (row + PATTERN_SIZE / 2 + 0.5);
				for (int col = 0; col < data.getEdgeLen(); col++) {
                    center.x = base.x + blockSize.x * (col + PATTERN_SIZE / 2 + 0.5);
                    // 根据单元块的主要颜色来写入数据
                    data.setBlock(col, row, this.isFilled(im, center, blockSize));
				}
			}
			success = true;
		}
		return success;
	}
	
	/**
	 * 根据中心点判断单元块是否被涂黑
	 * @param im 加载的图片
	 * @param center 中心点坐标
	 * @param blockSize 单元块大小
	 * @return 涂黑返回<code>true</code>
	 */
	private boolean isFilled(BufferedImage im, Point2D.Double center, Point2D.Double blockSize) {
		Point2D.Double offset = new Point2D.Double(blockSize.x * CHECK_RANGE / 2, blockSize.y * CHECK_RANGE / 2);
		int count = 0;
		for (int y = (int) (center.y - offset.y); y < center.y + offset.y; y++) {
			for (int x = (int) (center.x - offset.x); x < center.x + offset.x; x++) {
				// 由于颜色信息中前两位是透明度，通过位运算消除掉。
				int color = im.getRGB(x, y) & 0x00ffffff;
				if (color == 0) {
					count++;
				}
			}
		}
		return count > blockSize.x * blockSize.y * CHECK_RANGE * CHECK_RANGE * RATE;
	}
	
	/**
	 * 将图片转换为黑白单色图
	 * @param im 输入图片（可以是任何类型）
	 * @return 黑白单色图片，类型为{@linkplain BufferedImage#TYPE_BYTE_BINARY}
	 */
	private BufferedImage getMonochromeImage(BufferedImage im) {
		if (im.getType() != BufferedImage.TYPE_BYTE_BINARY) {
			BufferedImage monoim = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D g = monoim.createGraphics();
			g.drawImage(im, 0, 0, null);
			g.dispose();
			return monoim;
		} else {
			// 如果本来就是黑白单色图，无需转换，直接返回输入图片
			return im;
		}
	}
	
	/**
	 * 在图片中寻找识别标记图形
	 * @param im 图片
	 * @param sx 开始扫描的X坐标
	 * @param sy 开始扫描的Y坐标
	 * @return 识别标记图形中心点列表
	 */
	private List<Point> findPattern(BufferedImage im, int sx, int sy) {
		List<Point> pointList = new ArrayList<Point>(4);
		int skipRows = 1;
		// 定义数组用以存储标记图形的5个状态(黑、白、宽黑、白、黑)中的像素数
		int[] stateCount = new int[5];
		int currentState = 0; // 当前状态
		// 将宽高存至短变量中，以减少输入量
		int w = im.getWidth();
		int h = im.getHeight();
		// 以skipRows为间隔，扫描各行
		for (int y = sy; y < h; y += skipRows) {
			// 一行开始，初始化所有状态的像素数
			Arrays.fill(stateCount, 0);
			// 将状态恢复到状态0（黑）
			currentState = 0;
			for (int x = sx; x < w; x++) {
				// 取出颜色。用位运算去掉透明度信息。
				int color = im.getRGB(x, y) & 0x00ffffff;
				if (color == 0) {
					// 当前颜色为黑
					if ((currentState & 0x01) == 1) {
						// 奇数状态：我们正在计算白色像素数
						// 因此状态需要前进一步
						currentState++;
					}
					stateCount[currentState]++;
				} else {
					// 当前颜色为白
					if ((currentState & 0x01) == 1) {
						// 奇数状态：我们正在计算白色像素数
						stateCount[currentState]++;
					} else {
						// 偶梳状态：我们正在计算黑色像素数
						if (currentState == 4) {
							// 发现 黑白黑白黑 之后的白色，颜色模式匹配
							// 检查颜色宽度比例并试图获取模式中心点
							Point p = this.getPatternPoint(im, x, y, stateCount, pointList);
							if (p != null) {
								// 找到一个点
								pointList.add(p);
							} else {
								// 如果比例不符，跳过前一黑一白部分，重新计算
								currentState = 3;
								System.arraycopy(stateCount, 2, stateCount, 0, 3);
								stateCount[3] = 1;
								stateCount[4] = 0;
								continue;
							}
							// 或许已找到一个，查找下一个，
							// 恢复各种状态值。
							Arrays.fill(stateCount, 0);
							currentState = 0;
						} else {
							// 当前颜色与正在计算的颜色不同，
							// 状态向前推移，并追加像素数。
							stateCount[++currentState]++;
						}
					}
				}
			}
		}
		
		return pointList;
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
	 * 判断颜色模式的宽度模式，并在模式匹配成功后计算识别标记图形的中心点，并对中心点有效性进行验证。
	 * @param im 图片
	 * @param x 当前扫描的点的X坐标
	 * @param y 当前扫描的点的Y坐标
	 * @param stateCount 各个颜色状态的像素数
	 * @param list 已找到的点的列表
	 * @return 如果宽度匹配成功，所得点有效，则返回该点，否则返回<code>null</code>
	 */
	private Point getPatternPoint(BufferedImage im, int x, int y, int[] stateCount, List<Point> list) {
		// 计算匹配图形的总像素数
		int totalFinderSize = 0;
		for (int count : stateCount) {
			if (count <= 0) {
				// 如果某颜色的像素数为0，则直接返回
				return null;
			}
			totalFinderSize += count;
		}
		
		if (totalFinderSize < PATTERN_SIZE) {
			// 如果总像素数小于识别图像的最低允许像素数，则直接宣告失败返回
			return null;
		}
		
		// 计算单元宽度
		double mSize = totalFinderSize / PATTERN_SIZE;
		// 计算允许容错宽度
		double maxVar = mSize * variance;
		
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
		// 定义数组用以存储Y轴方向上各个颜色状态下的像素数
		int[] stateCountY = new int[stateCount.length];
		// 从当前扫描坐标向上下检查的最大范围
		int sizeLimit = (int) (mSize * 3 + maxVar + 1);
		// 向下检查得到的标记图形下边界
		int yd = this.fillStateCountY(im, px, y, stateCountY, 1, sizeLimit);
		// 向上检查得到的标记图形上边界
		int yu = this.fillStateCountY(im, px, y, stateCountY, -1, sizeLimit);
		if (yd >= 0 && yu >= 0 && yd > yu) {
            // 检查各个颜色宽度
            for (int i = 0; i < stateCountY.length; i++) {
                if (Math.abs(mSize * PATTERN[i] - stateCountY[i]) >= maxVar * PATTERN[i]) {
                    // 如果颜色宽度超出许可范围，返回
                    return null;
                }
            }
            // 计算中心点Y坐标
			int py = (yu + yd) / 2;
			Point p = new Point(px, py);
			if (this.isValidPoint(p, list, totalFinderSize, yd - yu)) {
				return p;
			}
		}
		return null;
	}
	
	private int fillStateCountY(BufferedImage im, int cx, int cy, int[] stateCountY, int dir, int sizeLimit) {
		int currentState = 2;
		for (int y = cy; y < im.getWidth() && y >= 0; y += dir) {
			int color = im.getRGB(cx, y) & 0x00ffffff;
			if (color == 0) {
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
}
