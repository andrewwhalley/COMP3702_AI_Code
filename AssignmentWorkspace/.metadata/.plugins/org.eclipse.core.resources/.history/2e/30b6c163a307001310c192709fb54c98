package tutorial1.navigation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import search.algorithms.*;
import search.heuristics.*;
/**
 * An implementation of the navigation problem from Tutorial 1.
 * @author lackofcheese
 */
public class NavigationSolver {
	/** The default file to read input from. */
	public static final String DEFAULT_INPUT = "src/navigation.in";
	/** A mapping to remember the states by their names. */
	private static Map<String, NavigationState> byName = new HashMap<String, NavigationState>();
	
	/**
	 * Reads the search parameters from the given file.
	 * @param inputFileName the file to read.
	 * @throws IOException if there are issues reading the file.
	 */
	private static void readFile(String inputFileName) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(inputFileName)); 
		// Read the number of vertices from the file.
		int numVertices = Integer.valueOf(input.readLine().trim());
		// Read all of the buildings in and remember them by name.
		for (int i = 0; i < numVertices; i++) {
			String name = input.readLine().trim();
			NavigationState building = new NavigationState(name);
			byName.put(name, building);
		}
		// Read the number of edges.
		int numEdges = Integer.valueOf(input.readLine().trim());
		/*  Read the edges, and store all of them in the mapping
		 *  successor function. */
		for (int i = 0; i < numEdges; i++) {
			String[] names = input.readLine().trim().split("\\s+");
			NavigationState b0 = byName.get(names[0]);
			NavigationState b1 = byName.get(names[1]);
			double cost = Double.valueOf(names[2]);
			b0.addSuccessor(b1, cost);
			b1.addSuccessor(b0, cost);
		}
		input.close();
	}
	
	/**
	 * @param args the command-line arguments. If any are given, the first
	 * will be taken as the file to read from.
	 */
	public static void main(String args[]) {
		try {
			if (args.length > 0) {
				readFile(args[0]);
			} else {
				readFile(DEFAULT_INPUT);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		NavigationState initialState = byName.get("78");
		NavigationState goalState = byName.get("82D");
		@SuppressWarnings("unused")
		Heuristic heuristic = new ZeroHeuristic();
		
		AbstractSearchAlgorithm algo;
		//algo = new DepthFirstSearch(initialState, goalState);
		//algo = new DepthLimitedSearch(5, initialState, goalState);
		//algo = new IterativeDeepeningSearch(initialState, goalState);
		
		algo = new BreadthFirstSearch(initialState, goalState);
		//algo = new AStarSearch(initialState, goalState, heuristic);
		
		algo.verboseSearch();
	}
}
