import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LogProcessor {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        String logFilePath = "E:\\NetSecurity\\Apache_2k.log";  // Set the correct path

        // Read log data
        List<String> logs = readLogFile(logFilePath);

        // Stage 1: Quick Filtering - Removing duplicates and non-critical logs
        List<String> filteredLogs = stage1QuickFiltering(logs);

        // Stage 2: Feature Extraction - Extract metadata, timestamps, etc.
        List<LogEntry> logEntries = stage2FeatureExtraction(filteredLogs);

        // Stage 3: Cascade Optimization with Threading and Non-Threading Benchmark
        stage3CascadeBenchmark(logEntries);
    }


    // Read log file into a list of strings
    private static List<String> readLogFile(String filePath) throws IOException {
        List<String> logs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                logs.add(line);
            }
        }
        return logs;
    }

    // Stage 1: Quick Filtering (removing duplicates and non-critical logs)
    private static List<String> stage1QuickFiltering(List<String> logs) {
        Set<String> uniqueLogs = new HashSet<>(logs);  // Remove duplicates
        List<String> filteredLogs = new ArrayList<>();

        for (String log : uniqueLogs) {
            if (isCriticalLog(log)) {  // Only keep critical logs
                filteredLogs.add(log);
            }
        }
        System.out.println("Stage 1 - Filtered Logs: " + filteredLogs.size());
        return filteredLogs;
    }

    // Check if log is critical (example rule, modify as needed)
    private static boolean isCriticalLog(String log) {
        return log.contains("ERROR") || log.contains("CRITICAL");
    }

    // Stage 2: Feature Extraction (Extract metadata, timestamps, etc.)
    private static List<LogEntry> stage2FeatureExtraction(List<String> logs) {
        List<LogEntry> logEntries = new ArrayList<>();

        for (String log : logs) {
            LogEntry entry = extractFeatures(log);
            if (entry != null) {
                logEntries.add(entry);
            }
        }
        System.out.println("Stage 2 - Extracted Features: " + logEntries.size());
        return logEntries;
    }

    // Extract metadata from log (Modify as needed)
    private static LogEntry extractFeatures(String log) {
        String[] parts = log.split(" ", 3); // Assuming logs follow "TIMESTAMP LEVEL MESSAGE" format
        if (parts.length < 3) return null;
        return new LogEntry(parts[0], parts[1], parts[2]);
    }

    // Stage 3: Cascade Benchmarking (Threaded vs Non-threaded Execution Time)
    private static void stage3CascadeBenchmark(List<LogEntry> logEntries) throws InterruptedException, ExecutionException {
        // Benchmarking threaded cascade with Callable and Future
        long startTime = System.nanoTime();
        List<String> filteredSignalCascadeThreaded = processWithCascadeThreaded(logEntries);
        long endTime = System.nanoTime();
        System.out.println("Threaded Cascade Execution Time: " + (endTime - startTime) + " ns");
        double snrThreaded = calculateSNR(logEntries, filteredSignalCascadeThreaded);

        System.out.println("MSE for Threaded Cascade: " + calculateMSE(logEntries, filteredSignalCascadeThreaded));

        // Benchmarking non-threaded cascade
        startTime = System.nanoTime();
        List<String> filteredSignalSeries = processWithCascadeNonThreaded(logEntries);
        endTime = System.nanoTime();
        System.out.println("Non-threaded Series Execution Time: " + (endTime - startTime) + " ns");
        double snrSeries = calculateSNR(logEntries, filteredSignalSeries);

        System.out.println("MSE for Series: " + calculateMSE(logEntries, filteredSignalSeries));
    }


    // Threaded processing using ExecutorService
    private static List<String> processWithCascadeThreaded(List<LogEntry> logEntries) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Callable<String>> tasks = new ArrayList<>();

        for (LogEntry entry : logEntries) {
            tasks.add(() -> filterLog(entry));  // Creating tasks to process log entries
        }

        // Execute all tasks and collect the results
        List<Future<String>> results = executor.invokeAll(tasks);
        List<String> processedLogs = new ArrayList<>();

        for (Future<String> result : results) {
            String processed = result.get();
            if (processed != null) {
                processedLogs.add(processed);
            }
        }

        executor.shutdown();
        return processedLogs;
    }


    // Non-threaded processing
    private static List<String> processWithCascadeNonThreaded(List<LogEntry> logEntries) {
        List<String> results = new ArrayList<>();
        for (LogEntry entry : logEntries) {
            String processed = filterLog(entry);
            if (processed != null) {
                results.add(processed);
            }
        }
        return results;
    }

    // Filtering logic for cascade
    private static String filterLog(LogEntry entry) {
        if (entry.level.equals("ERROR") || entry.level.equals("WARNING")) {
            return entry.timestamp + " " + entry.level + " " + entry.message;
        }
        return null;
    }

    // Calculate SNR (Example metric, modify as needed)
    private static double calculateSNR(List<LogEntry> original, List<String> filtered) {
        int signalPower = filtered.size();
        int noisePower = original.size() - filtered.size();

        // Handle the case where there is no noise (all logs passed the filter)
        if (noisePower == 0) {
            return Double.POSITIVE_INFINITY; // Indicates perfect filtering (no noise)
        }

        // Calculate SNR in decibels (dB)
        return 10 * Math.log10((double) signalPower / noisePower);
    }
    // Calculate MSE (Example metric, modify as needed)
    private static double calculateMSE(List<LogEntry> original, List<String> filtered) {
        return Math.abs(original.size() - filtered.size());
    }

}

// LogEntry class to hold structured log data
class LogEntry {
    String timestamp;
    String level;
    String message;

    public LogEntry(String timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }
}
//D:\JAVA\jdk-21.0.5\bin\java.exe "-javaagent:D:\idea\IntelliJ IDEA Community Edition 2022.2.2\lib\idea_rt.jar=54799:D:\idea\IntelliJ IDEA Community Edition 2022.2.2\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath E:\Java_2025\out\production\Logsection LogProcessor
//        Stage 1 - Filtered Logs: 113118
//        Stage 2 - Extracted Features: 113118
//        Threaded Cascade Execution Time: 51132900 ns
//        MSE for Threaded Cascade: 113118.0
//        Non-threaded Series Execution Time: 6239800 ns
//        MSE for Series: 113118.0
//
//        Process finished with exit code 0
