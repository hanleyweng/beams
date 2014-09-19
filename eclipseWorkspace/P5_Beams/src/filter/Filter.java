package filter;
import processing.core.PImage;

public class Filter {
	Filter() {
	};

	public PImage getFilteredImage() {
		// Can add timer here ...
		return null;
	}
	
	public PImage copyImage(PImage img) {
		// A more efficient method may be possible - e.g. TimeDisplacement example P5 sketch.
		// must use get, else the original image is manipulated directly. (since this is a pointer)
		return img.get(); 
	}

}
