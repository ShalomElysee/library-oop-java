# Library Management System (Java OOP – Final Project)

## Purpose
A small console application that manages a library: members borrow books and DVDs, return them,
and pay a late fee when they are late. It focuses on object-oriented design and business rules,
not on the interface.

## How to run it
Requirements: Java 25 and Maven (same as the course repository).

```bash
mvn compile exec:java          # runs com.junia.library.Application
```
Or from IntelliJ: run `Application.main`. Run it **from the project root**, because the default data
files are read from `src/main/resources/`. You can also pass your own files:
`java -cp target/classes com.junia.library.Application items.csv members.csv`.

The demo is scripted with a fixed date (2026-11-01) so the output is always the same.

## How to run the tests
```bash
mvn test
```
32 JUnit 5 tests: normal cases, edge cases (due date, exact limit, same-day return) and exceptions.

## Main features
- Load items and members from CSV files at startup
- Borrow an item (checks: member exists, item exists, item available, loan limit not reached)
- Return an item and compute the late fee
- Reports: overdue loans (sorted by due date) and available items by type

## Data files (`src/main/resources/`)
- `items.csv` : `type,id,title,creator` (type is `BOOK` or `DVD`)
- `members.csv` : `id,name,type` (type is `STUDENT` or `STAFF`)

Blank lines are ignored; an invalid line stops the loading with a clear error message.

## Project structure
```
src/main/java/com/junia/library/
├── model/        Domain classes: LibraryItem (abstract), Book, Dvd, Member, MemberType (enum), Loan
├── repository/   LibraryRepository: in-memory storage
├── service/      LibraryService (borrow/return), ReportService (queries), LateFeePolicy + StandardLateFeePolicy
├── exception/    Custom exceptions
├── utils/        CSV parsing and DataLoader
└── Application.java   Entry point (load -> operations -> display -> errors)
src/test/java/...  JUnit tests
```

## Main classes
| Class | Responsibility |
|---|---|
| `LibraryItem` / `Book` / `Dvd` | An item that can be lent, with its own loan duration and daily late fee |
| `Member` / `MemberType` | A borrower; the type defines the maximum number of active loans |
| `Loan` | A borrowing: dates, due date, late days, returned or not |
| `LibraryRepository` | Stores items, members and loans (services never touch the collections) |
| `LibraryService` | Business rules: borrow, return |
| `ReportService` | Read-only queries with streams |
| `AbstractCsvParser<T>` + 2 parsers | Read a CSV file and build objects |
| `DataLoader` | Fills the repository from the two files |

## Technical decisions
1. **Dates are parameters** (`borrow(..., LocalDate date)`) instead of calling `LocalDate.now()`.
   Why: tests and the demo become deterministic (a test about a 3-day delay does not depend on today).
2. **Business errors are checked exceptions, a broken data file is unchecked.**
   Why: a refused loan is a normal case the caller must handle; `DataLoadException` is thrown inside
   stream lambdas (which cannot throw checked exceptions) and means the app cannot start anyway.
3. **`Optional` in the repository** (`findItem`, `findMember`) instead of returning `null`.
   Why: the service is forced to decide what to do when nothing is found (`orElseThrow(NotFoundException)`).
4. **Validation in the model constructors** (blank id/title throws `IllegalArgumentException`).
   Why: an invalid object can never exist, so the rest of the code does not need to re-check.
5. **`LibraryService` and `ReportService` are separate.**
   Why: one changes state, the other only reads. Each class has a single reason to change.

## Where is...
- **Inheritance**: `Book` and `Dvd` extend `LibraryItem`; `ItemCsvParser` and `MemberCsvParser` extend `AbstractCsvParser`.
  It is meaningful because books and DVDs share data (id, title, availability) but have different rules.
- **Polymorphism**: `Loan` calls `item.getLoanDurationDays()` and `StandardLateFeePolicy` calls
  `item.getDailyLateFee()` on a `LibraryItem`; the result depends on the real object (book: 21 days / 0.50 €,
  DVD: 7 days / 1.50 €). The repository stores everything as `LibraryItem`.
- **Abstract class**: `LibraryItem` (a generic "item" does not exist alone, and it holds shared state) and
  `AbstractCsvParser<T>` (shared file reading, only the line parsing differs).
- **Interface**: `LateFeePolicy`. It is a *behavior* with no state, so an interface fits better than a class.
  `LibraryServiceTest` replaces it with a lambda to prove the service does not depend on a concrete rule.
- **Custom exceptions**: `LibraryException` (base), `ItemNotAvailableException`, `LoanLimitReachedException`,
  `NotFoundException`, and the unchecked `DataLoadException`.
- **Data loading**: `Application` calls `DataLoader`, which uses `ItemCsvParser` / `MemberCsvParser`
  (`Files.lines`, skip header, ignore blank lines) and adds the objects to the repository. No hard-coded objects.
- **Collections / Java features**: `Map` (repository, report), `List`, `Optional`, streams and lambdas,
  `enum` with a field (`MemberType`), `LocalDate`, generics (`AbstractCsvParser<T>`).

## SOLID
- **S**: each class has one job (model / storage / rules / queries / file parsing). `Application` only wires and displays.
- **O**: a new item type (e.g. `Magazine`) or a new fee rule is added by creating a class; `LibraryService` does not change.
- **L**: a `Book` or a `Dvd` can be used anywhere a `LibraryItem` is expected without breaking the code.
- **I**: `LateFeePolicy` has a single method; nobody is forced to implement something they do not use.
- **D**: `LibraryService` receives a `LateFeePolicy` (interface) in its constructor instead of creating a concrete one.
