import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelImageProcessor {
    private final ImageFilter filter;
    private final int numThreads;

    public ParallelImageProcessor(ImageFilter filter, int numThreads) {
        this.filter = filter;
        this.numThreads = numThreads;
    }

    public double[][] process(double[][] image) throws InterruptedException, ExecutionException {
        // 分块
        List<double[][]> blocks = ImageBlockSplitter.splitImage(image, 100); // 块大小为 100x100
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<double[][]>> futures = new ArrayList<>();

        // 提交任务
        for (double[][] block : blocks) {
            Callable<double[][]> task = () -> filter.filter(block);
            futures.add(executor.submit(task));
        }

        // 收集结果
        List<double[][]> processedBlocks = new ArrayList<>();
        for (Future<double[][]> future : futures) {
            processedBlocks.add(future.get());
        }

        executor.shutdown();

        // 合并块
        return ImageBlockSplitter.mergeBlocks(processedBlocks, image.length, image[0].length);
    }
}