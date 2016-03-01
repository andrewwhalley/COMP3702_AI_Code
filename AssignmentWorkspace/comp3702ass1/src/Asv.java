import java.awt.Point;
import java.awt.geom.Point2D;

public class Asv {
	Point2D coords;
	Point2D goalCoords;
	Asv left;
	Asv right;
	
	public Asv(double x, double y, double xFin, double yFin, Asv left, Asv right) {
		this.coords = new Point2D.Double(x, y);
		this.goalCoords = new Point2D.Double(xFin, yFin);
		this.left = left;
		this.right = right;
	}
	
	public void setCoords(Point coords) {
		this.coords = coords;
	}
	
	public double getDistanceLeft() {
		if(left == null) {
			return 0;
		}
		return this.coords.distance(this.left.getCoords());
	}
	
	public double getDistanceRight() {
		if(right == null) {
			return 0;
		}
		return this.coords.distance(this.right.getCoords());
	}
	
	public Point2D getCoords() {
		return coords;
	}
	
	public Point2D getGoalCoords() {
		return goalCoords;
	}
	
	public Asv getLeft() {
		return left;
	}
	
	public Asv getRight() {
		return right;
	}
	
	public String toString() {
		// Print the x,y co-ordinates of the initial and goal positions
		return "x,y: " + coords.toString() + " goal: " + goalCoords.toString()
				+ "\n";
	}
}
