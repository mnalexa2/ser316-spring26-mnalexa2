# Code Review Checklist

**Reviewer Name:** Michelle N. Alexander
**Date:** 27 Jan 2026
**Branch:** Review

## Instructions
Review ALL source files (in main not test) in the project and identify defects using the categories below. Log at least 5 defects total:
- At least 1 from CS (Coding Standards)
- At least 1 from CG (Code Quality/General)
- At least 1 from FD (Functional Defects)
- Remaining can be from any category

## Review Categories

- **CS**: Coding Standards (naming conventions, formatting, style violations)
- **CG**: Code Quality/General (design issues, code smells, maintainability)
- **FD**: Functional Defects (logic errors, incorrect behavior, bugs)
- **MD**: Miscellaneous (documentation, comments, other issues)

## Defect Log

| Defect ID | File          | Line(s) | Category | Description                                                      | Severity |
|-----------|---------------|---------|----------|------------------------------------------------------------------|----------|
| 1         | Checkout.java | 312     | CS       | Improper variable naming                                         | low      |
| 2         | Checkout.java | 242     | FD       | Use of == for typeString                                         | high     |
| 3         | Patron.java   | 130     | CG       | Redundant method for suspended                                   | med      |
| 4         | Patron.java   | 151     | FD       | missing else block                                               | med      |
| 5         | Book.java     | 186     | FD       | set to 100 instead of no. copies                                 | high     |
| 6         | Book.java     | 9-11    | CS       | variables should be identified as final for immutability         | low      |
| 7         | Book.java     | 1-2     | MD       | unused import statements                                         | low      |
| 8         | Patron.java   | 96-108  | CG       | can replace switch statement with enhanced for better formatting | med      |
| 9         | Checkout.java | 282-85  | FD       | Array list not populated                                         | high     |
| 10        |               |         |          |                                                                  |          |

**Severity Levels:**
- **Critical**: Causes system failure, data corruption, or security issues
- **High**: Major functional defect or significant quality issue
- **Medium**: Moderate issue affecting maintainability or minor functional problem
- **Low**: Minor style issue or cosmetic problem

## Example Entry

| Defect ID | File          | Line(s) | Category | Description                                | Severity |
|-----------|---------------|---------|----------|--------------------------------------------|----------|
| 1 | Checkout.java | 17      | CS       | Variable bookList misleading - Map not List | Medium |
| 2 | Book.java     | 107     | FD       | Magic number 100 should be totalCopies      | High |

## Notes
- Be specific with line numbers
- Provide clear, actionable descriptions
- Consider: readability, maintainability, correctness, performance, security
- Focus on issues that impact code quality or functionality
