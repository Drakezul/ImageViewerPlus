package de.grueb.crop;

import java.awt.Point;
import java.awt.Rectangle;

public class GeometryUtils {
	
	public static Rectangle calculateRectangle(Point a, Point b) {
		int x = Math.min(a.x, b.x);
		int y = Math.min(a.y, b.y);
		int width = Math.abs(b.x - a.x);
		int height = Math.abs(b.y - a.y);
		return new Rectangle(x, y, width, height);
	}

	public static Point forceBounds(final Point input, int minX, int maxX, int minY, int maxY) {
		Point p = new Point(input.x, input.y);
		p.x = Math.max(Math.min(p.x, maxX), minX);
		p.y = Math.max(Math.min(p.y, maxY), minY);
		return p;
	}

}
