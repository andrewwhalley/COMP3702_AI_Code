import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ASVSearch {

	private ArrayList<Point2D> asvList;
	private ArrayList<Point2D> goalList;
	private ArrayList<String> states;
	private ArrayList<Rectangle2D> obstacles;
	private AsvController AsvCont;
	private boolean collisionDetected;
	private final String OUTPUT_FILE = "output.txt";
	private final String ls = System.getProperty("line.separator");

	/**
	 * Create a new instance of ASVSearch
	 */
	public ASVSearch(List<Point2D> asvs, List<Point2D> goal,
			ArrayList<Rectangle2D> obstacles) {
		this.states = new ArrayList<String>();
		this.asvList = new ArrayList<Point2D>(asvs);
		this.goalList = new ArrayList<Point2D>(goal);
		this.AsvCont = new AsvController(asvs.size(), obstacles);
		this.obstacles = new ArrayList<Rectangle2D>(obstacles);
		StringBuilder initialConfig = new StringBuilder();
		// Add the initial state of the asvs to the output ArrayList
		for (Point2D p : asvList) {
			initialConfig.append(p.getX() + " " + p.getY() + " ");
		}
		states.add(initialConfig.toString());
	}

	private int getBearing(Point2D asv) {
		double xCurr = asv.getX();
		double yCurr = asv.getY();
		double xGoal = this.goalList.get(0).getX();
		double yGoal = this.goalList.get(0).getY();
		double angle = (double) Math.toDegrees(Math.atan2(yGoal - yCurr, xGoal
				- xCurr));
		if (angle < 0) {
			angle += 360;
		}
		return (int) angle;
	}

	/**
	 * Write each state to the given file
	 */
	public void writeFile() throws IOException {
		FileWriter writer = new FileWriter(OUTPUT_FILE);
		writer.write((states.size() - 1) + ls);
		for (String s : states) {
			writer.write(s + ls);
		}
		writer.close();
		System.out.println("Written to file");

	}

	/**
	 * @return true if all asv's are at their goal point
	 */
	private boolean checkGoals(int[] asvGoals) {
		int sum = 0;
		for (int goalInt : asvGoals) {
			sum += goalInt;
		}
		// asvList.size()
		return (sum == 1);
	}

	
	public void findPath() {
		int[] asvGoals = new int[asvList.size()];
		List<List<Point2D>> asvStates = new ArrayList<List<Point2D>>();
		while (true) {
			if (checkGoals(asvGoals)) {
				break;
			}
			// pick asv 0 as a pivot to work with.
			Point2D leadAsv = new Point2D.Double(asvList.get(0).getX(), 
					asvList.get(0).getY());
			while (!checkSingleCollision(leadAsv)) {
				int leadBearing = getBearing(leadAsv);
				System.out.println("Bearing is: " + leadBearing);
				if (leadBearing <= 45 || leadBearing > 315 || 
						collisionDetected) {
					asvStates.addAll(AsvCont.move(asvList, 0.002, 0));
					System.out.println("all states: " + asvStates);
					List<Point2D> leadState = new ArrayList<Point2D>(asvStates.
							get(asvStates.size() - 1));
					asvList = new ArrayList<Point2D>(leadState);
					leadAsv = new Point2D.Double(leadState.get(0).getX(), 
							leadState.get(0).getY());
				}
				if (leadBearing > 45 && leadBearing <= 135 || 
						collisionDetected) {
					asvStates.addAll(AsvCont.move(asvList, 0, 0.002));
					List<Point2D> leadState = new ArrayList<Point2D>(asvStates.
							get(asvStates.size() - 1));
					asvList = new ArrayList<Point2D>(leadState);
					leadAsv = new Point2D.Double(leadState.get(0).getX(), 
							leadState.get(0).getY());
				}
				if (leadBearing > 135 && leadBearing <= 225 || 
						collisionDetected) {
					asvStates.addAll(AsvCont.move(asvList, -0.002, 0));
					List<Point2D> leadState = new ArrayList<Point2D>(asvStates.
							get(asvStates.size() - 1));
					asvList = new ArrayList<Point2D>(leadState);
					leadAsv = new Point2D.Double(leadState.get(0).getX(), 
							leadState.get(0).getY());
				}
				if (leadBearing > 225 && leadBearing <= 315 || 
						collisionDetected) {
					asvStates.addAll(AsvCont.move(asvList, 0, -0.002));
					List<Point2D> leadState = new ArrayList<Point2D>(asvStates.
							get(asvStates.size() - 1));
					asvList = new ArrayList<Point2D>(leadState);
					leadAsv = new Point2D.Double(leadState.get(0).getX(), 
							leadState.get(0).getY());
				}
				System.out.println(leadAsv);
			}
			System.out.println("in collision");
			System.exit(0);
			while (checkSingleCollision(leadAsv)) {
				// rotate away from the collision
				
			}
				
		}
	}
	
	/**
	 * Find a path to the goal from the initial
	 */
//	public void findPath() {
//		/*
//		 * Iterate through each asv. Each time this is done, it is considered to
//		 * be a 'primitive step'
//		 */
//		int[] asvGoals = new int[asvList.size()];
//		while (true) {
//			if (checkGoals(asvGoals)) {
//				break;
//			}
//			StringBuilder asvStateString = new StringBuilder();
//			for (int i = 0; i < asvList.size(); i++) {
//				// Check if Asv at index i has reached the goal
//				if (goalReached(i) && asvGoals[i] != 1) {
//					/*
//					 * Mark asv as having reached the goal and hence don't move
//					 * it
//					 */
//					asvGoals[i] = 1;
//				}
//				double moveCoords;
//				collisionDetected = false;
//				// Start by getting the bearing to the goal
//				int bearing = getBearing(i);
//				// Find a spot to move that is not in collision
//				while (true) {
//					if (bearing <= 45 || bearing > 315 || collisionDetected) {
//						// goal is to the right
//						collisionDetected = false;
//						moveCoords = checkRight(i);
//						if (!collisionDetected) {
//							List<List<Point2D>> statesList = new ArrayList<List
//									<Point2D>>(AsvCont.move(asvList, moveCoords,
//											0));
//							asvList = new ArrayList<Point2D>(
//									statesList.get(statesList.size() - 1));
//							// asvList.get(i).setLocation(moveCoords);
//						}
//					}
//					if (bearing > 45 && bearing <= 135 || collisionDetected) {
//						// goal is up
//						collisionDetected = false;
//						moveCoords = checkUp(i);
//						if (!collisionDetected) {
//							List<List<Point2D>> statesList = new ArrayList<List
//									<Point2D>>(AsvCont.move(asvList, 0,
//											moveCoords));
//							asvList = new ArrayList<Point2D>(
//									statesList.get(statesList.size() - 1));
////							asvList.get(i).setLocation(moveCoords);
//						}
//					}
//					if (bearing > 135 && bearing <= 225 || collisionDetected) {
//						// goal is to the left
//						collisionDetected = false;
//						moveCoords = checkLeft(i);
//						if (!collisionDetected) {
//							List<List<Point2D>> statesList = new ArrayList<List
//									<Point2D>>(AsvCont.move(asvList, moveCoords,
//											0));
//							asvList = new ArrayList<Point2D>(
//									statesList.get(statesList.size() - 1));
////							asvList.get(i).setLocation(moveCoords);
//						}
//
//					}
//					if (bearing > 225 && bearing <= 315 || collisionDetected) {
//						// goal is down
//						collisionDetected = false;
//						moveCoords = checkDown(i);
//						if (!collisionDetected) {
//							List<List<Point2D>> statesList = new ArrayList<List
//									<Point2D>>(AsvCont.move(asvList, 0, 
//											moveCoords));
//							asvList = new ArrayList<Point2D>(
//									statesList.get(statesList.size() - 1));
////							asvList.get(i).setLocation(moveCoords);
//						}
//					}
//					if (!collisionDetected) {
//						break;
//					}
//				}
//				// Append the Asv's new location to the output string
//				asvStateString.append(asvList.get(i).getX() + " "
//						+ asvList.get(i).getY() + " ");
//			}
//			System.out.println("state: " + asvStateString.toString());
//			// Add the points string to the states array
//			states.add(asvStateString.toString());
//		}
//		try {
//			writeFile();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}

	/**
	 * Check to see if the ASV will be in collision if it moves to a given point
	 * 
	 * @param state
	 * @return
	 */
	private boolean checkSingleCollision(Point2D state) {
//		for (Rectangle2D r : obstacles) {
//			if (r.contains(state)) {
//				System.out.println("true for: " + state.toString());
//				return true;
//			}
//		}
		return false;
	}

	/**
	 * @return true if the asv is at the goal position
	 */
	private boolean goalReached(int index) {
		// System.out.println("asv: " + asvList.get(index).toString() +
		// " goal: "
		// + goalList.get(index).toString());
		if (Math.abs(asvList.get(index).getX() - goalList.get(index).getX()) < 0.001
				&& Math.abs(asvList.get(index).getY()
						- goalList.get(index).getY()) < 0.00001) {
			return true;
		}
		return false;
	}

	/**
	 * Check to see if the best movement is up and to the right
	 * 
	 * @return the coordinates of the next movement that will take the Asv
	 *         closer to the goal
	 */
//	private double checkRight(int index) {
//		// get distance of each point it could move to (up or right)
//		// TODO: Check for border conditions, i.e. getX = 1 and getY = 1
//		Point2D moveRight = new Point2D.Double(
//				asvList.get(index).getX() + 0.001, asvList.get(index).getY());
//		// check if in collision
//		if (checkSingleCollision(moveRight)) {
//			collisionDetected = true;
//		} else {
//			collisionDetected = false;
//			return 0.002;
//		}
//		// return generic point if others are in collision.
//		return 0;
//	}
//
//	private double checkUp(int index) {
//		Point2D moveUp = new Point2D.Double(asvList.get(index).getX(), asvList
//				.get(index).getY() + 0.001);
//		// check if in collision
//		if (checkSingleCollision(moveUp)) {
//			collisionDetected = true;
//		} else {
//			// return the coordinates if it isn't in collision
//			collisionDetected = false;
//			return 0.002;
//		}
//		// return closest one to goal
//		return 0;
//	}
//
//	/**
//	 * 
//	 * @return the coordinates of the next movement that will take the Asv
//	 *         closer to the goal
//	 */
//	private double checkLeft(int index) {
//		// get point to the left it could move to
//		// TODO: Check border case getX = 0
//		Point2D moveLeft = new Point2D.Double(
//				asvList.get(index).getX() - 0.001, asvList.get(index).getY());
//		// check if in collision
//		if (checkSingleCollision(moveLeft)) {
//			collisionDetected = true;
//		} else {
//			// return the coordinates if it isn't in collision
//			collisionDetected = false;
//			return -0.002;
//		}
//		// return closest one to goal
//		return 0;
//	}
//
//	/**
//	 * 
//	 * @return the coordinates of the next movement that will take the Asv
//	 *         closer to the goal
//	 */
//	private double checkDown(int index) {
//		// get point to bottom it could move to
//		// TODO: Check border case getY = 0
//		Point2D moveDown = new Point2D.Double(asvList.get(index).getX(),
//				asvList.get(index).getY() - 0.001);
//		// check if in collision
//		if (checkSingleCollision(moveDown)) {
//			collisionDetected = true;
//		} else {
//			// return the coordinates if it isn't in collision
//			collisionDetected = false;
//			return -0.002;
//		}
//		// return closest one to goal
//		return 0;
//	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Asvs:\n");
		s.append(AsvCont.toString());
		s.append("\nObstacles:\n");
		for (Rectangle2D o : this.obstacles) {
			s.append(o);
		}
		return s.toString();
	}

}
