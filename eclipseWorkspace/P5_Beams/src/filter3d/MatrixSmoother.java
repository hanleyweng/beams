package filter3d;

import java.util.ArrayList;

/**
 * A filter ideal for efficiently smoothing out the noisy kinect depthMap.
 * 
 * This object should be used live. It takes in a matrix every time it is updated.
 * 
 * @author hanleyweng
 * 
 */
public class MatrixSmoother {

	int maxMatrices;

	ArrayList<int[]> matrices;

	int[] smootherMatrix;

	int matrixMaxValue;

	int matrixWidth, matrixHeight;

	public MatrixSmoother(int matrixWidth, int matrixHeight) {
		this.matrixWidth = matrixWidth;
		this.matrixHeight = matrixHeight;
		maxMatrices = 1;
		matrices = new ArrayList<int[]>();
		matrixMaxValue = Integer.MIN_VALUE;

		smootherMatrix = new int[matrixWidth * matrixHeight];
	}

	public void updateStream(int[] matrix) {

		// Add Matrix
		matrices.add(0, matrix.clone());

		// For every pixel. Store that pixel to smootherMatrix if it isn't zero. If it is zero, try the next matrix.
		for (int i = 0; i < matrix.length; i++) {
			for (int m = 0; m < matrices.size(); m++) {
				int[] curMatrix = matrices.get(m);
				int value = curMatrix[i];
				if (value > matrixMaxValue) {
					matrixMaxValue = value;
				}
				if (value != 0) {
					smootherMatrix[i] = value;
					break;
				}
				// since we don't have a clause here for what to do when all pixels are zero; it will continue to use it's historic values as opposed to reverting back to zero.
			}
		}

		// Restrict size
		while (matrices.size() > maxMatrices) {
			// Remove last item if too full
			matrices.remove(matrices.size() - 1);
		}

	}

	public int[] getSmootherMatrix() {
		return smootherMatrix;
	}

	public int getMatrixMaxValue() {
		return matrixMaxValue;
	}

}