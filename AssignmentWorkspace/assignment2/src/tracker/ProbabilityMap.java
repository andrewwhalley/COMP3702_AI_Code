package tracker;

import game.ActionResult;
import game.AgentState;
import game.Percept;
import game.RectRegion;
import game.SensingParameters;
import geom.GeomTools;
import geom.GridCell;
import geom.TargetGrid;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import target.TargetPolicy;

public class ProbabilityMap {
	private final Double EPSILON = 1e-6;
	private double MAX_SIGHT_DISTANCE_ERROR = 1e-5;
	private TargetPolicy policy;
	private TargetGrid grid;
	private MotionProbability motion;
	private SensingParameters targetSP;
	private SensingParameters trackerSP;
	List<Map<GridCell, State>> p;
	private Map<GridCell, State> previous;
	private List<RectRegion> obstacles;
	private int horizon;
	private double targetScore;
	private final Rectangle2D field = new Rectangle2D.Double(0, 0, 1, 1);

	public ProbabilityMap(TargetPolicy policy, MotionProbability motion,
			SensingParameters targetSP, SensingParameters trackerSP,
			List<AgentState> targetInitialStates, List<RectRegion> obstacles,
			int horizon) {
		this.policy = policy;
		this.grid = policy.getGrid();
		this.motion = motion;
		this.obstacles = obstacles;
		this.horizon = horizon ;
		this.trackerSP = trackerSP;
		this.targetSP = targetSP;
		targetScore = 0;
		p = new ArrayList<Map<GridCell, State>>();
		p.add(updateTargetLocation(targetInitialStates.get(0)));
	}

	/**
	 * Updates the probability map and future predictions with the information
	 * gained this turn. This includes seeing the target, being seen y the
	 * target and seeing cells where the target is not.
	 * 
	 * @param previousResult
	 *            the resulting state from the previous turn
	 * @param scores
	 *            the current scores
	 * @param newPercepts
	 *            any percepts gained from seeing the target
	 */
	public void update(ActionResult previousResult, double[] scores,
			List<Percept> newPercepts) {
		previous = p.get(0);
		p.clear();
		p.add(previous);
		switch (newPercepts.size()) {
		// the tracker has line of sight on the target
		case 2:
			p.add(updateTargetLocation(newPercepts.get(1).getAgentState()));
			break;
		// the tracker has seen the target but it may have moved
		case 1:
			// the tracker has line of sight on the target
			if (newPercepts.get(0).getTurnNo() % 2 == 0) {
				p.add(updateTargetLocation(newPercepts.get(0).getAgentState()));
				break;
				// the tracker had line of sight on the target, but the target
				// has had a turn to move
			} else {
				// System.out.println("PING");
				Map<GridCell, State> temp = new HashMap<GridCell, State>();
				temp = updateTargetLocation(newPercepts.get(0).getAgentState());
				p.add(predict(temp, 1, previousResult.getResultingState()).get(
						1));
				break;
			}
			// the target has not been seen
		default:
			// the target has seen the tracker
			if (scores[1] > targetScore) {
				targetScore = scores[1];
				p.add(updateTargetLocation(previous, previousResult
						.getResultingState().getPosition()));
				// when neither agent has seen each other
			} else {
				/*
				previous = predict(previous, 1,
						previousResult.getResultingState()).get(1);
				*/
				p.add(updateTrackerView(previous,
						previousResult.getResultingState()));
			}
			break;
		}
		
		// System.out.println(p.get(0));
		// System.out.println("---");
		p = predict(p.get(0), horizon, previousResult.getResultingState());
		// System.out.println(p);

	}

