package filter;
//import processing.core.PImage;
import processing.core.*;
import util.OscHandler;

public class Lines extends Filter {
	PApplet parent;
	
	public Lines(PApplet p) {
		parent = p;
	};

	public PImage drawLines(PImage inputImg) {
		
		PGraphics pg = parent.createGraphics(inputImg.width, inputImg.height);
		int width = inputImg.width;
		int height = inputImg.height;
		
		int lineDist = 20;
		
		pg.beginDraw();
			pg.background(255);
			pg.fill(0);
			pg.noStroke();
		    for(int i = 0; i<width; i+=lineDist){
		      pg.rect(i, 0, 10, height);
		    }
		pg.endDraw();

		PImage outputImg = parent.createImage(width, height, parent.RGB);
		//Loop through all pixels in input image
		
		/*for(int x = 0; x<width; x++){
			for(int y=0; y<height; y++){
				int index = x + y * width;
				
				//If white, then outputImg pixel is pg pixel, otherwise it's black
				if(inputImg.pixels[index]<-1){
					outputImg.pixels[index] = 0;
				}else{
					outputImg.pixels[index] = pg.pixels[index];
				}
			}
		}*/
		pg.mask(inputImg);
		return pg;
	}
}
