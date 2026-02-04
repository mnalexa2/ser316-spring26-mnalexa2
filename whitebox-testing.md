# White Box Testing Report - Assignment 3

**Student Name:** Michelle N. Alexander
**ASU ID:** mnalexa2
**Date:** 3 February 2026

---

## Part 1: Control Flow Graph for countBooksByType()

### Graph Description

Draw or describe your control flow graph here. Include:
- Node numbers represent lines of code
- Edges showing control flow
- Conditions at decision points

**You can hand-draw and insert an image, or describe it in text format.**
![Control Flow Diagram](control-flow-graph.png)

### Node Coverage Sequences

List the sequences needed for complete node coverage:

**Sequence 1:**
- **Path A: 350, 352, 353, 381**
- **Purpose: Covers return path when input is invalid (no book type)*
- **Test case: countByBooksType(null, true)**


**Sequence 2:**
- **Path B: 350, 352, 356, 359, 361, 366, 368, 370, 371, 359, 380, 381**
- **Purpose: Covers path to count available books**
- **Test case: countBooksByType(FICTION, true) with one FICTION type book**


**Sequence 3:**
- **Path C: 350, 352, 356, 359, 361, 362, 359, 361, 366, 368, 373, 374, 359, 380, 381**
- **Purpose:Covers continue path for null and else path to count regardless of availability**
- **Test case: countBooksByType(FICTION, true) with one FICTION type book regardless if available and one null**



### Edge Coverage Sequences

List the sequences needed for complete edge coverage:

**Sequence 1:**
- **Edges covered: 352->353, 353->381**
- **Test case:countBooksByType(null, true)**


**Sequence 2:**
- **Edges covered: 352->356, 356->359, 359->361, 361->366, 
- 366->368, 368->370, 370->371, 371->359, 359->380, 380->381**
- **Test case: countBooksByType(FICTION, true) with one FICTION type book**


**Sequence 3:**
- **Edges covered: 361->362, 362->359, 366->359, 368->373, 373->375, 375->359, 370->359**
- **Test case:countBooksByType(null, true) with one ull book, mismatch book type, and unavailable fiction; onlyAvailable = false**


---

## Part 2: Code Coverage with JaCoCo

### Initial Coverage for Checkout.java

**Before adding tests:**
- **Line Coverage:** ___%
- **Branch Coverage:** ___%

### Coverage for countBooksByType()

**Before additional tests:**
- **Branch Coverage:** ___%

**After reaching 80% branch coverage:**
- **Branch Coverage:** ___%
- **Tests added:**

### Final Overall Coverage

- **Line Coverage:** ___%
- **Branch Coverage:** ___%

---

## Part 3: checkoutBook() Implementation

### Test-Driven Development Process

**Number of tests from BlackBox assignment:** ___

**Implementation challenges:**
1.
2.

**All tests passing:** [Yes/No]

---

## Part 4: Reflection

**How did white-box testing differ from black-box testing?**

**Which approach do you find more effective? Why?**

**Would you prefer TDD or implementation first test later? Why?**
