package filter3d;

import java.awt.Color;

import util.OscHandler;

public class ContourMatrixFromDepth {

	public ContourMatrixFromDepth() {
	}

	// Rainbow version
	public int[] getContourMatrix_rainbowVersion(int[] depthMatrix, float depthLoopOffset) {
		int[] outputMatrix = new int[depthMatrix.length];

		for (int i = 0; i < depthMatrix.length; i++) {

			float depthValue = depthMatrix[i];

			int depthLoopRange = 300;
			float hue = map((depthValue + depthLoopOffset) % depthLoopRange, 0, depthLoopRange, 0, 1.0f);
			int color = Color.HSBtoRGB(hue, 0.9f, 0.9f);
			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	public int[] getContourMatrix_lineVersion(int[] depthMatrix, float depthLoopOffset) {
		int[] outputMatrix = new int[depthMatrix.length];

		for (int i = 0; i < depthMatrix.length; i++) {

			float depthValue = depthMatrix[i];

			int depthLoopRange = 30;
			// float hue = map((depthValue + depthLoopOffset) % depthLoopRange, 0, depthLoopRange, 0, 1.0f);
			float progress = ((depthValue + depthLoopOffset) % depthLoopRange) / depthLoopRange;
			float progressSin = (float) Math.sin(progress * 2 * Math.PI);
			float bri = map(progressSin, -1, 1, 0, 1);
			int color = Color.HSBtoRGB(0, 1.0f, bri);
			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	public int[] getContourMatrix_linearGradient(int[] depthMatrix, float depthLoopOffset) {
		int[] outputMatrix = new int[depthMatrix.length];

		for (int i = 0; i < depthMatrix.length; i++) {

			float depthValue = depthMatrix[i];

			float hue = 0;
			int color = 0;

			int thresholdDepthLevel = OscHandler.testInt2;

			if (depthValue < thresholdDepthLevel) {
				// make it red-to-purple
				float hueRed = 1.0f;
				float huePurple = 0.8f;

				hue = map(depthValue, 0, thresholdDepthLevel, OscHandler.testFloat1, OscHandler.testFloat2);
			} else {
				// this is the background color
				// cobalt blue: 0.726619
				float cobalt = 0.726619f;
				hue = OscHandler.testFloat3;
			}

			int depthLoopRange = 60;
			float progress = ((depthValue + depthLoopOffset) % depthLoopRange) / depthLoopRange;
			float progressSin = (float) Math.sin(progress * OscHandler.testInt1 * Math.PI);
			float bri = map(progressSin, -1, 1, 0, 1);
			color = Color.HSBtoRGB(hue, 1.0f, bri);

			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	public int[] getContourMatrix_linearGradient1b(int[] depthMatrix, float depthLoopOffset, int frameCount) {
		int[] outputMatrix = new int[depthMatrix.length];

		// Determine Hue Weighting
		int hueWeightDuration_fc = 100;
		float hueWeightProgress_01 = map(frameCount % hueWeightDuration_fc, 0, hueWeightDuration_fc, 0, 1);
		float hueWeightProgress_looping_01 = (float) ((Math.cos(hueWeightProgress_01 * 2 * Math.PI) + 1) / 2);
		float hueProgressMultiplier = map(hueWeightProgress_looping_01, 0, 1, 0.5f, 2.0f);

		for (int i = 0; i < depthMatrix.length; i++) {

			float depthValue = depthMatrix[i];

			float hue = 0;
			int color = 0;

			int thresholdDepthLevel = 5000;

			if (depthValue < OscHandler.testInt2) {

				float hueProgress = map(depthValue, 0, thresholdDepthLevel, 0, 1);
				// manipulate hueProgress by a hueWeight
				hueProgress = (float) Math.pow(hueProgress, hueProgressMultiplier);

				// make it red-to-purple
				float hueRed = 1.0f;
				float huePurple = 0.8f;

				hue = map(hueProgress, 0, 1, OscHandler.testFloat1, OscHandler.testFloat2);
			} else {
				// this is the background color
				float cobalt = 0.726619f;
				hue = OscHandler.testFloat3;

			}

			// Contours
			int depthLoopRange = 60;
			float progress = ((depthValue + depthLoopOffset) % depthLoopRange) / depthLoopRange;
			float progressSin = (float) Math.sin(progress * OscHandler.testInt1 * Math.PI);
			float bri = map(progressSin, -1, 1, 0, 1);

			color = Color.HSBtoRGB(hue, 1.0f, bri);
			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	public int[] getContourMatrix_linearGradient2(int[] depthMatrix, float depthLoopOffset, int frameCount) {
		int[] outputMatrix = new int[depthMatrix.length];

		int[] thresholdDepthRange = { 0, OscHandler.testInt2 };

		// Determine range of hues
		int c1c2DepthRangeLength = 300;
		int c1c2DepthRangeCentre_min = thresholdDepthRange[0] + c1c2DepthRangeLength / 2;
		int c1c2DepthRangeCentre_max = thresholdDepthRange[1] - c1c2DepthRangeLength / 2;

		// Determine c1c2DepthRangeCentreProgress
		float c1c2DepthRangeCentreProgress_duration = 600; // <- loop duration
		float c1c2DepthRangeCentreProgress_progress01 = map(frameCount % c1c2DepthRangeCentreProgress_duration, 0, c1c2DepthRangeCentreProgress_duration, 0, 1);
		float c1c2DepthRangeCentreProgress_loop01 = (float) ((Math.cos(c1c2DepthRangeCentreProgress_progress01 * 2 * Math.PI) + 1) / 2);

		float c1c2DepthRangeCentre_cur = map(c1c2DepthRangeCentreProgress_loop01, 0, 1, c1c2DepthRangeCentre_min, c1c2DepthRangeCentre_max);
		float c1c2DepthRangeLbound = c1c2DepthRangeCentre_cur - c1c2DepthRangeLength / 2;
		float c1c2DepthRangeRbound = c1c2DepthRangeCentre_cur + c1c2DepthRangeLength / 2;

		// Hues - red and purple
		float c1hue = 1.0f;
		float c2hue = 0.75f;

		// Vars
		float hue = 0;
		int color = 0;
		float bri = 1;

		for (int i = 0; i < depthMatrix.length; i++) {

			float depthValue = depthMatrix[i];

			if (depthValue < thresholdDepthRange[1]) {

				// Set hue
				hue = map(depthValue, c1c2DepthRangeLbound, c1c2DepthRangeRbound, c1hue, c2hue);
				float maxHue = Math.max(c1hue, c2hue);
				float minHue = Math.min(c1hue, c2hue);
				if (hue > maxHue)
					hue = maxHue;
				if (hue < minHue)
					hue = minHue;

			} else {
				// make it blue
				hue = OscHandler.testFloat3;
			}

			// Contours
			// int depthLoopRange = 60;
			// float progress = ((depthValue + depthLoopOffset) % depthLoopRange) / depthLoopRange;
			// float progressSin = (float) Math.sin(progress * 2 * Math.PI);
			// bri = map(progressSin, -1, 1, 0, 1);

			color = Color.HSBtoRGB(hue, 1.0f, bri);
			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	static public final float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}
}