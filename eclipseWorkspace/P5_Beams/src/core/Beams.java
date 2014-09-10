package core;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import processing.video.Movie;
import util.OscHandler;
import SimpleOpenNI.SimpleOpenNI;
import codeanticode.syphon.SyphonServer;
import filter.DepthThresholder;
import filter.Posterize;
import filter.RemoveRed;
import filter.ScaledIn;
import filter.SlitScan;
import filter.ZaxisContours;
import filter.ZaxisSlitScan;
import filter3d.MatrixSmoother;

@SuppressWarnings("serial")
public class Beams extends PApplet {

	// OPTIONS!
	String INPUT_MODE = INPUT_MODE_KINECT;
	static final boolean RECEIVE_OSC = true; // start OSC Server in OscHandler class.
	static final boolean SEND_TO_SYPHON = true;

	// ////////////////////////////////////////////////////////////////////////////

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
	static int kinectWidth = 640;
	static int kinectHeight = 480;
	int[] depthMap;
	PVector[] realWorldMap;
	// http://forum.processing.org/one/topic/cannot-get-this-program-to-record.html
	float[] depthLookUp = new float[2048]; // We'll use a lookup table so that we don't have to repeat the math over and over

	// Kinect Filters
	MatrixSmoother matrixSmoother;
	PImage kinectDepthFilteredImage;

	// PEASYCAM
	PeasyCam cam;

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

		// //////////////////////////
		// Lookup table for all possible depth values (0 - 2047)
		for (int i = 0; i < depthLookUp.length; i++) {
			depthLookUp[i] = rawDepthToMeters(i);
		}

		// SETUP KINECT FILTERS
		this.setupKinectFilters();

		// SETUP PEASY CAM
		cam = new PeasyCam(this, swidth * 0.5, sheight * 0.5, -2000, 2700);

	}

	void setupKinectFilters() {
		matrixSmoother = new MatrixSmoother(kinectWidth, kinectHeight);
		kinectDepthFilteredImage = new PImage(kinectWidth, kinectHeight);
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
			this.drawForKinect();
		}

		// DRAW FOR MOVIE
		if (INPUT_MODE.equals(INPUT_MODE_MOVIE)) {
			if (mov.available()) {
				mov.read();
			}
			outputImg = depthThresholder.getFilteredImage(mov);
			outputImg = zaxisContours.getFilteredImage(outputImg);
			image(outputImg, 0, 0);

			// draw filtered image
			if (outputImg != null) {
				set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
			}
		}

		// ///////////////////////
		// SYPHON

		if (SEND_TO_SYPHON) {
			if (outputImg != null) {
				syphonServer.sendImage(outputImg);
			}
		}

	}

	public void drawForKinect() {
		if (!INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			System.err.println("Input mode is not Kinect.");
			return;
		}

		// UPDATE
		kinect.update();
		depthMap = kinect.depthMap();
		realWorldMap = kinect.depthMapRealWorld();
		// PImage depthImg = kinect.depthImage();
		// PImage colorImg = kinect.rgbImage();

		// UPDATE FILTERS
		matrixSmoother.updateStream(depthMap);

		// DRAW
		background(0);

		// this.setDepthMatrixToImage(matrixSmoother.getSmootherMatrix(), matrixSmoother.getMatrixMaxValue(), kinectDepthFilteredImage);
		// image(kinectDepthFilteredImage, 0, 0);

		this.drawPointsIn3D(depthMap);
		this.drawPointsIn3D(realWorldMap);

	}

	public void setDepthMatrixToImage(int[] matrix, int matrixMaxValue, PImage image) {
		if (matrix.length != image.pixels.length) {
			System.err.println("matrix size does not match image.pixels size");
			return;
		}

		image.loadPixels();
		for (int i = 0; i < matrix.length; i++) {
			int value = matrix[i];
			value = (int) mapWithCap(value, 0, matrixMaxValue, 255, 0);
			image.pixels[i] = 0xff000000 | (value << 16) | (value << 8) | value;
		}
		image.updatePixels();
	}

	// Note - we have two options for locationData to feed in; either realWorldPositions or depthPositions which we then compute real world positions with depthToWorld(x,y,rawDepth)
	public void drawPointsIn3D(PVector[] realWorldPositions) {
		pushMatrix();
		pushStyle();

		translate(swidth / 2, sheight / 2, 0);
		rotateX(radians(180));
		colorMode(HSB, 100);
		strokeWeight(1);
		stroke(0, 0, 100);

		int res = 3;
		for (int x = 0; x < kinectWidth; x += res) {
			for (int y = 0; y < kinectHeight; y += res) {
				int index = x + y * kinectWidth;
				PVector realWorldPoint = realWorldPositions[index];
				if (realWorldPoint.z == 0) { // ~
					continue;
				}

				// Draw Point
				point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);

			}
		}

		popStyle();
		popMatrix();
	}

	public void drawPointsIn3D(int[] depthValues) {
		pushMatrix();
		pushStyle();

		translate(swidth / 2, sheight / 2, 0);
		rotateX(radians(180));
		colorMode(HSB, 100);
		strokeWeight(2);
		stroke(30, 65, 100);

		int res = 3;
		for (int x = 0; x < kinectWidth; x += res) {
			for (int y = 0; y < kinectHeight; y += res) {
				int index = x + y * kinectWidth;
				int depth = depthValues[index];
				if (depth == 0) {
					continue;
				}

				PVector realWorldPoint = depthToWorld(x, y, depth);

				// Draw Point
				point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);

			}
		}

		popStyle();
		popMatrix();
	}

	// These functions come from: http://graphics.stanford.edu/~mdfisher/Kinect.html
	float rawDepthToMeters(int depthValue) {
		if (depthValue < 2047) {
			return (float) (1.0 / ((double) (depthValue) * -0.0030711016 + 3.3309495161));
		}
		return 0.0f;
	}

	PVector depthToWorld(int x, int y, int depthValue) {

		final double fx_d = 1.0 / 5.9421434211923247e+02;
		final double fy_d = 1.0 / 5.9104053696870778e+02;
		final double cx_d = 3.3930780975300314e+02;
		final double cy_d = 2.4273913761751615e+02;

		PVector result = new PVector();
		double depth = depthLookUp[depthValue];// rawDepthToMeters(depthValue);
		result.x = (float) ((x - cx_d) * depth * fx_d);
		result.y = (float) ((y - cy_d) * depth * fy_d);
		result.z = (float) (depth);
		return result;
	}

	/**
	 * same as map() function, except values are capped at start2 and stop2
	 * 
	 * @param value
	 * @param start1
	 * @param stop1
	 * @param start2
	 * @param stop2
	 * @return mapped and capped value
	 */
	public float mapWithCap(float value, float start1, float stop1, float start2, float stop2) {
		float v = map(value, start1, stop1, start2, stop2);
		float biggerValue = Math.max(start2, stop2);
		float smallerValue = Math.min(start2, stop2);
		v = Math.min(biggerValue, v);
		v = Math.max(smallerValue, v);
		return v;
	}

}
