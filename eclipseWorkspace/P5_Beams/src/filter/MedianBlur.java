package filter;
import gab.opencv.OpenCV;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import core.Beams;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import util.OscHandler;

public class MedianBlur extends Filter {

	OpenCV opencv;
	
	public MedianBlur(OpenCV opencv) {
		this.opencv = opencv;
	};

	public PImage getFilteredImage(PImage _depthImg) {

		PImage depthImg = this.copyImage(_depthImg);
		PImage outputImg = depthImg; // just so it references a PImage, to keep opencv.toPImage(m,outputImg) happy.
		
		opencv.loadImage(depthImg);
	    Mat m = new Mat();
	    // opencv.matBGRA for color. Much slower.
	    Imgproc.medianBlur(opencv.matGray, m, 21); // 3rd param is blur passes: must be an odd number.
	    opencv.toPImage(m,outputImg);
	    
		return outputImg;
	}
}
