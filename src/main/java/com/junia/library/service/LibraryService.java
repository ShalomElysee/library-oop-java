package com.junia.library.service;

import com.junia.library.exception.ItemNotAvailableException;
import com.junia.library.exception.LoanLimitReachedException;
import com.junia.library.exception.NotFoundException;
import com.junia.library.model.LibraryItem;
import com.junia.library.model.Loan;
import com.junia.library.model.Member;
import com.junia.library.repository.LibraryRepository;

import java.time.LocalDate;

/** Business rules for borrowing and returning. Dates are passed in so the logic is testable. */
public class LibraryService {

    private final LibraryRepository repository;
    private final LateFeePolicy lateFeePolicy;

    public LibraryService(LibraryRepository repository, LateFeePolicy lateFeePolicy) {
        this.repository = repository;
        this.lateFeePolicy = lateFeePolicy;
    }

    public Loan borrow(String memberId, String itemId, LocalDate date)
            throws NotFoundException, ItemNotAvailableException, LoanLimitReachedException {

        Member member = repository.findMember(memberId)
                .orElseThrow(() -> new NotFoundException("Unknown member: " + memberId));
        LibraryItem item = repository.findItem(itemId)
                .orElseThrow(() -> new NotFoundException("Unknown item: " + itemId));

        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("\"" + item.getTitle() + "\" is already borrowed.");
        }

        int activeLoans = repository.findActiveLoansOf(memberId).size();
        if (activeLoans >= member.getType().getMaxActiveLoans()) {
            throw new LoanLimitReachedException(member.getName() + " already has "
                    + activeLoans + " loans (max " + member.getType().getMaxActiveLoans() + ").");
        }

        Loan loan = new Loan(item, member, date);
        item.markAsBorrowed();
        repository.addLoan(loan);
        return loan;
    }

    /** Returns the item and gives back the late fee to pay (0 if on time). */
    public double returnItem(String itemId, LocalDate date) throws NotFoundException {
        Loan loan = repository.findActiveLoanForItem(itemId)
                .orElseThrow(() -> new NotFoundException("No active loan for item: " + itemId));

        // Compute the fee first: if markReturned rejects the date, nothing has changed yet.
        double fee = lateFeePolicy.calculate(loan, date);
        loan.markReturned(date);
        loan.getItem().markAsReturned();
        return fee;
    }
}
