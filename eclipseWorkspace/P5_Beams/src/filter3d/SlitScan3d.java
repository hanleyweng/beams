package filter3d;

import java.util.ArrayList;

import processing.core.PVector;

public class SlitScan3d {
	int maxMatrices;
	ArrayList<PVector[]> prvMatrices;
	public PVector[] outputMatrix;

	int matrixWidth, matrixHeight;
	int heightOfRow; // in realWorld

	public SlitScan3d(int matrixWidth, int matrixHeight) {
		this.matrixWidth = matrixWidth;
		this.matrixHeight = matrixHeight;
		prvMatrices = new ArrayList<PVector[]>();

		heightOfRow = 120; // ~
		maxMatrices = 4;

		outputMatrix = new PVector[matrixWidth * matrixHeight];
	}

	public void updateStream(PVector[] matrix, int newHeightOfRow) {

		// Copy matrix
		PVector[] newMatrix = new PVector[matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i] = matrix[i].get();
		}

		// Store matrix
		prvMatrices.add(0, newMatrix);

		// Get Max Matrices
		// int newHeightOfRow = OscHandler.heightOfRow; //<- perhaps we should put this in the main program rather than the class
		// if we have a newHeightOfRow,
		// set & clear prvFrames, otherwise array out of bounds
		// if (newHeightOfRow != heightOfRow) {
		// prvMatrices.clear();
		// heightOfRow = newHeightOfRow;
		// }
		// maxMatrices = matrixHeight / heightOfRow;

		// Restrict size
		while (prvMatrices.size() > maxMatrices) {
			// Remove last item if too full
			prvMatrices.remove(prvMatrices.size() - 1);
		}
	}

	public PVector[] getFilteredPMatrix() {
		// Here we can do two things - either slitScan by 2d-y-value, or by 3d-y-value
		// For now - lets use matrix-height, hence 2d-y-value

		// if (prvMatrices.size() > 0) {
		// outputMatrix = prvMatrices.get(prvMatrices.size() - 1);
		// System.out.println(prvMatrices.size());
		// }

		// outputMatrix = new PVector[matrixWidth * matrixHeight];

		// // Get every matrix
		for (int i = 0; i < prvMatrices.size(); i++) {
			PVector[] curMatrix = prvMatrices.get(i);

			// Determine where to start encoding pixels
			int startingY = i * heightOfRow;
			// System.out.println(heightOfRow);
			System.out.println(startingY);

			// Encode for each row of pixels
			for (int x = 0; x < matrixWidth; x++) {
				for (int y = startingY; y < startingY + heightOfRow; y++) {
					int index = x + y * matrixWidth;
					outputMatrix[index] = curMatrix[index].get();
				}
			}
		}
		return outputMatrix;
	}
}
