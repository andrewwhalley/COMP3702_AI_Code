import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


@SuppressWarnings("unused")
public class Obstacle extends Rectangle2D {
	
	private Point2D topLeft;
	private Rectangle2D obstacle;
	
	/**
	 * Initialise a new obstacle (Rectangle2D) for the simulation
	 * 
	 * @param x - top left corner x coord
	 * @param y - top left corner y coord
	 * @param width - width of the rectangle
	 * @param height - height of the rectangle
	 */
	public Obstacle(double x, double y, double width, double height) {		
		obstacle = new Rectangle2D.Double(x, y, width, height);
	}
	
	public String toString() {
		return obstacle.getX() + "," + obstacle.getY() + " with width: " + 
				obstacle.getWidth() + " and height: " + obstacle.getHeight() + 
				"\n";
	}

	@Override
	public Rectangle2D createIntersection(Rectangle2D r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle2D createUnion(Rectangle2D r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int outcode(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRect(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
