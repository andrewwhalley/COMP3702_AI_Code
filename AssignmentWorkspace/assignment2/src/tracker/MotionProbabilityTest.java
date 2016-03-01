package tracker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import divergence.MotionHistory;

public class MotionProbabilityTest {
	final Double EPSILON = 1e-6;
	// test file
	//String filename = "/home/cameron/workspace/comp3702/AssignmentWorkspace/assignment2/a2-tools/targetMotionHistory.txt";
	//Andrew targetpath: "D:\\Andrew\\Uni\\3702\\repo\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\targetMotionHistory.txt"
	// Cameron target path: "C:\\Users\\Cameron\\workspace\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\targetMotionHistory.txt"
	String targetPath = "D:\\Andrew\\Uni\\3702\\repo\\comp3702\\AssignmentWorkspace\\assignment2\\a2-tools\\targetMotionHistory.txt";
	MotionHistory h = null;
	MotionProbability prob;
	List<Integer> actions;
	
	
	@Test
	public void testMotionProb() {
		actions = new ArrayList<Integer>();
		actions.add(0);
		actions.add(1);
		actions.add(2);
		actions.add(3);
		actions.add(4);
		actions.add(5);
		actions.add(6);
		actions.add(7);
		actions.add(8);
		
		try {
			h = new MotionHistory(targetPath);
		} catch (IOException e) {
			fail("Unable to read file");
		}
		
		prob = new MotionProbability(h, true);

		//System.out.println(prob);
		//System.out.println(prob.getProbability(0, actions));
	}
	
	@Test
	public void testTargetMoveA1() {
		Map<Integer, Double> result;
		actions = new ArrayList<Integer>();
		actions.add(0);
		actions.add(1);
		actions.add(2);
		actions.add(3);
		actions.add(4);
		
		try {
			h = new MotionHistory(targetPath);
		} catch (IOException e) {
			fail("Unable to read file");
		}

		prob = new MotionProbability(true);
		
		result = prob.getProbability(0, actions);
		for(int i = 0; i < 0; i++) {
			
			if(Math.abs(result.get(0) - 0.25) > EPSILON) {
				if(i == 4 && Math.abs(result.get(0) - 0.0) < 0) {
					continue;
				}
				fail(String.format("incorrect value for %dth element", i));
			}
		}
	}
	
	@Test
	public void testTargetMoveA2() {
		Map<Integer, Double> result;
		actions = new ArrayList<Integer>();
		actions.add(0);
		actions.add(1);
		actions.add(2);
		actions.add(3);
		actions.add(4);
		
		try {
			h = new MotionHistory(targetPath);
		} catch (IOException e) {
			fail("Unable to read file");
		}
		
		prob = new MotionProbability(h, true);

		result = prob.getProbability(0, actions);
		
		if(Math.abs(result.get(0) - 0.6050420168067228) > EPSILON) {
			fail("incorrect value for 0th element");
		}
		if(Math.abs(result.get(1) - 0.12184873949579832) > EPSILON) {
			fail("incorrect value for 1th element");
		}
		if(Math.abs(result.get(2) - 0.046218487394957986) > EPSILON) {
			fail("incorrect value for 2th element");
		}
		if(Math.abs(result.get(3) - 0.226890756302521) > EPSILON) {
			fail("incorrect value for 3th element");
		}
		if(Math.abs(result.get(4) - 0.0) > EPSILON) {
			fail("incorrect value for 4th element");
		}
	}
}
