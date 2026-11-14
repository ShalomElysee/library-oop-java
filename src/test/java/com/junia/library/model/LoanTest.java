package com.junia.library.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    private static final LocalDate BORROW_DATE = LocalDate.of(2026, 11, 1);
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("M1", "Alice", MemberType.STUDENT);
    }

    @Test
    void dueDateDependsOnItemType() {
        Loan bookLoan = new Loan(new Book("B1", "Dune", "Herbert"), member, BORROW_DATE);
        Loan dvdLoan = new Loan(new Dvd("D1", "Inception", "Nolan"), member, BORROW_DATE);

        assertEquals(BORROW_DATE.plusDays(21), bookLoan.getDueDate());
        assertEquals(BORROW_DATE.plusDays(7), dvdLoan.getDueDate());
    }

    @Test
    void isNotLateOnTheDueDate() {
        Loan loan = new Loan(new Dvd("D1", "Inception", "Nolan"), member, BORROW_DATE);

        assertEquals(0, loan.daysLate(loan.getDueDate()));
        assertFalse(loan.isOverdue(loan.getDueDate()));
    }

    @Test
    void isLateTheDayAfterTheDueDate() {
        Loan loan = new Loan(new Dvd("D1", "Inception", "Nolan"), member, BORROW_DATE);
        LocalDate dayAfter = loan.getDueDate().plusDays(1);

        assertEquals(1, loan.daysLate(dayAfter));
        assertTrue(loan.isOverdue(dayAfter));
    }

    @Test
    void returnedLoanIsNeverOverdue() {
        Loan loan = new Loan(new Dvd("D1", "Inception", "Nolan"), member, BORROW_DATE);
        loan.markReturned(BORROW_DATE.plusDays(2));

        assertFalse(loan.isOverdue(BORROW_DATE.plusDays(100)));
        assertFalse(loan.isActive());
    }

    @Test
    void cannotReturnBeforeBorrowDate() {
        Loan loan = new Loan(new Book("B1", "Dune", "Herbert"), member, BORROW_DATE);

        assertThrows(IllegalArgumentException.class, () -> loan.markReturned(BORROW_DATE.minusDays(1)));
        assertTrue(loan.isActive());
    }

    @Test
    void cannotReturnTwice() {
        Loan loan = new Loan(new Book("B1", "Dune", "Herbert"), member, BORROW_DATE);
        loan.markReturned(BORROW_DATE.plusDays(1));

        assertThrows(IllegalStateException.class, () -> loan.markReturned(BORROW_DATE.plusDays(2)));
    }
}
