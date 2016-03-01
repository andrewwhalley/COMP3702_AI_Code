package tracker;

import static org.junit.Assert.*;
import game.ActionResult;
import game.AgentState;
import game.Percept;
import game.RectRegion;
import game.SensingParameters;
import geom.GeomTools;
import geom.GridCell;
import geom.TargetGrid;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import target.TargetPolicy;
import divergence.MotionHistory;

public class ProbabilityMapTest {

	@Test
	public void testProbabilityMap() {
		// test files
		//String path = "/home/cameron/workspace/comp3702/AssignmentWorkspace/assignment2/a2-tools/";
		//Andrew path: "D:\\Andrew\\Uni\\3702\\repo\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\"
		// Cam path: "C:\\Users\\Cameron\\workspace\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\"
		String path = "C:\\Users\\Cameron\\workspace\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\";
		
		String targetMotionHistory = path + "testHistory.txt";
		String trackerMotionHistory = path + "trackerMotionHistory.txt";
		String policy = path + "targetPolicy.txt";
		
		MotionHistory h1 = null;
		MotionHistory h2 = null;
		TargetPolicy p = null;
		AgentState a = new AgentState(new Point2D.Double(0.45, 1-0.55 ), Math.toRadians(90));
		AgentState t1 = new AgentState(new Point2D.Double(0.45, 1-0.45 ), Math.toRadians(270));

		AgentState t2 = new AgentState(new Point2D.Double(0.0, 1-0.0 ), Math.toRadians(90));
		TargetGrid g = null;
		SensingParameters tsp = new SensingParameters(0.2, Math.toRadians(60));
		List<RectRegion> obstacles = new ArrayList<RectRegion>();
		List<AgentState> targetInitialStates = new ArrayList<AgentState>();
		targetInitialStates.add(t1);
		
		try {
			h1 = new MotionHistory(targetMotionHistory);
			h2 = new MotionHistory(trackerMotionHistory);
			p = new TargetPolicy(policy);
			g = p.getGrid();
		} catch (IOException e) {
			fail("Unable to read file");
		}
		
		//System.out.println(GeomTools.canSee(t1, a, tsp, obstacles, 0, 0));
		
		//System.out.println(g.getCodeFromHeading(a.getHeading()));
		
		//System.out.println(g.getCell(a.getPosition()));
		//System.out.println(g.getCell(t1.getPosition()));
		
		ActionResult previousResult = new ActionResult(null, null, a, 0);
		double[] scores = new double[2];
		scores[0] = 0;
		scores[1] = 0;
		List<Percept> newPercepts = new ArrayList<Percept>();
		//newPercepts.add(new Percept(1, 0, t1));
		//newPercepts.add(new Percept(2, 0, t2));
		
		MotionProbability prob = new MotionProbability(true);
		
		ProbabilityMap pm = new ProbabilityMap(p, prob, tsp, tsp, targetInitialStates, obstacles, 2);
		
		pm.update(previousResult, scores, newPercepts);
		System.out.println(pm.getFindProbability(2, a));
		System.out.println(pm.getSightProbability(2, a));
		//System.out.println(pm.p.get(1));
		//pm.getSightProbability(g.getCentre(new GridCell(4, 4)));
		//pm.UpdateTargetLocation(g.getCentre(new GridCell(4, 4)));
		


		
		//System.out.println(pm);
		
	}

}
