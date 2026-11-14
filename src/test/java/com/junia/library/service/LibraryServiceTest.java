package com.junia.library.service;

import com.junia.library.exception.ItemNotAvailableException;
import com.junia.library.exception.LoanLimitReachedException;
import com.junia.library.exception.NotFoundException;
import com.junia.library.model.Book;
import com.junia.library.model.Dvd;
import com.junia.library.model.LibraryItem;
import com.junia.library.model.Loan;
import com.junia.library.model.Member;
import com.junia.library.model.MemberType;
import com.junia.library.repository.LibraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 1);

    private LibraryRepository repository;
    private LibraryService service;
    private LibraryItem dvd;

    @BeforeEach
    void setUp() {
        repository = new LibraryRepository();
        dvd = new Dvd("D1", "Inception", "Nolan");
        repository.addItem(new Book("B1", "Dune", "Herbert"));
        repository.addItem(new Book("B2", "1984", "Orwell"));
        repository.addItem(new Book("B3", "Hobbit", "Tolkien"));
        repository.addItem(new Book("B4", "Misérables", "Hugo"));
        repository.addItem(dvd);
        repository.addMember(new Member("M1", "Alice", MemberType.STUDENT));
        repository.addMember(new Member("M2", "Bob", MemberType.STUDENT));
        repository.addMember(new Member("M3", "Dupont", MemberType.STAFF));

        // Policy injected through the interface: here the real one, but a lambda would work too.
        service = new LibraryService(repository, new StandardLateFeePolicy());
    }

    @Test
    void borrowMakesItemUnavailable() throws Exception {
        Loan loan = service.borrow("M1", "D1", DAY);

        assertFalse(dvd.isAvailable());
        assertEquals(DAY.plusDays(7), loan.getDueDate());
    }

    @Test
    void cannotBorrowAnAlreadyBorrowedItem() throws Exception {
        service.borrow("M1", "B1", DAY);

        assertThrows(ItemNotAvailableException.class, () -> service.borrow("M2", "B1", DAY));
    }

    @Test
    void studentCannotBorrowMoreThanThreeItems() throws Exception {
        service.borrow("M1", "B1", DAY);
        service.borrow("M1", "B2", DAY);
        service.borrow("M1", "B3", DAY);

        assertThrows(LoanLimitReachedException.class, () -> service.borrow("M1", "B4", DAY));
    }

    @Test
    void staffCanBorrowMoreThanAStudent() throws Exception {
        service.borrow("M3", "B1", DAY);
        service.borrow("M3", "B2", DAY);
        service.borrow("M3", "B3", DAY);

        assertDoesNotThrow(() -> service.borrow("M3", "B4", DAY));
    }

    @Test
    void refusedBorrowDoesNotChangeItemState() throws Exception {
        service.borrow("M1", "B1", DAY);
        service.borrow("M1", "B2", DAY);
        service.borrow("M1", "B3", DAY);

        assertThrows(LoanLimitReachedException.class, () -> service.borrow("M1", "D1", DAY));
        assertTrue(dvd.isAvailable());
    }

    @Test
    void unknownMemberOrItemIsRefused() {
        assertThrows(NotFoundException.class, () -> service.borrow("NOPE", "B1", DAY));
        assertThrows(NotFoundException.class, () -> service.borrow("M1", "NOPE", DAY));
    }

    @Test
    void returnOnTimeCostsNothing() throws Exception {
        service.borrow("M1", "D1", DAY);

        double fee = service.returnItem("D1", DAY.plusDays(7));

        assertEquals(0, fee, 0.001);
        assertTrue(dvd.isAvailable());
    }

    @Test
    void lateDvdCostsOneAndAHalfEuroPerDay() throws Exception {
        service.borrow("M1", "D1", DAY);

        // Due after 7 days, returned after 10: 3 days late x 1.50
        assertEquals(4.5, service.returnItem("D1", DAY.plusDays(10)), 0.001);
    }

    @Test
    void lateBookCostsHalfAnEuroPerDay() throws Exception {
        service.borrow("M1", "B1", DAY);

        // Due after 21 days, returned after 24: 3 days late x 0.50
        assertEquals(1.5, service.returnItem("B1", DAY.plusDays(24)), 0.001);
    }

    @Test
    void itemCanBeBorrowedAgainAfterReturn() throws Exception {
        service.borrow("M1", "B1", DAY);
        service.returnItem("B1", DAY.plusDays(1));

        assertDoesNotThrow(() -> service.borrow("M2", "B1", DAY.plusDays(2)));
    }

    @Test
    void returningAnItemThatIsNotBorrowedIsRefused() {
        assertThrows(NotFoundException.class, () -> service.returnItem("B1", DAY));
    }

    @Test
    void returnDateBeforeBorrowDateIsRefusedAndKeepsLoanActive() throws Exception {
        service.borrow("M1", "B1", DAY);

        assertThrows(IllegalArgumentException.class, () -> service.returnItem("B1", DAY.minusDays(1)));
        assertFalse(repository.findItem("B1").orElseThrow().isAvailable());
    }

    @Test
    void feePolicyCanBeReplacedWithoutChangingTheService() throws Exception {
        LibraryService freeService = new LibraryService(repository, (loan, returnDate) -> 0.0);
        freeService.borrow("M1", "D1", DAY);

        assertEquals(0.0, freeService.returnItem("D1", DAY.plusDays(30)), 0.001);
    }
}
