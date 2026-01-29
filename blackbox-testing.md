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
| EP 4.2       | Null patron    | Invlid        | patron == null                 | 3.1             | return error immediately; no state change |

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

### BVA Tables Fine Count 2

| Test ID | Boundary | Input Value | Expected Return | Rationale                            |
|---------|----------|-------------|-----------------|--------------------------------------|
| BVA 2.1 | Below    | 9.00        | 0.0             | Below rejection threshold            |
| BVA 2.2 | At       | 10.00       | 4.1             | At threshold where checkout rejected |
| BVA 2.3 | Above    | 11.00       | 4.1             | Beyond rejection threshold           |

---
### BVA Tables Overdue Limits

| Test ID | Boundary     | Input Value          | Expected Return | Rationale                    |
|---------|--------------|----------------------|-----------------|------------------------------|
| BVA 3.1 | Below        | Student has 8 books  | 1.1             | Within 10 book limit         |
| BVA 3.2 | Exact Limit  | Student has 10 books | 1.1             | At limit                     |
| BVA 3.3 | Beyond Limit | Student has 11 books | 3.2             | Beyond 10 book limit         |
| BVA 3.4 | Overdue      | Two overdue books    | 1.0             | Max allowed before rejection |

---
### BVA Patron Type Limits

| Test ID | Boundary          | Input Value                   | Expected Return | Rationale           |
|---------|-------------------|-------------------------------|-----------------|---------------------|
| BVA 4.1 | Child Limit       | child has 3 books checked out | 1.1             | Warning: at limit   |
| BVA 4.2 | Child Over Limit  | child 4th book attempt        | 3.2             | Exceed child limit  |
| BVA 4.3 | Public Limit      | 5 books checked out           | 1.1             | Warning: at limit   |
| BVA 4.4 | Public Over Limit | 5th book attempt              | 3.2             | Exceed public limit |

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

| Test ID Name             | EP/BVA  | Input Description                          | Expected Return | Expected State Changes                            | Checkout0 | Checkout1 | Checkout2 | Checkout3 |
|--------------------------|---------|--------------------------------------------|-----------------|---------------------------------------------------|-----------|-----------|-----------|-----------|
| T1 testSuspendedPatron   | EP 2.1  | isAccountSuspended == true, book available | 3.0             | No state change to patron or book                 | ✓         | ✓         | ✗         | ✓         |
| T2 testFineAtBoundary    | BVA 2.2 | Patron with 10.00 in fines, book available | 4.1             | no state change                                   | ✗         | ✗         | ✓         | ✓         |
| T3 testFineBelowBoundary | BVA 2.1 | Patron with 9.00 in fines                  | 0.0             | Book count -1; book added to patron checkout list |           |           |           |           |
| T4 testRenewal           | EP 3.1  | Patron has ISBN checked out                | 0.1             | Due date reset; no change to available copies     |           |           |           |           |
| T5 testOverdueWarnHigh   | BVA 3.4 | Patron with exactly 2 overdue books        | 1.0             | Book count -1; book added to patron checkout list |           |           |           |           |
| T6 testStudentNearLimit  |         |                                            |                 |                                                   |           |           |           |           |
| T7                       |         |                                            |                 |                                                   |           |           |           |           |
| T8                       |         |                                            |                 |                                                   |           |           |           |           |
| T9                       |         |                                            |                 |                                                   |           |           |           |           |
| T10                      |         |                                            |                 |                                                   |           |           |           |           |
| T11                      |         |                                            |                 |                                                   |           |           |           |           |
| T12                      |         |                                            |                 |                                                   |           |           |           |           |
| T13                      |         |                                            |                 |                                                   |           |           |           |           |
| T14                      |         |                                            |                 |                                                   |           |           |           |           |
| T15                      |         |                                            |                 |                                                   |           |           |           |           |
| T16                      |         |                                            |                 |                                                   |           |           |           |           |
| T17                      |         |                                            |                 |                                                   |           |           |           |           |
| T18                      |         |                                            |                 |                                                   |           |           |           |           |
| T19                      |         |                                            |                 |                                                   |           |           |           |           |
| T20                      |         |                                            |                 |                                                   |           |           |           |           |

(Add rows until you have at least 20.)

---

## Part 4: Bug Analysis

### Easter Eggs Found
List any easter egg messages you observed:
- 
- 

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
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout1:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout2:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout3:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

### Comparative Analysis
Compare the four implementations:
- Which bugs are most critical (cause the worst failures)?
- Which implementation would you use if you had to choose?
- Why? Justify your choice considering bug severity and frequency.

---

## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**

**What was the most challenging aspect of this assignment?Sorting through what to do; getting organized**

**How did you decide on your EP and BVA?**

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**

