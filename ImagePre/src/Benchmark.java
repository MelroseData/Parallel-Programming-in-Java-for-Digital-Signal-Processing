import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Benchmark {
    public static void main(String[] args) throws ExecutionException, IOException, InterruptedException {
        // 输入输出路径
        String inputDir = "E:\\OCR\\grade_3_step2DataCleaning\\Mathematics\\SmoothProcessed";
        String outputDir = "E:\\OCR\\grade_3_step3OwnMethod\\Mathematics\\DenoiseSmoothProcessed";

        // 初始化滤波器和并行处理器
        ImageFilter filter = new LowPassImageFilter(0.5); // 低通滤波器
        ParallelImageProcessor processor = new ParallelImageProcessor(filter, 8); // 使用 8 线程

        // 读取图像
        File dir = new File(inputDir);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                double[][] image = loadImage(file.getPath());

                // 非线程版本的去噪
                long startTime = System.nanoTime();
                double[][] denoisedNonThreaded = filter.filter(image); // 直接调用滤波器
                long endTime = System.nanoTime();
                System.out.println("Non-threaded Execution Time: " + (endTime - startTime) + " ns");
                System.out.println("SNR for Non-threaded: " + calculateSNR(image, denoisedNonThreaded));
                System.out.println("MSE for Non-threaded: " + calculateMSE(image, denoisedNonThreaded));

                // 线程版本的去噪
                startTime = System.nanoTime();
                double[][] denoisedThreaded = processor.process(image); // 使用并行处理器
                endTime = System.nanoTime();
                System.out.println("Threaded Execution Time: " + (endTime - startTime) + " ns");
                System.out.println("SNR for Threaded: " + calculateSNR(image, denoisedThreaded));
                System.out.println("MSE for Threaded: " + calculateMSE(image, denoisedThreaded));

                // 保存去噪后的图像
                String outputPath = outputDir + File.separator + "denoised_" + file.getName();
                saveImage(denoisedThreaded, outputPath);
                System.out.println("Processed and saved: " + outputPath);
            }
        }
    }

    // 加载图像为二维数组
    private static double[][] loadImage(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        int width = img.getWidth();
        int height = img.getHeight();
        double[][] pixels = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // 提取灰度值
                pixels[y][x] = gray;
            }
        }
        return pixels;
    }

    // 保存二维数组为图像
    private static void saveImage(double[][] pixels, String outputPath) throws IOException {
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) Math.round(pixels[y][x]);
                int rgb = (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "png", new File(outputPath));
    }

    // 计算信噪比 (SNR)
    private static double calculateSNR(double[][] original, double[][] filtered) {
        double signalPower = 0;
        double noisePower = 0;
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                signalPower += Math.pow(original[i][j], 2);
                double noise = original[i][j] - filtered[i][j];
                noisePower += Math.pow(noise, 2);
            }
        }
        return 10 * Math.log10(signalPower / noisePower);
    }

    // 计算均方误差 (MSE)
    private static double calculateMSE(double[][] original, double[][] filtered) {
        double mse = 0;
        int totalPixels = original.length * original[0].length;
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                double error = original[i][j] - filtered[i][j];
                mse += Math.pow(error, 2);
            }
        }
        return mse / totalPixels;
    }
}

