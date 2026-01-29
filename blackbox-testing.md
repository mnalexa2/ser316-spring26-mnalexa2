# Black Box Testing Report - Assignment 2

**Student Name:** Michelle N. Alexander  
**ASU ID:** mnalexa2  
**Date:** 27 Jan 2026

---

## Part 1: Equivalence Partitioning (EP)

Identify equivalence partitions for the `checkoutBook(Book book, Patron patron)` method based on the specification (JavaDoc).

Create **multiple tables**, one per partition category (e.g., book state, patron state, renewal, limits, etc.).

Do **not** put everything into one table.

**Column Explanations:**
- **Partition ID**: Unique identifier (e.g., EP 1.1, EP 2.1)
- **State**: The specific state/value for this partition (e.g., "Unavailable", "Available")
- **Valid/Invalid**: Whether this partition represents valid or invalid input
- **Input Condition**: Precise condition that defines this partition
- **Expected Return**: What return code you expect
- **Expected Behavior**: What should happen

### Example EP Table: Book Availability

| Partition ID | State                  | Valid/Invalid | Input Condition                                          | Expected Return | Expected Behavior       |
|--------------|------------------------|---------------|----------------------------------------------------------|-----------------|-------------------------|
| EP 1.1       | Unavailable (0 copies) | Invalid       | availableCopies == 0 AND other conditions allow checkout | 2.0             | No copies to checkout   |
| EP 1.2       | Available (1+ copies)  | Valid         | availableCopies > 0 AND other conditions allow checkout  | Success         | Book can be checked out |
|              |                        |               |                                                          |                 |                         |
|              |                        |               |                                                          |                 |                         |

**Example test cases:** `testBookAvailable()`, `testUnavailableBook()`

---

### EP Tables: Patron Eligibility

| Partition ID | State                         | Valid/Invalid | Input Condition                            | Expected Return | Expected Behavior                                         |
|--------------|-------------------------------|---------------|--------------------------------------------|-----------------|-----------------------------------------------------------|
| EP 2.0       | Eligible (no suspension)      | Valid         | isAccountSuspended == false and fines < 10 | 0.0             | Book added to checkout list and available copies decrease |
| EP 2.1       | Not eligible (suspended)      | Invalid       | isAccountSuspended == true                 | 3.0             | Checkout rejected/ no change to Patron                    |
| EP 2.2       | Not eligible (fines too high) | Invalid       | getFineBalance >= 10                       | 4.1             | Checkout rejected/ no change to Patron                    |

---
### EP Tables: Patron Renewal

| Partition ID | State                      | Valid/Invalid | Input Condition              | Expected Return | Expected Behavior                     |
|--------------|----------------------------|---------------|------------------------------|-----------------|---------------------------------------|
| EP 3.0       | Renewal (no overdue books) | Valid         | patron has book checkout     | 0.1             | Due date updated; no change to copies |
| EP 3.1       | Renewal (overdue warning)  | Valid         | patron has one or two books  | 1.0             | Checkout approved/ warning issued     |
| EP 3.2       | Renewal (near limit)       | Valid         | patron within 2 of max limit | 1.1             | Checkout approved/ warning issued     |

---
### EP Tables: Book state

| Partition ID | State          | Valid/Invalid | Input Condition                | Expected Return | Expected Behavior                         |
|--------------|----------------|---------------|--------------------------------|-----------------|-------------------------------------------|
| EP 4.0       | Reference-only | Invalid       | book.isReferenceOnly() == true | 5.0             | checkout rejected; no state change        |
| EP 4.1       | Null book      | Invalid       | book == null                   | 2.1             | return error immediately; no state change |
| EP 4.2       | Null patron    | Invalid       | patron == null                 | 3.1             | return error immediately; no state change |

---

## Part 2: Boundary Value Analysis (BVA)

Important BVA cases may overlap with EP. That is OK. You can reference all relevant EP/BVA coverage in Part 3.

### Example BVA Table: Overdue Count (Threshold: 3)

| Test ID | Boundary     | Input Value      | Expected Return                  | Rationale                   |
|---------|--------------|------------------|----------------------------------|-----------------------------|
| BVA 1.1 | Below        | overdueCount = 0 | Success (depends on other setup) | Below warning threshold     |
| BVA 1.2 | Warning High | overdueCount = 2 | 1.0                              | Just below reject threshold |
| BVA 1.3 | At           | overdueCount = 3 | 4.0                              | At rejection boundary       |
| BVA 1.4 | Above        | overdueCount = 4 | 4.0                              | Above rejection boundary    |

---

