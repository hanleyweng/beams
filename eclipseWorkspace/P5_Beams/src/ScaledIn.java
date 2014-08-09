import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Scale In Effect
 * 
 * @author hanleyweng
 * 
 */
public class ScaledIn extends Filter {

	PImage img;

	ArrayList<PImage> prvFrames = new ArrayList<PImage>();
	int maxFrames;

	ScaledIn() {
	};

	PImage getFilteredImage(PApplet p, PImage srcImg) {

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
