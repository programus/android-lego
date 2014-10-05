package org.programus.image.utils.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;

import javax.swing.JPanel;

public class HistogramPanel extends JPanel {
	private static final long serialVersionUID = 464138632365075522L;
	
	private static final int LEN = 0x100;
	private static final int MARGIN = 10;
	private static final Color RECT_COLOR = new Color(0x00, 0x80, 0xff);
	
	private long[] histogram;
	private Color[] colors;
	private long max;
	
	public HistogramPanel() {
		this.histogram = new long[LEN];
		this.colors = new Color[LEN];
		for (int i = 0; i < colors.length; i++) {
			int gray = i & 0xff;
			this.colors[i] = new Color(gray, gray, gray);
		}
		this.setPreferredSize(new Dimension(LEN * 3, LEN));
	}

	public void setImage(BufferedImage image) {
		Raster raster = image.getData();
		int w = image.getWidth();
		int h = image.getHeight();
		Arrays.fill(histogram, 0);
		max = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int v = raster.getSample(x, y, 0);
				if (++histogram[v] > max) {
					max = histogram[v];
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int w = this.getWidth() - (MARGIN << 1);
		int h = this.getHeight() - (MARGIN << 1);
		double uy = (double) h / max;
		double ux = (double) w / this.histogram.length;

		Rectangle2D.Double rect = new Rectangle2D.Double(MARGIN, MARGIN, ux, uy);
		for (int i = 0; i < this.histogram.length; i++) {
			g2d.setColor(this.colors[i]);
			rect.height = h;
			rect.x = MARGIN + ux * i;
			rect.y = MARGIN;
			g2d.fill(rect);
			g2d.setColor(RECT_COLOR);
			rect.height = uy * this.histogram[i];
			rect.y = MARGIN + h - rect.height;
			g2d.fill(rect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawRect(MARGIN - 1, MARGIN - 1, w + 1, h + 1);
	}

}
