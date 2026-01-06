import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Benchmark {

    public static void main(String[] args) throws ExecutionException, IOException {
        String dataDirectory = "E:\\Java_2025\\data\\MIMIC-III-Waveform-Database\\"; // Directory containing .dat and .hea files
        File dir = new File(dataDirectory);

        // List to store results for each file
        List<String[]> results = new ArrayList<>();

        // Add CSV header
        results.add(new String[]{
                "File Name",
                "Non-threaded Cascade Time (ns)",
                "SNR Cascade",
                "MSE Cascade",
                "Threaded Cascade Time (ns)",
                "SNR Threaded Cascade",
                "MSE Threaded Cascade",
                "Non-threaded Series Time (ns)",
                "SNR Series",
                "MSE Series",
                "Threaded Series Time (ns)",
                "SNR Threaded Series",
                "MSE Threaded Series"
        });

        // Process each .dat file in the directory
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".dat")) {
                String datFilePath = file.getAbsolutePath();
                String headerFilePath = datFilePath.replace(".dat", ".hea");

                // Read the ECG signal from the .dat file
                double[] ecgSignal = readDatFile(datFilePath, headerFilePath);

                // Process the ECG signal (e.g., denoising)
                double lowCutoff = 0.2; // Low cutoff frequency for filtering
                double highCutoff = 0.8; // High cutoff frequency for filtering
                Filter filter = new Filter(); // Initialize filter
                Cascade cascade = new Cascade(); // Initialize cascade
                Series series = new Series(); // Initialize series

                // Benchmarking non-threaded cascade
                long startTime = System.nanoTime();
                double[] filteredSignalCascade = cascade.applyCascade(ecgSignal, lowCutoff, highCutoff, filter);
                long endTime = System.nanoTime();
                long cascadeTime = endTime - startTime;
                double cascadeSNR = calculateSNR(ecgSignal, filteredSignalCascade);
                double cascadeMSE = calculateMSE(ecgSignal, filteredSignalCascade);

                // Benchmarking threaded cascade
                startTime = System.nanoTime();
                double[] filteredSignalCascadeThreaded;
                try {
                    filteredSignalCascadeThreaded = cascade.applyCascadeWithThreads(ecgSignal, lowCutoff, highCutoff, filter);
                } catch (InterruptedException e) {
                    System.err.println("Thread was interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    return;
                }
                endTime = System.nanoTime();
                long cascadeThreadedTime = endTime - startTime;
                double cascadeThreadedSNR = calculateSNR(ecgSignal, filteredSignalCascadeThreaded);
                double cascadeThreadedMSE = calculateMSE(ecgSignal, filteredSignalCascadeThreaded);

                // Benchmarking non-threaded series
                startTime = System.nanoTime();
                double[] filteredSignalSeries = series.applySeries(ecgSignal, lowCutoff, highCutoff, filter);
                endTime = System.nanoTime();
                long seriesTime = endTime - startTime;
                double seriesSNR = calculateSNR(ecgSignal, filteredSignalSeries);
                double seriesMSE = calculateMSE(ecgSignal, filteredSignalSeries);

                // Benchmarking threaded series
                startTime = System.nanoTime();
                double[] filteredSignalSeriesThreaded;
                try {
                    filteredSignalSeriesThreaded = series.applySeriesWithThreads(ecgSignal, lowCutoff, highCutoff, filter);
                } catch (InterruptedException e) {
                    System.err.println("Thread was interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    return;
                }
                endTime = System.nanoTime();
                long seriesThreadedTime = endTime - startTime;
                double seriesThreadedSNR = calculateSNR(ecgSignal, filteredSignalSeriesThreaded);
                double seriesThreadedMSE = calculateMSE(ecgSignal, filteredSignalSeriesThreaded);

                // Add results to the list
                results.add(new String[]{
                        file.getName(),
                        String.valueOf(cascadeTime),
                        String.valueOf(cascadeSNR),
                        String.valueOf(cascadeMSE),
                        String.valueOf(cascadeThreadedTime),
                        String.valueOf(cascadeThreadedSNR),
                        String.valueOf(cascadeThreadedMSE),
                        String.valueOf(seriesTime),
                        String.valueOf(seriesSNR),
                        String.valueOf(seriesMSE),
                        String.valueOf(seriesThreadedTime),
                        String.valueOf(seriesThreadedSNR),
                        String.valueOf(seriesThreadedMSE)
                });
            }
        }

        // Write results to a CSV file
        writeResultsToCSV(results, "benchmark_results_SpO2.csv");
    }

    // Write results to a CSV file
    private static void writeResultsToCSV(List<String[]> results, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : results) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }

    // Read a .dat file from the MIT-BIH Arrhythmia Database
    private static double[] readDatFile(String datFilePath, String headerFilePath) throws IOException {
        int numSamples = getNumSamplesFromHeader(headerFilePath);
        try (DataInputStream dis = new DataInputStream(new FileInputStream(datFilePath))) {
            double[] signal = new double[numSamples];
            byte[] buffer = new byte[2];
            for (int i = 0; i < numSamples; i++) {
                dis.readFully(buffer);
                int rawValue = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                signal[i] = (rawValue - 2048) * 5.0 / 1000.0; // Convert to µV
            }
            return signal;
        }
    }

    // Get the number of samples from the .hea file
    private static int getNumSamplesFromHeader(String headerFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(headerFilePath))) {
            String line = reader.readLine();
            String[] parts = line.split(" ");
            return Integer.parseInt(parts[3]);
        }
    }

    // Calculate Signal-to-Noise Ratio (SNR)
    private static double calculateSNR(double[] original, double[] filtered) {
        double signalPower = 0;
        double noisePower = 0;
        for (int i = 0; i < original.length; i++) {
            signalPower += Math.pow(original[i], 2);
            double noise = original[i] - filtered[i];
            noisePower += Math.pow(noise, 2);
        }
        return 10 * Math.log10(signalPower / noisePower);
    }

    // Calculate Mean Squared Error (MSE)
    private static double calculateMSE(double[] original, double[] filtered) {
        double mse = 0;
        for (int i = 0; i < original.length; i++) {
            double error = original[i] - filtered[i];
            mse += Math.pow(error, 2);
        }
        return mse / original.length;
    }
}
//
//import java.util.Random;
//import java.util.concurrent.ExecutionException;
//import java.io.*;
//import java.util.Random;
//
//public class Benchmark {
//    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
//        int signalLength = 1000000;
//        double[] signal = generateSignal(signalLength);
//        double lowCutoff = 0.2;
//        double highCutoff = 0.8;
//        Filter filter = new Filter();
//        Cascade cascade = new Cascade();
//        Series series = new Series();
//
//        // Benchmarking non-threaded cascade
//        long startTime = System.nanoTime();
//        double[] filteredSignalCascade = cascade.applyCascade(signal, lowCutoff, highCutoff, filter);
//        long endTime = System.nanoTime();
//        System.out.println("Non-threaded Cascade Execution Time: " + (endTime - startTime) + " ns");
//        System.out.println("SNR for Cascade: " + calculateSNR(signal, filteredSignalCascade));
//        System.out.println("MSE for Cascade: " + calculateMSE(signal, filteredSignalCascade));
//
//        // Benchmarking threaded cascade
//        startTime = System.nanoTime();
//        double[] filteredSignalCascadeThreaded = cascade.applyCascadeWithThreads(signal, lowCutoff, highCutoff, filter);
//        endTime = System.nanoTime();
//        System.out.println("Threaded Cascade Execution Time: " + (endTime - startTime) + " ns");
//        System.out.println("SNR for Threaded Cascade: " + calculateSNR(signal, filteredSignalCascadeThreaded));
//        System.out.println("MSE for Threaded Cascade: " + calculateMSE(signal, filteredSignalCascadeThreaded));
//
//        // Benchmarking non-threaded series
//        startTime = System.nanoTime();
//        double[] filteredSignalSeries = series.applySeries(signal, lowCutoff, highCutoff, filter);
//        endTime = System.nanoTime();
//        System.out.println("Non-threaded Series Execution Time: " + (endTime - startTime) + " ns");
//        System.out.println("SNR for Series: " + calculateSNR(signal, filteredSignalSeries));
//        System.out.println("MSE for Series: " + calculateMSE(signal, filteredSignalSeries));
//
//        // Benchmarking threaded series
//        startTime = System.nanoTime();
//        double[] filteredSignalSeriesThreaded = series.applySeriesWithThreads(signal, lowCutoff, highCutoff, filter);
//        endTime = System.nanoTime();
//        System.out.println("Threaded Series Execution Time: " + (endTime - startTime) + " ns");
//        System.out.println("SNR for Threaded Series: " + calculateSNR(signal, filteredSignalSeriesThreaded));
//        System.out.println("MSE for Threaded Series: " + calculateMSE(signal, filteredSignalSeriesThreaded));
//
//        // Optionally save signals to a file
//        saveSignalToFile(filteredSignalCascade, "filtered_cascade.txt");
//        saveSignalToFile(filteredSignalCascadeThreaded, "filtered_cascade_threaded.txt");
//        saveSignalToFile(filteredSignalSeries, "filtered_series.txt");
//        saveSignalToFile(filteredSignalSeriesThreaded, "filtered_series_threaded.txt");
//    }
//
//    // Your existing methods for signal generation, SNR, MSE, and file saving
//    private static double[] generateSignal(int length) {
//        Random random = new Random();
//        double[] signal = new double[length];
//        for(int i = 0; i < length; ++i) {
//            signal[i] = random.nextDouble();
//        }
//        return signal;
//    }
//
//    private static double calculateSNR(double[] original, double[] filtered) {
//        double signalPower = 0;
//        double noisePower = 0;
//        for (int i = 0; i < original.length; i++) {
//            signalPower += original[i] * original[i];
//            double noise = original[i] - filtered[i];
//            noisePower += noise * noise;
//        }
//        return 10 * Math.log10(signalPower / noisePower);
//    }
//
//    private static double calculateMSE(double[] original, double[] filtered) {
//        double mse = 0;
//        for (int i = 0; i < original.length; i++) {
//            double error = original[i] - filtered[i];
//            mse += error * error;
//        }
//        return mse / original.length;
//    }
//
//    private static void saveSignalToFile(double[] signal, String filePath) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
//        for (double value : signal) {
//            writer.write(value + "\n");
//        }
//        writer.close();
//    }
//}