### BVA Tables Fine Count 

| Test ID | Boundary | Input Value | Expected Return | Rationale                            |
|---------|----------|-------------|-----------------|--------------------------------------|
| BVA 2.1 | Below    | 9.00        | 0.0             | Below rejection threshold            |
| BVA 2.2 | At       | 10.00       | 4.1             | At threshold where checkout rejected |
| BVA 2.3 | Above    | 11.00       | 4.1             | Beyond rejection threshold           |

---
### BVA Tables Overdue Limits

| Test ID | Boundary | Input Value     | Expected Return | Rationale                  |
|---------|----------|-----------------|-----------------|----------------------------|
| BVA 3.1 | Below    | 0 overdue books | 0.0             | no warnings/rejections     |
| BVA 3.2 | High     | 2 overdue books | 1.0             | Maximum allowed/warning    |
| BVA 3.3 | At Limit | 3 overdue books | 4.0             | Boundary for rejection     |
| BVA 3.4 | Above    | 4 overdue books | 4.0             | Beyond rejection threshold |

---
### BVA Patron Type Limits

| Test ID | Boundary           | Input Value          | Expected Return | Rationale                     |
|---------|--------------------|----------------------|-----------------|-------------------------------|
| BVA 4.1 | Child Limit        | 3 books checked out  | 1.1             | Warning: at limit             |
| BVA 4.2 | Child Over Limit   | 4th book attempt     | 3.2             | Rejection: Exceed child limit |
| BVA 4.3 | Public Limit       | 5 books checked out  | 1.1             | Warning: at limit             |
| BVA 4.4 | Public Over Limit  | 6th book attempt     | 3.2             | Exceed public limit           |
| BVA 4.5 | Staff Near Limit   | 13 books checked out | 1.1             | Warning: approaching limit    |
| BVA 4.6 | Staff Over Limit   | 16th book attempt    | 3.2             | Exceed staff limit            |
| BVA 4.6 | Student Over Limit | 16th book attempt    | 3.2             | Exceed student limit          |

---

## Part 3: Test Cases Designed

List at least **20** test cases you designed based on your EP/BVA analysis.

Each test case should include:
- EP/BVA coverage
- specific inputs / setup
- expected return code
- expected **observable state changes** (if any)

> Do not test console output.

### Test Case Table
At least some of your tests should verify observable state changes, not just return values.

**Checkout0-3 Columns:** Mark each implementation as Pass (✓) or Fail (✗) for this test case. This helps you track which implementations have bugs and will be useful for Part 4 analysis.

| Test ID Name             | EP/BVA  | Input Description                                    | Expected Return | Expected State Changes                            | Checkout0 | Checkout1 | Checkout2 | Checkout3 |
|--------------------------|---------|------------------------------------------------------|-----------------|---------------------------------------------------|-----------|-----------|-----------|-----------|
| T1 testSuspendedPatron   | EP 2.1  | isAccountSuspended == true, book available           | 3.0             | No state change to patron or book                 | ✓         | ✓         | ✗         | ✓         |
| T2 testFineAtBoundary    | BVA 2.2 | Patron with 10.00 in fines, book available           | 4.1             | No state change                                   | ✗         | ✗         | ✓         | ✓         |
| T3 testFineBelowBoundary | BVA 2.1 | Patron with 9.00 in fines, book available            | 0.0             | Book count -1; book added to patron checkout list |           |           |           |           |
| T4 testRenewal           | EP 3.0  | Patron has book checked out                          | 0.1             | Due date reset; no change to available copies     |           |           |           |           |
| T5 testOverdueWarnHigh   | BVA 3.4 | Patron with exactly 2 overdue books                  | 1.0             | Book count -1; book added to patron checkout list |           |           |           |           |
| T6 testStudentOverLimit  | BVA 4.6 | Student has 10 books checked out; attempt 11th       | 3.2             | No state change to book or patron                 |           |           |           |           |
| T7 testChildAtLimit      | BVA 4.1 | Child has 2 books checked out; attempt 3rd           | 1.1             | Book count -1; child now has 3 books              |           |           |           |           |
| T8 testChildOverLimit    | BVA 4.2 | Child has 3 books checked out; attempt 4th           | 3.2             | No state change                                   |           |           |           |           |
| T9 testPublicAtLimit     | BVA 4.3 | Public has 4 books checked out; attempt 5th          | 1.1             | Book count -1; public now has 5 books             |           |           |           |           |
| T10 testPublicOverLimit  | BVA 4.4 | Public has 5 books checked out; attempt 6th          | 3.2             | No state change                                   |           |           |           |           |
| T11 testOverdueReject    | BVA 3.3 | Patron has 3 overdue books                           | 4.0             | No state change                                   |           |           |           |           |
| T12 testBookUnavailable  | EP 1.1  | available copies == 0                                | 2.0             | No state change to patron list                    |           |           |           |           |
| T13 testReferenceBook    | EP 4.0  | Book not available for checkout (Reference)          | 5.0             | No state change                                   |           |           |           |           |
| T14 testBookNull         | EP 4.2  | Book object == null                                  | 2.1             | No state change                                   |           |           |           |           |
| T15 testPatronNull       | EP 4.2  | Patron == null                                       | 3.1             | No state change                                   |           |           |           |           |
| T16 testPriorityPatron   | EP 4.2  | Both patron == null and book == null                 | 3.1             | No state change; patron checked first             |           |           |           |           |
| T17 testPatronSuspended  | EP 2.1  | Suspended patron and book == null                    | 3.0             | No state change; checks eligibility first         |           |           |           |           |
| T18 testStaffNearLimit   | BVA 4.5 | Staff has 13 books checked out; attempt 14th         | 1.1             | Book count -1; staff now has 14 books             |           |           |           |           |
| T19 testRenewalOverLimit | EP 3.0  | Student with 10 books renewing current               | 0.1             | No change to available copies; due date updated   |           |           |           |           |
| T20 testSuccessCheckout  | EP 1.2  | Normal checkout with eligible patron and no warnings | 0.0             | Book count -1; patron list updated                |           |           |           |           |

