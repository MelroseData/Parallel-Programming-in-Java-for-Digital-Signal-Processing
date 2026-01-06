import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.*;
import java.io.*;
import javax.sound.sampled.*;

public class Benchmark {
    public static void main(String[] args) throws ExecutionException, IOException, InterruptedException, UnsupportedAudioFileException {
        String soundDirectory = "E:\\Java_2025\\sound track\\Audio Wise V1.0"; // Directory containing .wav files

        // Get all .wav files in the directory
        File directory = new File(soundDirectory);
        File[] wavFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        if (wavFiles == null || wavFiles.length == 0) {
            System.out.println("No .wav files found in the directory: " + soundDirectory);
            return;
        }

        // Process each .wav file
        for (File wavFile : wavFiles) {
            System.out.println("Processing file: " + wavFile.getName());

            // Read the sound signal from the .wav file
            double[] soundSignal = readWavFile(wavFile.getAbsolutePath());

            // Process the sound signal (e.g., denoising)
            double lowCutoff = 100.0; // Low cutoff frequency for filtering (e.g., 100 Hz)
            double highCutoff = 4000.0; // High cutoff frequency for filtering (e.g., 4000 Hz)
            Filter filter = new Filter(); // Initialize filter
            Cascade cascade = new Cascade(); // Initialize cascade

            // Set advanced filters (optional)
            filter.setButterworthFilter(4, lowCutoff); // Example: 4th order Butterworth filter
            // filter.setChebyshevFilter(4, lowCutoff, 0.5); // Example: 4th order Chebyshev filter with 0.5 dB ripple

            // Benchmarking non-threaded cascade
            long startTime = System.nanoTime();
            double[] filteredSignalCascade = cascade.applyCascade(soundSignal, lowCutoff, highCutoff, filter);
            long endTime = System.nanoTime();
            System.out.println("Non-threaded Cascade Execution Time: " + (endTime - startTime) + " ns");
            System.out.println("SNR for Cascade: " + calculateSNR(soundSignal, filteredSignalCascade));
            System.out.println("MSE for Cascade: " + calculateMSE(soundSignal, filteredSignalCascade));

            // Benchmarking threaded cascade
            startTime = System.nanoTime();
            double[] filteredSignalCascadeThreaded = cascade.applyCascadeWithThreads(soundSignal, lowCutoff, highCutoff, filter);
            endTime = System.nanoTime();
            System.out.println("Threaded Cascade Execution Time: " + (endTime - startTime) + " ns");
            System.out.println("SNR for Threaded Cascade: " + calculateSNR(soundSignal, filteredSignalCascadeThreaded));
            System.out.println("MSE for Threaded Cascade: " + calculateMSE(soundSignal, filteredSignalCascadeThreaded));

            // Save filtered signals to files
            String outputFileName = wavFile.getName().replace(".wav", "_filtered.txt");
            saveSignalToFile(filteredSignalCascade, soundDirectory + "\\" + outputFileName.replace(".wav", "_cascade.txt"));
            saveSignalToFile(filteredSignalCascadeThreaded, soundDirectory + "\\" + outputFileName.replace(".wav", "_cascade_threaded.txt"));
        }
    }
    // Read a .wav file and extract the audio signal
    private static double[] readWavFile(String wavFilePath) throws IOException, UnsupportedAudioFileException {
        File file = new File(wavFilePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();

        // Check if the audio format is supported (PCM_SIGNED)
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new UnsupportedAudioFileException("Unsupported audio format: " + format.getEncoding());
        }

        // Read the audio data into a byte array
        byte[] audioBytes = audioInputStream.readAllBytes();
        int numSamples = audioBytes.length / (format.getSampleSizeInBits() / 8);
        double[] signal = new double[numSamples];

        // Convert byte array to double array (normalized to [-1, 1])
        ByteBuffer buffer = ByteBuffer.wrap(audioBytes);
        buffer.order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < numSamples; i++) {
            if (format.getSampleSizeInBits() == 16) {
                signal[i] = buffer.getShort() / 32768.0; // Normalize 16-bit PCM to [-1, 1]
            } else if (format.getSampleSizeInBits() == 8) {
                signal[i] = (buffer.get() - 128) / 128.0; // Normalize 8-bit PCM to [-1, 1]
            } else {
                throw new UnsupportedAudioFileException("Unsupported sample size: " + format.getSampleSizeInBits());
            }
        }

        return signal;
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

