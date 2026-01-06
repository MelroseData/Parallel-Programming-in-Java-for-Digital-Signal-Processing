public class LowPassImageFilter implements ImageFilter {
    private final double cutoff;

    public LowPassImageFilter(double cutoff) {
        this.cutoff = cutoff;
    }

    @Override
    public double[][] filter(double[][] imageBlock) {
        int rows = imageBlock.length;
        int cols = imageBlock[0].length;
        double[][] filtered = new double[rows][cols];
        // 简单的低通滤波逻辑（示例）
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i > 0 && j > 0 && i < rows - 1 && j < cols - 1) {
                    filtered[i][j] = (imageBlock[i-1][j] + imageBlock[i][j-1] +
                            imageBlock[i][j] + imageBlock[i][j+1] +
                            imageBlock[i+1][j]) / 5.0;
                } else {
                    filtered[i][j] = imageBlock[i][j];
                }
            }
        }
        return filtered;
    }
}