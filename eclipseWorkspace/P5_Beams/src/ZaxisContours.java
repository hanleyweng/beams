import processing.core.PImage;

public class ZaxisContours extends Filter {

	ZaxisContours() {
	};

	PImage getFilteredImage(PImage depthImg) {

		PImage outputImg = this.copyImage(depthImg);

		int width = outputImg.width;
		int height = outputImg.height;
		outputImg.loadPixels();

		depthImg.loadPixels();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = x + y * width;

				// if pixel grayColor is modulus 5 - color red

				// set curGrayValue of pixel to simple value of red
				int currColor = depthImg.pixels[index];
				int curGrayValue = (currColor >> 16) & 0xFF;

				if (curGrayValue % OscHandler.testInt1 == 0) {
					int newColor = 0xff000000 | (255 << 16) | (0 << 8) | 0;
					outputImg.pixels[index] = newColor;
				}
			}
		}

		outputImg.updatePixels();

		return outputImg;
	}
}
