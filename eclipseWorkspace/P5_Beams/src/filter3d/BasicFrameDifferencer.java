package filter3d;

import java.util.ArrayList;

public class BasicFrameDifferencer {
	int maxMatrices;

	int[] outputMatrix;
	ArrayList<int[]> matrices;

	// maxMatrices informs the lag between currentFrame and lastFrame (which is sampled)
	public BasicFrameDifferencer(int maxMatrices, int matrixWidth, int matrixHeight) {
		this.maxMatrices = maxMatrices;
		matrices = new ArrayList<int[]>();
		outputMatrix = new int[matrixWidth * matrixHeight];
	}

	public void updateStream(int[] matrix, int threshold) {
		// Add Matrix
		matrices.add(0, matrix.clone());

		// Compare every pixel of curMatrix to prvMatrix - if difference above threshold then encode
		int[] lastMatrix = matrices.get(matrices.size() - 1);

		for (int i = 0; i < matrix.length; i++) {
			int curValue = matrix[i];
			int lastValue = lastMatrix[i];
			if (Math.abs(curValue - lastValue) > threshold) {
				outputMatrix[i] = curValue;
			} else {
				outputMatrix[i] = 0;
			}
		}

		// Restrict size
		while (matrices.size() > maxMatrices) {
			// Remove last item if too full
			matrices.remove(matrices.size() - 1);
		}
	}

	public int[] getOutputMatrix() {
		return outputMatrix;
	}
}
