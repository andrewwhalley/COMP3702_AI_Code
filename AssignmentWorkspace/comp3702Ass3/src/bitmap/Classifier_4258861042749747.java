package bitmap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.Serializable;
import java.util.Random;

public class Classifier_4258861042749747 extends LetterClassifier {

	// global variables
	private static String name="Advanced Classifer, 42588610-42749747";
	private MLNN multiLayer = null; // The MultiLayer Neural Network to be used
	private Random rand; // For getting random values
	private double[][] targets=null; // target vectors;
	private Preprocessor pp;
	
	/**
	 * 
	 * @param nRows
	 * @param nCols
	 */
	public Classifier_4258861042749747(int nRows, int nCols) {
		rand=new Random(System.currentTimeMillis());
		multiLayer = new MLNN(nRows*nCols, getClassCount() , rand.nextInt());
		targets=new double[getClassCount()][getClassCount()];
	    for (int c=0; c<getClassCount(); c++) {
	    	targets[c][c]=1;
	    }
	    pp = new Preprocessor();
	}
	
	/**
	 * Identifies the classifier, e.g. by the name of the author/contender, or 
	 * by whatever you want to identify this instance when loaded elsewhere.
	 * @return the identifier
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Getter method for the MultiLayer neural network in use
	 * @return the multilayer neural network
	 */
	public MLNN getMLNN() {
		return this.multiLayer;
	}
	
	
	public void train(ClassifiedBitmap[] maps, int nPresentations, double eta) {  
		double rmse = 0;
		for (int p=0; p<nPresentations; p++) {
		      int sample=rand.nextInt(maps.length);
		      // Scale the bitmap using preprocessing
		      rmse = multiLayer.train((pp.scaleBitmap((Bitmap)maps[sample])).toDoubleArray(), targets[maps[sample].getTarget()], eta);
//		      rmse = multiLayer.train(((Bitmap)maps[sample]).toDoubleArray(), targets[maps[sample].getTarget()], eta);
		  }
	}
	
	/**
	 * Classifies the bitmap
	 * @param map the bitmap to classify
	 * @return the probabilities of all the classes (should add up to 1).
	 */
	public double[] test(Bitmap map) {
		// Use preprocessing
		Bitmap pmap = pp.scaleBitmap(map);
		double[] out = multiLayer.feedForward(pmap.toDoubleArray());
		// Don't use preprocessing
//		double[] out=multiLayer.feedForward(map.toDoubleArray());
		return out;
	}

	/**
	 * Multi-Layer Neural Network with one hidden layer
	 */
	private class MLNN implements Serializable {
		
		double[] y;             // the output values produced by each node (indices important, see weights/biases)
		public double[][] w;    // the trainable weight values [to node][from input]
		public double[][] h;	// the trainable hidden weight values
		public double[] hValues; // The input values to each hidden layer (sigmoid applied)
		public double[] bias;   // the trainable bias values for initial nodes
		public double[] hiddenBias; // the trainable bias values for the hidden nodes
		Random rand;            // a random number generator for initial weight values
		
		/**
		 * 
		 * @param nInput
		 * @param nOutput
		 * @param seed
		 */
		public MLNN(int nInput, int nOutput, int seed) {
			// allocate space for node and weight values
		    y=new double[nOutput];
		    w=new double[nOutput][nInput];
		    bias=new double[nOutput];
			hiddenBias = new double[nOutput];
			h = new double[nOutput][nOutput];
			hValues = new double[nOutput];
			
			// Initialise values
			rand = new Random(seed);
		    for (int j = 0; j < nOutput; j++) {
		    	for (int i = 0; i < nInput; i++) {
		    		w[j][i] = rand.nextGaussian()*.1;
		    	}
		    	bias[j] = rand.nextGaussian()*.1;
		    	for (int k = 0; k < nOutput; k++) {
		    		h[j][k] = rand.nextGaussian()*.1;
		    	}
		    	hiddenBias[j] = rand.nextGaussian()*.1;
		    }
		}
		
		/** Computes the output values of the output nodes in the network given input values.
		 *  @param  x  The input values.
		 *  @return double[]    The vector of computed output values
		 */
		public double[] feedForward(double[] x) {
			// Feed inputs into hidden layer
			for (int j = 0; j < y.length; j++) {
				double sum = 0; // reset summed activation value
			   	for (int i = 0; i < x.length; i++) {
			   		sum += x[i]*w[j][i];
			   	}
			   	hValues[j] = outputFunction(sum + bias[j]);
			}
			// Feed hidden layer values to output
			for (int k = 0; k < h.length; k++) {
				double outerSum = 0;
				for (int i = 0; i < hValues.length; i++) {
					outerSum += hValues[i]*h[k][i];
				}
				y[k] = outputFunction(outerSum + hiddenBias[k]);
			}
			return y;
		}
		
