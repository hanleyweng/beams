package util;
import oscP5.*;

@SuppressWarnings("unused")
public class OscHandler {

	OscP5 oscP5;
	
	/*
	 * Step 1: Assign a static field to be utilised elsewhere in the application. 
	 */
	public static int testInt1 = 7;
	public static int heightOfRow = 16;
	public static int depthThreshold = 30;
	public static int zaxisSlice = 7;
	public static int zaxisThreshold = 12;
	
	public OscHandler() {
		oscP5 = new OscP5(this, 12345);
		registerMessages();
	}
	
	/**
	 * Step 2: Register a method callback here which handles routing the received osc message:
	 */
	private void registerMessages() {
		oscP5.plug(this, "testInt1", "/testInt1");
		oscP5.plug(this, "heightOfRow", "/heightOfRow");
		oscP5.plug(this, "depthThreshold", "/depthThreshold");
		oscP5.plug(this, "zaxisSlice", "/zaxisSlice");
		oscP5.plug(this, "zaxisThreshold", "/zaxisThreshold");
	}
	
	/**
	 * Step 3: assign the received osc value to your static field!
	 * @param val the received osc value
	 */
	private void testInt1(int val) {
		testInt1 = val;
	}
	private void heightOfRow(int val) {
		heightOfRow = val;
	}
	private void depthThreshold(int val) {
		depthThreshold = val;
	}
	private void zaxisSlice(int val) {
		zaxisSlice = val;
	}
	private void zaxisThreshold(int val) {
		zaxisThreshold = val;
	}
	
	
}
