import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Point2D;
import java.util.List;


@SuppressWarnings("unused")
public class assg1_42588610_42719747 {

	/**
	 * @param args
	 */
	private static int numASV;
	private static String initial;
	private static String goal;
	private static ArrayList<Rectangle2D> obstacles;
//	private static ArrayList<Asv> asvList;
	private static AsvController asvCont;
	private static List<Point2D> asvList;
	private static List<Point2D> goalList;
	
	private static void readFile(String inputFileName) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(inputFileName));
		// Read the number of ASV's from the file.
		numASV = Integer.valueOf(input.readLine().trim());
		// Read the initial and goal co-ordinates in
		initial = input.readLine().trim();
		goal = input.readLine().trim();
		// Read the number of obstacles.
		String s = input.readLine();
		if (s != null) {
			s.trim();
			int numObstacles = Integer.valueOf(s);
			obstacles = new ArrayList<Rectangle2D>();
			// Read in the co-ordinates of each obstacle
			for (int i = 0; i < numObstacles; i++) {
				createObstacles(input.readLine());
			}
		}
		input.close();
	}
	
	/**
	 * Create a List of ASVs and their coordinates, based on the input file
	 * 
	 */
	private static void createASVs() {
		asvList = new ArrayList<Point2D>();
		goalList = new ArrayList<Point2D>();
		String[] initialArr = initial.split("\\s+");
		String[] goalArr = goal.split("\\s+");
		int index = 0;
		while (index < 2 * numASV) {
			// Get the ASV details
			double x = Double.valueOf(initialArr[index]);
			double y = Double.valueOf(initialArr[index + 1]);
			double xFin = Double.valueOf(goalArr[index]);
			double yFin = Double.valueOf(goalArr[index + 1]);
			// Add that ASV to the list
			asvList.add(new Point2D.Double(x, y));
			goalList.add(new Point2D.Double(xFin, yFin));
			// Move on to the next ASV
			index += 2;
		}
		
	}
	
	private static void createObstacles(String obs) {
		String[] obsArr = obs.split("\\s+");
		if (obsArr.length != 8) {
			System.err.println("Invalid Obstacle details");
			System.exit(0);
		}
		// x & y are top left corner values
		double x = Double.valueOf(obsArr[6]);
		double y = Double.valueOf(obsArr[7]);
		// Calculate width and height based off the input values
		double width = Double.valueOf(obsArr[4]) - x;
		double height = y - Double.valueOf(obsArr[1]);
		// Create a new obstacle and add it to the obstacle arrayList
		obstacles.add(new Rectangle2D.Double(x, y - height, width, height));
	}
	
	public static void main(String[] args) {
		// Input file first
		// Output file second
		String input_file_name = new String(args[0]);
		// String output_file_name = new String(args[1]);
		// Attempt to read the input file
		try {
			readFile(input_file_name);
		} catch (IOException e) {
			System.err.println("Couldn't read from File " + input_file_name);
			e.printStackTrace();
			return;
		}
		// Create the ArrayList of ASVs
		createASVs();
		// Initialise the Search
		ASVSearch search = new ASVSearch(asvList, goalList, obstacles);
		// TODO: Do the search
		search.findPath();
		
		// TODO: Output the file
	}
}
