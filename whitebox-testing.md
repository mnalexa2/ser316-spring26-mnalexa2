# White Box Testing Report - Assignment 3

**Student Name:** Michelle N. Alexander
**ASU ID:** mnalexa2
**Date:** 4 February 2026

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
- **Test case: countBooksByType(Book.BookType.FICTION, false) with one null book, mismatch book type, and unavailable fiction**


---

## Part 2: Code Coverage with JaCoCo

### Initial Coverage for Checkout.java

**Before adding tests:**
- **Line Coverage:** 48%
- **Branch Coverage:** 46%

### Coverage for countBooksByType()

**Before additional tests:**
- **Branch Coverage:** 75%

**After reaching 60% branch coverage:**
- **Branch Coverage:** 75%
- **Tests added: testCalculateFine_Branches() and testIsValidISBN_Branches()**

### Final Overall Coverage

- **Line Coverage:** 71%
- **Branch Coverage:** 67%

---

## Part 3: checkoutBook() Implementation

### Test-Driven Development Process

**Number of tests from BlackBox assignment:** 20

**Implementation challenges:**
1. getting assert values correct and managing correct return codes
2. correctly updating available copies

**All tests passing:** Yes

---

## Part 4: Reflection

**How did white-box testing differ from black-box testing?**
It was much easier and cleaner to implement since it was based on what code is being written and defined paths, rather than 
how the code should perform. It also helps with code efficiency to find bugs or unused code.

**Which approach do you find more effective? Why?**
For myself, I found the Whitebox testing more effective, even though it may be harder to meet requirements, 
I think the efficiency in the time it takes is valuable.

**Would you prefer TDD or implementation first test later? Why?**
I prefer the implementation first because I am a visual person so connecting the flow with what needs to be implemented is just 
more beneficial, but it is risky because you may do a lot of work before you realize something is missed. 
