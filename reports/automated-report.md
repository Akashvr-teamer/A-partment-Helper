# A-partment Helper — Automated QA Report

- Run time: `2026-10-11T23:35:52+05:30`
- Working directory: `C:\Users\MYPC\Downloads\A-partment-latest\A-partment-Helper-main`
- Java files discovered: 10
- SQL files discovered: 2

> Static scans only indicate that code/schema patterns exist. They do not prove that screens or database workflows work. Complete every relevant manual test in SCRATCHPAD.md.

| ID | Result | Details |
|---|---|---|
| AUTO-01 | **PASS** | Expected folders: src=True, sql=True, lib=True. Run from repository root. |
| AUTO-02 | **PASS** | src/Main.java found. |
| AUTO-03 | **PASS** | Found 10 Java source file(s). |
| AUTO-04 | **PASS** | PostgreSQL JDBC JAR found: postgresql-42.7.3.jar |
| AUTO-06 | **PASS** | sql/setup_postgres.sql found. |
| AUTO-07 | **WARN** | users indicator not found in SQL/source scan (static indicator only). |
| AUTO-08 | **PASS** | visitor/pass indicator found in SQL/source scan (static indicator only). |
| AUTO-09 | **PASS** | parking indicator found in SQL/source scan (static indicator only). |
| AUTO-10 | **PASS** | maintenance/complaints indicator found in SQL/source scan (static indicator only). |
| AUTO-11 | **PASS** | payments indicator found in SQL/source scan (static indicator only). |
| AUTO-12 | **PASS** | Primary-key declarations in SQL: 7. |
| AUTO-13 | **PASS** | Foreign-key/references declarations in SQL: 6. |
| AUTO-14 | **PASS** | Resident demo-account email found in SQL/README. |
| AUTO-15 | **PASS** | Worker demo-account email found in SQL/README. |
| AUTO-16 | **PASS** | visitor pass generation indicator found in Java source (does not prove runtime behavior). |
| AUTO-17 | **PASS** | gate entry/check-in indicator found in Java source (does not prove runtime behavior). |
| AUTO-18 | **PASS** | gate exit/check-out indicator found in Java source (does not prove runtime behavior). |
| AUTO-19 | **PASS** | parking allocation/release indicator found in Java source (does not prove runtime behavior). |
| AUTO-20 | **PASS** | maintenance complaint workflow indicator found in Java source (does not prove runtime behavior). |
| AUTO-21 | **PASS** | payment record workflow indicator found in Java source (does not prove runtime behavior). |
| AUTO-05 | **BLOCKED** | javac not found. Install/configure JDK 17+ and rerun. |

## Summary

- PASS: 19
- WARN: 1
- FAIL: 0
- BLOCKED: 1

## Required next actions
1. Fix any FAIL compilation result.
2. Review WARN results; some can be false negatives because names and patterns vary.
3. Run every manual case in `SCRATCHPAD.md` using a test database.
4. Record failures and evidence in the scratchpad. Do not claim testing is complete while any test is unchecked.
