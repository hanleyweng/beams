package filter;
import processing.core.PImage;
import util.OscHandler;

public class DepthThresholder extends Filter {

	public DepthThresholder() {
	};

	public PImage getFilteredImage(PImage depthImg) {

		PImage outputImg = this.copyImage(depthImg);

		int width = outputImg.width;
		int height = outputImg.height;
		outputImg.loadPixels();

		depthImg.loadPixels();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = x + y * width;
				
				int currColor = depthImg.pixels[index];
				int curGrayValue = (currColor >> 16) & 0xFF;
				
				// if curGrayValue is less than threshold, it is too far away. 
				// paint black.
				if (curGrayValue < OscHandler.depthThreshold) {
					outputImg.pixels[index] = 0;
				}
			}
		}

		outputImg.updatePixels();

		return outputImg;
	}
}
