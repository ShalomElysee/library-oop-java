package com.junia.library.service;

import com.junia.library.model.Loan;

import java.time.LocalDate;

/**
 * How a late fee is computed. LibraryService depends on this interface, not on a concrete
 * rule, so a new rule (e.g. a cap, a staff discount) can be added without touching the service.
 */
public interface LateFeePolicy {
    double calculate(Loan loan, LocalDate returnDate);
}
