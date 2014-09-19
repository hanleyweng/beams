package filter3d;

import java.awt.Color;

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

	public int[] getContourMatrix_linearGradient(int[] depthMatrix, float depthLoopOffset, int frameCount) {
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

			if (depthValue < thresholdDepthLevel) {

				float hueProgress = map(depthValue, 0, thresholdDepthLevel, 0, 1);
				// manipulate hueProgress by a hueWeight
				hueProgress = (float) Math.pow(hueProgress, hueProgressMultiplier);

				// make it red-to-purple
				float hueRed = 1.0f;
				float huePurple = 0.8f;

				hue = map(hueProgress, 0, 1, hueRed, huePurple);
			} else {
				// make it blue
				hue = 0.3f;
			}

			// Contours
			// int depthLoopRange = 60;
			// float progress = ((depthValue + depthLoopOffset) % depthLoopRange) / depthLoopRange;
			// float progressSin = (float) Math.sin(progress * 2 * Math.PI);
			// float bri = map(progressSin, -1, 1, 0, 1);

			color = Color.HSBtoRGB(hue, 1.0f, 1.0f);
			outputMatrix[i] = color;
		}

		return outputMatrix;
	}

	static public final float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}
}