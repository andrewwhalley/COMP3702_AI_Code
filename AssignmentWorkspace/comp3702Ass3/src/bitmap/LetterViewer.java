package bitmap;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

public class LetterViewer {
	ClassifiedBitmap[] bitmaps;

	public LetterViewer(String[] args) {
		try {
			bitmaps = LetterClassifier.loadLetters(args[0]);
		} catch (IOException ex) {
			System.err.println("Error loading data.txt: " + ex.getMessage());
		}
		
		
		ViewerFrame frame = new ViewerFrame(this);
		
	    frame.validate();
	    // Center the window
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension frameSize = frame.getSize();
	    if (frameSize.height > screenSize.height) {
	      frameSize.height = screenSize.height;
	    }
	    if (frameSize.width > screenSize.width) {
	      frameSize.width = screenSize.width;
	    }
	    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	    frame.setVisible(true);
	}
	
	public ClassifiedBitmap get(int position) {
		if(position < 0 || position > bitmaps.length) return null; 
		return bitmaps[position];
	}
	

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: LetterViewer <bitmap-file>");
			System.exit(1);
		}
		new LetterViewer(args);
	}
}
