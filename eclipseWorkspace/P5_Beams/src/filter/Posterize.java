package filter;
import processing.core.PConstants;
import processing.core.PImage;
import util.OscHandler;

public class Posterize extends Filter {
	PImage img;

	public Posterize() {
	};

	/**
	 * 
	 * @return posterized image based on posterizeLevel.
	 */
	public PImage getFilteredImage(PImage image) {
		this.img = this.copyImage(image); 
		
		img.filter(PConstants.POSTERIZE,OscHandler.posterizeLevel);
		// Return img
		return img;
	}

}
