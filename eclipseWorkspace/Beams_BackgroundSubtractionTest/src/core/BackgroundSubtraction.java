package core;

import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import processing.video.Movie;
import SimpleOpenNI.SimpleOpenNI;
// This is from Processing2.2.1

// TODO: Add Analysis'

@SuppressWarnings("serial")
public class BackgroundSubtraction extends PApplet {

	// OPTIONS!
	String INPUT_MODE = INPUT_MODE_INTERNAL_CAMERA;

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

	// Input - Pre-recorded movie of kinect depth information
	Movie mov;

	BufferOfMatrices depthBuffer, grayImageBuffer;

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

		//
		// depthBuffer = new BufferOfMatrices(50, 30 * 1000);
		grayImageBuffer = new BufferOfMatrices(10, 10 * 1000);

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

	int[] depthMap;
	PVector[] realWorldMap;

	@Override
	public void draw() {
		// Draw BG Circle to represent frames are not yet available to render
		ellipse(swidth / 2, sheight / 2, 50, 50);

		// UPDATE

		// KINECT TEST
		if (INPUT_MODE.equals(INPUT_MODE_KINECT)) {
			// UPDATE
			kinect.update();
			depthMap = kinect.depthMap();
			realWorldMap = kinect.depthMapRealWorld();

			// DRAW
			pushStyle();
			colorMode(HSB, 100);

			int res = 5;

			int index;
			for (int y = 0; y < kinect.depthHeight(); y += res) {
				for (int x = 0; x < kinect.depthWidth(); x += res) {
					index = x + y * kinect.depthWidth();
					float depthValue = depthMap[index]; // depthMap is distance in mm
					if (depthMap[index] > 0) {
						float brightness = map(depthValue, 0, 3000, 100, 0);
						stroke(0, 0, brightness);
						strokeWeight(5);
						point(x, y);
					}
				}
			}

			popStyle();
		}

		// CAMERA TEST
		// DRAW FOR INTERNAL CAMERA
		if (INPUT_MODE.equals(INPUT_MODE_INTERNAL_CAMERA)) {
			background(0);

			// Read rgbCam
			if (rgbCam.available() == true) {
				// update
				rgbCam.read();

				// Make GRAYSCALE Image
				// Load Pixels
				rgbCam.loadPixels();
				float[][] grayMatrix = new float[rgbCam.width][rgbCam.height];
				for (int y = 0; y < rgbCam.height; y++) {
					for (int x = 0; x < rgbCam.width; x++) {
						int index = x + y * rgbCam.width;
						int currColor = rgbCam.pixels[index];
						// Get current Colors
						int currR = (currColor >> 16) & 0xFF;
						// int currG = (currColor >> 8) & 0xFF;
						// int currB = currColor & 0xFF;
						// set new color of pixel
						rgbCam.pixels[index] = 0xff000000 | (currR << 16) | (currR << 8) | currR;

						grayMatrix[x][y] = currR;
					}
				}
				// Add Pixels to a buffer
				grayImageBuffer.updateStream(grayMatrix);

				// Update Pixels
				rgbCam.updatePixels();
			}

			// Display image
			pushMatrix();
			scale(0.5f);
			image(rgbCam, 0, 0);

			// Draw Background Image Also
			if (grayImageBuffer.medianValueImage != null) {
				image(grayImageBuffer.medianValueImage, 640, 0);
			}

			popMatrix();

		}

	}

	class BufferOfMatrices {
		int maxBufferSize; // maxImages
		int bufferDuration; // in millis
		int timeAtLastImage;

		ArrayList<float[][]> matrices = new ArrayList<float[][]>();

		// boolean calculateBackgroundOnUpdate = true;

		float[][] medianValueMatrix;
		PImage medianValueImage;

		BufferOfMatrices(int maxBufferSize, int bufferDuration_inMillis) {
			this.maxBufferSize = maxBufferSize;
			this.bufferDuration = bufferDuration_inMillis;

			timeAtLastImage = millis();
		}

		float getBufferStorageFrequency() {
			return bufferDuration / maxBufferSize;
		}

		/**
		 * Object should be continuously fed with matrices - it will decide if it keeps them or not
		 */
		void updateStream(float[][] matrix) {
			// Decide if we should add matrix
			if ((timeAtLastImage + this.getBufferStorageFrequency()) < millis()) {
				// Add Matrix
				matrices.add(0, matrix);
				timeAtLastImage = millis();
				// Note - millis() Returns the number of milliseconds (thousandths of a second) since starting the program.

				// Calculate MedianValueMatrix
				if (matrices.size() > 0) {
					medianValueMatrix = medianValuesOfMatrices(this);

					if (medianValueImage == null) {
						medianValueImage = new PImage(matrices.get(0).length, matrices.get(0)[0].length);
					}
					
					// Create Image of medianValues
					medianValueImage.loadPixels(); // ~
					pushStyle();
					colorMode(HSB, 100);
					strokeWeight(1);
					float[][] bgImage = grayImageBuffer.getMedianValueMatrix();
					if (bgImage != null) {
						for (int x = 0; x < bgImage.length; x++) {
							for (int y = 0; y < bgImage[0].length; y++) {
								int value = (int) bgImage[x][y];
								// value is in 255
								// int brightness = (int) map(value,0,255,0,100);
								// stroke(0, 0, brightness);
								// point(x, y);
								int index = x + y * bgImage.length;
								medianValueImage.pixels[index] = 0xff000000 | (value << 16) | (value << 8) | value;
							}
						}
					}
					popStyle();
					medianValueImage.updatePixels();

				}

			}

			// Restrict size
			while (matrices.size() > maxBufferSize) {
				// Remove last item if too full
				matrices.remove(matrices.size() - 1);
			}

		}

		float[][] getMedianValueMatrix() {
			return medianValueMatrix;
		}
	}

	/**
	 * - Useful for simple non-recursive background modeling. Mean values are also possible
	 * 
	 * - Note this is quite simple and does not weight more recent matrices higher than older matrices
	 * 
	 * @param matrices
	 *            - accepts matrices of the same width and height
	 * @return
	 */
	float[][] medianValuesOfMatrices(BufferOfMatrices buffer) {
		int numMatrices = buffer.matrices.size();
		if (buffer.matrices.size() == 0)
			return null;

		int width = buffer.matrices.get(0).length;
		int height = buffer.matrices.get(0)[0].length;

		float[][] returnMatrix = new float[width][height];

		// Go through entire 2d-array
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// Get all t-numbers for a specific cell
				float[] numbers = new float[numMatrices];
				for (int i = 0; i < buffer.matrices.size(); i++) {
					numbers[i] = buffer.matrices.get(i)[x][y];
				}
				// Arrange numbers
				Arrays.sort(numbers);
				float median;
				if (numbers.length % 2 == 0)
					median = (numbers[numbers.length / 2] + numbers[numbers.length / 2 - 1]) / 2;
				else
					median = numbers[numbers.length / 2];

				returnMatrix[x][y] = median;
			}
		}

		return returnMatrix;
	}

}
