package org.programus.robot.signgenerator.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.programus.robot.signgenerator.model.SignData;
import org.programus.robot.signgenerator.utils.Pref;
import org.programus.robot.signgenerator.vis.SignPainter;

public class SignCanvasPanel extends JPanel {
	private static final long serialVersionUID = 4836862363501363603L;
	private Pref pref = Pref.getInstance();
	private SignData data;
	private SignPainter painter;
	
	private boolean showGrid = true;
	
	public boolean isShowGrid() {
		return showGrid;
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
		this.repaint();
	}

	private MouseAdapter mouseAdapter = new MouseAdapter() {
		private SignPainter painter;
		
		private SignPainter getPainter() {
			if (this.painter == null) {
				this.painter = new SignPainter(SignCanvasPanel.this.painter);
			}
			
			return this.painter;
		}
		
		private void drawPoint(int x, int y, boolean color) {
            Dimension size = new Dimension();
            getSize(size);
            SignPainter painter = this.getPainter();
            painter.setCanvasSize(size);
            int edgeLen = data.getEdgeLen();
            double unit = painter.getUnit();
            Rectangle2D.Double signArea = painter.getSignArea();
            int col = (int) ((x - signArea.x) / unit);
            int row = (int) ((y - signArea.y) / unit);
            if (col < edgeLen && row < edgeLen && col >= 0 && row >= 0) {
                data.setBlock(col, row, color);
                repaint();
            }
		}

        @Override
        public void mouseDragged(MouseEvent e) {
            SignPainter painter = this.getPainter();
        	this.drawPoint(e.getX(), e.getY(), (e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
            setCursor((painter.getSignArea().contains(e.getX(), e.getY())) ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            this.mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Dimension size = new Dimension();
            getSize(size);
            SignPainter painter = this.getPainter();
            painter.setCanvasSize(size);
            setCursor((painter.getSignArea().contains(e.getX(), e.getY())) ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
        }
    };
	
	public SignCanvasPanel() {
		this(new SignData());
	}
	
	public SignCanvasPanel(SignData data) {
		this.data = data;
		
		this.initData();
		this.initUI();
		this.initDrawListener();
	}
	
	private void initData() {
		int edgeLen = pref.getEdgeLen();
		int unit = pref.getDefaultUnit();
		this.data.setEdgeLen(edgeLen);
		this.painter = new SignPainter(data);
		int minLen = painter.getDefaultEdge(unit);
		Dimension size = new Dimension(minLen, minLen);
		this.setPreferredSize(size);
		this.setMinimumSize(size);
	}
	
	private void initUI() {
		this.painter.setColors(Color.WHITE, Color.BLACK, Color.LIGHT_GRAY);
	}
	
	private void initDrawListener() {
		this.addMouseMotionListener(this.mouseAdapter);
		this.addMouseListener(mouseAdapter);
	}
	
	public SignData getData() {
		return this.data;
	}
	
	public SignPainter getPainter() {
		return this.painter;
	}
	
	public void clear() {
		this.data.clear();
		this.repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		Dimension size = new Dimension();
		this.getSize(size);
		this.painter.setCanvasSize(size);
		this.painter.drawImage(g2d, showGrid);
	}
}
