package com.kevin.algo.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Result
 * ------
 * Plain container for backtest outputs. 
 *   - params      : arbitrary key/value notes about the run (fast/slow, file path, etc.)
 *   - metrics     : common KPIs (trades, netPnl, sharpe, maxDrawdown)
 *   - equityCurve : running account value over time (pairs of t & eq)
 *
 * Why i not used  records/maps only?
 *   - POJO keeps type-safety + easy JSON marshalling with Gson.
 *   - We can grow it later (ex: per-trade ledger) without breaking callers.
 */

public class Result {

    /** Small struct for name/value metrics. */
    public static class Metrics {
        public int trades;
        public double netPnl;       // absolute P&L (e.g., dollars)
        public double sharpe;       // placeholder; compute later
        public double maxDrawdown;  // -0.12 = -12% (fraction)
    }

    /** One equity point (time label + equity value). */
    public static class EquityPoint {
        public String t;   // time label (ISO date string for now)
        public double eq;  // equity value (e.g., 100000.0)
        public EquityPoint() {}
        public EquityPoint(String t, double eq) { this.t = t; this.eq = eq; }
    }

    // ---- Result body ----
    private final java.util.Map<String, Object> params = new java.util.HashMap<>();
    private final Metrics metrics = new Metrics();
    private final List<EquityPoint> equityCurve = new ArrayList<>();

    public java.util.Map<String, Object> getParams() { return params; }
    public Metrics getMetrics() { return metrics; }
    public List<EquityPoint> getEquityCurve() { return equityCurve; }

    // helpers to make wiring easy later:
    public Result putParam(String key, Object value) { params.put(key, value); return this; }
    public Result addPoint(String t, double eq) { equityCurve.add(new EquityPoint(t, eq)); return this; }
}
