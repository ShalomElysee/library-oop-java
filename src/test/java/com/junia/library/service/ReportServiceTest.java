package com.junia.library.service;

import com.junia.library.model.Book;
import com.junia.library.model.Dvd;
import com.junia.library.model.Loan;
import com.junia.library.model.Member;
import com.junia.library.model.MemberType;
import com.junia.library.repository.LibraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 1);

    private LibraryService library;
    private ReportService reports;

    @BeforeEach
    void setUp() {
        LibraryRepository repository = new LibraryRepository();
        repository.addItem(new Book("B1", "Dune", "Herbert"));
        repository.addItem(new Book("B2", "1984", "Orwell"));
        repository.addItem(new Dvd("D1", "Inception", "Nolan"));
        repository.addMember(new Member("M1", "Alice", MemberType.STAFF));
        library = new LibraryService(repository, new StandardLateFeePolicy());
        reports = new ReportService(repository);
    }

    @Test
    void overdueLoansAreSortedByDueDate() throws Exception {
        library.borrow("M1", "B1", DAY);  // due in 21 days
        library.borrow("M1", "D1", DAY);  // due in 7 days

        List<Loan> overdue = reports.getOverdueLoans(DAY.plusDays(30));

        assertEquals(2, overdue.size());
        assertEquals("D1", overdue.get(0).getItem().getId());
    }

    @Test
    void noOverdueLoanWhenEverythingIsOnTime() throws Exception {
        library.borrow("M1", "B1", DAY);

        assertTrue(reports.getOverdueLoans(DAY.plusDays(21)).isEmpty());
    }

    @Test
    void returnedLoansAreNotReportedAsOverdue() throws Exception {
        library.borrow("M1", "D1", DAY);
        library.returnItem("D1", DAY.plusDays(20));

        assertTrue(reports.getOverdueLoans(DAY.plusDays(30)).isEmpty());
    }

    @Test
    void countsAvailableItemsByType() throws Exception {
        library.borrow("M1", "B1", DAY);

        Map<String, Long> counts = reports.countAvailableItemsByType();

        assertEquals(1L, counts.get("Book"));
        assertEquals(1L, counts.get("DVD"));
    }
}
