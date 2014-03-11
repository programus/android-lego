package org.programus.robot.signgenerator.vis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import org.programus.robot.signgenerator.model.SignData;

public class SignPainter implements Printable{
	private final static int SIGN_FIND_PATTERN_LEN = 1 + 1 + 3 + 1 + 1;
	private final static int PADDING = 5;
	
	private SignData data;

	private Color bgColor;
	private Color fgColor;
	private Color gridColor;

	private double areaEdge;
	private double unit;
	private Point2D.Double center;
	private Rectangle2D.Double signArea;
	private Dimension size;
	
	public SignPainter(SignData data) {
		this.data = data;
	}
	
	public SignPainter(SignPainter painter) {
		this(painter.data);
		this.setColors(painter.bgColor, painter.fgColor, painter.gridColor);
	}
	
	public void setCanvasSize(Dimension size) {
		int edgeLen = this.data.getEdgeLen();
		this.size = size;
		this.areaEdge = this.getEdge(size);
		this.unit = this.getUnit(areaEdge);
		this.center = new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
        this.signArea = new Rectangle2D.Double(center.x - edgeLen * unit / 2, center.y - edgeLen * unit / 2, edgeLen * unit, edgeLen * unit);
	}
	
	public double getAreaEdge() {
		return this.areaEdge;
	}
	
	public double getUnit() {
		return this.unit;
	}
	
	public Point2D.Double getCenterPoint() {
		return this.center;
	}
	
	public Rectangle2D.Double getSignArea() {
		return this.signArea;
	}

	private double getEdge(Dimension size) {
		return Math.min(size.width, size.height);
	}
	
	private double getUnit(double edge) {
		return edge / (SIGN_FIND_PATTERN_LEN * 2.0 + this.data.getEdgeLen() + PADDING * 2);
	}
	
	public void setColors(Color bgColor, Color fgColor, Color gridColor) {
		if (bgColor != null) {
			this.bgColor = bgColor;
		}
		if (fgColor != null) {
			this.fgColor = fgColor;
		}
		if (gridColor != null) {
			this.gridColor = gridColor;
		}
	}
	
	public int getDefaultEdge(int defaultUnit) {
		int edgeLen = this.data.getEdgeLen();
		return (edgeLen + (SIGN_FIND_PATTERN_LEN + PADDING) * 2) * defaultUnit;
	}
	
	public void drawImage(Graphics2D g, boolean drawGrid) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color backup = g.getColor();
		Color bgBackup = g.getBackground();

		g.setBackground(bgColor);
		this.drawBackground(g);
		this.drawFindPattern(g);
		this.drawSignData(g);
		if (drawGrid) {
			this.drawGrid(g);
		}

		g.setColor(backup);
		g.setBackground(bgBackup);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}
	
	private void drawBackground(Graphics2D g) {
		g.setColor(bgColor);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight());
		g.fill(rect);
	}
	
	private void drawFindPattern(Graphics2D g) {
		Ellipse2D.Double[] shapes = new Ellipse2D.Double[] {
             new Ellipse2D.Double(0, 0, SIGN_FIND_PATTERN_LEN * unit, SIGN_FIND_PATTERN_LEN * unit),
             new Ellipse2D.Double(0, 0, (SIGN_FIND_PATTERN_LEN - 2) * unit, (SIGN_FIND_PATTERN_LEN - 2) * unit),
             new Ellipse2D.Double(0, 0, (SIGN_FIND_PATTERN_LEN - 4) * unit, (SIGN_FIND_PATTERN_LEN - 4) * unit)
		};
		double half = this.data.getEdgeLen() * unit / 2;
		double offset = SIGN_FIND_PATTERN_LEN * unit;
		Rectangle2D.Double bound = new Rectangle2D.Double(center.x - half - offset, center.y - half - offset, half * 2 + offset, half * 2 + offset);
		this.drawSingleFindPattern(g, bound.getMinX(), bound.getMinY(), shapes, unit);
		this.drawSingleFindPattern(g, bound.getMaxX(), bound.getMinY(), shapes, unit);
		this.drawSingleFindPattern(g, bound.getMaxX(), bound.getMaxY(), shapes, unit);
		this.drawSingleFindPattern(g, bound.getMinX(), bound.getMaxY(), shapes, unit);
	}
	
	private void drawSingleFindPattern(Graphics2D g, double x, double y, Ellipse2D.Double[] shapes, double unit) {
		for (int i = 0; i < shapes.length; i++) {
			Ellipse2D.Double s = shapes[i];
			s.x = x + i * unit;
			s.y = y + i * unit;
			Color color = ((i & 0x01) == 0) ? fgColor : bgColor;
			g.setColor(color);
			g.fill(s);
		}
	}
	
	private void drawGrid(Graphics2D g) {
		g.setColor(gridColor);
		Line2D.Double line = new Line2D.Double();
		double xx = signArea.getMaxX();
		double yy = signArea.getMaxY();
		int count = this.data.getEdgeLen();
		for (int i = 0; i <= count; i++) {
			double offset = i * unit;
			line.setLine(signArea.x + offset, signArea.y, signArea.x + offset, yy);
			g.draw(line);
			line.setLine(signArea.x, signArea.y + offset, xx, signArea.y + offset);
			g.draw(line);
		}
	}
	
	private void drawSignData(Graphics2D g) {
		g.setColor(fgColor);
		int edgeLen = data.getEdgeLen();
		double unitX = signArea.width / edgeLen;
		double unitY = signArea.height / edgeLen;
		Rectangle2D.Double block = new Rectangle2D.Double(0, 0, unitX + 0.4, unitY + 0.4); 
		for (int y = 0; y < edgeLen; y++) {
			for (int x = 0; x < edgeLen; x++) {
				if (this.data.isBlockBlack(x, y)) {
					block.x = signArea.x + x * unitX - 0.2;
					block.y = signArea.y + y * unitY - 0.2;
					g.fill(block);
				}
			}
		}
	}
	
	public BufferedImage getImage() {
		BufferedImage im = new BufferedImage(size.width, size.height, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = im.createGraphics();
		this.drawImage(g, false);
		g.dispose();
		return im;
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		this.setCanvasSize(new Dimension((int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight()));
		this.drawImage(g2d, false);
		return pageIndex > 0 ? Printable.NO_SUCH_PAGE : Printable.PAGE_EXISTS;
	}
}
