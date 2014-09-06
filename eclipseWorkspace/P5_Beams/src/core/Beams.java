package core;
import processing.core.*; // This is from Processing2.2.1
import processing.video.*;
import util.OscHandler;
import SimpleOpenNI.*;
import codeanticode.syphon.*;
import filter.DepthThresholder;
import filter.Posterize;
import filter.RemoveRed;
import filter.ScaledIn;
import filter.SlitScan;
import filter.ZaxisContours;
import filter.ZaxisSlitScan;

// TODO: Add Analysis'

@SuppressWarnings("serial")
public class Beams extends PApplet {

	// OPTIONS!
	String INPUT_MODE = INPUT_MODE_MOVIE;
	static final boolean RECEIVE_OSC = true; // start OSC Server in OscHandler class.
	static final boolean SEND_TO_SYPHON = true;

	
	//////////////////////////////////////////////////////////////////////////////

	static final String INPUT_MODE_INTERNAL_CAMERA = "INPUT_MODE_INTERNAL_CAMERA";
	static final String INPUT_MODE_KINECT = "INPUT_MODE_KINECT";
	static final String INPUT_MODE_MOVIE = "INPUT_MODE_MOVIE";

	int swidth = 800;
	int sheight = 600;

	// Input - Camera
	Capture rgbCam;
	int rgbCamWidth = 640;
	int rgbCamHeight = 480;
	int rgbCamFps = 30;

	// Input - Kinect
	SimpleOpenNI kinect;
	
	// Input - Pre-recorded movie of kinect depth information
	Movie mov;

	// Filters
	RemoveRed removeRedFilter = new RemoveRed();
	DepthThresholder depthThresholder = new DepthThresholder();
	SlitScan slitScan = new SlitScan();
	ScaledIn scaledIn = new ScaledIn();
	ZaxisSlitScan zaxisSlit = new ZaxisSlitScan();
	ZaxisContours zaxisContours = new ZaxisContours();
	Posterize posterize = new Posterize();

	PImage outputImg;

	// Handle osc messages from PureData.
	OscHandler oscHandler;
	
	// Output to Syphon
	SyphonServer syphonServer;

	@Override
	public void setup() {
		size(swidth, sheight, OPENGL);

		// INITIALIZE CHOSEN CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_INTERNAL_CAMERA)) {
			this.setupInternalCamera();
		} else if (INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			this.setupKinectCamera();
		} else if (INPUT_MODE.equals(INPUT_MODE_MOVIE)) {
			this.setupMovie();
		}
		
		if (RECEIVE_OSC) {
			oscHandler = new OscHandler();
		}

		// Create syphon server to send frames out.
		if (SEND_TO_SYPHON) {
			syphonServer = new SyphonServer(this, "BeamsSyphon");
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

		// align depth data to image data
		kinect.alternativeViewPointDepthToImage();
		kinect.setDepthColorSyncEnabled(true);
	}
	
	void setupMovie() {
		mov = new Movie(this, "Raw Depth Kinect video.mp4");
		mov.loop();
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

				// draw filtered image
				if (outputImg != null) {
					set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
				}

				// add any inbuilt p5 filters here
				// ...

			}
		}

		// DRAW FOR KINECT CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			kinect.update();

			PImage depthImg = kinect.depthImage();
			PImage colorImg = kinect.rgbImage();
			// outputImg = slitScan.getFilteredImage(outputImg);

			// // Drawing a blend of depth and color img
			// TODO: Create filter to blend images together.
			// pushStyle();
			// image(depthImg, 0, 0);
			// tint(255, 100);
			// image(colorImg, 0, 0);
			// popStyle();

			// outputImg = zaxisSlit.getFilteredImage(depthImg, colorImg);
			outputImg = zaxisContours.getFilteredImage(depthImg);

			image(outputImg, 0, 0);

		}
		
		// DRAW FOR MOVIE
		if (INPUT_MODE.equals(INPUT_MODE_MOVIE)) {
			if (mov.available()) {
				mov.read();
			}
			outputImg = depthThresholder.getFilteredImage(mov);
			outputImg = zaxisContours.getFilteredImage(outputImg);
			image(outputImg, 0, 0);
		}

		// ///////////////////////

		// draw filtered image
		if (outputImg != null) {
			set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
		}

		// add any inbuilt p5 filters here
		// ...

		if (SEND_TO_SYPHON) {
			if (outputImg != null) {
				syphonServer.sendImage(outputImg);
			}
		}

	}

}
