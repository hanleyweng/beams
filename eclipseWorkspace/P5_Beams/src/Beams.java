import processing.core.*; // This is from Processing2.2.1

import processing.video.*;

// TODO: Add Video

// TODO: Add Analysis'

// TODO: Add Filters

@SuppressWarnings("serial")
public class Beams extends PApplet {

	int swidth = 800;
	int sheight = 600;

	public void setup() {
		size(swidth, sheight);
		colorMode(HSB, 100);
	}

	public void draw() {
		ellipse(swidth / 2, sheight / 2, 50, 50);
	}

}
