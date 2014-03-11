package org.programus.robot.signgenerator.model;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;


public class SignData {
	private int edgeLen;
	private boolean[][] data;
	
	public SignData(int len) {
		this.setEdgeLen(len);
	}
	
	public SignData() {
	}
	
	public void setEdgeLen(int edgeLen) {
		this.edgeLen = edgeLen;
		this.data = new boolean[edgeLen][edgeLen];
	}
	
	public int getEdgeLen() {
		return this.edgeLen;
	}
	
	public boolean[][] getData() {
		return this.data;
	}
	
	public void setBlock(int x, int y, boolean isBlack) {
		if (x < edgeLen && y < edgeLen) {
            this.data[x][y] = isBlack;
		}
	}
	
	public boolean isBlockBlack(int x, int y) {
		return x < edgeLen && y < edgeLen ? this.data[x][y] : false;
	}
	
	public void clear() {
		for (boolean[] row : this.data) {
			Arrays.fill(row, false);
		}
	}
	
	public void draw(Graphics2D g, Rectangle2D.Double area) {
		double unitX = area.width / this.edgeLen;
		double unitY = area.height / this.edgeLen;
		Rectangle2D.Double block = new Rectangle2D.Double(0, 0, unitX, unitY); 
		for (int y = 0; y < this.edgeLen; y++) {
			for (int x = 0; x < this.edgeLen; x++) {
				if (data[x][y]) {
					block.x = area.x + x * unitX;
					block.y = area.y + y * unitY;
					g.fill(block);
				}
			}
		}
	}
}
