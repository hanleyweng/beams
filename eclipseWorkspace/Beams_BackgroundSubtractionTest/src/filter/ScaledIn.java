package filter;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Scale In Effect - v1 - this method is actually a bit heavy since it also does
 * 
 * @author hanleyweng
 * 
 */
public class ScaledIn extends Filter {

	PImage img;

	ArrayList<PImage> prvFrames = new ArrayList<PImage>();
	int maxFrames;

	PImage prvResultImg;

	public ScaledIn() {
	};

	/**
	 * Faster approach that only scales down the previous stored image.
	 * 
	 * @param p
	 * @param srcImg
	 * @return
	 */
	PImage getFilteredImage(PApplet p, PImage srcImg) {
		int width = srcImg.width;
		int height = srcImg.height;
		PGraphics pg = p.createGraphics(width, height);

		pg.beginDraw();
		pg.image(srcImg, 0, 0);

		if (prvResultImg != null) {
			// Draw Previous Image on top, scaled down
			pg.translate(width / 2, height / 2);
			pg.scale(0.98f);
			pg.tint(255, 225);
			pg.image(prvResultImg, -width / 2, -height / 2);
		}
		pg.endDraw();

		// Store Previous Image
		prvResultImg = this.copyImage(pg);

		return pg;
	}

	/**
	 * A heavy approach that stored the temporal frames
	 * 
	 * @param p
	 * @param srcImg
	 * @return
	 */
	PImage getFilteredImage_oldMethod1(PApplet p, PImage srcImg) {

		float scaleInFactor = 0.90f;

		maxFrames = 20;

		img = this.copyImage(srcImg);

		int width = img.width;
		int height = img.height;

		// for every frame, overlay it on top
		// draw the newest frame on top

		// Note - this could be made more efficient by computing the result and overlaying - instead of storing multiple frames
		// Could also be made more efficient with pixel-based manipulation.

		PGraphics pg = p.createGraphics(width, height);
		pg.beginDraw();
		pg.pushMatrix();
		pg.translate(width / 2, height / 2);
		pg.image(srcImg, -width / 2, -height / 2);

		for (int i = prvFrames.size() - 1; i >= 0; i--) {
			PImage curFrame = prvFrames.get(i);
			pg.scale(scaleInFactor);
			pg.tint(255, 100);
			pg.image(curFrame, -width / 2, -height / 2);
		}
		pg.popMatrix();
		pg.endDraw();

		// Store srcImg in prvFrames
		prvFrames.add(this.copyImage(srcImg));

		// Clear prvFrames from memory
		if (prvFrames.size() > maxFrames) {
			prvFrames.remove(0);
		}

		return pg;
	}

}
