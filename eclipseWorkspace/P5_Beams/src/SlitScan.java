import java.util.ArrayList;

import processing.core.PImage;

/**
 * Currently, this is a vertical slit scan from the bottom up.
 * 
 * @author hanleyweng
 * 
 */
public class SlitScan extends Filter {

	PImage img;

	ArrayList<PImage> prvFrames = new ArrayList<PImage>();
	int maxFrames;

	SlitScan() {
	};

	PImage getFilteredImage(PImage srcImg) {

		img = srcImg.get();

		int width = img.width;
		int heightOfRow = 10; // in pixels
		maxFrames = img.height / heightOfRow;

		img.loadPixels();

		// Get every image
		for (int i = 0; i < prvFrames.size(); i++) {
			PImage curFrame = prvFrames.get(i);

			// Determine where to draw that image
			int startingY = i * heightOfRow;

			// Draw row of pixels
			for (int x = 0; x < width; x++) {
				for (int y = startingY; y < startingY + heightOfRow; y++) {
					img.pixels[x + y * width] = curFrame.pixels[x + y * width];
				}
			}

		}

		img.updatePixels();

		// Store srcImg in prvFrames
		prvFrames.add(srcImg.get());

		// Clear prvFrames from memory
		if (prvFrames.size() > maxFrames) {
			prvFrames.remove(0);
		}

		return img;
	}
}
