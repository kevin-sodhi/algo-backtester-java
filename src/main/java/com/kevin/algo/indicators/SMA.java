package com.kevin.algo.indicators;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple Moving Average (rolling window)
 */
public class SMA {

    private final int period;
    private final Queue<Double> window = new LinkedList<>();
    private double sum = 0.0;

    public SMA(int period) { this.period = period; }

    /** Add a new value and update the moving average */
    public void add(double price) {
        sum += price;
        window.add(price);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    /** Convenience overload if your Candle class calls this */
    public void accumulate(com.kevin.algo.core.Candle bar) {
        add(bar.getClose());
    }

    /** Is SMA ready (enough samples)? */
    public boolean isReady() {
        return window.size() >= period;
    }

    /** Current SMA value; NaN if not ready */
    public double value() {
        return isReady() ? sum / window.size() : Double.NaN;
    }

    /** Optional: last n samples for debug */
    public int size() { return window.size(); }
}