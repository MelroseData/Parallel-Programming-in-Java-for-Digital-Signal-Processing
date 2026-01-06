import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ImageDenoiser {
    public static double[][] loadImage(String path) throws IOException {
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

    public static void saveImage(double[][] pixels, String outputPath) throws IOException {
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // 输入输出路径
        String inputDir = "F:\\OCR\\grade_3\\processed_images";
        String outputDir = "F:\\OCR\\grade_3\\denoised_images";

        // 初始化滤波器和并行处理器
        ImageFilter filter = new LowPassImageFilter(0.5);
        ParallelImageProcessor processor = new ParallelImageProcessor(filter, 8); // 使用 8 线程

        // 处理所有图片
        File dir = new File(inputDir);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                double[][] image = loadImage(file.getPath());
                double[][] denoised = processor.process(image);
                String outputPath = outputDir + File.separator + "denoised_" + file.getName();
                saveImage(denoised, outputPath);
                System.out.println("Processed: " + outputPath);
            }
        }
    }
}