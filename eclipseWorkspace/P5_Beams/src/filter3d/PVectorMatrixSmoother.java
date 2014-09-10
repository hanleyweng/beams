package filter3d;

import java.util.ArrayList;

import processing.core.PVector;

/**
 * A filter ideal for efficiently smoothing out the noisy kinect depth-realWorldMap.
 * 
 * This object should be used live. It takes in a matrix every time it is updated.
 * 
 * @author hanleyweng
 * 
 */
public class PVectorMatrixSmoother {

	int maxMatrices;

	ArrayList<PVector[]> matrices;

	PVector[] smootherMatrix;

	float matrixMaxZValue;

	int matrixWidth, matrixHeight;

	public PVectorMatrixSmoother(int matrixWidth, int matrixHeight) {
		this.matrixWidth = matrixWidth;
		this.matrixHeight = matrixHeight;
		maxMatrices = 1;
		matrices = new ArrayList<PVector[]>();
		matrixMaxZValue = Integer.MIN_VALUE;

		smootherMatrix = new PVector[matrixWidth * matrixHeight];
	}

	public void updateStream(PVector[] pvectorDepthMatrix) {

		// Add Matrix
		matrices.add(0, pvectorDepthMatrix);

		// For every pixel. Store that pixel to smootherMatrix if it isn't zero. If it is zero, try the next matrix.
		for (int i = 0; i < pvectorDepthMatrix.length; i++) {
			for (int m = 0; m < matrices.size(); m++) {
				PVector[] curMatrix = matrices.get(m);
				PVector curPoint = curMatrix[i];
				float value = curPoint.z;
				if (value > matrixMaxZValue) {
					matrixMaxZValue = value;
				}
				if (value != 0) {
					smootherMatrix[i] = curPoint.get();
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

	public PVector[] getSmootherMatrix() {
		return smootherMatrix;
	}

	public float getMatrixMaxZValue() {
		return matrixMaxZValue;
	}

}