	/**
	 * Updates the previous prediction of the current turn by eliminating the
	 * cells the target has been observed not to be in.
	 * 
	 * @param current
	 *            the turn to be modified
	 * @param tracker
	 *            the state of the tracker
	 * @return the updated map of probabilities
	 */
	Map<GridCell, State> updateTrackerView(Map<GridCell, State> current,
			AgentState tracker) {
		Map<GridCell, State> pMap = new HashMap<GridCell, State>(current);
		Double total = 0.0;

		for (GridCell cell : current.keySet()) {
			if (GeomTools.canSee(tracker, grid.getCentre(cell), trackerSP,
					obstacles, MAX_SIGHT_DISTANCE_ERROR)) {
				pMap.remove(cell);
			}
		}
		for (State state : pMap.values()) {
			total += state.getTotalProbability();
		}
		// System.out.println(total);
		for (State state : pMap.values()) {
			state.adjustProbability(1 / total);
		}
		// System.out.println(pMap.values());

		return pMap;
	}

	/**
	 * Updates a probability map when the target has been seen and its location
	 * is known. Clears all probabilities and enters a single cell with 100%
	 * probability
	 * 
	 * @param target
	 *            the agent state from a percept of the target
	 */
	Map<GridCell, State> updateTargetLocation(AgentState target) {
		Map<GridCell, State> pMap = new HashMap<GridCell, State>();

		GridCell cell = grid.getCell(target.getPosition());
		Double heading = target.getHeading();
		pMap.put(cell, new State());
		pMap.get(cell).add(grid.getCodeFromHeading(heading), 1.0);

		return pMap;
	}

	/**
	 * Updates the probability map when the target has been seen and its
	 * location is known. Clears all probabilities and enters a single cell with
	 * 100% probability
	 * 
	 * @param cell
	 *            the cell the target was seen in
	 * @param heading
	 *            the heading of the target in radians
	 */
	Map<GridCell, State> updateTargetLocation(GridCell cell, Double heading) {
		Map<GridCell, State> pMap = new HashMap<GridCell, State>();

		pMap.put(cell, new State());
		pMap.get(cell).add(grid.getCodeFromHeading(heading), 1.0);

		return pMap;
	}

	/**
	 * Updates the probability map when the tracker has been seen but the target
	 * cannot be seen. Distributes 100% probability over the cells the target
	 * could be in.
	 * 
	 * @param current
	 *            the map to be updated
	 * @param location
	 *            the location of the tracker
	 */
	Map<GridCell, State> updateTargetLocation(Map<GridCell, State> current,
			Point2D location) {
		Map<GridCell, State> pMap = new HashMap<GridCell, State>();
		// current = p.get(horizon);
		Double total = 0.0;
		Double probability;
		Map<GridCell, Map<Integer, Double>> t = new HashMap<GridCell, Map<Integer, Double>>();

		AgentState targetState;

		for (GridCell cell : current.keySet()) {
			for (Integer action : current.get(cell).getHeadings()) {
				targetState = new AgentState(grid.getCentre(cell),
						grid.getHeading(action));
				if (GeomTools.canSee(targetState, location, targetSP,
						obstacles, MAX_SIGHT_DISTANCE_ERROR)) {
					if (!t.containsKey(cell)) {
						t.put(cell, new HashMap<Integer, Double>());
					}

					probability = current.get(cell).getHeadingProbability(
							action);
					t.get(cell).put(action, probability);

					total += probability;
				}
			}
		}

		for (GridCell cell : t.keySet()) {
			pMap.put(cell, new State());
			for (Integer action : t.get(cell).keySet()) {
				probability = t.get(cell).get(action) / total;
				pMap.get(cell).add(action, probability);
			}
		}
		return pMap;
		// System.out.println(p.get(0));
	}

	/**
	 * Adds a probability to a probability map. Unlike a regular map put
	 * operation, duplicate values are added and not replaced. This allows for
	 * summation of probabilities.
	 * 
	 * @param turn
	 *            the turn to have a probability state added to
	 * @param cell
	 *            the cell to have a probability state added to
	 * @param state
	 *            the state to be added
	 */
	private void addProbability(Map<GridCell, State> turn, GridCell cell,
			Integer heading, Double probability) {
		if (!turn.containsKey(cell)) {
			turn.put(cell, new State());
		}
		turn.get(cell).add(heading, probability);
	}

