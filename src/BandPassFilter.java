public class BandPassFilter {
    public double[] applyFilter(double[] signal, double lowCutoff, double highCutoff) {
        // A simple band-pass filter implementation (combination of low-pass and high-pass)
        LowPassFilter lowPass = new LowPassFilter();
        HighPassFilter highPass = new HighPassFilter();

        double[] lowPassed = lowPass.applyFilter(signal, lowCutoff);
        return highPass.applyFilter(lowPassed, highCutoff);
    }
}
