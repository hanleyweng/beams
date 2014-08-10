import processing.core.*; // This is from Processing2.2.1

import processing.video.*;

import SimpleOpenNI.*;

// TODO: Add Analysis'

@SuppressWarnings("serial")
public class Beams extends PApplet {

	static final String INPUT_MODE_INTERNAL_CAMERA = "INPUT_MODE_INTERNAL_CAMERA";
	static final String INPUT_MODE_KINECT = "INPUT_MODE_KINECT";

	String INPUT_MODE = INPUT_MODE_KINECT;

	// ///////////////////////////////////////////////////////////////////////////

	int swidth = 800;
	int sheight = 600;

	// Input - Camera
	Capture rgbCam;
	int rgbCamWidth = 640;
	int rgbCamHeight = 480;
	int rgbCamFps = 30;

	// Input - Kinect
	SimpleOpenNI kinect;

	// Filters
	RemoveRedFilter removeRedFilter = new RemoveRedFilter();
	SlitScan slitScan = new SlitScan();
	ScaledIn scaledIn = new ScaledIn();

	PImage outputImg;

	@Override
	public void setup() {
		size(swidth, sheight, OPENGL);
		colorMode(HSB, 100);

		// INITIALIZE CHOSEN CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_INTERNAL_CAMERA)) {
			this.setupInternalCamera();
		}
		if (INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			this.setupKinectCamera();
		}

	}

	void setupInternalCamera() {
		// INITIALIZE INTERNAL CAMERA
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
	}

	void setupKinectCamera() {
		// INITIALIZE KINECT CAMERA
		kinect = new SimpleOpenNI(this);
		if (kinect.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!");
			exit();
			return;
		}
		// mirror is by default enabled
		kinect.setMirror(true);

		// enable depthMap generation
		kinect.enableDepth();

		// enable ir generation
		kinect.enableRGB();
	}

	@Override
	public void draw() {
		// Draw BG Circle to represent frames are not yet available to render
		ellipse(swidth / 2, sheight / 2, 50, 50);

		// DRAW FOR INTERNAL CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_INTERNAL_CAMERA)) {
			// Read rgbCam
			if (rgbCam.available() == true) {
				rgbCam.read();

				// Analyze Camera Feed Here
				// ...

				// create new images from custom filters
				outputImg = rgbCam;
				// outputImg = removeRedFilter.getFilteredImage(outputImg);
				outputImg = slitScan.getFilteredImage(outputImg);
				// outputImg = scaledIn.getFilteredImage(this, outputImg);

			}
		}

		// DRAW FOR KINECT CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			kinect.update();

			outputImg = kinect.depthImage();

			outputImg = slitScan.getFilteredImage(outputImg);
		}

		// ///////////////////////

		// draw filtered image
		if (outputImg != null) {
			set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
		}

		// add any inbuilt p5 filters here
		// ...

	}

}
