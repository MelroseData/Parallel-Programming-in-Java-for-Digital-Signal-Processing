import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
public class Series {
    public double[] applySeries(double[] signal, double lowCutoff, double highCutoff, Filter filter) {
        double[] lowPassResult = filter.lowPassFilter(signal, lowCutoff);
        double[] highPassResult = filter.highPassFilter(signal, highCutoff);
        double[] bandPassResult = filter.bandPassFilter(signal, lowCutoff, highCutoff);

        // Combine the results if needed or return one of them as the final signal
        return bandPassResult; // Assuming band-pass filtering is the primary output
    }
    public double[] applySeriesWithThreads(double[] signal, double lowCutoff, double highCutoff, Filter filter) throws InterruptedException, ExecutionException {
        // Create a thread pool with 3 threads for applying each filter
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // List to hold tasks (each filter will be applied in a separate task)
        List<Callable<double[]>> tasks = new ArrayList<>();

        // Add tasks for each filter
        tasks.add(() -> filter.lowPassFilter(signal, lowCutoff));   // Low-pass filter
        tasks.add(() -> filter.highPassFilter(signal, highCutoff)); // High-pass filter
        tasks.add(() -> filter.bandPassFilter(signal, lowCutoff, highCutoff)); // Band-pass filter

        // Execute all tasks in parallel
        List<Future<double[]>> results = executor.invokeAll(tasks);

        // Shut down the executor
        executor.shutdown();

        // Create a 1D array to store the combined results
        double[] finalResult = new double[3 * signal.length];  // 3 filters, each with the same length as the signal

        // Iterate over the results and store them in the 1D array
        int index = 0;
        for (Future<double[]> result : results) {
            double[] res = result.get();  // Get the filtered signal from the Future
            System.arraycopy(res, 0, finalResult, index * signal.length, res.length);  // Copy the filtered signal to the 1D array
            index++;
        }

        // Return the combined 1D array containing all three filter results
        return finalResult;
    }

//    public double[] applySeries(double[] signal, double lowCutoff, double highCutoff, Filter filter) {
//        // Apply all filters at once
//        double[] lowPassResult = filter.lowPassFilter(signal, lowCutoff);
//        double[] highPassResult = filter.highPassFilter(signal, highCutoff);
//        double[] bandPassResult = filter.bandPassFilter(signal, lowCutoff, highCutoff);
//
//        // Combine results (return them separately for simplicity)
//        return new double[] { lowPassResult[0], highPassResult[0], bandPassResult[0] };  // Example result
//    }

    // Threaded Series (apply all filters simultaneously using threads)
//    public double[][] applySeriesWithThreads(double[] signal, double lowCutoff, double highCutoff, Filter filter) throws InterruptedException, ExecutionException {
//        ExecutorService executor = Executors.newFixedThreadPool(3);  // 3 filters to apply
//        List<Callable<double[]>> tasks = new ArrayList<>();
//
//        tasks.add(() -> filter.lowPassFilter(signal, lowCutoff));
//        tasks.add(() -> filter.highPassFilter(signal, highCutoff));
//        tasks.add(() -> filter.bandPassFilter(signal, lowCutoff, highCutoff));
//
//        List<Future<double[]>> results = executor.invokeAll(tasks);
//
//        // Wait for all tasks to finish and collect the results
//        executor.shutdown();
//
//        // Allocate separate arrays for each filter result
//        double[][] finalResult = new double[3][signal.length]; // Three filters, each result should have the same length as the signal
//        int index = 0;
//        for (Future<double[]> result : results) {
//            double[] res = result.get();
//            System.arraycopy(res, 0, finalResult[index], 0, res.length);
//            index++;
//        }
//
//        return finalResult;  // Return an array of filter results
//    }
}
