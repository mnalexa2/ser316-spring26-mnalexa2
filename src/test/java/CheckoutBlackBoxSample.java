import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample Black-Box tests for the Checkout system.
 * This class demonstrates how to write black-box tests using:
 * - Equivalence Partitioning (EP)
 * - Boundary Value Analysis (BVA)
 * - Parametrized tests across multiple implementations
 *
 * Black-box testing focuses on testing the SPECIFICATION WITHOUT
 * looking at the implementation.
 *
 * The parameterized structure allows testing all Checkout implementations
 * with the same tests to identify which implementations have bugs.
 */
public class CheckoutBlackboxSample {

    private Checkout checkout;

    /**
     * Provides the list of Checkout classes to test.
     * Each test will run against ALL implementations.
     */
    @SuppressWarnings("unchecked")
    static Stream<Class<? extends Checkout>> checkoutClassProvider() {
        return (Stream<Class<? extends Checkout>>) Stream.of(
                Checkout0.class,
                Checkout1.class,
                Checkout2.class,
                Checkout3.class
        );
    }

    // Uncomment when you implement the method in assign 3 and comment the above
//    static Stream<Class<? extends Checkout>> checkoutClassProvider() {
//        return Stream.of(Checkout.class);
//    }


    /**
     * Helper method to create Checkout instance from class using reflection.
     */
    private Checkout createCheckout(Class<? extends Checkout> clazz) throws Exception {
        Constructor<? extends Checkout> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }
    //max values/fines
    //state changes: book availability, patron checkout count, patron’s checked-out list
    //partition categories: book state(avail/unavail), patron state, renewal, limits, etc.

    //book checkout normally; success code 0.0; check if book available
    //book renewal normally; success code 0.1
    //book renewal with warning 1-2; success code 1.0
    //book renewal with warning within 2; success code 1.1
    //book is null; return code 2.1
    //patron account suspended; return code 3.0
    //patron is null; return code 3.1
    //patron at max checkout limit; return code 3.2
    //patron has >=3 overdue books; return code 4.0
    //patron has >=10 in od fines; return code 4.1
    //book is reference only; return code 5.0

    //success state; verify book available decrease and patron assigned book
    //renewal state; available copies stays the same
    //fine state; verify below threshold of $10

    /**
     * TEST 1: Tests if patron is suspended
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T1: Suspended patron returns code 3.0")
    public void testSuspendedPatron(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create patron with book
        Book book = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 5);
       Patron patron = new Patron("S001", "Test Patron", "test@example.com",
                Patron.PatronType.STUDENT);

        patron.setAccountSuspended(true);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 3.0 for suspended patron
        assertEquals(3.0, result, 0.01,
                "Expected error code 3.0 for suspended patron for" + checkoutClass.getSimpleName());

        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should NOT receive book when suspended for " + checkoutClass.getSimpleName());
        assertEquals(5, book.getAvailableCopies(),
                "Book count must remain unchanged for " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 2: Tests if patron has $10.00 in fines
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T2: Patron at fine threshold returns code 4.1")
    public void testFineAtBoundary(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create patron with book
        Book book = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("S002", "Test Patron", "test@example.com",
                Patron.PatronType.STUDENT);

        patron.addFine(10.00);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 4.1 for high fines
        assertEquals(4.1, result, 0.01,
                "Expected error code 4.1 for high fines for" + checkoutClass.getSimpleName());

        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should NOT receive book.");
        assertEquals(5, book.getAvailableCopies(),
                "Book count should not change.");
    }

    /**
     * TEST 3: Checks if patron is below fine boundary
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T3: Patron below fine boundary returns code 0.0")
    public void testFineBelowBoundary(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create patron with book
        Book book = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("S003", "Test Patron", "test@example.com",
                Patron.PatronType.STUDENT);

        patron.addFine(9.00);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 0.0 for fine at boundary
        assertEquals(0.0, result, 0.01,
                "Expected error code 0.0 for fine at boundary for" + checkoutClass.getSimpleName());

        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should successfully receive book for " + checkoutClass.getSimpleName());
        assertEquals(5, book.getAvailableCopies(),
                "Book count should decrease by one for successful checkout in " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 4: Checks if patron has book checked out
     * This tests an invalid equivalence partition.
     */
@ParameterizedTest
@MethodSource("checkoutClassProvider")
@DisplayName("T4: Patron has book checked out return code 0.1")
public void testRenewal(Class<? extends Checkout> checkoutClass) throws Exception {
    checkout = createCheckout(checkoutClass);
// Setup: Create patron
    Patron patron = new Patron("S004", "Test Student", "test@example.com",
            Patron.PatronType.STUDENT);
    Book book = new Book("978-0-123456-78-9", "Java For Dummies", "Author", Book.BookType.TEXTBOOK, 1);

    checkout.registerPatron(patron);
    checkout.addBook(book);

    // Setup: Pre-condition: patron already has book
    patron.addCheckedOutBook(book.getIsbn(), LocalDate.now());

    double result = checkout.checkoutBook(book, patron);

    // Verify: Should return 0.1
    assertEquals(0.1, result, 0.01);
    assertEquals(1, book.getAvailableCopies(), "Copies for renewal should not change");
}

