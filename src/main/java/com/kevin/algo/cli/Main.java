package com.kevin.algo.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kevin.algo.core.Candle;
import com.kevin.algo.data.CsvDataFeed;    
import com.kevin.algo.data.DataFeed;
import com.kevin.algo.indicators.SMA;

/**
 * Main.java (Milestone 2)
 * -----------------------
 * Reads a CSV, computes SMA(fast) and SMA(slow), prints real values in JSON.
 * Next milestone: add Strategy + BacktestEngine for trades & equity curve.
 */
public class Main {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        // 1) Parse flags
        Map<String, String> flags = parseArgs(args);
        String csv = flags.getOrDefault("csv", "");
        String fast = flags.getOrDefault("fast", "");
        String slow = flags.getOrDefault("slow", "");
        String strategy = flags.getOrDefault("strategy", "macrossover");

        // 2) Response scaffold
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);

        Map<String, Object> params = new HashMap<>();
        params.put("csv", csv);
        params.put("fast", tryParseInt(fast, null));
        params.put("slow", tryParseInt(slow, null));
        params.put("strategy", strategy);
        response.put("params", params);

        // 3) Validate numeric input
        Integer fastN = tryParseInt(fast, null);
        Integer slowN = tryParseInt(slow, null);
        if (csv == null || csv.isEmpty() || fastN == null || slowN == null) {
            response.put("ok", false);
            response.put("error", "Missing/invalid flags. Example: --csv data/sample.csv --fast 5 --slow 20");
            System.out.println(GSON.toJson(response));
            return;
        }

        // 3b) Validate the CSV path and make it absolute (so relative paths never break)
        Path csvPath = Path.of(csv).toAbsolutePath();
        if (!Files.exists(csvPath)) {
            response.put("ok", false);
            response.put("error", "CSV not found at: " + csvPath);
            System.out.println(GSON.toJson(response));
            return;
        }

        // 4) Build feed + indicators
        DataFeed feed = new CsvDataFeed(csvPath.toString());
        SMA smaFast = new SMA(fastN);
        SMA smaSlow = new SMA(slowN);

        // 5) Stream CSV into indicators
        int bars = 0;
        while (feed.hasNext()) {
            Candle bar = feed.next();
            smaFast.accumulate(bar);
            smaSlow.accumulate(bar);
            bars++;
        }

        // 6) Populate real metrics (no equity curve yet)
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("barsRead", bars);
        metrics.put("smaFastReady", smaFast.isReady());
        metrics.put("smaSlowReady", smaSlow.isReady());
        metrics.put("smaFastValue", smaFast.value()); // NaN until ready
        metrics.put("smaSlowValue", smaSlow.value());
        response.put("metrics", metrics);

        response.put("equityCurve", null);
        response.put("message", "CSV parsed and SMA computed. Next: Strategy + Engine.");

        // 7) Print JSON
        System.out.println(GSON.toJson(response));
    }

    /** Parses args like: --key value */
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String token = args[i];
            if (token.startsWith("--")) {
                String key = token.substring(2).trim();
                String value = "";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) { value = args[i + 1]; i++; }
                map.put(key, value);
            }
        }
        return map;
    }

    /** Safe integer parsing. */
    private static Integer tryParseInt(String s, Integer fallback) {
        try { return (s == null || s.isEmpty()) ? fallback : Integer.parseInt(s); }
        catch (NumberFormatException nfe) { return fallback; }
    }
}