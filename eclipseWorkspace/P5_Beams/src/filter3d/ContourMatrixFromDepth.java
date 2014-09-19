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

	static public final float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}
}