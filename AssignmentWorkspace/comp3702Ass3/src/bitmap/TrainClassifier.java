package bitmap;

import java.io.*;

/**
 * This program trains a classifier and saves it in a file to be read when used.
 * @author Mikael Boden
 * @version 1.0
 */

public class TrainClassifier {

  public TrainClassifier(String[] args) {
    // create the classifier
//    NNClassifier c=new NNClassifier(32, 32);
    // or - for ID3 - replace with the following line of code
//     ID3Classifier c=new ID3Classifier(32, 32);
	// or - for Advanced NN Classifier:
    Classifier_4258861042749747 c = new Classifier_4258861042749747(32, 32);
	// start timing
    long startTime = System.nanoTime();
    // load data
    try {
    	
    	
     ClassifiedBitmap[] bitmaps=LetterClassifier.loadLetters(args[1]);
      
     // build a pre-processed data set
     // toggle pre-processing here
     
//      for (int i=0; i < bitmaps.length; i++) {
//    	  bitmaps[i] = Preprocessor.scaleBitmap(bitmaps[i]);
//      }
     
      // use first 60% of the data to train
      int size = (int)(bitmaps.length*0.6);
      ClassifiedBitmap[] smallerSet = new ClassifiedBitmap[size];
      for (int i=0; i < size; i++) {
    	  smallerSet[i] = bitmaps[i];
      }
      c.train(smallerSet, 40000, 0.4);
      // train it using all available training data
      // c.train(bitmaps,100000,0.12);
      // or - for ID3 - replace with the following line of code
//       c.train(smallerSet,0.4,2);
//       c.train(bitmaps,0.5,5);
    } catch (IOException ex) {
      System.err.println("Error loading data.txt: "+ex.getMessage());
    }
    try {
      Classifier.save(c, args[0]);
    } catch (Exception ex) {
      System.err.println("Failed to serialize and save file: "+ex.getMessage());
    }
    
    // output total time
    long endTime = System.nanoTime();
    System.out.println(String.format("Total Time: %f", (endTime - startTime)/1.0E9));
  }

  public static void main(String[] args) {
    if (args.length!=2) {
      System.err.println("Usage: TrainClassifier <classifier-file> <bitmap-file>");
      System.exit(1);
    }
    new TrainClassifier(args);
    System.out.println("Done.");
  }

}