package com.kevin.algo.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Main.java (Milestone 0)
 * -----------------------
 * Minimal CLI entry for the project.
 * - Parses flags like: --csv data/sample.csv --fast 5 --slow 20 --strategy macrossover
 * - Prints a JSON payload to stdout. Node will consume this later.
 * Next milestones will replace placeholders with real backtest results.
 */
public class Main {

    // Pretty-print JSON so manual tests are easy to read in Terminal.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        // 1) Parse command-line flags into a map.
        Map<String, String> flags = parseArgs(args);

        // 2) Pull params with defaults.
        String csv = flags.getOrDefault("csv", "");
        String fast = flags.getOrDefault("fast", "");
        String slow = flags.getOrDefault("slow", "");
        String strategy = flags.getOrDefault("strategy", "macrossover"); // default

        // 3) Build the response we’ll extend in later milestones.
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);

        Map<String, Object> params = new HashMap<>();
        params.put("csv", csv);
        params.put("fast", tryParseInt(fast, null));
        params.put("slow", tryParseInt(slow, null));
        params.put("strategy", strategy);
        response.put("params", params);

        // Placeholders to be filled soon.
        response.put("metrics", null);
        response.put("equityCurve", null);
        response.put("message", "CLI wired. Next: models + data feed + SMA + MA crossover.");

        // 4) Print JSON to stdout.
        System.out.println(GSON.toJson(response));
    }

    /** Parses args of the form: --key value */
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String token = args[i];
            if (token.startsWith("--")) {
                String key = token.substring(2).trim(); // drop "--"
                String value = "";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    value = args[i + 1];
                    i++; // skip the value next loop
                }
                map.put(key, value);
            }
        }
        return map;
    }

    /** Safe integer parsing so bad input doesn’t crash the CLI. */
    private static Integer tryParseInt(String s, Integer fallback) {
        try {
            return (s == null || s.isEmpty()) ? fallback : Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return fallback;
        }
    }
}