public class Filter {
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
