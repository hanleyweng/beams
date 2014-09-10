package filter3d;

import java.util.ArrayList;

public class SlitScan3d {
	int maxMatrices;
	ArrayList<int[]> prvMatrices;
	public int[] outputMatrix;

	int matrixWidth, matrixHeight;
	int heightOfRow; // in realWorld

	public SlitScan3d(int matrixWidth, int matrixHeight) {
		this.matrixWidth = matrixWidth;
		this.matrixHeight = matrixHeight;
		prvMatrices = new ArrayList<int[]>();

		// heightOfRow = 30;
		// maxMatrices = 16;

		outputMatrix = new int[matrixWidth * matrixHeight];
	}

	public void updateStream(int[] matrix, int newHeightOfRow) {

		// Copy matrix

		// Store matrix
		prvMatrices.add(0, matrix.clone());

		// Get Max Matrices
		// int newHeightOfRow = OscHandler.heightOfRow; //<- perhaps we should put this in the main program rather than the class
		// if we have a newHeightOfRow,
		// set & clear prvFrames, otherwise array out of bounds
		if (newHeightOfRow != heightOfRow) {
			prvMatrices.clear();
			heightOfRow = newHeightOfRow;
		}
		maxMatrices = matrixHeight / heightOfRow;

		// Restrict size
		while (prvMatrices.size() > maxMatrices) {
			// Remove last item if too full
			prvMatrices.remove(prvMatrices.size() - 1);
		}
	}

	public int[] getFilteredMatrix() {
		// Here we can do two things - either slitScan by 2d-y-value, or by 3d-y-value
		// Here we're just doing simple 2d-y-alue slitScan

		// // Get every matrix
		for (int i = 0; i < prvMatrices.size(); i++) {
			int[] curMatrix = prvMatrices.get(i);

			// Determine where to start encoding pixels
			int startingY = i * heightOfRow;

			// Encode for each row of pixels
			for (int x = 0; x < matrixWidth; x++) {
				for (int y = startingY; y < startingY + heightOfRow; y++) {
					int index = x + y * matrixWidth;
					outputMatrix[index] = curMatrix[index];
				}
			}
		}
		return outputMatrix;
	}
}
