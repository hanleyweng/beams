package core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import processing.core.*; // This is from Processing2.2.1
import processing.video.*;
import util.OscHandler;
import SimpleOpenNI.*;
import codeanticode.syphon.*;
import filter.DepthThresholder;
import filter.KeepOnlyRed;
import filter.Lines;
import filter.MedianBlur;
import filter.MorphologicalGradient;
import filter.MorphologicalSmoothing;
import filter.Posterize;
import filter.RemoveRed;
import filter.ScaledIn;
import filter.SlitScan;
import filter.ZaxisContours;
import filter.ZaxisSlitScan;
import gab.opencv.*;

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

	// Input - Camera
	Capture rgbCam;
	public static int rgbCamWidth = 640;
	public static int rgbCamHeight = 480;
	int rgbCamFps = 30;
	
	int swidth = rgbCamWidth*2;
	int sheight = rgbCamHeight;

	// Input - Kinect
	SimpleOpenNI kinect;
	
	// Input - Pre-recorded movie of kinect depth information
	Movie mov;
	
	// OpenCV
	OpenCV opencv;

	// Filters
	RemoveRed removeRedFilter = new RemoveRed();
	KeepOnlyRed keepOnlyRed = new KeepOnlyRed();
	DepthThresholder depthThresholder = new DepthThresholder();
	SlitScan slitScan = new SlitScan();
	ScaledIn scaledIn = new ScaledIn();
	ZaxisSlitScan zaxisSlit = new ZaxisSlitScan();
	
	ZaxisContours zaxisContours = new ZaxisContours();
	Posterize posterize = new Posterize();
	MedianBlur medianBlur;  					 // setup in setup(): requires opencv.
	MorphologicalGradient morphologicalGradient; // setup in setup(): requires opencv.
	MorphologicalSmoothing morphologicalSmoothing; // setup in setup(): requires opencv.
	Lines lines = new Lines(this);
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
		
		// setup opencv
		opencv = new OpenCV(this, rgbCamWidth, rgbCamHeight);
		medianBlur = new MedianBlur(opencv);		
		morphologicalGradient = new MorphologicalGradient(opencv);
		morphologicalSmoothing = new MorphologicalSmoothing(opencv);
		
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
			outputImg = depthThresholder.getFilteredImage(depthImg);
			outputImg = zaxisContours.getFilteredImage(outputImg);
			outputImg = slitScan.getFilteredImage(outputImg);

		}
		
		
		// DRAW FOR MOVIE
		if (INPUT_MODE.equals(INPUT_MODE_MOVIE)) {
			if (mov.available()) {
				mov.read();
			}
			outputImg = depthThresholder.getFilteredImage(mov);
			outputImg = slitScan.getFilteredImage(outputImg);
			outputImg = morphologicalSmoothing.getFilteredImage(outputImg);
			//outputImg = posterize.getFilteredImage(outputImg);
			//outputImg = zaxisContours.getFilteredImage(outputImg);
		}
		// DRAW FOR MOVIE
		PImage outputImg2 = null;
		if (INPUT_MODE.equals(INPUT_MODE_MOVIE)) {
			//outputImg2 = morphologicalSmoothing.getFilteredImage(outputImg);
			//outputImg2 = lines.drawLines(outputImg2);
			//outputImg2 = slitScan.getFilteredImage(outputImg);
			//outputImg2 = morphologicalGradient.getFilteredImage(outputImg2);
			//outputImg = posterize.getFilteredImage(outputImg);
			//outputImg = zaxisContours.getFilteredImage(outputImg);
		}
		

		// draw filtered image
		if (outputImg != null) {
			set(0, 0, outputImg); // faster way of drawing (non-manipulated) image
			//fill(0);
			//rect(rgbCamWidth, 0, rgbCamWidth, rgbCamHeight);
			
			/////////// IVE BEEN PLAYING AROUND HERE!
			//image(outputImg2, rgbCamWidth, 0);
//			set(rgbCamWidth, 0, outputImg2);
		}

		// add any inbuilt p5 filters here
		// ...

		if (SEND_TO_SYPHON) {
			if (outputImg != null) {
				syphonServer.sendImage(outputImg);
				//syphonServer.sendImage(get(rgbCamWidth,0,rgbCamWidth, rgbCamHeight));
			}
		}

	}

}
