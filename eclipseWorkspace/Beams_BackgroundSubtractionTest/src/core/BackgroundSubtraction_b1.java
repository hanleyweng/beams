package core;

import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import processing.video.Movie;
import SimpleOpenNI.SimpleOpenNI;
// This is from Processing2.2.1

// TODO: Next look into backgroundModelling kinect.depthMap

@SuppressWarnings("serial")
public class BackgroundSubtraction_b1 extends PApplet {

	// OPTIONS!
	String INPUT_MODE = INPUT_MODE_KINECT;

	// ////////////////////////////////////////////////////////////////////////////

	static final String INPUT_MODE_INTERNAL_CAMERA = "INPUT_MODE_INTERNAL_CAMERA";
	static final String INPUT_MODE_KINECT = "INPUT_MODE_KINECT";
	static final String INPUT_MODE_MOVIE = "INPUT_MODE_MOVIE";

	int swidth = 1280;
	int sheight = 960;

	// Input - Camera
	Capture rgbCam;
	int rgbCamWidth = 640;
	int rgbCamHeight = 480;
	int rgbCamFps = 30;

	// Input - Kinect
	SimpleOpenNI kinect;
	static int kinectDepthWidth = 640;
	static int kinectDepthHeight = 480;

	// Input - Pre-recorded movie of kinect depth information
	Movie mov;

	BufferOfDepthMatrices depthBuffer;
	DepthMatrixSmoother smootherDepthMatrix;

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
		depthBuffer = new BufferOfDepthMatrices(5, 10 * 1000);

		// smootherDepthMatrix = new DepthMatrixSmoother(DepthMatrixSmoother.MODE_FirstNonzero, 1, kinectDepthWidth, kinectDepthHeight);
		smootherDepthMatrix = new DepthMatrixSmoother(DepthMatrixSmoother.MODE_LowestNonzeroValue, 60, kinectDepthWidth, kinectDepthHeight);
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
			// pushStyle();
			// colorMode(HSB, 100);

			// Remember depth for smoother image
			smootherDepthMatrix.updateStream(depthMap);

			// Draw smoother depthMatrix as image
			smootherDepthMatrix.getImageOfDepthMatrix();
			if (smootherDepthMatrix.image != null) {
				image(smootherDepthMatrix.image, 0, 480);
			}

			// Store depth map in buffer
			depthBuffer.updateStream(smootherDepthMatrix.smootherMatrix, kinectDepthWidth, kinectDepthHeight);

			// Display Depth Map
			image(kinect.depthImage(), 0, 0);

			// Display background image
			if (depthBuffer.backgroundImage != null) {
				image(depthBuffer.backgroundImage, 640, 0);
			}

			// Display foreground image
			if (depthBuffer.foregroundImage != null) {
				image(depthBuffer.getForegroundImageFromCurrentMatrix(smootherDepthMatrix.smootherMatrix, kinectDepthWidth, kinectDepthHeight, 200), 640, 480);
			}

