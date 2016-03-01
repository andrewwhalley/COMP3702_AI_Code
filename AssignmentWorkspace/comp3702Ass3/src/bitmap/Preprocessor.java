package bitmap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class Preprocessor {
	
	static public ClassifiedBitmap scaleBitmap(ClassifiedBitmap map) {
		Bitmap temp = scaleBitmap(new Bitmap(map.toString()));
		String line = temp.toString() + " " + String.valueOf(map.getTarget());
		ClassifiedBitmap cb = new ClassifiedBitmap(line);
		return cb;
	}
	
	static public Bitmap scaleBitmap(Bitmap map) {
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
