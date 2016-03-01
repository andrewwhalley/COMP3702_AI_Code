package tracker;

import game.ActionResult;
import game.Agent;
import game.AgentState;
import game.Percept;
import game.RectRegion;
import game.SensingParameters;
import game.TrackerAction;
import geom.GeomTools;
import geom.GridCell;
import geom.TargetGrid;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import target.TargetPolicy;
import divergence.MotionHistory;



public class Tracker implements Agent {
	/** The number of targets. */
	private int numTargets;
	/** The policy of the target(s). */
	private TargetPolicy targetPolicy;
	/**
	 * The motion history of the target(s), or null if no history is available.
	 * */
	private MotionProbability targetMotion;
	/** The sensing parameters of the target(s). */
	private SensingParameters targetSensingParams;
	/** The initial state(s) of the target(s). */
	private List<AgentState> targetInitialStates;

	/** The motion history of this tracker. */
	private MotionProbability trackerMotion;
	/** The sensing parameters of this tracker. */
	private SensingParameters mySensingParams;
	/** The initial state of this tracker. */
	private AgentState myInitialState;

	/** The obstacles. */
	private List<RectRegion> obstacles;
	/** The goal region. */
	private RectRegion goalRegion;
	

	/** Variables for checking if tracker can see the target */
	private final double MAX_SIGHT_DISTANCE_ERROR = 1e-5;
	private final int NUM_CAMERA_ARM_STEPS = 1000;

	/** Number of turns ahead to predict */
	private final int HORIZON = 2;
	
	/** The priority queue of potential actions */
	private Queue<WeightedAction> actionQueue;
	
	private final int HEADING_PRECISION = 4;
	private double stepSize;
	private double rotationAngle;
	private final Rectangle2D field = new Rectangle2D.Double(0, 0, 1, 1);
	private ProbabilityMap pm;
	

	/**
	 * Constructs a tracker with the given parameters.
	 * 
	 * This gives your tracker all of the information it has about the initial
	 * state of the game, including very important aspects such as the target's
	 * policy, which is known to the tracker.
	 * 
	 * @param numTargets
	 *            the number of targets.
	 * @param targetPolicy
	 *            the policy of the target(s).
	 * @param targetMotionHistory
	 *            the motion history of the target(s), or null if no history is
	 *            available.
	 * @param targetSensingParams
	 *            the sensing parameters of the target(s).
	 * @param targetInitialStates
	 *            the initial state(s) of the target(s).
	 * @param trackerMotionHistory
	 *            the motion history of this tracker.
	 * @param trackerSensingParams
	 *            the sensing parameters of this tracker.
	 * @param trackerInitialState
	 *            the initial state of this tracker.
	 * @param obstacles
	 *            the obstacles.
	 * @param goalRegion
	 *            the goal region.
	 */
	public Tracker(int numTargets, TargetPolicy targetPolicy,
			MotionHistory targetMotionHistory,
			SensingParameters targetSensingParams,
			List<AgentState> targetInitialStates,

			MotionHistory trackerMotionHistory,
			SensingParameters trackerSensingParams,
			AgentState trackerInitialState,

			List<RectRegion> obstacles, RectRegion goalRegion) {
		this.numTargets = numTargets;
		this.targetPolicy = targetPolicy;
		this.targetMotion = new MotionProbability(targetMotionHistory, true);
		this.targetSensingParams = targetSensingParams;
		this.targetInitialStates = targetInitialStates;

		this.trackerMotion = new MotionProbability(trackerMotionHistory, false);
		this.mySensingParams = trackerSensingParams;
		this.myInitialState = trackerInitialState;

		this.obstacles = obstacles;
		this.goalRegion = goalRegion;
		initialise();
	}

	/**
	 * Initialises the tracker's policy.
	 * 
	 * This handles any setup your agent requires for its policy before the game
	 * actually starts. If you don't require any setup, leave this method blank.
	 */
	public void initialise() {
		actionQueue = new PriorityQueue<WeightedAction>();
		stepSize = 1.0/targetPolicy.getGridSize();
		rotationAngle = 1.0/HEADING_PRECISION;
		pm = new ProbabilityMap(targetPolicy, targetMotion, targetSensingParams, mySensingParams, targetInitialStates, obstacles, HORIZON);
	}
	
	/**
	 * This method is called when the tracker wants to determine if it requires 
	 * assistance from headquarters. This occurs if the target is out of the 
	 * tracker's view. 
	 */
	public boolean callHeadquarters(AgentState trackerState) {
//		if (GeomTools.canSee(trackerState, point, mySensingParams, 
//				obstacles, MAX_SIGHT_DISTANCE_ERROR)) {
//			
//		}
		return false;
	}

