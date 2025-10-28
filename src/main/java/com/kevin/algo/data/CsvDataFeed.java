package com.kevin.algo.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import com.kevin.algo.core.Candle;

/**
 * CsvDataFeed
 * -----------
 * Streams Candle objects from a CSV file with header:
 *   date,open,high,low,close,volume
 *
 * Notes:
 * - No external CSV library: we keep it simple (split by comma).
 * - Skips the first header line automatically.
 * - Throws a RuntimeException on IO/parse errors to keep the demo lean;
 *   you can swap to checked handling later.
 */
public class CsvDataFeed implements DataFeed {

    private final BufferedReader reader;
    private String nextLine; // buffer the next data line (null means EOF)
    private boolean headerSkipped = false;

    public CsvDataFeed(String csvPath) {
        try {
            this.reader = Files.newBufferedReader(Path.of(csvPath));
            this.nextLine = reader.readLine(); // first line (header expected)
        } catch (IOException e) {
            throw new RuntimeException("Failed to open CSV: " + csvPath, e);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            // On first call, skip header and read the first data line.
            if (!headerSkipped) {
                headerSkipped = true;
                // if the file had only one line (header), nextLine now holds header;
                // read the first data row
                nextLine = reader.readLine();
            }
            return nextLine != null;
        } catch (IOException e) {
            throw new RuntimeException("Error advancing CSV", e);
        }
    }

    @Override
    public Candle next() {
        if (nextLine == null) throw new IllegalStateException("No more rows");
        try {
            // parse current line → Candle
            Candle c = parseLine(nextLine);
            // advance to the next line for the following call
            nextLine = reader.readLine();
            if (nextLine == null) {
                // optional: close reader at EOF
                reader.close();
            }
            return c;
        } catch (IOException e) {
            throw new RuntimeException("Error reading next CSV line", e);
        }
    }

    /** Parses a CSV line of: date,open,high,low,close,volume */
    private Candle parseLine(String line) {
        String[] cols = line.split(",", -1); // keep empty fields if any
        if (cols.length < 6) {
            throw new IllegalArgumentException("Bad CSV row, expected 6 columns: " + line);
        }
        LocalDate date = LocalDate.parse(cols[0].trim()); // "YYYY-MM-DD"
        double open  = Double.parseDouble(cols[1].trim());
        double high  = Double.parseDouble(cols[2].trim());
        double low   = Double.parseDouble(cols[3].trim());
        double close = Double.parseDouble(cols[4].trim());
        long volume  = Long.parseLong(cols[5].trim());
        return new Candle(date, open, high, low, close, volume);
    }
}