			// popStyle();
		}

	}

	/**
	 * Stores a series of matrices and goes through them until a non-zero value can be found for each pixel. This object should be used live. It takes in a matrix every time it is updated.
	 * 
	 * @author hanleyweng
	 * 
	 */
	class DepthMatrixSmoother {
		int maxMatrices;

		static final String MODE_FirstNonzero = "Gets first non zero value - fast."; // Fast as it does not need to store frames.
		static final String MODE_LowestNonzeroValue = "Gets lowest non zero value in the buffer - means however that closer objects will stay on screen longer.";
		String MODE = DepthMatrixSmoother.MODE_FirstNonzero;

		ArrayList<int[]> matrices;

		int[] smootherMatrix;

		int width;
		int height;
		int matrixMaxValue;
		PImage image;

		/**
		 * 
		 * @param maxMatrices
		 *            - note - this is redundant if mode is set too MODE_FirstNonzero.
		 * @param matrixWidth
		 * @param matrixHeight
		 */
		DepthMatrixSmoother(String mode, int maxMatrices, int matrixWidth, int matrixHeight) {

			if (!mode.equals(MODE_FirstNonzero)) {
				if (!mode.equals(MODE_LowestNonzeroValue)) {
					System.err.println("Please select an appropriate mode.");
				}
			}

			this.MODE = mode;

			this.maxMatrices = maxMatrices;
			this.width = matrixWidth;
			this.height = matrixHeight;

			smootherMatrix = new int[matrixWidth * matrixHeight];
			matrices = new ArrayList<int[]>();
			image = new PImage(width, height);
			matrixMaxValue = Integer.MIN_VALUE;

			if (this.MODE.equals(MODE_FirstNonzero)) {
				maxMatrices = 1;
			}
		}

		void updateStream(int[] matrix) {

			// Add Matrix
			matrices.add(0, matrix);

			// Calculate smoother matrix
			if (this.MODE.equals(MODE_FirstNonzero)) {
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
			}
			if (this.MODE.equals(MODE_LowestNonzeroValue)) {
				// println(matrices.size());
				// For every pixel - identify the lowest-non-zero-value in the series and assign it
				for (int i = 0; i < matrix.length; i++) {
					int lowestNonZeroValue = Integer.MAX_VALUE;
					// int lowestNonZeroValue = Integer.MIN_VALUE;
					for (int m = 0; m < matrices.size(); m++) {
						int[] curMatrix = matrices.get(m);
						int value = curMatrix[i];
						if (value > matrixMaxValue) {
							matrixMaxValue = value;
						}
						if ((value < lowestNonZeroValue) && (value != 0)) {
							// if ((value > lowestNonZeroValue) && (value != 0)) {
							lowestNonZeroValue = value;
						}
					}
					smootherMatrix[i] = lowestNonZeroValue;
				}
			}

			// Restrict size
			while (matrices.size() > maxMatrices) {
				// Remove last item if too full
				matrices.remove(matrices.size() - 1);
			}
		}

		PImage getImageOfDepthMatrix() {
			// Generate image
			image.loadPixels();
			for (int i = 0; i < smootherMatrix.length; i++) {
				int value = smootherMatrix[i];
				value = (int) mapWithCap(value, 0, matrixMaxValue, 255, 0);
				image.pixels[i] = 0xff000000 | (value << 16) | (value << 8) | value;
			}
			image.updatePixels();

			return image;
		}

	}

	/**
	 * Contains a series of Depth Matrices which can be used to model depth-background and hence depth-foreground.
	 * 
	 * This object should be continuously updated with matrices - it itself will decide if it keeps them or not.
	 * 
	 * @author hanleyweng
	 * 
	 */
	class BufferOfDepthMatrices {
		int maxBufferSize; // maxImages
		int bufferDuration; // in millis
		int timeOfLastAddition;

		ArrayList<int[][]> matrices = new ArrayList<int[][]>();

		int[][] backgroundModel_medianValueMatrix; // backgroundModel
		PImage backgroundImage;
		int backgroundImageMaxValue = 8000; // <- this value is dynamically set in updateStream

		PImage foregroundImage;

		BufferOfDepthMatrices(int maxBufferSize, int bufferDuration_inMillis) {
			this.maxBufferSize = maxBufferSize;
			this.bufferDuration = bufferDuration_inMillis;

			timeOfLastAddition = 0;
		}

		void updateStream(int[] matrix, int matrixWidth, int matrixHeight) {
			if ((timeOfLastAddition + this.getBufferStorageFrequency()) < millis()) {
				int[][] newMatrix = convertOneDimensionalToTwoDimensionalArray(matrix, matrixWidth, matrixHeight);
				updateStream(newMatrix);
			}
		}

		void updateStream(int[][] matrix) {
			// Decide if we should add matrix
			if ((timeOfLastAddition + this.getBufferStorageFrequency()) < millis()) {

				// Add Matrix
				matrices.add(0, matrix);
				timeOfLastAddition = millis();
				// Calculate MedianValueMatrix
				if (matrices.size() > 0) {
					// backgroundModel_medianValueMatrix = medianValuesOfIntMatrices(this, true, false);
					backgroundModel_medianValueMatrix = medianValuesOfIntMatrices(this, true, true);

					if (backgroundImage == null) {
						backgroundImage = new PImage(matrices.get(0).length, matrices.get(0)[0].length);
						foregroundImage = new PImage(matrices.get(0).length, matrices.get(0)[0].length);
					}

					// Create Image of medianValues - backgroundImage
					backgroundImage.loadPixels(); // ~
					int[][] bgImage = backgroundModel_medianValueMatrix;
					int maxValue = Integer.MIN_VALUE;
					if (bgImage != null) {
						for (int x = 0; x < bgImage.length; x++) {
							for (int y = 0; y < bgImage[0].length; y++) {
								int value = bgImage[x][y];
								if (maxValue < value) {
									maxValue = value;
								}
								value = (int) map(value, 0, backgroundImageMaxValue, 0, 255);
								value = Math.max(value, 0);
								value = Math.min(value, backgroundImageMaxValue);
								int index = x + y * bgImage.length;
								backgroundImage.pixels[index] = 0xff000000 | (value << 16) | (value << 8) | value; // grayscale mapping of a value from 0-255
								// backgroundImage.pixels[index] = value; // value represents a color and can be directly mapped

							}
						}
					}
					if (maxValue > backgroundImageMaxValue) {
						backgroundImageMaxValue = maxValue;
					}
					backgroundImage.updatePixels();

				}

			}

			// Restrict size
			while (matrices.size() > maxBufferSize) {
				// Remove last item if too full
				matrices.remove(matrices.size() - 1);
			}
		}

		PImage getForegroundImageFromCurrentMatrix(int[] matrix, int matrixWidth, int matrixHeight, int threshold) {
			int[][] curMatrix = convertOneDimensionalToTwoDimensionalArray(matrix, matrixWidth, matrixHeight);
			return getForegroundImageFromCurrentMatrix(curMatrix, threshold);
		}

		PImage getForegroundImageFromCurrentMatrix(int[][] curMatrix, int threshold) {
			int[][] bgImage = backgroundModel_medianValueMatrix;
			if (bgImage != null) {
				foregroundImage.loadPixels();
				for (int x = 0; x < bgImage.length; x++) {
					for (int y = 0; y < bgImage[0].length; y++) {
						int bgValue = bgImage[x][y];
						int curValue = curMatrix[x][y];
						int index = x + y * bgImage.length;
						if (abs(curValue - bgValue) > threshold) {
							foregroundImage.pixels[index] = 0xff000000 | (255 << 16) | (255 << 8) | 255; // <- here we choose to just make it black and white
						} else {
							foregroundImage.pixels[index] = 0;
						}
					}
				}
				foregroundImage.updatePixels();
			}
			return foregroundImage;
		}

		int[][] convertOneDimensionalToTwoDimensionalArray(int[] matrix, int matrixWidth, int matrixHeight) {
			int[][] newMatrix = new int[matrixWidth][matrixHeight];
			for (int x = 0; x < matrixWidth; x++) {
				for (int y = 0; y < matrixHeight; y++) {
					int index = x + y * matrixWidth;
					newMatrix[x][y] = matrix[index];
				}
			}
			return newMatrix;
		}

		float getBufferStorageFrequency() {
			return bufferDuration / maxBufferSize;
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
	int[][] medianValuesOfIntMatrices(BufferOfDepthMatrices buffer, boolean halveEvenMidPoints, boolean ignoresZeroValues) {
		int numMatrices = buffer.matrices.size();
		if (buffer.matrices.size() == 0)
			return null;

		int width = buffer.matrices.get(0).length;
		int height = buffer.matrices.get(0)[0].length;

		int[][] returnMatrix = new int[width][height];

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
				median = numbers[numbers.length / 2];
				if (halveEvenMidPoints) {
					if (numbers.length % 2 == 0) {
						median = (numbers[numbers.length / 2] + numbers[numbers.length / 2 - 1]) / 2;
					}
				}

				returnMatrix[x][y] = (int) median;
			}
		}

		return returnMatrix;
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
