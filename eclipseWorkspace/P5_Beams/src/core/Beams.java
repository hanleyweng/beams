package core;

import java.util.ArrayList;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import processing.video.Movie;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
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
import filter3d.BasicFrameDifferencer;
import filter3d.ContourMatrixFromDepth;
import filter3d.PVectorMatrixSmoother;
import filter3d.SlitScan3d;
import gab.opencv.OpenCV;

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

	int swidth = 640;
	int sheight = 480;

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
	PVectorMatrixSmoother depthMapZsmoother;
	SlitScan3d depthMapSlitScanner;
	SlitScan3d colorMapSlitScanner;
	BasicFrameDifferencer frameDifferencer;
	ContourMatrixFromDepth contourMatrixer;

	// PEASYCAM
	PeasyCam cam;

	// Input - Pre-recorded movie of kinect depth information
	Movie mov;

	// OpenCV
	OpenCV opencv;

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
		frameRate(30);
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

		// SETUP KINECT FILTERS
		this.setupKinectFilters();

		// SETUP PEASY CAM
		cam = new PeasyCam(this, swidth * 0.5, sheight * 0.5, -2000, 2700);

		// PERSPECTIVE
		float cameraZ = (height / 2.0f) / tan(PI * 60.0f / 360.0f);
		// Default // perspective(PI / 3.0f, width * 1f / height, cameraZ / 10.0f, cameraZ * 10.0f);
		// Set clipping plane further out
		perspective(PI / 3.0f, width * 1f / height, cameraZ / 10.0f, cameraZ * 1000.0f);

	}

	void setupKinectFilters() {
		depthMapZsmoother = new PVectorMatrixSmoother(kinectWidth, kinectHeight);
		depthMapSlitScanner = new SlitScan3d(kinectWidth, kinectHeight);
		colorMapSlitScanner = new SlitScan3d(kinectWidth, kinectHeight);
		frameDifferencer = new BasicFrameDifferencer(30, kinectWidth, kinectHeight);
		contourMatrixer = new ContourMatrixFromDepth();
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
		// realWorldMap = kinect.depthMapRealWorld();
		// PImage depthImg = kinect.depthImage();
		// PImage colorImg = kinect.rgbImage();

		// UPDATE FILTERS
		int[] curDepthMatrix = depthMap;

		// frameDifferencer.updateStream(depthMap, 30);
		// curDepthMatrix = frameDifferencer.getOutputMatrix();

		// curDepthMatrix = getMatrixWithinDepthRange(0, 3000, curDepthMatrix);

		depthMapSlitScanner.updateStream(curDepthMatrix, 20);
		curDepthMatrix = depthMapSlitScanner.getFilteredMatrix();

		// colorMapSlitScanner.updateStream(colorImg.pixels, 20);

		// Background Image Update
		int[] bgDepthMatrix = depthMap;
		bgDepthMatrix = getMatrixWithinDepthRange(3000, 5000, bgDepthMatrix);

		// DRAW
		background(0);

		this.drawPointsIn3D(curDepthMatrix, contourMatrixer.getContourMatrix_linearGradient2(curDepthMatrix, frameCount * 5f, frameCount), 15);

		// this.drawPointsIn3D(depthMapSlitScanner.getFilteredMatrix(), null, 40);
		// this.drawPointsIn3D(depthMapSlitScanner.getFilteredMatrix(), colorMapSlitScanner.getFilteredMatrix());
		// this.drawMeshIn3D(depthMapSlitScanner.getFilteredMatrix(), 7);

		// this.drawPointsIn3D(frameDifferencer.getOutputMatrix(), null, 6);

		// this.drawMeshIn3D(curDepthMatrix, 3);

		// this.drawMeshIn3D(curDepthMatrix, colorMapSlitScanner.getFilteredMatrix(), 7);

		// this.drawPointsIn3D(frameDifferencer.getOutputMatrix(), null, 3);

		// this.drawMeshIn3D(curDepthMatrix, contourMatrixer.getContourMatrix_rainbowVersion(curDepthMatrix, frameCount * 5f), 3); //<- be careful of using frameCount here in case it exceeds maximum value

		// this.drawPointsIn3D(curDepthMatrix, null, 10);

		// Drawing FrameRate
		cam.beginHUD();
		fill(255);
		textAlign(RIGHT, TOP);
		text(frameRate, swidth - 20, 20);
		cam.endHUD();

	}

	public void keyPressed() {
		if (key == 'c') {
			cam.reset();
		}
	}

	public int[] getMatrixWithinDepthRange(int minDepthRange, int maxDepthRange, int[] depthMatrix) {
		int[] outputMatrix = new int[depthMatrix.length];

		for (int i = 0; i < depthMatrix.length; i++) {
			int depthValue = depthMatrix[i];

			if ((depthValue < minDepthRange) || (depthValue > maxDepthRange)) {
				depthValue = 0;
			}

			outputMatrix[i] = depthValue;
		}

		return outputMatrix;
	}

	public void setMatrixAsPImage(PImage img, int[] depthValues) {
		if (depthValues.length != img.pixels.length) {
			System.err.println("matrix size does not match image.pixels size");
			return;
		}

		int minValue = Integer.MAX_VALUE;
		int maxValue = Integer.MIN_VALUE;
		for (int i = 0; i < depthValues.length; i++) {
			int curValue = depthValues[i];
			if (curValue > maxValue) {
				maxValue = curValue;
			}
			if (curValue < minValue) {
				minValue = curValue;
			}
		}

		setMatrixAsPImage(img, depthValues, minValue, maxValue);

	}

	public void setMatrixAsPImage(PImage img, int[] depthValues, int minValue, int maxValue) {
		if (depthValues.length != img.pixels.length) {
			System.err.println("matrix size does not match image.pixels size");
			return;
		}

		// PImage img = new PImage(width, height); // <- this method may be inefficient, may be more efficient to pass in an existing PImage

		img.loadPixels();
		for (int i = 0; i < depthValues.length; i++) {
			int grayValue = 0;
			int curValue = depthValues[i];
			if (curValue != 0) {
				grayValue = (int) map(depthValues[i], minValue, maxValue, 0, 255);
			}
			img.pixels[i] = 0xff000000 | (grayValue << 16) | (grayValue << 8) | grayValue;
		}

		img.updatePixels();
	}

	/**
	 * Draws a point cloud from a depthMap.
	 * 
	 * @param depthValues
	 * @param pixelColors
	 *            - can be null
	 * @param resolution
	 */
	public void drawPointsIn3D(int[] depthValues, int[] pixelColors, int resolution) {
		pushMatrix();
		pushStyle();

		translate(swidth / 2, sheight / 2, 0);
		rotateX(radians(180));

		int currColor = 0xff000000 | (255 << 24) | (255 << 16) | (255 << 8) | 255;
		// int currColor2 = 0xff000000 | (150 << 24) | (255 << 16) | (255 << 8) | 255;

		stroke(255);

		int res = resolution;
		for (int x = 0; x < kinectWidth; x += res) {
			for (int y = 0; y < kinectHeight; y += res) {
				int index = x + y * kinectWidth;
				int depth = depthValues[index];

				PVector realWorldPoint = new PVector();
				kinect.convertProjectiveToRealWorld(new PVector(x, y, depth), realWorldPoint);

				// Decide if we should draw point
				if (realWorldPoint.z == 0) { // ~
					continue;
				}

				// Set color of point
				if (pixelColors != null) {
					currColor = pixelColors[index];

					// int r = (currColor >> 16) & 0xFF;
					// int g = (currColor >> 8) & 0xFF;
					// int b = currColor & 0xFF;
					// int a = 100; // (currColor>>24)&0xFF;

					// currColor2 = (a << 24) | (r << 16) | (g << 8) | b;

					// Draw Additional Point
					// stroke(currColor2);

					// stroke(r, g, b, 10);
					// strokeWeight(30);
					// point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);
					//
					// stroke(r, g, b, 70);
					// strokeWeight(10);
					// point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);

				}

				stroke(currColor);
				strokeWeight(3);
				point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);

			}
		}

		popStyle();
		popMatrix();
	}

	/**
	 * Draws mesh from a depthMap with color.
	 * 
	 * @param depthValues
	 * @param pixelColors
	 *            - can be null
	 * @param resolution
	 */
	public void drawMeshIn3D(int[] depthValues, int[] pixelColors, int resolution) {
		TriangleMesh mesh = new TriangleMesh();

		pushMatrix();
		pushStyle();

		translate(swidth / 2, sheight / 2, 0);
		rotateX(radians(180));
		// strokeWeight(1);
		// stroke(255);
		colorMode(HSB, 100);
		lights();
		// // DEFAULT LIGHTS
		// lightFalloff(1, 0, 0); // like fill - for lights
		// lightSpecular(0, 0, 0); // like fill - for lights
		// ambientLight(0, 0, 50);
		// // directionalLight(0, 0, 75, 0, 0, -1);
		// // directionalLight(0, 0, 75, 0, -1, 0);
		// directionalLight(0, 0, 75, 0, -1, -1);
		directionalLight(0, 0, 75, -1, -1, -1);

		PVector pointLightHsb = new PVector(0, 0, 100);
		pointLight(pointLightHsb.x, pointLightHsb.y, pointLightHsb.z, 0, -500, 0);
		noStroke();

		int res = resolution;

		// ArrayList<Vec3D> realWorldPoints = new ArrayList<Vec3D>();
		Vec3D[] realWorldPoints = new Vec3D[kinectWidth * kinectHeight];
		// Store PVectors of points in real world locations
		for (int x = 0; x < kinectWidth; x += res) {
			for (int y = 0; y < kinectHeight; y += res) {
				int index = x + y * kinectWidth;
				int depth = depthValues[index];
				PVector realWorldPoint = new PVector();
				kinect.convertProjectiveToRealWorld(new PVector(x, y, depth), realWorldPoint);
				Vec3D point = new Vec3D(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);
				realWorldPoints[index] = point;
			}
		}
		// Generate meshes to store
		ArrayList<Integer> faceColors = new ArrayList<Integer>();
		for (int x = 0; x < kinectWidth - res; x += res) {
			for (int y = 0; y < kinectHeight - res; y += res) {

				// Get Quad of points
				Vec3D[] quadPoints = new Vec3D[4];
				// Quad goes: TopLeft, TopRight, BottomRight, BottomLeft
				boolean filteredOut = false;
				for (int i = 0; i < 4; i++) {
					// v- instead of incrementing by one here, it perhaps should be res actually!
					int cx = x;
					int cy = y;
					if (i == 1) {
						cx += res;
					}
					if (i == 2) {
						cx += res;
						cy += res;
					}
					if (i == 3) {
						cy += res;
					}
					int index = cx + cy * kinectWidth;
					Vec3D curVec = realWorldPoints[index];
					if (curVec.z == 0) {
						filteredOut = true;
						continue;
					}
					quadPoints[i] = curVec;
				}
				if (!filteredOut) {
					// Filter out faces that stretch very long distances
					int tooFarDistance = 70;
					if (Math.abs(quadPoints[0].z - quadPoints[1].z) > tooFarDistance) {
						filteredOut = true;
					}
					if (Math.abs(quadPoints[0].z - quadPoints[2].z) > tooFarDistance) {
						filteredOut = true;
					}
					if (Math.abs(quadPoints[0].z - quadPoints[3].z) > tooFarDistance) {
						filteredOut = true;
					}
					if (Math.abs(quadPoints[1].z - quadPoints[2].z) > tooFarDistance) {
						filteredOut = true;
					}
					if (Math.abs(quadPoints[1].z - quadPoints[3].z) > tooFarDistance) {
						filteredOut = true;
					}
					if (Math.abs(quadPoints[2].z - quadPoints[3].z) > tooFarDistance) {
						filteredOut = true;
					}
				}

				if (filteredOut) {
					// if filtered out continue without adding faces
					continue;
				}

				// Generate 2 faces
				mesh.addFace(quadPoints[0], quadPoints[1], quadPoints[3]);
				mesh.addFace(quadPoints[1], quadPoints[2], quadPoints[3]);

				// Add colors
				if (pixelColors != null) {
					faceColors.add(pixelColors[x + y * kinectWidth]);
					faceColors.add(pixelColors[x + y * kinectWidth]);
				}

			}
		}

		// Draw Mesh
		// fill(25, 60, 80);
		stroke(0, 0, 100);
		noStroke();
		fill(70, 60, 80, 60);

		beginShape(TRIANGLES);
		// iterate over all faces/triangles of the mesh
		for (int i = 0; i < mesh.faces.size(); i++) {
			if (pixelColors != null) {
				fill(faceColors.get(i));
			}

			Face f = mesh.faces.get(i);
			vertex(f.a);
			vertex(f.b);
			vertex(f.c);
		}
		endShape();

		popStyle();
		popMatrix();
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

	void vertex(Vec3D v) {
		vertex(v.x, v.y, v.z);
	}

}