(Add rows until you have at least 20.)

---

## Part 4: Bug Analysis

### Easter Eggs Found
List any easter egg messages you observed:
- [EASTER EGG #15.2]: ...xvFZjo5PgG0 (test renewal to complete!)
- [EASTER EGG #10.1/3]: 'Testing can show the presence of bugs,'
- [EASTER EGG #17]: 'The happy path matters too.'
- [EASTER EGG #13]: 'Limits exist to be thoroughly tested.'
- [EASTER EGG #13]: 'The difference between theory and practice is that in theory, there is no difference.'
- [EASTER EGG #13]: 'Boundaries are where bugs hide.'
- [EASTER EGG #15.1]: https://www.youtube.com/watch?v=xvFZjo5PgG0
- [EASTER EGG #14]: 'Renew, reuse, recycle... books.'
- [EASTER EGG #14]: 'A book renewed is a book re-loved.'
- [EASTER EGG #14]: 'Renewing a book is like giving it a second chance.'


### Implementation Results

| Implementation | Bugs Found (count) |
|----------------|--------------------|
| Checkout0      |                    |
| Checkout1      |                    |
| Checkout2      |                    |
| Checkout3      |                    |

### Bugs Discovered
List distinct bugs you identified for each implementation. Each bug must cite at least one test case that revealed it.

**Checkout0:**
- Bug 1: Assertion failed with wrong error code (should be 1.0 but returned 0.0) - Revealed by: T5

**Checkout1:**
- Bug 1: Expected patron should successfully receive book for checkout but returned false - Revealed by: T3
- Bug 2: Allowed checkout (return code 0.0) when student exceeded limit - Revealed by: T6
- Bug 3: Returned wrong error code (should be 3.2 but returned 0.0) - Revealed by: T8
- Bug 4: Returned wrong error code (should be 3.2 but returned 0.0) - Revealed by: T10
- Bug 5: Assertion failed with wrong error code (should be 1.0 but returned 0.0) - Revealed by: T5

**Checkout2:**
- Bug 1: Allowed checkout with wrong error code (should be 3.2 but returned 1.1) - Revealed by: T6
- Bug 2: Expected book count to decrease (from 5 to 4) but did not - Revealed by: T3
- Bug 3: Assertion failed with wrong error code (should be 0.1 but returned 0.0) - Revealed by: T4
- Bug 4: Assertion failed with wrong error code (should be 1.0 but returned 0.0) - Revealed by: T5

**Checkout3:**
- Bug 1: Copies for renewal should not change (should be 1 but returned 0) - Revealed by: T4
- Bug 2: Assertion failed with wrong error code (should be 1.0 but returned 0.0) - Revealed by: T5

### Comparative Analysis
Compare the four implementations:
- Which bugs are most critical (cause the worst failures)?
- Which implementation would you use if you had to choose?
- Why? Justify your choice considering bug severity and frequency.

---

## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**

**What was the most challenging aspect of this assignment? Sorting through what to do; getting organized**

**How did you decide on your EP and BVA? I made a flow diagram first**

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**

