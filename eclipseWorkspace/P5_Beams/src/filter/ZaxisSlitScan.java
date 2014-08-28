package filter;
import java.util.ArrayList;
import processing.core.PImage;

public class ZaxisSlitScan extends Filter {

	ArrayList<PImage> prvRgbFrames = new ArrayList<PImage>();
	ArrayList<PImage> prvDepthFrames = new ArrayList<PImage>();
	int maxFrames = 30;

	public ZaxisSlitScan() {
	};

	PImage getFilteredImage(PImage depthImg, PImage rgbImg) {

		// Store srcImg in prvFrames
		prvRgbFrames.add(this.copyImage(rgbImg));
		prvDepthFrames.add(this.copyImage(depthImg));

		// Clear prvFrames from memory
		if (prvRgbFrames.size() > maxFrames) {
			prvRgbFrames.remove(0);
			prvDepthFrames.remove(0);
		}

		// ///////////////////////////////////////////////

		// Get the oldest image
		// Get Depth range to draw oldest image

		// Convert Depth Image

		PImage outputImg = this.copyImage(rgbImg);
		int width = outputImg.width;
		int height = outputImg.height;
		outputImg.loadPixels();
		for (int i = prvRgbFrames.size() - 1; i >= 0; i--) {
			PImage curRgbImage = prvRgbFrames.get(i);
			PImage curDepthImage = prvDepthFrames.get(i);

			curRgbImage.loadPixels();
			curDepthImage.loadPixels();

			float grayGap = 255f / prvRgbFrames.size();
			float maxGrayValue = grayGap * i;
			float minGrayValue = maxGrayValue - grayGap;

			// If pixel of depth image is within gray zone
			// write rgb value to outputImg

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int index = x + y * width;

					// set curGrayValue of pixel to simple value of red
					int currColor = curDepthImage.pixels[index];
					int curGrayValue = (currColor >> 16) & 0xFF;

					if ((curGrayValue > minGrayValue) && (curGrayValue < maxGrayValue)) {
						// outputImg.pixels[index] = curRgbImage.pixels[index];
						outputImg.pixels[index] = curDepthImage.pixels[index];
					}
				}
			}
		}

		outputImg.updatePixels();

		return outputImg;
	}
}
