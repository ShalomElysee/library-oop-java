package com.junia.library.utils;

import com.junia.library.exception.DataLoadException;
import com.junia.library.model.LibraryItem;
import com.junia.library.model.Member;
import com.junia.library.repository.LibraryRepository;

import java.nio.file.Path;

/** Fills a repository from the CSV files: the only place that knows about files. */
public class DataLoader {

    public void load(LibraryRepository repository, Path itemsFile, Path membersFile) {
        try {
            for (LibraryItem item : new ItemCsvParser().parse(itemsFile)) {
                repository.addItem(item);
            }
            for (Member member : new MemberCsvParser().parse(membersFile)) {
                repository.addMember(member);
            }
        } catch (IllegalArgumentException e) {
            // Thrown by the repository on duplicate ids.
            throw new DataLoadException("Inconsistent data: " + e.getMessage(), e);
        }
    }
}
