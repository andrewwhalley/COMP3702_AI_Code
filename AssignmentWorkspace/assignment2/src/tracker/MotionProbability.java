package tracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CosNaming.IstringHelper;

import divergence.MotionHistory;
import divergence.MotionHistory.HistoryEntry;

public class MotionProbability {
	Map<Integer, Map<Integer, Double>> probabilities = null;
	boolean isTarget;

	/**
	 * Constructor for movement types A1 or B1 (i.e. without motion history)
	 * 
	 * @param history
	 *            the motion history of the agent
	 * @param isTarget
	 *            whether the agent is a target or tracker
	 */
	public MotionProbability(boolean isTarget) {
		this.isTarget = isTarget;
	}
	
	/**
	 * Constructor for movement types A2 or B2 (i.e. with motion history)
	 * 
	 * @param history
	 *            the motion history of the agent
	 * @param isTarget
	 *            whether the agent is a target or tracker
	 */
	public MotionProbability(MotionHistory history, boolean isTarget) {
		this(isTarget);
		
		probabilities = new TreeMap<Integer, Map<Integer, Double>>();
		Map<Integer, Map<Integer, AtomicInteger>> counter = new TreeMap<Integer,
				Map<Integer, AtomicInteger>>();
		Iterator<HistoryEntry> it = history.iterator();
		HistoryEntry h;

		while (it.hasNext()) {
			h = it.next();
			int a = h.getDesiredActionCode();
			int r = h.getResultCode();

			if (!counter.containsKey(a)) {
				counter.put(a, new TreeMap<Integer, AtomicInteger>());
			}
			if (!counter.get(a).containsKey(r)) {
				counter.get(a).put(r, new AtomicInteger(0));
			}
			counter.get(a).get(r).incrementAndGet();
		}

		for (Integer i : counter.keySet()) {
			probabilities.put(i, new TreeMap<Integer, Double>());
			int total = 0;

			for (Integer j : counter.get(i).keySet()) {
				total += counter.get(i).get(j).intValue();
			}
			for (Integer j : counter.get(i).keySet()) {
				probabilities.get(i).put(j,
						counter.get(i).get(j).intValue() / (double) total);
			}
		}
	}

	/**
	 * 
	 * @param action
	 *            the action being taken
	 * @param result
	 *            the result being tested against
	 * @return the probability of the given result occurring
	 */
	public Map<Integer, Double> getProbability(Integer action,
			List<Integer> destinations) {
		Map<Integer, Double> results = new HashMap<Integer, Double>();

		// return even distribution if agent movement type '1'
		if (probabilities == null) {
			for (Integer i : destinations) {
				// POLICY TIEBREAKER GOES HERE
				results.put(i, 1.0 / (destinations.size()));
			}
			return results;
		}

		// if action does not exist in probability list, return null
		if (!probabilities.containsKey(action)) {
			return null;
		}

		// return predicted distribution if agent movement type '2'
		Double prob;
		Double total = 0.0;

		// count total probability
		for (Integer i : destinations) {
			prob = probabilities.get(action).get(i);
			if (prob != null) {
				total += prob;
			}
		}

		for (Integer i : destinations) {
			prob = probabilities.get(action).get(i);
			if (prob == null) {
				results.put(i, 0.0);
			} else {
				results.put(i, prob / total);
			}
		}
		return results;
	}

	@Override
	public String toString() {
		if (probabilities == null)
			return "Movement Type A";

		StringBuilder output = new StringBuilder();
		// output.append("A | 0   | 1   | 2   | 3   | 4   | 5   | 6   | 7   | 8   | 9   |");
		// output.append(String.format("%n---------------------------------------------------------------"));
		// output.append(String.format("A: %d:{", args))
		for (Integer i : probabilities.keySet()) {
			output.append(String.format("%d:{", i));
			for (Integer j : probabilities.get(i).keySet()) {
				output.append(String.format("%d: %.0f%%, ", j, probabilities
						.get(i).get(j) * 100));
			}
			output.setLength(output.length() - 2);
			output.append(String.format("}%n"));
		}

		return output.toString();
	}
}
