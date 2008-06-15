package org.drools.examples.learner;

import org.drools.learner.tools.ClassAnnotation;
import org.drools.learner.tools.FieldAnnotation;


@ClassAnnotation(label_element = "getLabel")
public class Triangle {
	
	@FieldAnnotation(readingSeq = 0, discrete = false)
	private double x;
	
	@FieldAnnotation(readingSeq = 1, discrete = false)
	private double y;
	
	@FieldAnnotation(readingSeq = 2, discrete = false)
	private double z;
	
	/* Triangle rules
	 * - The sum of every two sides of a triangle must be greater than the third side.
	 * or 
	 * - The length of any side of a triangle is greater than the absolute difference 
	 * of the lengths of the other two sides 
	 */
	public boolean getLabel() {
		return (z < x + y) && (x < z + y) && (y < x + z);
	}
	public Triangle() {

	}
	public Triangle(double[] ds) {
		this.x= ds[0];
		this.y= ds[1];
		this.z= ds[2];
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
}
