import java.awt.Color;

import processing.core.PImage;

public class RemoveRedFilter extends Filter {
	PImage img;

	RemoveRedFilter() {
	};

	void updateWith(PImage img) {
		this.img = img;
		
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
	}

	PImage getImage() {
		return img;
	}
}
