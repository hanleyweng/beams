package core;

import java.util.ArrayList;

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

	// Kinect Filters
	MatrixSmoother matrixSmoother;

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

		this.setupKinectFilters();
	}

	void setupKinectFilters() {
		matrixSmoother = new MatrixSmoother(kinectWidth, kinectHeight);
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
		this.drawDepthMatrix(matrixSmoother.getSmootherMatrix(), matrixSmoother.matrixMaxValue, matrixSmoother.matrixWidth, matrixSmoother.matrixHeight);

		// image(depthImg, 0, 0);

		// TODO: feed depthMapArray into a filter for a smootherMatrix

	}

	public void drawDepthMatrix(int[] matrix, int mmaxValue, int mwidth, int mheight) {
		for (int x = 0; x < mwidth; x++) {
			for (int y = 0; y < mheight; y++) {
				int index = x + y * mwidth;
				int matrixValue = matrix[index];
				int displayValue = (int) mapWithCap(matrixValue, 0, mmaxValue, 255, 0);
				stroke(displayValue);
				point(x, y);
			}
		}
	}

	/**
	 * A filter ideal for efficiently smoothing out the noisy kinect depthMap.
	 * 
	 * This object should be used live. It takes in a matrix every time it is updated.
	 * 
	 * @author hanleyweng
	 * 
	 */
	public class MatrixSmoother {

		int maxMatrices;

		ArrayList<int[]> matrices;

		int[] smootherMatrix;

		int matrixMaxValue;

		int matrixWidth, matrixHeight;

		MatrixSmoother(int matrixWidth, int matrixHeight) {
			this.matrixWidth = matrixWidth;
			this.matrixHeight = matrixHeight;
			maxMatrices = 1;
			matrices = new ArrayList<int[]>();
			matrixMaxValue = Integer.MIN_VALUE;

			smootherMatrix = new int[matrixWidth * matrixHeight];
		}

		void updateStream(int[] matrix) {

			// Add Matrix
			matrices.add(0, matrix);

			// For every pixel. Store that pixel to smootherMatrix if it isn't zero. If it is zero, try the next matrix.
			for (int i = 0; i < matrix.length; i++) {
				for (int m = 0; m < matrices.size(); m++) {
					int[] curMatrix = matrices.get(m);
					int value = curMatrix[i];
					if (value > matrixMaxValue) {
						matrixMaxValue = value;
					}
					if (value != 0) {
						smootherMatrix[i] = value;
						break;
					}
					// since we don't have a clause here for what to do when all pixels are zero; it will continue to use it's historic values as opposed to reverting back to zero.
				}
			}

			// Restrict size
			while (matrices.size() > maxMatrices) {
				// Remove last item if too full
				matrices.remove(matrices.size() - 1);
			}

		}

		int[] getSmootherMatrix() {
			return smootherMatrix;
		}

		int getMatrixMaxValue() {
			return matrixMaxValue;
		}

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