	double getFindProbability(int depth, AgentState tracker) {
		AgentState target;
		double prob = 0.0;
		for (GridCell cell : p.get(depth+1).keySet()) {
			if (grid.getCentre(cell).distance(tracker.getPosition()) > trackerSP
					.getRange() + MAX_SIGHT_DISTANCE_ERROR) {
				continue;
			}

			for (Integer heading : p.get(depth+1).get(cell).getHeadings()) {
				target = new AgentState(grid.getCentre(cell),
						grid.getHeading(heading));
				if (GeomTools.canSee(tracker, target, trackerSP, obstacles,
						MAX_SIGHT_DISTANCE_ERROR, 0)) {
					prob += p.get(depth+1).get(cell)
							.getHeadingProbability(heading);
				}
			}

			if (GeomTools.canSee(tracker, grid.getCentre(cell), trackerSP,
					obstacles, MAX_SIGHT_DISTANCE_ERROR)) {

			}
		}
		//System.out.println(prob);
		return prob;
	}

	/**
	 * Returns the probability the target is in a given cell
	 * 
	 * @param turn
	 *            the turn to be checked against
	 * @param cell
	 *            the cell to be checked
	 * @return the probability the target is in the given cell
	 */
	Double getLocationProbability(int depth, GridCell cell) {
		if (p.get(depth).containsKey(cell)) {
			return p.get(depth).get(cell).total;
		}
		return 0.0;
	}

	/**
	 * Calculates the chance of the target being able to see an agent
	 * 
	 * @param state
	 *            the state of the agent being observed
	 * @return the chance of the agent being in line of sight
	 */
	Double getSightProbability(int depth, AgentState state) {
		return getSightProbability(depth, state.getPosition());
	}

	/**
	 * Calculates the chance of the target being able to see a location
	 * 
	 * @param location
	 *            the location to be checked
	 * @return the chance of the location being in line of sight
	 */
	Double getSightProbability(int depth, Point2D location) {
		Double total = 0.0;
		AgentState targetState;

		for (GridCell cell : p.get(depth+1).keySet()) {
			for (Integer heading : p.get(depth+1).get(cell).getHeadings()) {
				targetState = new AgentState(grid.getCentre(cell),
						grid.getHeading(heading));
				if (GeomTools.canSee(targetState, location, targetSP,
						obstacles, MAX_SIGHT_DISTANCE_ERROR)) {
					total += p.get(depth+1).get(cell)
							.getHeadingProbability(heading);
				}
			}
		}
		return total;
	}

	/**
	 * Generates probability maps for the number of turns ahead given as
	 * horizon. Movement is calculated from a MotionProbability given during
	 * object creation.
	 * 
	 * @param baseState
	 *            the original state
	 * @param horizon
	 *            the depth to predict to
	 * @param trackerState
	 *            the starting state of the tracker
	 * @return a list of maps, where each map represents a turn
	 */
	List<Map<GridCell, State>> predict(Map<GridCell, State> baseState,
			Integer horizon, AgentState trackerState) {
		double parentProb;
		Map<Integer, Double> actionProb;
		List<Integer> actionList = new ArrayList<Integer>();
		List<Map<GridCell, State>> predictions = new ArrayList<Map<GridCell, State>>();

		predictions.add(baseState);

		// Loop through turns to be predicted
		for (Integer turn = 1; turn < horizon + 1; turn++) {

			predictions.add(new HashMap<GridCell, State>());

			// Loop through the cells of the previous turn
			for (GridCell cell : predictions.get(turn - 1).keySet()) {
				actionList.clear();
				// System.out.println(grid.getCell(trackerState.getPosition()));
				// Find surrounding cells
				for (int x = -1; x < 2; x++) {
					for (int y = -1; y < 2; y++) {
						GridCell endCell = new GridCell(cell.getRow() + y,
								cell.getCol() + x);
						// Add valid cells to a list
						if (inBounds(endCell)
								&& GeomTools.canMove(grid.getCentre(cell),
										grid.getCentre(endCell), false, 0,
										obstacles)) {
							// System.out.println(endCell);
							actionList.add(grid
									.encodeFromIndices(cell, endCell));
						}
					}
				}

				GridCell nextCell = policy.getNextIndex(cell);

				actionProb = motion.getProbability(
						grid.encodeFromIndices(cell, nextCell), actionList);

				parentProb = predictions.get(turn - 1).get(cell)
						.getTotalProbability();

				for (Integer action : actionProb.keySet()) {
					double cellProb = actionProb.get(action);

					if (cellProb < EPSILON)
						continue;

					addProbability(predictions.get(turn),
							grid.decodeFromIndices(cell, action), action,
							cellProb * parentProb);
				}
			}

			/*
			 * double total = 0.0; for (GridCell c : p.get(turn).keySet()) {
			 * //System.out.println(String.format("c: %s, p: %.6f", c, //
			 * p.get(turn).get(c).getProbability())))); total +=
			 * p.get(turn).get(c).getProbability(); }
			 * System.out.println(p.get(turn).get(new GridCell(5, 5)));
			 * System.out.println("--- " + total);
			 */
		}
		return predictions;
	}

