package filter;
import processing.core.PImage;

public class KeepOnlyRed extends Filter {
	PImage img;

	public KeepOnlyRed() {
	};

	/**
	 * 
	 * @param image - the image to keep red only from
	 * @return an image with only red
	 */
	public PImage getFilteredImage(PImage image) {
		this.img = this.copyImage(image); // must use get, else the original image is manipulated directly. (since this is a pointer)

		int width = img.width;
		int height = img.height;

		// Load Pixels
		img.loadPixels();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = x + y * width;

				int currColor = img.pixels[index];
				int currR = (currColor >> 16) & 0xFF;
				// if red then paint red, otherwise paint transp.
				img.pixels[index] = currR == 255 ? 0xFFFF0000 : 0x00000000; 
			}
		}
		// Update Pixels
		img.updatePixels();
		
		// Return img
		return img;
	}

}
