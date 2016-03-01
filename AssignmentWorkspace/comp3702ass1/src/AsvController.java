import java.util.List;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AsvController {
	/** The maximum distance any ASV can travel between two states */
	public static final double MAX_STEP = 0.001;
	/** The minimum allowable boom length */
	public static final double MIN_BOOM_LENGTH = 0.05;
	/** The maximum allowable boom length */
	public static final double MAX_BOOM_LENGTH = 0.075;
	/** The workspace bounds */
	public static final Rectangle2D BOUNDS = new Rectangle2D.Double(0, 0, 1, 1);
	/** The default value for maximum error */
	public static final double MAX_ERROR = 1e-5;

	private List<Rectangle2D> obstacles;
	private double minArea;
	private int numAsvs;

	public AsvController(int numAsvs, List<Rectangle2D> obstacles) {
		this.obstacles = obstacles;
		this.numAsvs = numAsvs;
		minArea = Math.PI * Math.pow(0.007 * (numAsvs - 1), 2);
	}

	List<Point2D> changeForm(List<Point2D> state, List<Point2D> positions) {
		List<Point2D> endState = new ArrayList<>();

		return endState;
	}

	List<List<Point2D>> move(List<Point2D> state, double x, double y) {
		List<List<Point2D>> stateList = new ArrayList<List<Point2D>>();
		double c = Point2D.distance(0, 0, x, y);
		double maxSteps = c / MAX_STEP;
		double xStep = x / maxSteps;
		double yStep = y / maxSteps;
//		System.out.println(String.format("maxSteps: %f, c: %f,  xStep: %f, yStep: %f", maxSteps, c,
//				xStep, yStep));

		//stateList.add(new ArrayList<Point2D>(state));
		for (int j = 0; j < maxSteps; j++) {
			stateList.add(new ArrayList<Point2D>());
			for (int i = 0; i < numAsvs; i++) {
				stateList.get(j).add(new Point2D.Double(state.get(i).getX() + j
						* xStep, state.get(i).getY() + j * yStep));
			}
		}

		return stateList;
	}

	/**
	 * Changes the angle of part of the ASV system.
	 * 
	 * Takes a state, the index of the ASV to be the pivot and an angle. All
	 * ASVs to the pivots right will have their locations changed by at most
	 * MAX_STEP per state. All ASVs to the left of and including the pivot are
	 * not changed.
	 * 
	 * @param state
	 * @param pivotAsv
	 * @param theta
	 * @return the list of states that occurred during the length change
	 */
	List<List<Point2D>> rotate(List<Point2D> state, int pivotAsv, double theta) {
		List<Asv> asvList = createAsvList(state);
		List<List<Point2D>> stateList = new ArrayList<List<Point2D>>();
		// List<Point2D> currState;
		double arcSteps;
		Asv pivot = asvList.get(pivotAsv);
		Asv end = pivot;

		for (int i = pivotAsv + 1; i < numAsvs; i++) {
			if (pivot.getCoordinates()
					.distance(asvList.get(i).getCoordinates()) > pivot
					.getCoordinates().distance(end.getCoordinates())) {
				end = asvList.get(i);

			}
		}
		// System.out.println(end.getCoordinates());

		arcSteps = Math.abs(Math.ceil((pivot.getCoordinates().distance(
				end.getCoordinates()) * Math.toRadians(theta))
				/ MAX_STEP));
		double arcStepSize;

		double phi = Math.toDegrees(Math.atan2(end.getY() - pivot.getY(),
				end.getX() - pivot.getY()));

		Arc2D arc;
		List<List<double[]>> rotationList = new ArrayList<List<double[]>>();

		for (int i = pivotAsv + 1; i < numAsvs; i++) {
			rotationList.add(new ArrayList<double[]>());
			end = asvList.get(i);
			arc = new Arc2D.Double();
			arc.setArcByCenter(pivot.getX(), pivot.getY(), pivot
					.getCoordinates().distance(end.getCoordinates()), phi,
					-theta, Arc2D.OPEN);
			double[] output = new double[6];

			System.out.println(String.format("Start: %s,  end: %s",
					arc.getStartPoint(), arc.getEndPoint()));

			for (PathIterator it = arc.getPathIterator(null, 1e-6); !it
					.isDone(); it.next()) {
				it.currentSegment(output);
				double[] current = { output[0], output[1] };
				rotationList.get(i - pivotAsv - 1).add(current);
				// System.out.println(String.format("%.5f, %.5f",
				// rotationList.get(i).get(j)[0],
				// rotationList.get(i).get(j)[1]));
			}
		}
		/*
		 * double[] curr = new double[2]; double[] prev = new double[2];
		 * //System.out.println(String.format("Steps: %f", arcSteps)); int count
		 * = 0; for(int i = 0; i < rotationList.size(); i++) { count = 0;
		 * //System.out.println(String.format("---ASV %d--- %d", i + pivotAsv +
		 * 1, rotationList.get(i).size())); arcStepSize =
		 * rotationList.get(i).size()/arcSteps; for(double j = 0; j <
		 * rotationList.get(i).size(); j += arcStepSize) { int index = (int)
		 * Math.ceil(j); if(index > rotationList.get(i).size()-1) {
		 * //System.out.println(String.format("%2.0f     %.5f, %.5f", j, //
		 * rotationList.get(i).get(rotationList.get(i).size()-1)[0], //
		 * rotationList.get(i).get(rotationList.get(i).size()-1)[1])); count++;
		 * break; } curr[0] = rotationList.get(i).get(index)[0]; curr[1] =
		 * rotationList.get(i).get(index)[1];
		 * //System.out.println(String.format("%3.0f     %.5f, %.5f     %f", j,
		 * // rotationList.get(i).get(index)[0], //
		 * rotationList.get(i).get(index)[1], // Point2D.distance(curr[0],
		 * curr[1], prev[0], prev[1]))); prev[0] = curr[0]; prev[1] = curr[1];
		 * count++; } } //System.out.println(count);
		 */
		List<List<double[]>> pointList = new ArrayList<List<double[]>>();

		// stateList.add(state);

		for (int i = 0; i < numAsvs; i++) {
			if (i <= pivotAsv) {
				for (int j = 0; j < arcSteps; j++) {
					if (i == 0) {
						stateList.add(new ArrayList<Point2D>());
					}
					stateList.get(j).add(state.get(i));
				}
				continue;
			}

			int index = i - pivotAsv - 1;
			arcStepSize = rotationList.get(index).size() / arcSteps;
			System.out.println("arcsteps: " + arcSteps);
			pointList.add(new ArrayList<double[]>());
			float currStep = 0;
			// System.out.println(String.format("ASS: %.5f, TEST: %f",
			// arcStepSize, (rotationList.get(index).size() / arcSteps)));

			for (int j = 0; j < arcSteps - 1; j++) {
				double[] posArray = (rotationList.get(index).get((int) Math
						.ceil(currStep)));
				Point2D position = new Point2D.Double(posArray[0], posArray[1]);
				stateList.get(j).add(position);
				// System.out.println(String.format("currStep: %.3f",
				// currStep));
				currStep += arcStepSize;
			}

			double[] posArray = (rotationList.get(index).get(rotationList.get(
					index).size() - 1));
			Point2D position = new Point2D.Double(posArray[0], posArray[1]);
			stateList.get((int) (arcSteps - 1)).add(position);
		}

		// System.out.println("-------");
		return stateList;
	}

	/**
	 * Changes the length of a boom.
	 * 
	 * Takes a state, the index of the ASV to the booms left and a length. All
	 * ASVs to the booms right will have their locations changed by at most
	 * MAX_STEP per state. All ASVs to the left of the boom are not changed.
	 * 
	 * @param state
	 * @param leftAsvIndex
	 * @param boomLength
	 * @return the list of states that occured during the length change
	 */
	List<List<Point2D>> changeBoom(List<Point2D> state, int leftAsvIndex,
			double boomLength) {
		List<Asv> asvList = createAsvList(state);
		List<List<Point2D>> stateList = new ArrayList<List<Point2D>>();
		List<Point2D> currState;
		Asv currAsv = asvList.get(leftAsvIndex);
		Asv nextAsv = currAsv.getRight();

		double c = currAsv.getDistanceRight();
		double ratio = boomLength / c;

		double a = currAsv.getX() + (nextAsv.getX() - currAsv.getX()) * ratio;
		double b = currAsv.getY() + (nextAsv.getY() - currAsv.getY()) * ratio;
		Point2D destination = new Point2D.Double(a, b);
		// System.out.println(String.format(
		// "ratio: %.4f, c: %.4f, a: %.4f, b: %.4f", ratio, c, a, b));
		// System.out.println(String.format("src: %s,  dest: %s",
		// nextAsv.getCoordinates(), destination));

		double deltaC = nextAsv.getCoordinates().distance(destination);
		double deltaX = destination.getX() - nextAsv.getX();
		double deltaY = destination.getY() - nextAsv.getY();
		double xStep = deltaX / (deltaC / MAX_STEP);
		double yStep = deltaY / (deltaC / MAX_STEP);

		// System.out.println(String.format("DC: %f, DX: %f, DY: %f", deltaC,
		// deltaX, deltaY));
		// System.out.println(String.format("xStep: %f, yStep: %f, numSteps: %f",
		// xStep, yStep, deltaC / MAX_STEP));
		// System.out.println(String.format("R: %f, %s", ratio,
		// destination.toString()));

		stateList.add(state);

		while (deltaC > MAX_STEP) {
			currState = new ArrayList<Point2D>();
			for (int i = 0; i < numAsvs; i++) {
				if (i <= leftAsvIndex) {
					currState.add(stateList.get(0).get(i));
				} else {
					asvList.get(i).setCoordinates(
							asvList.get(i).getX() + xStep,
							asvList.get(i).getY() + yStep);
					currState.add((Point2D) asvList.get(i).getCoordinates()
							.clone());
				}
			}
			deltaC -= MAX_STEP;
			stateList.add(currState);
		}

		currState = new ArrayList<Point2D>();
		xStep = destination.getX() - nextAsv.getX();
		yStep = destination.getY() - nextAsv.getY();

		for (int i = 0; i < numAsvs; i++) {
			if (i <= leftAsvIndex) {
				currState.add(stateList.get(0).get(i));
			} else {
				asvList.get(i).setCoordinates(asvList.get(i).getX() + xStep,
						asvList.get(i).getY() + yStep);
				currState
						.add((Point2D) asvList.get(i).getCoordinates().clone());
			}
		}

		stateList.add(currState);

		// System.out.println(stateList);
		return stateList;
	}

	/**
	 * Validates a given state for boom length, convexity, area, bounds and
	 * collisions.
	 * 
	 * Possible return values are: 0 - success 1 - boom length invalid 2 -
	 * convexity invalid 3 - area invalid 4 - bounds invalid 5 - collision
	 * detected
	 * 
	 * @param state
	 * @return the validation status
	 */
	int validateState(List<Point2D> state) {
		List<Asv> asvList = createAsvList(state);

		if (!validateBoomLength(asvList)) {
			return 1;
		} else if (!validateConvexity(asvList)) {
			return 2;
		} else if (!validateArea(asvList)) {
			return 3;
		} else if (validateBounds(asvList)) {
			return 4;
		} else if (validateCollisions(asvList, obstacles)) {
			return 5;
		}

		return 0;
	}

	/**
	 * Creates a list of ASVs and doubly links them.
	 * 
	 * @param state
	 *            the positions of the ASVs
	 * @return a list of ASVs
	 */
	List<Asv> createAsvList(List<Point2D> state) {
		List<Asv> asvList = new ArrayList<Asv>();

		for (int i = 0; i < state.size(); i++) {
			Asv asv = new Asv(state.get(i).getX(), state.get(i).getY());

			if (asvList.size() > 0) {
				asvList.get(asvList.size() - 1).setRight(asv);
				asv.setLeft(asvList.get(asvList.size() - 1));
			} else {
				asv.setLeft(null);
			}
			asv.setRight(null);
			asvList.add(asv);
		}
		return asvList;
	}

	/**
	 * Returns whether the booms lengths are within acceptable ranges.
	 * 
	 * @return if the boom lengths are valid
	 */
	private boolean validateBoomLength(List<Asv> asvList) {
		for (int i = 0; i < numAsvs - 2; i++) {
			if (!(asvList.get(i).getDistanceRight() - MIN_BOOM_LENGTH > -MAX_ERROR && asvList
					.get(i).getDistanceRight() - MAX_BOOM_LENGTH < MAX_ERROR)) {
				return false;
			}
		}
		return true;
	}

	private boolean validateConvexity(List<Asv> asvList) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Calculates the current area of the ASV system and returns if it is larger
	 * than the minimum size
	 * 
	 * @return true if the area is valid
	 */
	private boolean validateArea(List<Asv> asvList) {
		// Area calculation 1 - to be removed
		double area = 0;
		Iterator<Asv> it = asvList.iterator();
		Point2D normal = it.next().getCoordinates();

		while (it.hasNext()) {
			Asv asv = it.next();
			if (!it.hasNext()) {
				break;
			}
			double a = asv.coordinates.distance(normal);
			double b = asv.coordinates.distance(asv.getRight().coordinates);
			double c = normal.distance(asv.getRight().coordinates);
			double p = (a + b + c) / 2;
			area += Math.sqrt(p * (p - a) * (p - b) * (p - c));
		}

		// Area calculation 2 - to be used in production
		double tArea = 0;
		int j = asvList.size() - 1;

		for (int i = 0; i < asvList.size(); i++) {
			tArea += (asvList.get(j).getX() + asvList.get(i).getX())
					* (asvList.get(j).getY() - asvList.get(i).getY());
			j = i;
		}
		tArea *= 0.5;

		if (!((area - tArea) < 0.001 && (area - tArea) > -0.001)) {
			System.err.println(String.format(
					"INCONSISTANT AREA\nvalue 1: %f, value 2: %f\n", area,
					tArea));
		}

		if (tArea >= minArea) {
			return true;
		}
		return false;
	}

	private boolean validateBounds(List<Asv> asvList) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Detects if any of the ASVs or booms are in collision with any obstacles.
	 * 
	 * @return if any collisions have occurred
	 */
	private boolean validateCollisions(List<Asv> asvList,
			List<Rectangle2D> obstacles) {
		Iterator<Rectangle2D> itObs;
		Iterator<Asv> itAsv = asvList.iterator();
		Line2D boom;

		while (itAsv.hasNext()) {
			itObs = obstacles.iterator();
			boom = itAsv.next().getRightBoom();

			if (boom == null) {
				break;
			}

			while (itObs.hasNext()) {
				Rectangle2D obstacle = itObs.next();
				if (obstacle.intersectsLine(boom)) {
					return true;
				}
			}
		}
		return false;
	}

	public String printStates(List<List<Point2D>> stateList) {
		String output = new String();
		for (int i = 0; i < stateList.size(); i++) {
			output += String.format("%03d: ", i);
			for (int j = 0; j < stateList.get(i).size(); j++) {
				// System.out.println(String.format("%d, %d", i, j));
				output += String.format("%d(%.3f,%.3f) ", j, stateList.get(i)
						.get(j).getX(), stateList.get(i).get(j).getY());
				// System.out.println(String.format("%d, %d", i, j));
			}
			output += "\n";
		}
		return output;
	}

	private class Asv {
		private Point2D coordinates;
		private Asv left;
		private Asv right;

		Asv(double x, double y) {
			this.coordinates = new Point2D.Double(x, y);
		}

		/**
		 * Gets the position of the ASV.
		 * 
		 * @return the ASVs coordinates
		 */
		Point2D getCoordinates() {
			return coordinates;
		}

		/**
		 * Returns the distance between this ASV and the ASV to its left.
		 * <p>
		 * Returns null if this ASV is the left-most.
		 * <p>
		 * POSSIBLY REDUNDANT
		 * 
		 * @return the distance between the ASVs
		 */
		double getDistanceLeft() {
			if (left == null) {
				return 0;
			}
			return this.coordinates.distance(this.left.getCoordinates());
		}

		/**
		 * Returns the distance between this ASV and the ASV to its right.
		 * <p>
		 * Returns null if this ASV is the right-most.
		 * 
		 * @return the distance between the ASVs
		 */
		double getDistanceRight() {
			if (right == null) {
				return 0;
			}
			return this.coordinates.distance(this.right.getCoordinates());
		}

		/**
		 * Gets ASV to this ASVs left.
		 * <p>
		 * Null if this ASV is the left-most.
		 * <p>
		 * POSSIBLY REDUNDANT
		 * 
		 * @return previous ASV in the ASV list
		 */
		Asv getLeft() {
			return left;
		}

		/**
		 * Gets ASV to this ASVs right.
		 * <p>
		 * Null if this ASV is the right-most.
		 * 
		 * @return next ASV in the ASV list
		 */
		Asv getRight() {
			return right;
		}

		/**
		 * Creates and returns a line representing a boom between this ASV and
		 * the one to its right
		 * 
		 * @return a line representing a boom
		 */
		Line2D getRightBoom() {
			if (right == null) {
				return null;
			}
			return new Line2D.Double(coordinates, right.getCoordinates());
		}

		/**
		 * Gets the X position of the ASV.
		 * 
		 * @return the ASVs X coordinate
		 */
		double getX() {
			return coordinates.getX();
		}

		/**
		 * Gets the Y position of the ASV.
		 * 
		 * @return the ASVs Y coordinate
		 */
		double getY() {
			return coordinates.getY();
		}

		/**
		 * Sets the coordinates of the ASV.
		 * 
		 * @param x
		 *            the X coordinate
		 * @param y
		 *            the Y coordinate
		 */
		void setCoordinates(double x, double y) {
			this.coordinates.setLocation(x, y);
		}

		/**
		 * Links the ASV with the ASV on its left.
		 * <p>
		 * POSSIBLY REDUNDANT
		 * 
		 * @param left
		 */
		void setLeft(Asv left) {
			this.left = left;
		}

		/**
		 * Links the ASV with the ASV on its right.
		 * 
		 * @param right
		 */
		void setRight(Asv right) {
			this.right = right;
		}

		public String toString() {
			// Print the x,y co-ordinates of the initial and goal positions
			return "x,y: " + coordinates.toString() + "\n";
		}
	}

}