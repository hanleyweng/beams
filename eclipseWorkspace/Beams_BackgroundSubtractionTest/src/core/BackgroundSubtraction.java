package core;
import processing.core.*; // This is from Processing2.2.1
import processing.video.*;
import util.OscHandler;
import SimpleOpenNI.*;
import codeanticode.syphon.*;
import filter.DepthThresholder;
import filter.RemoveRedFilter;
import filter.ScaledIn;
import filter.SlitScan;
import filter.ZaxisContours;
import filter.ZaxisSlitScan;

// TODO: Add Analysis'

@SuppressWarnings("serial")
public class BackgroundSubtraction extends PApplet {

	// OPTIONS!
	String INPUT_MODE = INPUT_MODE_MOVIE;
	
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
		

	}

}
