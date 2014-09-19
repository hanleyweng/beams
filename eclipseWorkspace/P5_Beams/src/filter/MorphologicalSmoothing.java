package filter;
import gab.opencv.OpenCV;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import processing.core.PImage;
import util.OscHandler;

public class MorphologicalSmoothing extends Filter {
	
	OpenCV opencv;
	
	public MorphologicalSmoothing(OpenCV opencv) {
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
//	    Mat structuringElement = Imgproc.getStructuringElement(0, new Size(2*ksize+1, 2*ksize+1), new Point(ksize, ksize)); //Anchor position within the element. The default value (-1, -1) means that the anchor is at the center.
	    //Mat structuringElement = Imgproc.getStructuringElement(1, new Size(OscHandler.testInt1, OscHandler.testInt1));
	    int erosion_size = OscHandler.erosionValue;
        int dilation_size = OscHandler.dilationValue;
        int opening_size = OscHandler.openingValue;
        int closing_size = OscHandler.closingValue;
        		
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(erosion_size + 1, erosion_size+1));
        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilation_size + 1, dilation_size+1));
        Mat openElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(opening_size + 1, opening_size+1));
        Mat closeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(closing_size + 1, closing_size+1));
        		
        
	    /// Apply the specified morphology operation
	    //Imgproc.morphologyEx(opencv.matGray, outputMat, 2);
	    Imgproc.erode(opencv.matGray, outputMat, erodeElement);
	    Mat outputMat2 = new Mat();
//	    Imgproc.morphologyEx(outputMat, outputMat2, Imgproc.MORPH_OPEN, element);
	    Imgproc.morphologyEx(outputMat, outputMat2, Imgproc.MORPH_OPEN, openElement);
	    Imgproc.dilate(outputMat2, outputMat, dilateElement);
	    Imgproc.morphologyEx(outputMat, outputMat2, Imgproc.MORPH_CLOSE, closeElement);
	    
	    opencv.toPImage(outputMat2, outputImg);
	    
		return outputImg;
	}
}
