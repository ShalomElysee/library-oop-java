package com.junia.library.utils;

import com.junia.library.exception.DataLoadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Template: reading the file is common, turning one line into an object is up to each subclass. */
public abstract class AbstractCsvParser<T> {

    public List<T> parse(Path path) {
        try (var lines = Files.lines(path)) {
            return lines
                    .skip(1) // header
                    .filter(line -> !line.isBlank())
                    .map(this::parseLine)
                    .toList();
        } catch (IOException e) {
            throw new DataLoadException("Cannot read file: " + path, e);
        }
    }

    protected abstract T parseLine(String line);

    /** Splits a line and checks the column count so subclasses can index columns safely. */
    protected String[] splitColumns(String line, int expectedColumns) {
        String[] columns = line.split(",", -1);
        if (columns.length != expectedColumns) {
            throw new DataLoadException("Invalid line (expected " + expectedColumns
                    + " columns): " + line);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        return columns;
    }
}
