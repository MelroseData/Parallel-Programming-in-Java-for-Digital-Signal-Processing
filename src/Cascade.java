import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
public class Cascade {
    public double[] applyCascade(double[] signal, double lowCutoff, double highCutoff, Filter filter) {
        // Apply Low-Pass Filter first
        double[] lowPassResult = filter.lowPassFilter(signal, lowCutoff);

        // Then apply High-Pass Filter to the result of Low-Pass
        double[] highPassResult = filter.highPassFilter(lowPassResult, highCutoff);

        // Finally apply Band-Pass Filter
        return filter.bandPassFilter(highPassResult, lowCutoff, highCutoff);
    }

    // Threaded Cascade (same as sequential but using threads for each filter)
    public double[] applyCascadeWithThreads(double[] signal, double lowCutoff, double highCutoff, Filter filter) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(3);  // 3 filters to apply
        List<Callable<double[]>> tasks = new ArrayList<>();

        tasks.add(() -> filter.lowPassFilter(signal, lowCutoff));
        tasks.add(() -> filter.highPassFilter(signal, highCutoff));
        tasks.add(() -> filter.bandPassFilter(signal, lowCutoff, highCutoff));

        List<Future<double[]>> results = executor.invokeAll(tasks);

        // Wait for all tasks to finish and return the final result (cascade application)
        executor.shutdown();
        double[] finalResult = signal;
        for (Future<double[]> result : results) {
            finalResult = result.get(); // Apply the result of each filter
        }

        return finalResult;
    }
}
