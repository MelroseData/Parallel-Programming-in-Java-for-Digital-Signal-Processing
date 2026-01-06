public class HighPassFilter {
    public double[] applyFilter(double[] signal, double cutoff) {
        // Simple high-pass filter implementation
        double[] output = new double[signal.length];
        for (int i = 1; i < signal.length; i++) {
            output[i] = signal[i] - signal[i - 1] + (cutoff * (output[i - 1]));
        }
        return output;
    }
}