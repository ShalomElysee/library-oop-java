package com.junia.library.utils;

import com.junia.library.exception.DataLoadException;
import com.junia.library.model.Book;
import com.junia.library.model.Dvd;
import com.junia.library.model.LibraryItem;

/** Format: type,id,title,creator (type is BOOK or DVD). */
public class ItemCsvParser extends AbstractCsvParser<LibraryItem> {

    @Override
    protected LibraryItem parseLine(String line) {
        String[] columns = splitColumns(line, 4);
        String type = columns[0].toUpperCase();

        try {
            return switch (type) {
                case "BOOK" -> new Book(columns[1], columns[2], columns[3]);
                case "DVD" -> new Dvd(columns[1], columns[2], columns[3]);
                default -> throw new DataLoadException("Unknown item type '" + columns[0] + "' in line: " + line);
            };
        } catch (IllegalArgumentException e) {
            throw new DataLoadException("Invalid item in line: " + line, e);
        }
    }
}
