import processing.core.PImage;

public class Filter {
	Filter() {
	};

	PImage getFilteredImage() {
		// Can add timer here ...
		return null;
	}
	
	PImage copyImage(PImage img) {
		// A more efficient method may be possible - e.g. TimeDisplacement example P5 sketch.
		return img.get();
	}

}
