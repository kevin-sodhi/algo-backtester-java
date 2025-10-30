package com.kevin.algo.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;    
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kevin.algo.data.CsvDataFeed;
import com.kevin.algo.data.DataFeed;
import com.kevin.algo.engine.BacktestEngine;
import com.kevin.algo.indicators.SMA;
import com.kevin.algo.portfolio.Portfolio;
import com.kevin.algo.strategy.MovingAverageCrossover;

/**
 * Main.java (Milestone 2)
 * -----------------------
 * Reads a CSV, computes SMA(fast) and SMA(slow), prints real values in JSON.
 * Next milestone: add Strategy + BacktestEngine for trades & equity curve.
 * 
 * cd algo-backtester-java
 * mvn -q clean package -DskipTests
 * java -jar target/algo-backtester-java-1.0.0-jar-with-dependencies.jar --csv data/my.csv --fast 3 --slow 5 --strategy macrossover
 */
public class Main {

    private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
        @Override
        public void write(JsonWriter out, LocalDate value) throws java.io.IOException {
            // Serialize LocalDate as ISO string (yyyy-MM-dd)
            out.value(value != null ? value.toString() : null);
        }
        @Override
        public LocalDate read(JsonReader in) throws java.io.IOException {
            String s = in.nextString();
            return (s == null) ? null : LocalDate.parse(s);
        }
    })
    .setPrettyPrinting()
    .create();

    public static void main(String[] args) {


        // 1) CLI Parse flags
        Map<String, String> flags = parseArgs(args);
        String csv = flags.getOrDefault("csv", "");
        String strategy = flags.getOrDefault("strategy", "macrossover");
        int fast = tryParseInt(flags.get("fast"), 3);
        int slow = tryParseInt(flags.get("slow"), 5);
        double cash = tryParseDouble(flags.get("cash"), 10000.0);
        double fee  = tryParseDouble(flags.get("fee"), 0.0);
        double slip = tryParseDouble(flags.get("slip"), 0.0);

        

        // 2️⃣ Validate CSV
        Path csvPath = Path.of(csv).toAbsolutePath();
        Map<String, Object> response = new HashMap<>();
        if (!Files.exists(csvPath)) {
            response.put("ok", false);
            response.put("error", "CSV not found: " + csvPath);
            System.out.println(GSON.toJson(response));
            return;
        }
        // 3️⃣ Init components
        DataFeed feed = new CsvDataFeed(csvPath.toString());
        SMA smaFast = new SMA(fast);
        SMA smaSlow = new SMA(slow);
        MovingAverageCrossover strat = new MovingAverageCrossover();
        Portfolio pf = new Portfolio(cash, fee, slip);
        BacktestEngine engine = new BacktestEngine();

        // 4️⃣ Run engine
        BacktestEngine.Output out = engine.run(feed, smaFast, smaSlow, strat, pf);
        // 5️⃣ Compute simple metrics
        int barsRead = out.series.size();
        int trades = pf.closedTrades().size();
        double finalEquity = pf.finalEquity(out.series.get(barsRead - 1).close);
        double totalReturnPct = (finalEquity / cash - 1.0) * 100.0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("barsRead", barsRead);
        metrics.put("trades", trades);
        metrics.put("totalReturnPct", totalReturnPct);

        // 6️⃣ Assemble JSON
        Map<String, Object> params = new HashMap<>();
        params.put("csv", csv);
        params.put("strategy", strategy);
        params.put("fast", fast);
        params.put("slow", slow);
        params.put("cash", cash);
        params.put("fee", fee);
        params.put("slip", slip);

        response.put("ok", true);
        response.put("message", "Backtest complete");
        response.put("params", params);
        response.put("metrics", metrics);
        response.put("series", out.series);
        response.put("signals", out.signals);
        response.put("equity", out.equity);

        // 7️⃣ Print JSON
        System.out.println(GSON.toJson(response));
    }

    /** ---------------- Utility helpers ------------------ */

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String token = args[i];
            if (token.startsWith("--")) {
                String key = token.substring(2);
                String val = "";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    val = args[++i];
                }
                map.put(key, val);
            }
        }
        return map;
    }

    private static int tryParseInt(String s, int fallback) {
        try { return (s == null || s.isEmpty()) ? fallback : Integer.parseInt(s); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static double tryParseDouble(String s, double fallback) {
        try { return (s == null || s.isEmpty()) ? fallback : Double.parseDouble(s); }
        catch (NumberFormatException e) { return fallback; }
    }
}