package com.junia.library.service;

import com.junia.library.model.LibraryItem;
import com.junia.library.model.Loan;
import com.junia.library.repository.LibraryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Read-only queries, kept apart from LibraryService (which changes state). */
public class ReportService {

    private final LibraryRepository repository;

    public ReportService(LibraryRepository repository) {
        this.repository = repository;
    }

    public List<Loan> getOverdueLoans(LocalDate today) {
        return repository.getAllLoans().stream()
                .filter(loan -> loan.isOverdue(today))
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .toList();
    }

    public Map<String, Long> countAvailableItemsByType() {
        return repository.getAllItems().stream()
                .filter(LibraryItem::isAvailable)
                .collect(Collectors.groupingBy(LibraryItem::getTypeLabel, Collectors.counting()));
    }
}
