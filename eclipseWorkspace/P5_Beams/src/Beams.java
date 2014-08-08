import processing.core.*; // This is from Processing2.2.1

import processing.video.*;

// TODO: Add Video

// TODO: Add Analysis'

// TODO: Add Filters

@SuppressWarnings("serial")
public class Beams extends PApplet {

	int swidth = 800;
	int sheight = 600;

	Capture rgbCam;
	int rgbCamWidth = 640;
	int rgbCamHeight = 480;
	int rgbCamFps = 30;

	RemoveRedFilter removeRedFilter = new RemoveRedFilter();

	PImage outputImg;

	public void setup() {
		size(swidth, sheight);
		colorMode(HSB, 100);

		// INITIALIZE CAMERA
		String[] cameras = Capture.list();
		if (cameras == null) {
			println("Failed to retrieve the list of available cameras, will try the default...");
			rgbCam = new Capture(this, 640, 480);
		}
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
		}
		// Initialize Camera
		rgbCam = new Capture(this, rgbCamWidth, rgbCamHeight, rgbCamFps);

		// Start capturing the images from the camera
		rgbCam.start();

		// INITIALIZE FILTERS
	}

	public void draw() {
		// Draw BG Circle to represent frames are not yet available to render
		ellipse(swidth / 2, sheight / 2, 50, 50);

		// Read rgbCam
		if (rgbCam.available() == true) {
			rgbCam.read();

			// Analyze Camera Feed Here
			// ...
			removeRedFilter.updateWith(rgbCam);
			outputImg = removeRedFilter.getImage();
			// create new images from custom filters
			// ...
		}

		// draw filtered image
		if (outputImg != null) {
			set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
		}
		// ...

		// add any inbuilt p5 filters here
		// ...
	}

}
