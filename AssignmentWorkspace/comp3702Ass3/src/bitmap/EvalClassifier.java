package bitmap;

import java.io.*;

/**
 * <p>This program tests a classifier after loading it and the bitmaps.</p>
 * @author Mikael Boden
 * @version 1.0
 */

public class EvalClassifier {

  public EvalClassifier(String[] args) {
    // create the classifier
    Classifier c=null;
    try {
      c=Classifier.load(args[0]);
    } catch (IOException ex) {
      System.err.println("Load of classifier failed: "+ex.getMessage());
      System.exit(2);
    } catch (ClassNotFoundException ex) {
      System.err.println("Loaded classifier does not match available classes: "+ex.getMessage());
      System.exit(3);
    }
    if (c!=null) {
      // load data
      try {
        ClassifiedBitmap[] bitmaps=LetterClassifier.loadLetters(args[1]);
        // Use last 70% of data to test
        int size = (int)(bitmaps.length*0.4);
        ClassifiedBitmap[] smallerSet = new ClassifiedBitmap[size];
        for (int i=0; i < size; i++) {
      	  smallerSet[i] = bitmaps[i + (int)(Math.ceil(bitmaps.length*0.6))];
        }
        run(c, smallerSet);
        // run test on all data
//        run(c, bitmaps);
      } catch (IOException ex) {
        System.err.println("Error loading bitmap file: "+ex.getMessage());
      }
    }
  }

  public static void run(Classifier c, ClassifiedBitmap[] bitmaps) {
    int count = 0;
	  
	  // test it using available data
    System.out.println("Evaluating classifier "+c.getName());
    System.out.println("Sample\tTarget\tActual\tCorrect");
    for (int i=0; i<bitmaps.length; i++) {
      int actual=c.index((Bitmap)bitmaps[i]);
      int target=bitmaps[i].getTarget();
      System.out.println(i+" \t"+c.getLabel(target)+" \t"+c.getLabel(actual)+" \t"+(target==actual?"YES":"NO"));
      if(target==actual) count++;
    }
//    int numAs = 0;
//    System.out.println("OutputWeight,Correct");
//    for (int i=0; i<bitmaps.length; i++) {
//      
//      int target=bitmaps[i].getTarget();
//      // Only print the results if the letter was 'A'
//      //if (c.getLabel(target).equals("A")) {
//    	  int actual=c.index((Bitmap)bitmaps[i]);
//    	  // Prints weight of output node, and whether or not it was best choice
//    	  System.out.println(","+(target==actual?"YES":"NO"));
//    	  numAs++;
//    	  if(target==actual) count++;
//      }
//      
//    }
    System.out.println(String.format("Total: %d/%d, %.2f%%", count, bitmaps.length, (double) count/bitmaps.length*100));
//    System.out.println(String.format("Total: %d/%d, %.2f%%", count, numAs, (double) count/numAs*100));
  }

  public static void main(String[] args) {
    if (args.length!=2) {
      System.err.println("Usage: EvalClassifier <classifier-file> <bitmap-file>");
      System.exit(1);
    }
    new EvalClassifier(args);
    System.out.println("Done.");
  }

}