package de.grueb.crop.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import de.grueb.crop.GeometryUtils;

public class ImageCropper {

	Point origin = new Point(0, 0), x = new Point(50, 0), y = new Point(0, 50), xy = new Point(50, 50);

	@Test
	void calculateRectangleOriginX() {
		assertEquals(new Rectangle(0, 0, 50, 0), GeometryUtils.calculateRectangle(origin, x));
	}
	
	@Test
	void calculateRectangleOriginY() {
		assertEquals(new Rectangle(0, 0, 0, 50), GeometryUtils.calculateRectangle(origin, y));
	}
	
	@Test
	void calculateRectangleOriginXY() {
		assertEquals(new Rectangle(0, 0, 50, 50), GeometryUtils.calculateRectangle(origin, xy));
	}
	
	@Test
	void calculateRectangleXOrigin() {
		assertEquals(new Rectangle(0, 0, 50, 0), GeometryUtils.calculateRectangle(x, origin));
	}
	
	@Test
	void calculateRectangleXY() {
		assertEquals(new Rectangle(0, 0, 50, 50), GeometryUtils.calculateRectangle(x, y));
	}
	
	@Test
	void calculateRectangleXXY() {
		assertEquals(new Rectangle(50, 0, 0, 50), GeometryUtils.calculateRectangle(x, xy));
	}
	
	@Test
	void calculateRectangleXYOrigin() {
		assertEquals(new Rectangle(0, 0, 50, 50), GeometryUtils.calculateRectangle(xy, origin));
	}
		
	@Test
	void calculateRectangleXYX() {
		assertEquals(new Rectangle(50, 0, 0, 50), GeometryUtils.calculateRectangle(xy, x));
	}
	
	@Test
	void calculateRectangleXYY() {
		assertEquals(new Rectangle(0, 50, 50, 0), GeometryUtils.calculateRectangle(xy, y));
	}
	
	@Test
	void calculateRectangleYOrigin() {
		assertEquals(new Rectangle(0, 0, 0, 50), GeometryUtils.calculateRectangle(y, origin));
	}
	
	@Test
	void calculateRectangleYX() {
		assertEquals(new Rectangle(0, 0, 50, 50), GeometryUtils.calculateRectangle(y, x));
	}
	
	@Test
	void calculateRectangleYXY() {
		assertEquals(new Rectangle(0, 50, 50, 0), GeometryUtils.calculateRectangle(y, xy));
	}
}
