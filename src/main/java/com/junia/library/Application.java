package com.junia.library;

import com.junia.library.exception.DataLoadException;
import com.junia.library.exception.LibraryException;
import com.junia.library.model.Loan;
import com.junia.library.repository.LibraryRepository;
import com.junia.library.service.LibraryService;
import com.junia.library.service.ReportService;
import com.junia.library.service.StandardLateFeePolicy;
import com.junia.library.utils.DataLoader;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Flow: load data from files -> run business operations -> display results -> handle errors.
 * Usage: java ... Application [itemsFile membersFile]  (defaults to the files in src/main/resources)
 */
public class Application {

    // Fixed "today" so the demo always gives the same output.
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);

    public static void main(String[] args) {
        Path itemsFile = args.length >= 2 ? Path.of(args[0]) : Path.of("src/main/resources/items.csv");
        Path membersFile = args.length >= 2 ? Path.of(args[1]) : Path.of("src/main/resources/members.csv");

        LibraryRepository repository = new LibraryRepository();
        try {
            new DataLoader().load(repository, itemsFile, membersFile);
        } catch (DataLoadException e) {
            System.err.println("Cannot start: " + e.getMessage());
            return;
        }
        System.out.println(repository.getAllItems().size() + " items loaded from " + itemsFile);

        LibraryService library = new LibraryService(repository, new StandardLateFeePolicy());
        ReportService reports = new ReportService(repository);

        System.out.println("\n--- Borrowing (on " + TODAY + ") ---");
        tryBorrow(library, "M001", "B001", TODAY);   // ok
        tryBorrow(library, "M001", "D001", TODAY);   // ok
        tryBorrow(library, "M002", "B001", TODAY);   // error: already borrowed
        tryBorrow(library, "M001", "B002", TODAY);   // ok (Alice: 3 loans)
        tryBorrow(library, "M001", "B003", TODAY);   // error: STUDENT limit is 3
        tryBorrow(library, "M999", "B004", TODAY);   // error: unknown member

        System.out.println("\n--- Available items by type ---");
        reports.countAvailableItemsByType().forEach((type, count) -> System.out.println(type + ": " + count));

        LocalDate later = TODAY.plusDays(10);
        System.out.println("\n--- Overdue loans on " + later + " ---");
        reports.getOverdueLoans(later).forEach(loan -> System.out.println(
                loan.getItem().getTitle() + " (" + loan.getMember().getName() + "), due " + loan.getDueDate()));

        System.out.println("\n--- Returns on " + later + " ---");
        tryReturn(library, "D001", later);  // 3 days late x 1.50
        tryReturn(library, "B001", later);  // on time
        tryReturn(library, "B003", later);  // error: never borrowed
    }

    private static void tryBorrow(LibraryService library, String memberId, String itemId, LocalDate date) {
        try {
            Loan loan = library.borrow(memberId, itemId, date);
            System.out.println("OK    " + loan.getMember().getName() + " borrowed \""
                    + loan.getItem().getTitle() + "\", due " + loan.getDueDate());
        } catch (LibraryException e) {
            System.out.println("ERROR " + e.getMessage());
        }
    }

    private static void tryReturn(LibraryService library, String itemId, LocalDate date) {
        try {
            double fee = library.returnItem(itemId, date);
            System.out.printf("OK    %s returned, fee: %.2f EUR%n", itemId, fee);
        } catch (LibraryException e) {
            System.out.println("ERROR " + e.getMessage());
        }
    }
}
