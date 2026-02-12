import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages library checkout operations.
 * Handles book checkouts, returns, renewals, and fine calculations.
 */
public class Checkout {

    public static final double SUCCESS_NORMAL = 0.0;
    public static final double SUCCESS_RENEWAL = 0.1;
    public static final double SUCCESS_WARNING_OVERDUE = 1.0;
    public static final double SUCCESS_WARNING_MAX_LIMIT = 1.1;
    public static final double BOOK_UNAVAIL = 2.0;
    public static final double BOOK_NULL = 2.1;
    public static final double ERR_SUSPENDED = 3.0;
    public static final double ERR_NULL_PATRON = 3.1;
    public static final double ERR_MAX_LIMIT = 3.2;
    public static final double ERR_OVERDUE_COUNT = 4.0;
    public static final double ERR_FINE_BALANCE = 4.1;
    public static final double BOOK_REFERENCE = 5.0;
    public static final double FINE_OVER_SEVEN = 0.50;
    private static final double MAX_FINE_AMOUNT = 25.0;
    public static final double FINE_FIRST_SEVEN = 0.25;
    public static final double FINE_OVER_FOURTEEN = 1.00;


    private Map<String, Book> bookList; // ISBN -> Book
    private Map<String, Patron> patrons; // PatronID -> Patron
    private List<Transaction> history; //

    /**
     * Inner class to track checkout transactions.
     */
    private static class Transaction {
        Patron patron;
        Book book;
        //LocalDate checkoutDate; // SER316 TASK 2 SPOTBUGS FIX
        //LocalDate dueDate; // SER316 TASK 2 SPOTBUGS FIX
        LocalDate returnDate;

        Transaction(Patron patron, Book book, LocalDate checkoutDate, LocalDate dueDate) {
            this.patron = patron;
            this.book = book;
            // this.checkoutDate = checkoutDate; // SER316 TASK 2 SPOTBUGS FIX
            //this.dueDate = dueDate; // SER316 TASK 2 SPOTBUGS FIX
            this.returnDate = null;
        }
    }

    /**
     * Constructs a new Checkout instance.
     *
     * Initializes the class with an empty collection of books, an empty collection
     * of patrons, and an empty history list to track checked out activities.
     */
    public Checkout() {
        this.bookList = new HashMap<>();
        this.patrons = new HashMap<>();
        this.history = new ArrayList<>();
    }

    /**
     * Adds a book to the book list using its ISBN as the key.
     *
     * @param book the book to be added to the book list
     */
    public void addBook(Book book) {
        bookList.put(book.getIsbn(), book);
    }

    /**
     * Registers a new patron in the system by adding the patron to the collection of registered patrons.
     *
     * @param patron the Patron object to be registered, containing the patron's details and unique ID
     */
    public void registerPatron(Patron patron) {
        patrons.put(patron.getPatronId(), patron);
    }
    /**
     * Validates if a patron is eligible to check out books you can assume this method is correct.
     * This helper method consolidates patron-related eligibility checks.
     * Students can assume this method is correct and use it in their implementation.

     * Returns error codes for the following conditions (checked in order):
     * - Patron is null → 3.1
     * - Account is suspended → 3.0
     * - Has 3 or more overdue books → 4.0
     * - Has $10.00 or more in fines → 4.1
     *
     * @param patron The patron to validate
     * @return 0.0 if eligible, or appropriate error code (3.1, 3.0, 4.0, 4.1)
     */
    public double validatePatronEligibility(Patron patron) {
        if (patron == null) {
            return ERR_NULL_PATRON;
        }
        if (patron.isAccountSuspended()) {
            return ERR_SUSPENDED;
        }
        if (patron.getOverdueCount() >= 3) {
            return ERR_OVERDUE_COUNT;
        }
        if (patron.getFineBalance() >= 10.0) {
            return ERR_FINE_BALANCE;
        }
        return SUCCESS_NORMAL; // Eligible
    }

