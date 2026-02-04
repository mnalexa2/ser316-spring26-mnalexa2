import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample White-Box tests for the Checkout system.
 * This class demonstrates how to write white-box tests using:
 * - Control Flow Graph (CFG) analysis
 * - Statement coverage
 * - Branch coverage
 * - Path coverage

 * White-box testing focuses on testing the IMPLEMENTATION by
 * examining the code structure and ensuring all paths are tested.
 */
public class CheckoutWhiteBoxSample {

    private Checkout checkout;

    @BeforeEach
    public void setUp() {
        checkout = new Checkout();
    }

    //Sequence 1
    @Test
    @DisplayName("WB Test: countBooksByType - null type branch")
    public void testCountBooksByType_Sequence1() {
        // Branch: type == null; return 0;
        int result = checkout.countBooksByType(null, true);
        assertEquals(0, result, "Should return 0 for null type");
    }
    //Sequence 2
    @Test
    @DisplayName("WB Test: countBooksByType - available matching book")
    public void testCountBooksByType_Sequence2() {
        // Branch: type == FICTION;
        Book availableBook = new Book("978-0-1234-5678-9", "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", Book.BookType.FICTION, 1);
        checkout.addBook(availableBook);

        int result = checkout.countBooksByType(Book.BookType.FICTION, true);
        assertEquals(1, result, "Should return 1 available fiction book");
    }
    //Sequence 3
    @Test
    @DisplayName("WB Test: countBooksByType - continue and else availability")
    public void testCountBooksByType_Sequence3() {
        // Branch: type == null; else
        checkout.addBook(new Book("123-0-6789-1234-9", "Harry Potter and the Chamber of Secrets", "J.K. Rowling", Book.BookType.FICTION, 0));
        int result = checkout.countBooksByType(Book.BookType.FICTION, false);
        assertEquals(1, result, "Should count book regardless of availability");
    }

}
