public class LowPassFilter {
    public double[] applyFilter(double[] signal, double cutoff) {
        // Simple low-pass filter implementation
        double[] output = new double[signal.length];
        for (int i = 1; i < signal.length; i++) {
            output[i] = output[i - 1] + (cutoff * (signal[i] - output[i - 1]));
        }
        return output;
    }
}