		/** Adapts weights in the network given the specification of which values that should appear at the output (target)
		 *  when the input has been presented.
		 *  The procedure is known as error backpropagation. This implementation is "online" rather than "batched", that is,
		 *  the change is not based on the gradient of the global error, merely the local -- pattern-specific -- error.
		 *  @param  x  The input values.
		 *  @param  d  The desired output values.
		 *  @param  eta     The learning rate, always between 0 and 1, typically a small value, e.g. 0.1
		 *  @return double  An error value (the root-mean-squared-error).
		 */
		public double train(double[] x, double[] d, double eta) {

			// present the input and calculate the outputs
		    feedForward(x);

		    // allocate space for errors of individual nodes
		    double[] error = new double[y.length];
		    double[] hiddenError = new double[h.length];

		    // compute the error of output nodes (explicit target is available -- so quite simple)
		    // also, calculate the root-mean-squared-error to indicate progress
		    double rmse=0;
		    for (int j=0; j<y.length; j++) {
		    	double diff=d[j]-y[j];
		    	error[j]=diff * outputFunctionDerivative(y[j]);
		    	rmse+=diff*diff;
		    }
		    rmse=Math.sqrt(rmse/y.length);
		    
		    // Calculate error on the hidden inputs
		    for (int k = 0; k < h.length; k++) {
		    	double errorSum = 0;
		    	for (int i = 0; i < y.length; i++) {
		    		errorSum += error[i] * h[k][i];
		    	}
		    	hiddenError[k] = errorSum * outputFunctionDerivative(hValues[k]);
		    }
		    
		    // adjust weights and bias for inputs
		    for (int j = 0; j < h.length; j++) {
		    	for (int i = 0; i < x.length; i++) {
		    		w[j][i] += hiddenError[j]*x[i]*eta;
		    	}
		    	bias[j]+=hiddenError[j]*1.0*eta;
		    }

		    // change hidden weights and bias according to errors
		    for (int j=0; j<y.length; j++) {
		    	for (int i=0; i<h.length; i++) {
		    		h[j][i]+=error[j]*hValues[i]*eta;
		    	}
		    	hiddenBias[j]+=error[j]*1.0*eta; // bias can be understood as a weight from a node which is always 1.0.
		    }
		    return rmse;
		}
		
		/** The logistic function. Computes the output value of a node given the summed incoming activation.
		 *  Also used for calculating hidden weights. Generates a value bounded between 0 and 1
		 *  @param  net The summed incoming activation
		 *  @return double
		 */
		public double outputFunction(double net) {
			return 1.0/(1.0+Math.exp(-net));
		}
		
		/**  This one is the derivative of the logistic function which is efficiently computed with respect to the output value
		 * Used during backpropagation
		 *  @param  x The value by which the gradient is determined.
		 *  @return double  the gradient at x.
		 */
		public double outputFunctionDerivative(double x) {
			return x*(1.0-x);
		}
	}
	
	/**
	 * 
	 * Preprocessor class to improve performance.
	 * 
	 * Scales the bitmap to center the images
	 */
	private class Preprocessor implements Serializable {
		
		private ClassifiedBitmap scaleBitmap(ClassifiedBitmap map) {
			Bitmap temp = scaleBitmap(new Bitmap(map.toString()));
			String line = temp.toString() + " " + String.valueOf(map.getTarget());
			ClassifiedBitmap cb = new ClassifiedBitmap(line);
			return cb;
		}
		
		public Bitmap scaleBitmap(Bitmap map) {
			int imgTop = 0;
			int imgBtm = 0;
			int imgLft = 0;
			int imgRgt = 0;
			int h;
			int w;

			// find rightmost line
			for (int i = 0; i < 32; i++) {
				for (int j = 0; j < 32; j++) {
					if (map.get(j, i) == true) {
						imgRgt = i + 1;
					}
				}
			}

			// find leftmost line
			for (int i = 31; i > 0; i--) {
				for (int j = 0; j < 32; j++) {
					if (map.get(j, i) == true) {
						imgLft = i - 1;
					}
				}
			}

			// find topmost line CORRECT
			for (int i = 31; i > 0; i--) {
				for (int j = 0; j < 32; j++) {
					if (map.get(i, j) == true) {
						imgTop = i - 1;
					}
				}
			}

			// find bottommost line
			for (int i = 0; i < 32; i++) {
				for (int j = 0; j < 32; j++) {
					if (map.get(i, j) == true) {
						imgBtm = i + 1;
					}
				}
			}

			// find the new hight and width
			h = imgBtm - imgTop;
			w = imgRgt - imgLft;

			// find the offset to centre the image
			int hOffset = 0;
			int vOffset = 0;

			if (h > w) {
				hOffset = (h - w) / 2;
			} else {
				vOffset = (w - h) / 2;
			}

			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
			SampleModel model = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, 32, 32, 1);
			Raster raster = Raster.createRaster(model, model.createDataBuffer(), new Point(0, 0));
			WritableRaster wr = raster.createCompatibleWritableRaster();

			// convert bitmap to raster
			int[] t = new int[1];
			int[] f = new int[1];
			int[] data = new int[1];
			t[0] = 1;
			f[0] = 0;
			for (int i = imgTop-vOffset; i <= imgBtm+vOffset; i++) {
				for (int j = imgLft-hOffset; j <= imgRgt+hOffset; j++) {
					if (map.get(i, j)) {
						wr.setPixel( j - imgLft+hOffset, i - imgTop+vOffset, t);
					} else {
					}

				}
			}

			// scale image
			float scale;

			scale = (h > w) ? h : w;
			scale = 32 / scale;

			image = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_BINARY);
			image.setData(wr);
			Graphics2D g = (Graphics2D) image.getGraphics();
			g.scale(scale, scale);
			g.drawImage(image, 0, 0, null);
			g.dispose();

			raster = image.getData();

			for (int i = 0; i < 32; i++) {
				//System.out.println(i);
				for (int j = 0; j < 32; j++) {
					raster.getPixel(i, j, data);
					//System.out.print(data[0] + " ");
				}
			}

			Bitmap scaledMap = new Bitmap(32, 32);

			// convert back to bitmap
			for (int i = 0; i < 32; i++) {
				for (int j = 0; j < 32; j++) {
					raster.getPixel(i, j, data);
					if (data[0] == 1) {
						scaledMap.set(j, i, true);
					} else {
						scaledMap.set(j, i, false);
					}
				}
			}
			return scaledMap;
		}
	}

}
