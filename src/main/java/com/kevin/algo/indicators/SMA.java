package com.kevin.algo.indicators;

import com.kevin.algo.core.Candle;
import com.kevin.algo.dsa.ArrayQueue;

/**
 * SMA (Simple Moving Average)
 * ---------------------------
 * Rolling average of 'close' over the last N bars.
 *
 * Implementation details:
 *  - Stores the last N closes in a circular ArrayQueue (double[])
 *  - Maintains a running 'sum' so updates are O(1):
 *      * When queue not full: offer(close), sum += close
 *      * When full:
 *          old = poll()       // remove oldest
 *          sum -= old
 *          offer(close)
 *          sum += close
 *  - value() returns sum / windowSize when ready, else Double.NaN
 */
public class SMA implements Indicator {

    private final int window;
    private final ArrayQueue q;
    private double sum = 0.0;
    private int seen = 0; // total bars seen (helps with readiness)

    public SMA(int window) {
        if (window <= 0) throw new IllegalArgumentException("window must be > 0");
        this.window = window;
        this.q = new ArrayQueue(window);
    }

    @Override
    public void accumulate(Candle bar) {
        double close = bar.getClose();
        if (q.isFull()) {
            // remove oldest value from sum, then add the new one
            double old = q.poll();
            sum -= old;
            q.offer(close);
            sum += close;
        } else {
            q.offer(close);
            sum += close;
        }
        seen++;
    }

    @Override
    public boolean isReady() {
        // ready when we have at least 'window' closes
        return q.size() == window;
    }

    @Override
    public double value() {
        return isReady() ? (sum / window) : Double.NaN;
    }

    /** For debugging/inspection (not required). */
    public int window() { return window; }
    public int samples() { return seen; }
}