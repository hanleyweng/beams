package filter;
import processing.core.PImage;
import util.OscHandler;

public class ZaxisContours extends Filter {

	public ZaxisContours() {
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

				// set curGrayValue of pixel to simple value of red
				int currColor = depthImg.pixels[index];
				int curGrayValue = (currColor >> 16) & 0xFF;
				
				// only bother working on visible depth field values
				if (curGrayValue > 0) {
					// zaxis controlled by depth slices & a threshold that defines the size!
					if (curGrayValue % OscHandler.zaxisSlice < OscHandler.zaxisThreshold) {
						int newColor = 0xff000000 | (255 << 16) | (0 << 8) | 0;
						outputImg.pixels[index] = newColor;
					} else {
						// make non zaxis contours transp.
						outputImg.pixels[index] = 0x00000000;
					}
				} else {
					// make non zaxis contours transp.
					outputImg.pixels[index] = 0x00000000;
				}
			}
		}

		outputImg.updatePixels();

		return outputImg;
	}
}