    // Save signal to a file
    private static void saveSignalToFile(double[] signal, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (double value : signal) {
                writer.write(value + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

class Filter {
    private ButterworthFilter butterworthFilter;
    private ChebyshevFilter chebyshevFilter;

    // Default low-pass and high-pass filter methods (for use when no advanced filter is set)
    public double[] applyLowPassFilter(double[] signal, double cutoff) {
        double[] filteredSignal = new double[signal.length];
        for (int i = 1; i < signal.length; i++) {
            filteredSignal[i] = signal[i] * cutoff + (i > 0 ? filteredSignal[i - 1] * (1.0 - cutoff) : 0.0);
        }
        return filteredSignal;
    }

    public double[] applyHighPassFilter(double[] signal, double cutoff) {
        double[] filteredSignal = new double[signal.length];
        for (int i = 1; i < signal.length; i++) {
            filteredSignal[i] = signal[i] - signal[i - 1] * cutoff;
        }
        return filteredSignal;
    }

    public double[] applyBandPassFilter(double[] signal, double lowCutoff, double highCutoff) {
        return applyHighPassFilter(applyLowPassFilter(signal, lowCutoff), highCutoff);
    }

    // Methods for advanced filters: Butterworth and Chebyshev
    public double[] butterworthFilter(double[] signal, double cutoff, int order) {
        if (butterworthFilter != null) {
            return butterworthFilter.applyLowPassFilter(signal);  // Apply Butterworth filter
        }
        return applyLowPassFilter(signal, cutoff);  // Fall back to original method
    }

    public double[] chebyshevFilter(double[] signal, double cutoff, int order) {
        if (chebyshevFilter != null) {
            return chebyshevFilter.applyLowPassFilter(signal);  // Apply Chebyshev filter
        }
        return applyLowPassFilter(signal, cutoff);  // Fall back to original method
    }

    // Constructor to initialize with no filters by default
    public Filter() {
        this.butterworthFilter = null;  // No filter by default
        this.chebyshevFilter = null;    // No filter by default
    }

    // Methods to set filters (can be invoked by the user)
    public void setButterworthFilter(int order, double cutoffFrequency) {
        this.butterworthFilter = new ButterworthFilter(order, cutoffFrequency);
    }

    public void setChebyshevFilter(int order, double cutoffFrequency, double ripple) {
        this.chebyshevFilter = new ChebyshevFilter(order, cutoffFrequency, ripple);
    }

    // Wrapper methods for low-pass, high-pass, and band-pass filters
    public double[] lowPassFilter(double[] signal, double cutoff) {
        if (butterworthFilter != null) {
            return butterworthFilter(signal, cutoff, 1);  // Assuming order 1 for now
        } else if (chebyshevFilter != null) {
            return chebyshevFilter(signal, cutoff, 1);  // Assuming order 1 for now
        } else {
            return applyLowPassFilter(signal, cutoff);
        }
    }

    public double[] highPassFilter(double[] signal, double cutoff) {
        if (butterworthFilter != null) {
            return butterworthFilter(signal, cutoff, 1);  // Apply Butterworth high-pass
        } else if (chebyshevFilter != null) {
            return chebyshevFilter(signal, cutoff, 1);  // Apply Chebyshev high-pass
        } else {
            return applyHighPassFilter(signal, cutoff);
        }
    }

    public double[] bandPassFilter(double[] signal, double lowCutoff, double highCutoff) {
        if (butterworthFilter != null) {
            // Apply Butterworth band-pass filter
            return butterworthFilter(signal, lowCutoff, 1);  // Assuming order 1
        } else if (chebyshevFilter != null) {
            // Apply Chebyshev band-pass filter
            return chebyshevFilter(signal, lowCutoff, 1);  // Assuming order 1
        } else {
            return applyBandPassFilter(signal, lowCutoff, highCutoff);
        }
    }
}

class Cascade {
    public double[] applyCascade(double[] signal, double lowCutoff, double highCutoff, Filter filter) {
        // Apply the filter in a cascaded manner
        double[] filteredSignal = filter.bandPassFilter(signal, lowCutoff, highCutoff);
        // Additional cascading steps can be added here
        return filteredSignal;
    }

    public double[] applyCascadeWithThreads(double[] signal, double lowCutoff, double highCutoff, Filter filter) throws InterruptedException, ExecutionException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int chunkSize = signal.length / numThreads;
        Future<double[]>[] futures = new Future[numThreads];

        // Divide the signal into chunks and process each chunk in parallel
        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? signal.length : start + chunkSize;
            futures[i] = executor.submit(() -> {
                // Apply the cascade of filtering steps (band-pass filtering as part of cascade)
                double[] chunkSignal = Arrays.copyOfRange(signal, start, end);
                chunkSignal = filter.bandPassFilter(chunkSignal, lowCutoff, highCutoff);
                // Additional cascading steps could be added here if needed
                return chunkSignal;
            });
        }

        // Combine the results
        double[] filteredSignal = new double[signal.length];
        for (int i = 0; i < numThreads; i++) {
            double[] chunkResult = futures[i].get();
            System.arraycopy(chunkResult, 0, filteredSignal, i * chunkSize, chunkResult.length);
        }

        executor.shutdown();
        return filteredSignal;
    }

}