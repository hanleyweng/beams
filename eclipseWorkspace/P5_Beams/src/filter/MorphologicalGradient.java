package filter;
import gab.opencv.OpenCV;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import processing.core.PImage;
import util.OscHandler;

public class MorphologicalGradient extends Filter {

	public static final int GRADIENT = 4;
	
	OpenCV opencv;
	
	public MorphologicalGradient(OpenCV opencv) {
		this.opencv = opencv;
	};

	public PImage getFilteredImage(PImage _depthImg) {

		PImage depthImg = this.copyImage(_depthImg);
		PImage outputImg = depthImg; // just so it references a PImage, to keep opencv.toPImage(m,outputImg) happy.
		
		opencv.loadImage(depthImg);
//		opencv.invert();
	    Mat outputMat = new Mat();
	    // from opencv tutorial http://docs.opencv.org/doc/tutorials/imgproc/opening_closing_hats/opening_closing_hats.html#morphology-2
//	    Mat element = getStructuringElement( morph_elem, Size( 2*morph_size + 1, 2*morph_size+1 ), Point( morph_size, morph_size ) );
	    // Element: 0: Rect - 1: Cross - 2: Ellipse
//	    Mat structuringElement = Imgproc.getStructuringElement(2, new Size(2*ksize+1, 2*ksize+1), new Point(ksize, ksize)); //Anchor position within the element. The default value (-1, -1) means that the anchor is at the center.
	    Mat structuringElement = Imgproc.getStructuringElement(1, new Size(OscHandler.testInt1, OscHandler.testInt1));
	    
	    /// Apply the specified morphology operation
	    Imgproc.morphologyEx(opencv.matGray, outputMat, 2, structuringElement);
	    opencv.toPImage(outputMat,outputImg);
	    
		return outputImg;
	}
}