	@Override
	/**
	 * This method is used by the game to ask your tracker for an action
	 * when it is the tracker's turn.
	 * 
	 * It also passes your tracker all of the information it is allowed
	 * to have about the changing of the state of the game. In particular,
	 * this includes the following:
	 * 
	 * @param turnNo the current turn. This is the turn number within the game;
	 * this will be 0 for your very first turn, then 2, then 4, etc. 
	 * This is always even because odd-numbered turns are taken by the targets.
	 * 
	 * @param previousResult the result of the previous attempted action. This
	 * contains four components:
	 * previousResult.getDesiredAction() - the action that was previously
	 * 		attempted by this tracker, or null if there is no such action.
	 *		In other words, this is the output from the last time this
	 * 		method was called, or null if this is the first time this method
	 * 		is being called.
	 * previousResult.getDivergedAction() - the action that resulted after random
	 * 		divergence was applied to your desired action, or null if there was 
	 * 		no such action.
	 * previousResult.getResultingState() - the state this resulted in, which is
	 * 		also the current state of your tracker.
	 * previousResult.getReward() - the reward obtained for the previous
	 * action.
	 * 
	 * @param scores the scores accrued by each individual player.
	 * Note that player #0 is the tracker, and players #1, #2, etc. are
	 * targets. 
	 * In order to win the game, your score will need to be higher than
	 * the total of all of the target scores.
	 * Normally numTargets = 1, so scores will only consist of two numbers.
	 * 
	 * @param newPercepts any percepts obtained by your tracker since
	 * its last turn.
	 * For example, if it's currently turn #2, this will contain any percepts
	 * from turns #0 and #1.
	 * The percept consists of three components:
	 * percept.getAgentNo() - the agent that was seen.
	 * percept.getTurnNo() - the turn after which it was seen.
	 * percept.getAgentState() - the state the agent was seen in.
	 */
	public TrackerAction getAction(int turnNo, ActionResult previousResult,
			double[] scores, List<Percept> newPercepts) {
		AgentState myState = previousResult.getResultingState();

		/*
		// TODO Write this method!

		// System.out.println(newPercepts);
		// Check to see if the tracker should call headquarters.
		if (callHeadquarters(myState)) {
			// TODO Secondary check to see if it is the best action
			return new TrackerAction(myState, true);
		}
		if (this.mySensingParams.hasCamera()) {
			// Want to adjust the length of the camera if it is being used.
			// TODO Secondary check to see if it is the best action
		}
		// TODO Form the probability map.
		// TODO Calculate utility of each action. Choose action with lowest utility.
		TargetGrid grid = targetPolicy.getGrid();
		GridCell current = grid.getCell(myState.getPosition());
		GridCell next = targetPolicy.getNextIndex(current);
		double heading = grid.getHeading(grid.encodeFromIndices(current, next));
		return new TrackerAction(myState, heading, 1.0 / grid.getGridSize());
	}
	
	TrackerAction getAction(AgentState state, ProbabilityMap pm, int horizon) {
		*/
		List<List<WeightedAction>> actionList = new ArrayList<List<WeightedAction>>();
		TrackerAction trackerAction = new TrackerAction(myState, false);
		Point2D endPoint;
		double utility;
		
		pm.update(previousResult, scores, newPercepts);
		
		actionList.add(new ArrayList<WeightedAction>());
		actionList.get(0).add(new WeightedAction(null, trackerAction, 0));
		
		for(int depth = 0; depth < HORIZON; depth++) {
			//System.out.println("------ " + depth);
			actionList.add(new ArrayList<WeightedAction>());
			for(WeightedAction action : actionList.get(depth)) {
				//System.out.println("--- " + action.getState());
				// get movement actions
				for (int i = 0; i < HEADING_PRECISION; i++) {
					trackerAction = new TrackerAction(action.getState(), Math.PI * 2 * i * rotationAngle, stepSize);
					endPoint = trackerAction.getStartState().getPosition();
					
					// check if the move is valid
					if(!GeomTools.canMove(trackerAction.getStartState().getPosition(), endPoint, false, 0, obstacles) || !inBounds(endPoint)) {
						continue;
					}
					
					utility = pm.getFindProbability(depth, trackerAction.getResultingState()) - pm.getSightProbability(depth, trackerAction.getResultingState()) + action.getUtility();
					
					//System.out.println("move: " + utility + " | " + Math.toDegrees(trackerAction.getHeading()));
					
					actionList.get(depth+1).add(new WeightedAction(action, trackerAction, utility));
				}
				
				// get turning actions
				for (int i = 0; i < HEADING_PRECISION; i++) {
					trackerAction = new TrackerAction(action.getState(), Math.PI * 2 * i * rotationAngle, 0);
					utility = pm.getFindProbability(depth, trackerAction.getResultingState()) - pm.getSightProbability(depth, trackerAction.getResultingState());
		
					//System.out.println("turn: " + utility + " | " + Math.toDegrees(trackerAction.getHeading()));
					
					actionList.get(depth+1).add(new WeightedAction(action, trackerAction, utility));
				}
				// get call HQ action
				
				// get arm length change action
			}
		}
		
		WeightedAction w = Collections.max(actionList.get(HORIZON));
		while(w.getParent().getParent() != null) {
			w = w.getParent();
		}

		return w.getTrackerAction();
	}
	
	/**
	 * Checks if a given point is in bounds
	 * 
	 * @param point
	 *            the point to be checked
	 * @return boolean result of the check
	 */
	private boolean inBounds(Point2D point) {
		if (!field.contains(point))
			return false;
		return true;
	}

	
	class WeightedAction implements Comparable<WeightedAction>{
		private WeightedAction parent;

		private TrackerAction action;
		private double utility;
		
		public WeightedAction(WeightedAction parent,
				TrackerAction trackerAction, double utility) {
			this.parent = parent;
			this.action = trackerAction;
			this.utility = utility;
		}

		public WeightedAction getParent() {
			return parent;
		}

		public AgentState getState() {
			return action.getResultingState();
		}

		public TrackerAction getTrackerAction() {
			return action;
		}

		public double getUtility() {
			return utility;
		}

		@Override
		public int compareTo(WeightedAction o) {

			if (utility > o.getUtility()) {
				return 1;
			} else if (utility < o.getUtility()) {
				return -1;
			}
			return 0;
		}
	}
	
}
