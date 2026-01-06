import java.util.ArrayList;
import java.util.List;

public class ImageBlockSplitter {
    public static List<double[][]> splitImage(double[][] image, int blockSize) {
        List<double[][]> blocks = new ArrayList<>();
        int rows = image.length;
        int cols = image[0].length;
        for (int i = 0; i < rows; i += blockSize) {
            for (int j = 0; j < cols; j += blockSize) {
                int endRow = Math.min(i + blockSize, rows);
                int endCol = Math.min(j + blockSize, cols);
                double[][] block = new double[endRow - i][endCol - j];
                for (int x = i; x < endRow; x++) {
                    System.arraycopy(image[x], j, block[x - i], 0, endCol - j);
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    public static double[][] mergeBlocks(List<double[][]> blocks, int totalRows, int totalCols) {
        double[][] merged = new double[totalRows][totalCols];
        int blockSize = blocks.get(0).length;
        int idx = 0;
        for (int i = 0; i < totalRows; i += blockSize) {
            for (int j = 0; j < totalCols; j += blockSize) {
                double[][] block = blocks.get(idx++);
                int endRow = Math.min(i + blockSize, totalRows);
                int endCol = Math.min(j + blockSize, totalCols);
                for (int x = i; x < endRow; x++) {
                    System.arraycopy(block[x - i], 0, merged[x], j, endCol - j);
                }
            }
        }
        return merged;
    }
}