import oscP5.*;

@SuppressWarnings("unused")
public class OscHandler {

	OscP5 oscP5;
	
	/*
	 * Step 1: Assign a static field to be utilised elsewhere in the application. 
	 */
	public static int testInt1 = 7;
	public static int heightOfRow = 16;
	
	OscHandler() {
		oscP5 = new OscP5(this, 12345);
		registerMessages();
	}
	
	/**
	 * Step 2: Register a method callback here which handles routing the received osc message:
	 */
	private void registerMessages() {
		oscP5.plug(this, "testInt1", "/testInt1");
		oscP5.plug(this, "heightOfRow", "/heightOfRow");
	}
	
	/**
	 * Step 3: assign the received osc value to your static field!
	 * @param msg the received osc value
	 */
	private void testInt1(int val) {
		testInt1 = val;
		System.out.println(testInt1);
	}
	private void heightOfRow(int val) {
		heightOfRow = val;
	}
	
	
}
