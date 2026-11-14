package com.junia.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class Loan {

    private final LibraryItem item;
    private final Member member;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    public Loan(LibraryItem item, Member member, LocalDate borrowDate) {
        this.item = item;
        this.member = member;
        this.borrowDate = borrowDate;
        // The due date depends on the real type of the item (polymorphism).
        this.dueDate = borrowDate.plusDays(item.getLoanDurationDays());
    }

    public void markReturned(LocalDate date) {
        if (returnDate != null) {
            throw new IllegalStateException("This loan is already returned.");
        }
        if (date.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before the borrow date.");
        }
        this.returnDate = date;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(dueDate);
    }

    /** Number of days after the due date, 0 if on time. */
    public long daysLate(LocalDate onDate) {
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, onDate));
    }

    public LibraryItem getItem() {
        return item;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Optional<LocalDate> getReturnDate() {
        return Optional.ofNullable(returnDate);
    }
}
