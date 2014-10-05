package org.programus.image.utils.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = -651244046714769459L;

	private BufferedImage image;
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.image != null) {
			Graphics2D g2d = (Graphics2D) g;
			double w = this.getWidth();
			double h = this.getHeight();
			double iw = this.image.getWidth();
			double ih = this.image.getHeight();
			double scale = Math.min(w / iw, h / ih);
			g2d.translate(w / 2, h / 2);
			if (scale < 1) {
				g2d.scale(scale, scale);
			}
			g2d.translate(-iw / 2, -ih / 2);
			g2d.drawImage(image, 0, 0, null);
		}
	}
}