    /**
     * TEST 5: Warning for patron with two overdue books
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T5: Patron warning for overdue books return code 1.0")
    public void testOverdueWarnHigh(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create patron with book
        Patron patron = new Patron("S005", "Overdue Patron", "test@example.com",
                Patron.PatronType.STUDENT);
        Book bookToBorrow = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 5);

        checkout.registerPatron(patron);
        checkout.addBook(bookToBorrow);

for (int i = 0; i < 2; i++) {
    Book overdueBook = new Book("978-0-123456-78-9" + i, "Overdue Book " + i, "Author", Book.BookType.FICTION, 1);
    checkout.addBook(overdueBook);

    patron.addCheckedOutBook(overdueBook.getIsbn(), LocalDate.now().minusDays(1));
    // Set status to overdue
    patron.getCheckedOutBooks().put(overdueBook.getIsbn(), LocalDate.now().minusDays(1));
}
        double result = checkout.checkoutBook(bookToBorrow, patron);

        // Verify: Should return 1.0 and warning
        assertEquals(1.0, result, 0.01,
                "Expected warning code 1.0 for 2 overdue books for " + checkoutClass.getSimpleName());
        // State check: Checkout successful
        assertTrue(patron.hasBookCheckedOut(bookToBorrow.getIsbn()),
                "Patron SHOULD successfully receive book with warning for " + checkoutClass.getSimpleName());
        // State check: Decrement book count from 5 to 4
        assertEquals(4, bookToBorrow.getAvailableCopies(),
                "Book count should decrease to 4 in " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 6: Checks student checked out count within limits
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T6: Student has 10 books checked out; attempt 11th")
    public void testStudentOverLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create student
        Patron patron = new Patron("S006", "Test Student", "test@example.com",
                Patron.PatronType.STUDENT);

        checkout.registerPatron(patron);

        for (int i = 0; i < 10; i++) {
            Book existingBook = new Book("ISBN" + i, "Existing" + i,
                    "Test Author", Book.BookType.TEXTBOOK, 1);
            checkout.addBook(existingBook);
            checkout.checkoutBook(existingBook, patron);
        }
        // Setup: 11th book attempt
        Book eleventhBook = new Book("978-0-123456-78-9", "Book Eleven", "Author", Book.BookType.TEXTBOOK, 5);
        checkout.addBook(eleventhBook);

        double result = checkout.checkoutBook(eleventhBook, patron);

        // Verify: Should return 3.2 for exceeding limit
        assertEquals(3.2, result, 0.01,
                "Expected error code 3.2 for exceeding student checkout limit for" + checkoutClass.getSimpleName());

        assertFalse(patron.hasBookCheckedOut(eleventhBook.getIsbn()),
                "Patron should NOT receive 11th book in " + checkoutClass.getSimpleName());
        assertEquals(5, eleventhBook.getAvailableCopies(),
                "Book count must remain unchanged for " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 7: Checks checked out count for child at limit
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T7: Child has 2 books checked out; attempt 3rd")
    public void testChildAtLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create child patron
        Patron patron = new Patron("C001", "Test Child", "test@example.com",
                Patron.PatronType.CHILD);

        checkout.registerPatron(patron);

        for (int i = 0; i < 2; i++) {
            Book existingBook = new Book("ISBN" + i, "Existing" + i,
                    "Test Author", Book.BookType.CHILDREN, 1);
            checkout.addBook(existingBook);
            checkout.checkoutBook(existingBook, patron);
        }
        // Setup: 3rd book attempt with warning
        Book thirdBook = new Book("978-0-123456-78-9", "Book Four", "Author", Book.BookType.CHILDREN, 4);
        checkout.addBook(thirdBook);

        double result = checkout.checkoutBook(thirdBook, patron);

        // Verify: Should return 1.0 with warning
        assertEquals(1.0, result, 0.01,
                "Expected warning code 1.0 for 3 checked out books (at limit) for " + checkoutClass.getSimpleName());
        // State check: Checkout successful
        assertTrue(patron.hasBookCheckedOut(thirdBook.getIsbn()),
                "Patron SHOULD successfully receive book with warning for " + checkoutClass.getSimpleName());
        // State check: Decrement book count from 4 to 3
        assertEquals(3, thirdBook.getAvailableCopies(), "Book count should decrease to 3 in " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 8:Checks checked out count for child within limits
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T8: Child has 3 books checked out; attempt 4th")
    public void testChildOverLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create child patron
        Patron patron = new Patron("C002", "Test Child", "test@example.com",
                Patron.PatronType.CHILD);

        checkout.registerPatron(patron);

        for (int i = 0; i < 3; i++) {
            Book existingBook = new Book("ISBN" + i, "Existing" + i,
                    "Test Author", Book.BookType.CHILDREN, 1);
            checkout.addBook(existingBook);
            checkout.checkoutBook(existingBook, patron);
        }
        // Setup: 11th book attempt
        Book fourthBook = new Book("978-0-123456-78-9", "Book Four", "Author", Book.BookType.CHILDREN, 5);
        checkout.addBook(fourthBook);

        double result = checkout.checkoutBook(fourthBook, patron);

        // Verify: Should return 3.2 for exceeding limit
        assertEquals(3.2, result, 0.01,
                "Expected error code 3.2 for exceeding child checkout limit for" + checkoutClass.getSimpleName());

        assertFalse(patron.hasBookCheckedOut(fourthBook.getIsbn()),
                "Patron should NOT receive 4th book in " + checkoutClass.getSimpleName());
        assertEquals(5, fourthBook.getAvailableCopies(),
                "Book count must remain unchanged for " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 9: Checks checked out count for public at limit
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T9: Public has 4 books checked out; attempt 5th")
    public void testPublicAtLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create public patron
        Patron patron = new Patron("P001", "Test Public", "test@example.com",
                Patron.PatronType.PUBLIC);

        checkout.registerPatron(patron);

        for (int i = 0; i < 4; i++) {
            Book existingBook = new Book("ISBN" + i, "Existing" + i,
                    "Test Author", Book.BookType.NONFICTION, 1);
            checkout.addBook(existingBook);
            checkout.checkoutBook(existingBook, patron);
        }
        // Setup: 5th book attempt with warning
        Book fifthBook = new Book("978-0-123456-78-9", "The Fifth Book", "Author", Book.BookType.NONFICTION, 4);
        checkout.addBook(fifthBook);

        double result = checkout.checkoutBook(fifthBook, patron);

        // Verify: Should return 1.0 with warning
        assertEquals(1.1, result, 0.01,
                "Expected warning code 1.1 for 5 checked out books (at limit) for " + checkoutClass.getSimpleName());
        // State check: Checkout successful
        assertTrue(patron.hasBookCheckedOut(fifthBook.getIsbn()),
                "Patron SHOULD successfully receive book with warning for " + checkoutClass.getSimpleName());
        // State check: Decrement book count from 4 to 3
        assertEquals(3, fifthBook.getAvailableCopies(), "Book count should decrease to 3 in " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 10: Checks public checked out count within limits
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T10: Public has 5 books checked out; attempt 6th")
    public void testPublicOverLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create student
        Patron patron = new Patron("P002", "Test Public", "test@example.com",
                Patron.PatronType.PUBLIC);

        checkout.registerPatron(patron);

        for (int i = 0; i < 5; i++) {
            Book existingBook = new Book("ISBN" + i, "Existing" + i,
                    "Test Author", Book.BookType.NONFICTION, 1);
            checkout.addBook(existingBook);
            checkout.checkoutBook(existingBook, patron);
        }
        // Setup: 11th book attempt
        Book sixthBook = new Book("978-0-123456-78-9", "The Sixth Book", "Author", Book.BookType.NONFICTION, 5);
        checkout.addBook(sixthBook);

        double result = checkout.checkoutBook(sixthBook, patron);

        // Verify: Should retuen 3.2 for exceeding limit
        assertEquals(3.2, result, 0.01,
                "Expected error code 3.2 for exceeding public checkout limit for" + checkoutClass.getSimpleName());

        assertFalse(patron.hasBookCheckedOut(sixthBook.getIsbn()),
                "Patron should NOT receive 6th book in " + checkoutClass.getSimpleName());
        assertEquals(5, sixthBook.getAvailableCopies(),
                "Book count must remain unchanged for " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 11: Patron has 3 overdue books (reject)
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T5: Patron rejection for 3 overdue books return code 4.0")
    public void testOverdueReject(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create patron with book
        Patron patron = new Patron("F001", "Rejected Overdue Patron", "test@example.com",
                Patron.PatronType.FACULTY);
        Book bookToBorrow = new Book("978-0-123456-78-9", "Rejected Book",
                "Test Author", Book.BookType.TEXTBOOK, 5);

        checkout.registerPatron(patron);
        checkout.addBook(bookToBorrow);

        for (int i = 0; i < 3; i++) {
            Book overdueBook = new Book("978-0-123456-78-9" + i, "Overdue Book " + i, "Author", Book.BookType.TEXTBOOK, 5);
            checkout.addBook(overdueBook);

            patron.addCheckedOutBook(overdueBook.getIsbn(), LocalDate.now().minusDays(1));
            }
        double result = checkout.checkoutBook(bookToBorrow, patron);

        // Verify: Should return 1.0 and warning
        assertEquals(4.0, result, 0.01,
                "Expected warning code 4.0 for 3 overdue books for " + checkoutClass.getSimpleName());
        // State check: Checkout successful
        assertTrue(patron.hasBookCheckedOut(bookToBorrow.getIsbn()),
                "Patron should NOT receive book when they have 3 overdue books in " + checkoutClass.getSimpleName());
        // State check: Decrement book count from 5 to 4
        assertEquals(5, bookToBorrow.getAvailableCopies(),
                "Book count remains unchanged for rejected checkout in" + checkoutClass.getSimpleName());
    }

    /**
     * T12: Attempt to checkout unavailable book
     * Tests attempts checkout a book with availableCopies == 0
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T12: Book with 0 copies returns code 2.0")
    public void testBookUnavailable(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Book with available copies set to 0
        Book book = new Book("978-0-000000-12-1", "No Book Here", "Author", Book.BookType.FICTION, 0);
        Patron patron = new Patron("F002", "Test", "test@edu", Patron.PatronType.FACULTY);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        // Execute
        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 2.0 for 0 available copies
        assertEquals(2.0, result, 0.01, "Should return 2.0 for 0 copies in " + checkoutClass.getSimpleName());

        // State Check: rejected; no state change
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()), "Patron should NOT get the book");
        assertEquals(0, book.getAvailableCopies(), "Count must remain 0");
    }

    /**
     * T13: Book not available for checkout (Reference)
     * Tests attempts checkout a reference type book
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T13: Reference book returns code 5.0")
    public void testReferenceBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Book type is REFERENCE
        Book book = new Book("978-0-000000-13-2", "Library Use Only", "Author", Book.BookType.REFERENCE, 5);
        Patron patron = new Patron("S009", "Student patron", "test@edu", Patron.PatronType.STUDENT);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        // Execute
        double result = checkout.checkoutBook(book, patron);

        // Verify: Code 5.0 for Reference books
        assertEquals(5.0, result, 0.01, "Should return 5.0 for Reference books in " + checkoutClass.getSimpleName());

        // State Check: Rejection (No changes)
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()), "Patron should NOT receive a reference book");
        assertEquals(5, book.getAvailableCopies(), "Book count must remain unchanged");
    }

    /**
     * T14: Book object is null
     * Tests invalid equivalence partition for book input
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T14: Null Book returns code 2.1")
    public void testBookNull(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Valid patron and null book
        Patron patron = new Patron("P014", "Valid User", "test@edu", Patron.PatronType.STUDENT);
        checkout.registerPatron(patron);

        // Execute
        double result = checkout.checkoutBook(null, patron);

        // Verify: return code 2.1 for Null Book
        assertEquals(2.1, result, 0.01, "Should return 2.1 for null book in " + checkoutClass.getSimpleName());

        // State Check: No change
        assertFalse(patron.hasBookCheckedOut(null), "Patron list should not change for null book");
    }

    /**
     * T15: Patron object is null
     * Tests invalid equivalence partition for patron input
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T15: Null Patron returns code 3.1")
    public void testPatronNull(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Valid book, but null patron
        Book book = new Book("978-0-000000-15-0", "Valid Book", "Author", Book.BookType.FICTION, 5);
        checkout.addBook(book);

        // Execute
        double result = checkout.checkoutBook(book, null);

        // Verify: Code 3.1 for Null Patron
        assertEquals(3.1, result, 0.01, "Should return 3.1 for null patron in " + checkoutClass.getSimpleName());

        // State Check: No changes allowed
        assertEquals(5, book.getAvailableCopies(), "Book count must remain 5 for null patron");
    }

    /**
     * T16: Priority validation sequence (patron before book)
     * Tests priority: Patron validation should happen before Book validation
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T16: Null Patron priority check returns code 3.1")
    public void testNullPatronPriority(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Both are null
        double result = checkout.checkoutBook(null, null);

        // Verify: Should return 3.1 because Patron check should happen first
        assertEquals(3.1, result, 0.01,
                "Priority Error: Expected null patron to be caught before null Book in " + checkoutClass.getSimpleName());
    }

    /**
     * T17: Suspended Patron vs. Null Book.
     * Tests priority: Patron status (3.0) should be checked before Book existence (2.1).
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T17: Suspended Patron priority check returns code 3.0")
    public void testSuspendedPatronPriority(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Suspended Patron but uull Book
        Patron suspendedPatron = new Patron("P017", "Suspended", "test@edu", Patron.PatronType.STUDENT);
        suspendedPatron.setAccountSuspended(true);
        checkout.registerPatron(suspendedPatron);

        // Execute
        double result = checkout.checkoutBook(null, suspendedPatron);

        // Verify: Should return 3.0
        assertEquals(3.0, result, 0.01,
                "Priority Error: Expected 3.0 (Suspended) before 2.1 (Null Book) in " + checkoutClass.getSimpleName());
    }

    /**
     * T18: Staff has 13 books checked out; attempt 14th
     * Tests a valid partition with a near-limit warning
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T18: Staff at 13 books successfully borrows 14th - return code 1.1")
    public void testStaffNearLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create Staff Patron
        Patron patron = new Patron("P011", "Staff Member", "staff@edu", Patron.PatronType.STAFF);
        checkout.registerPatron(patron);

        // Setup: Fill staff account with 13 books
        for (int i = 0; i < 13; i++) {
            Book filler = new Book("ISBN fill" + i, "Staff Filler" + i, "Author", Book.BookType.FICTION, 1);
            checkout.addBook(filler);
            checkout.checkoutBook(filler, patron);
        }

        // Setup: The 14th book attempt for return code 1.1
        Book fourteenthBook = new Book("978-0-000000-18-4", "The 14th Book", "Author", Book.BookType.FICTION, 5);
        checkout.addBook(fourteenthBook);

        // Execute
        double result = checkout.checkoutBook(fourteenthBook, patron);

        // Verify: retuen code 1.1 for Staff approaching limit
        assertEquals(1.1, result, 0.01,
                "Expected warning code 1.1 for Staff at 13/14 books in " + checkoutClass.getSimpleName());

        // State Check: Success
        assertTrue(patron.hasBookCheckedOut(fourteenthBook.getIsbn()),
                "Staff SHOULD receive the book despite the 1.1 warning in " + checkoutClass.getSimpleName());

        // State Check: Book count should decrease
        assertEquals(4, fourteenthBook.getAvailableCopies(),
                "Book count should have decreased to 4 in " + checkoutClass.getSimpleName());
    }

    /**
     * T19: Student with 10 books renewing current
     * Tests that a renewal is allowed even at the checkout limit
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T19: Student at 10-book limit successfully renews - return code 0.1")
    public void testRenewalOverLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create Student Patron
        Patron patron = new Patron("S010", "Limit Renew", "limit@edu", Patron.PatronType.STUDENT);
        checkout.registerPatron(patron);

        // Setup: Checkout 9 books first
        for (int i = 0; i < 9; i++) {
            Book fillerBook = new Book("ISBN-FILL-" + i, "Filler " + i, "Author", Book.BookType.FICTION, 1);
            checkout.addBook(fillerBook);
            checkout.checkoutBook(fillerBook, patron);
        }

        // Setup: Checkout the 10th book for renew
        Book renewalBook = new Book("978-0-000000-19-0", "The Renew Book", "Author", Book.BookType.FICTION, 5);
        checkout.addBook(renewalBook);
        checkout.checkoutBook(renewalBook, patron);

        int countAfterInitial = renewalBook.getAvailableCopies();

        //
        // Execute: Attempts to renew the 10th book while at the 10-book limit
        double result = checkout.checkoutBook(renewalBook, patron);

        // Verify: Renewal should return code 0.1
        assertEquals(0.1, result, 0.01,
                "Expected renewal code 0.1 even when at checkout limit for " + checkoutClass.getSimpleName());

        // State Check: Patron has the book
        assertTrue(patron.hasBookCheckedOut(renewalBook.getIsbn()),
                "Patron should still have the book checked out in " + checkoutClass.getSimpleName());

        // State Check: Library count
        assertEquals(countAfterInitial, renewalBook.getAvailableCopies(),
                "Available copies should remain unchanged after renewal in " + checkoutClass.getSimpleName());
    }

    /**
     * TEST 20: Happy case! Normal successful checkout
     * This tests a standard valid partition with no warnings or errors
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T20: Successful checkout returns code 0.0")
    public void testSuccessCheckout(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create a perfectly eligible patron
        Patron patron = new Patron("P020", "Perfect Patron", "perfect@edu", Patron.PatronType.STUDENT);
        // Setup: Create a standard circulating book
        Book book = new Book("978-0-000000-20-0", "Standard Fiction", "Author", Book.BookType.FICTION, 5);

        checkout.registerPatron(patron);
        checkout.addBook(book);

        // Execute
        double result = checkout.checkoutBook(book, patron);

        // Verify: Success code 0.0
        assertEquals(0.0, result, 0.01,
                "Expected success code 0.0 for normal checkout in " + checkoutClass.getSimpleName());

        // State Check: Patron list MUST be updated
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should have the book in their list in " + checkoutClass.getSimpleName());

        // State Check: Book count MUST decrease
        assertEquals(4, book.getAvailableCopies(),
                "Book count should have decreased to 4 in " + checkoutClass.getSimpleName());
    }

    /**
     * SAMPLE TEST 1: Tests checkout with unavailable book
     * This tests an invalid equivalence partition.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T1: Unavailable book returns error code 2.0")
    public void testUnavailableBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);

        // Setup: Create unavailable book
        Book book = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 5);
        book.setAvailableCopies(0);  // We are pretending it has been checked out by others and is not available anymore

        Patron patron = new Patron("P001", "Test Patron", "test@example.com",
                Patron.PatronType.STUDENT);

        checkout.addBook(book);
        checkout.registerPatron(patron);

        // Execute checkout
        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 2.0 for unavailable book
        assertEquals(2.0, result, 0.01,
                "Expected error code 2.0 for unavailable book for " + checkoutClass.getSimpleName());

        // Verify: Patron should NOT have the book
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should NOT have book in list for " + checkoutClass.getSimpleName());
    }

    /**
     * SAMPLE TEST 2: Tests successful checkout of an available book
     * This tests the valid equivalence partition - all conditions met.
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T2: Successful checkout - available book, eligible patron")
    public void testBookAvailable(Class<? extends Checkout> checkoutClass) throws Exception {
            checkout = createCheckout(checkoutClass);

        // Setup: Create available book and eligible patron
        Book book = new Book("978-0-123456-78-9", "Test Book",
                "Test Author", Book.BookType.FICTION, 1);

        Patron patron = new Patron("P001", "Test Patron", "test@example.com",
                Patron.PatronType.STUDENT);

        checkout.addBook(book); // adding the book to the library
        checkout.registerPatron(patron); // adding a patrol to the system

        // Execute checkout
        double result = checkout.checkoutBook(book, patron);

        // Verify: Should return 0.0 for success
        assertEquals(0.0, result, 0.01,
                "Expected successful checkout (0.0) for " + checkoutClass.getSimpleName());

        // Verify: Book should now be unavailable
        assertFalse(book.isAvailable(),
                "Book should be unavailable after checkout for " + checkoutClass.getSimpleName());

        // Verify: Patron should have the book in their checked-out list
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
                "Patron should have book in checked-out list for " + checkoutClass.getSimpleName());

        // Verify: Checkout count increased
        assertEquals(1, patron.getCheckoutCount(),
                "Patron checkout count should be 1 for " + checkoutClass.getSimpleName());
    }
}
