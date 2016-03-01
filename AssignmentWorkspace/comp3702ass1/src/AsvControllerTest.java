import static org.junit.Assert.fail;

import java.util.List;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.junit.Test;


public class AsvControllerTest {

	@Test
	public void testRotate() {
		List<Point2D> asvList= new ArrayList<Point2D>();
		asvList.add(new Point2D.Double(0, 0));
		asvList.add(new Point2D.Double(0.05, 0.05));
		asvList.add(new Point2D.Double(0.1, 0));
		
		AsvController asvCont = new AsvController(asvList.size(), null);
		
		System.out.println(asvCont.printStates(asvCont.move(asvList, 0.002, 0)));
	}
}
