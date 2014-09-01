package filter;
import processing.core.PImage;

public class RemoveRedFilter extends Filter {
	PImage img;

	public RemoveRedFilter() {
	};

	/**
	 * 
	 * @param image - the image to subtract red from
	 * @return an image with no red
	 */
	PImage getFilteredImage(PImage image) {
		this.img = this.copyImage(image); // must use get, else the original image is manipulated directly. (since this is a pointer)

		int width = img.width;
		int height = img.height;

		// Load Pixels
		img.loadPixels();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = x + y * width;

				int currColor = img.pixels[index];

				// Get current Colors
				int currR = (currColor >> 16) & 0xFF;
				int currG = (currColor >> 8) & 0xFF;
				int currB = currColor & 0xFF;

				// take out red
				currR = 0;

				// set new color of pixel
				img.pixels[index] = 0xff000000 | (currR << 16) | (currG << 8) | currB;

			}
		}
		// Update Pixels
		img.updatePixels();
		
		// Return img
		return img;
	}

}