	/**
	 * Checks if a given cell is valid (not in collision with an obstacle or out
	 * of bounds)
	 * 
	 * @param cell
	 *            the cell to be checked
	 * @return boolean result of the check
	 */
	private boolean inBounds(GridCell cell) {
		Point2D point = grid.getCentre(cell);
		if (!field.contains(point))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return p.get(0).toString();
	}

	/**
	 * class representing a breakdown of the probabilities in a given cell by
	 * heading
	 */
	private class State implements Comparable<State> {
		private Map<Integer, Double> m;
		private Double total = 0.0;

		public State() {
			m = new TreeMap<Integer, Double>();
		}

		/**
		 * Adds a probability to the map. This differs to a regular map by
		 * adding to an existing value instead of replacing it, allowing for
		 * summation of probabilities
		 * 
		 * @param heading
		 *            the given heading
		 * @param probability
		 *            the probability of the action having occurred
		 */
		public void add(int heading, Double probability) {
			if (m.containsKey(heading)) {
				m.put(heading, m.get(heading) + probability);
			} else {
				m.put(heading, probability);
			}
			total += probability;
		}

		/**
		 * Re-weighs the probabilites by a given ratio
		 * 
		 * @param ratio
		 *            the ratio to be adjusted by
		 */
		void adjustProbability(Double ratio) {
			for (int heading : m.keySet()) {
				m.put(heading, m.get(heading) * ratio);
			}
			total = 0.0;
			for (Double val : m.values()) {
				total += val;
			}
		}

		/**
		 * Returns the probability of a target having a given heading if it is
		 * in this cell. This is out of the probability that the target is in
		 * this cell (i.e. the total of all headings does not necessarily add to
		 * 1.0)
		 * 
		 * @param heading
		 *            the given heading
		 * @return the probability that the target is in this cell with a given
		 *         heading
		 */
		public Double getHeadingProbability(int heading) {
			if (m.containsKey(heading))
				return m.get(heading);
			return 0.0;
		}

		/**
		 * Returns the probability of the target being in this cell with any
		 * heading
		 * 
		 * @return the probability that the target is in this cell
		 */
		public Double getTotalProbability() {
			return total;
		}

		/**
		 * Returns a set containing the headings contained in this state
		 * 
		 * @return the set of headings
		 */
		public Set<Integer> getHeadings() {
			return m.keySet();
		}

		@Override
		public String toString() {
			return "[m=" + m + ", total=" + total + "]";
		}

		@Override
		public int compareTo(State o) {
			Double comparator = total - o.getTotalProbability();

			if (comparator > 0) {
				return 1;
			} else if (comparator < 0) {
				return -1;
			}
			return 0;
		}
	}
}
