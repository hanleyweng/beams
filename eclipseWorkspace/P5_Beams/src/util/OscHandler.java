package util;
import oscP5.*;

@SuppressWarnings("unused")
public class OscHandler {

	OscP5 oscP5;
	
	/*
	 * Step 1: Assign a static field to be utilised elsewhere in the application. 
	 */
	public static int heightOfRow = 16;
	public static int depthThreshold = 30;
	public static int zaxisSlice = 7;
	public static int zaxisThreshold = 12;
	public static int posterizeLevel = 40;
	
	public static int testInt1 = 10;
	public static int testInt2 = 10;
	public static float testFloat1 = .5f;
	public static float testFloat2 = .5f;
	
	public OscHandler() {
		oscP5 = new OscP5(this, 12345);
		registerMessages();
	}
	
	/**
	 * Step 2: Register a method callback here which handles routing the received osc message:
	 */
	private void registerMessages() {
		oscP5.plug(this, "heightOfRow", "/heightOfRow");
		oscP5.plug(this, "depthThreshold", "/depthThreshold");
		oscP5.plug(this, "zaxisSlice", "/zaxisSlice");
		oscP5.plug(this, "zaxisThreshold", "/zaxisThreshold");
		oscP5.plug(this, "posterizeLevel", "/posterizeLevel");
		oscP5.plug(this, "testInt1", "/testInt1");
		oscP5.plug(this, "testInt2", "/testInt2");
		oscP5.plug(this, "testFloat1", "/testFloat1");
		oscP5.plug(this, "testFloat2", "/testFloat2");
	}
	
	/**
	 * Step 3: assign the received osc value to your static field!
	 * @param val the received osc value
	 */
	private void heightOfRow(int val) {
		heightOfRow = val;
	}
	private void depthThreshold(int val) {
		depthThreshold = val;
	}
	private void zaxisSlice(int val) {
		zaxisSlice = val;
	}
	private void posterizeLevel(int val) {
		posterizeLevel = val;
	}
	private void testInt1(int val) {
		testInt1 = val;
	}	
	private void testInt2(int val) {
		testInt2 = val;
	}	
	private void testFloat1(float val) {
		testFloat1 = val;
	}	
	private void testFloat2(float val) {
		testFloat2 = val;
	}	
	
}