    /**
     * Main checkout method - processes a book checkout for a patron.
     * This method performs comprehensive validation and returns a status code.
     ** TESTING NOTE: Transaction history is maintained internally and can be assumed
     * to work correctly. Students performing black-box testing should focus on:
     * - Return codes (observable via the method return value)
     * - Book availability changes (observable via book.getAvailableCopies())
     * - Patron's checked-out books (observable via patron.getCheckedOutBooks())
     * Do NOT attempt to test transaction history - it is not publicly accessible.

     * Return codes:
     *   0.0 - Success, book checked out normally
     *   0.1 - Success, renewal (patron already had this book, renewal sets the due date to
     *   (today + patron.getLoanPeriodDays()).)
     *   1.0 - Success with warning (patron has 1-2 overdue books)
     *   1.1 - Success with warning (patron within 2 of max checkout limit after this checkout)
     *        Max limits: FACULTY=20 (e.g. warning at 18, 19, 20 including current checkout), STAFF=15, STUDENT=10,
     *        PUBLIC=5, CHILD=3
     *   2.0 - Book unavailable (all copies checked out)
     *   2.1 - Book is null
     *   3.0 - Patron account is suspended
     *   3.1 - Patron is null
     *   3.2 - Patron at maximum checkout limit (FACULTY=20, STAFF=15, STUDENT=10, PUBLIC=5, CHILD=3)
     *   4.0 - Patron has 3 or more overdue books
     *   4.1 - Patron has $10.00 or more in unpaid fines
     *   5.0 - Book is reference-only (cannot be checked out)

     * Validation order (observable priority when multiple conditions apply):
     *   1. Call validatePatronEligibility() assume this method is correct - returns in this order
     *      1.1. eligible 0.0 -> continue with step 2
     *      1.2. checks patron null (3.1) -> return this error code right away
     *      1.3. suspended (3.0) -> return this error code right away
     *      1.4. overdue count >= 3 (4.0) -> return this error code right away
     *      1.5. fines >= $10 (4.1) -> return this error code right away
     *   2. Check if book is null (2.1)
     *   3. Check if book is reference-only (5.0)
     *   4. If renewal, return 0.1 immediately (renew skips step 5)
     *   5. If not-renewal
     *      5.1. Check if book is available (2.0)
     *      5.2. Check if patron is at max checkout limit (3.2)
     *      5.3. Process checkout (update patron checkedOutBooks, call book.checkout()), then determine
     *      success code (priority 1.0, then 1.1, else 0.0)


     * Success non-renewal:
     *   - book will be added to list of checkedOutBooks of patron with dueDate = today + patron.getLoanPeriodDays()
     *   - book.checkout() will be called reducing the availability by 1

     * Success renewal:
     *  - patron.getCheckedOutBooks() is updated to today + loanPeriodDays; book.checkout() is not called; available
     *  copies do not change.

     * Additional notes:
     *  - getCheckoutCount() refers to the number of books currently checked out
     *  (size of the patron's checked-out collection), not lifetime transactions; renewals do not increase this count.
     *  - For any non-success return code (2.x–5.x), neither the patron's checked-out books nor the book's available
     *  copies should change.
     *  - Tests may assume due dates equal LocalDate.now().plusDays(patron.getLoanPeriodDays())
     *  on the day the test runs.
     *  - A book is unavailable if and only if book.getAvailableCopies() <= 0 (i.e., book.isAvailable() is false).
     *  - Console output (including Easter eggs) is non-functional and should not be asserted in tests.
     *
     * @param book The book to check out (can be null)
     * @param patron The patron checking out the book (can be null)
     * @return Status code indicating result (see above)
     */
    public double checkoutBook(Book book, Patron patron) {
//        Implement me in Assignment 3
        //Patron eligibility
        double eligibility = validatePatronEligibility(patron);
        if (eligibility != SUCCESS_NORMAL) {
            return eligibility;
        }

        // Normal success
        // Book validation
        if (book == null) {
            return BOOK_NULL;
        }
        if (book.getType() == Book.BookType.REFERENCE) {
            return BOOK_REFERENCE;
        }
        // Renewal checkout
        if (patron.hasBookCheckedOut(book.getIsbn())) {
            patron.addCheckedOutBook(book.getIsbn(), LocalDate.now().plusDays(patron.getLoanPeriodDays())
            );
            return SUCCESS_RENEWAL;
        }
        // Book available
        if (book.getAvailableCopies() <= 0) {
            return BOOK_UNAVAIL;
        }
        // Get patron checkout limit
        if (patron.getCheckoutCount() >= patron.getMaxCheckoutLimit()) {
            return ERR_MAX_LIMIT;
        }

        // Reduce book count
        book.checkout();
        patron.addCheckedOutBook(book.getIsbn(), LocalDate.now().plusDays(patron.getLoanPeriodDays())
        );
        if (patron.getOverdueCount() > 0) {
            return SUCCESS_WARNING_OVERDUE;
        }
        int currentCount = patron.getCheckoutCount();
        int max = patron.getMaxCheckoutLimit();

        if (patron.getType() == Patron.PatronType.CHILD) {
            if (currentCount == max) return SUCCESS_WARNING_OVERDUE;
        } else {
            if (currentCount >= max - 2) return SUCCESS_WARNING_MAX_LIMIT;
        }

        return SUCCESS_NORMAL;
    }



