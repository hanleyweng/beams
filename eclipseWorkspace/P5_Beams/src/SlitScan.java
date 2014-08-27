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
	int heightOfRow; // in pixels
	
	int maxFrames;
	
	SlitScan() {
	};

	PImage getFilteredImage(PImage srcImg) {

		img = this.copyImage(srcImg);

		int width = img.width;
		int newHeightOfRow = OscHandler.heightOfRow;
		// if we have a newHeightOfRow,
		// set & clear prvFrames, otherwise array out of bounds
		if(newHeightOfRow != heightOfRow) {
			prvFrames.clear();
			heightOfRow = newHeightOfRow;
		}
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
		prvFrames.add(this.copyImage(srcImg));

		// Clear prvFrames from memory
		if (prvFrames.size() > maxFrames) {
			prvFrames.remove(0);
		}

		return img;
	}
}