    /**
     * Calculates the fine amount for an overdue book. Assume this javadoc is correct.

     * Fine calculation rules:
     * - First 7 days overdue: $0.25 per day
     * - Days 8-14 overdue: $0.50 per day
     * - Days 15+ overdue: $1.00 per day
     * - REFERENCE and TEXTBOOK types: double the normal rate
     * - Maximum fine per book: $25.00

     * Examples:
     * - 5 days overdue, FICTION: 5 * $0.25 = $1.25
     * - 10 days overdue, NONFICTION: (7 * $0.25) + (3 * $0.50) = $3.25
     * - 20 days overdue, TEXTBOOK: ((7*$0.25) + (7*$0.50) + (6*$1.00)) * 2 = $23.50
     * - 50 days overdue, FICTION: would be $41.75, but capped at $25.00
     *
     * @param numOfDays Number of days the book is overdue
     * @param bookType The type of book (affects fine rate)
     * @return Fine amount in dollars
     */
    public double calculateFine(int numOfDays, Book.BookType bookType) {
        if (numOfDays <= 0) {
            return SUCCESS_NORMAL;
        }

        double fine = 0.0;

        // First 7 days: $0.25/day
        int days1 = Math.min(numOfDays, 7);
        fine += days1 * FINE_FIRST_SEVEN;


        // Days 8-14: $0.50/day
        if (numOfDays > 7) {
            int days2 = Math.min(numOfDays - 7, 7);
            fine += days2 * FINE_OVER_SEVEN;
        }

        // Days 15+: $1.00/day
        if (numOfDays > 14) {
            int days3 = numOfDays - 14;
            fine += days3 * FINE_OVER_FOURTEEN;
        }

        // Double rate for REFERENCE and TEXTBOOK
        if (bookType == Book.BookType.REFERENCE || bookType == Book.BookType.TEXTBOOK) {
            fine *= 2.0;
        }

        // Cap at maximum fine amount
        return Math.min(fine, MAX_FINE_AMOUNT);
    }

    /**
     * Validates ISBN format you can assume this javadoc is correct.
     * Valid formats:
     * - ISBN-10: 10 digits (e.g., "0123456789")
     * - ISBN-13: 13 digits (e.g., "9780123456789")
     * - ISBN with hyphens: XXX-X-XXXX-XXXX-X (e.g., "978-0-1234-5678-9")

     * Invalid:
     * - null or empty strings
     * - Contains letters or special characters (except hyphens)
     * - Wrong number of digits after removing hyphens
     *
     * @param isbn The ISBN string to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return false;
        }


        String numbers = isbn.replace("-", "");

        // Check if all remaining characters are digits
        if (!numbers.matches("\\d+")) {
            return false;
        }

        // Check length (must be 10 or 13 digits)
        int length = numbers.length();
        return length == 10 || length == 13;
    }

    /**
     * Checks if a patron type string matches a given type.
     *
     * @param typeString The type as a string
     * @param expectedType The expected patron type
     * @return true if types match
     */
    public boolean isPatronType(String typeString, Patron.PatronType expectedType) {
        if (typeString == null || expectedType == null) {
            return false;
        }

        return typeString.equals(expectedType.toString());
    }

    /**
     * Processes a book return.
     * Calculates any overdue fines and updates patron/book status.
     *
     * @param isbn The ISBN of the book being returned
     * @param patron The patron returning the book
     * @return Fine amount charged (0.0 if not overdue)
     */
    public double returnBook(String isbn, Patron patron) {
        if (patron == null || !patron.hasBookCheckedOut(isbn)) {
            return -1.0;
        }

        Book book = bookList.get(isbn);
        if (book == null) {
            return -1.0;
        }

        LocalDate dueDate = patron.getCheckedOutBooks().get(isbn);
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);

        double fine = 0.0;
        if (daysOverdue > 0) {
            fine = calculateFine((int) daysOverdue, book.getType());
            patron.addFine(fine);
        }

        // Update patron and book
        patron.removeCheckedOutBook(isbn);
        book.returnBook();

        // Update transaction history to mark book as returned
        for (Transaction t : history) {
            if (t.patron.equals(patron) && t.book.equals(book) && t.returnDate == null) {
                t.returnDate = today;
                break;
            }
        }

        return fine;
    }

    /**
     * Counts available books of a specific type in inventory.
     * Useful for inventory management and reporting.

     * This method demonstrates more complex control flow for white-box testing:
     * - Loop iteration
     * - Nested conditional statements
     * - Multiple decision points
     *
     * @param type The book type to count (FICTION, NONFICTION, REFERENCE, TEXTBOOK, CHILDREN)
     * @param onlyAvailable If true, counts only books with availableCopies > 0;
     *                      if false, counts all books of the type regardless of availability
     * @return Number of books matching the criteria (0 if type is null or no matches found)
     */
    public int countBooksByType(Book.BookType type, boolean onlyAvailable) {

        if (type == null) { // If there is no book of that type, return 0 and exit
            return 0;
        }
        // If there is a book of that type, set variable
        int looped = 0;

        int count = 0;

        // Loop through all books in inventory
        for (Book b : bookList.values()) {

            if (b == null) { // If book is null, continue looping;If book is found (not null), go to if statement
                continue;
            }

            // Check if book matches the requested type
            if (b.getType() == type) {
                //if the requested type is found, go to next if statement, if not, go back to loop
                // Nested condition: filter by availability if requested
                if (onlyAvailable) {
                    // Only count if book has available copies
                    if (b.isAvailable()) {
                        count++;
                    }
                } else {
                    // Count all books of this type regardless of availability
                    count++;
                }
            }
        }

        return count;
    }

    public Map<String, Book> getInventory() {
        return new HashMap<>(bookList); // SER316 TASK 2 SPOTBUGS FIX
    }

    public Map<String, Patron> getPatrons() {
        return new HashMap<>(patrons);// SER316 TASK 2 SPOTBUGS FIX
    }
}
