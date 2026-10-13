# A-partment Helper — Automated QA Report

- Run time: `2026-10-13T12:15:43.402578+05:30`
- Working directory: `/home/anair/Desktop/Coding/A-partment-Helper`
- Java files discovered: 31
- SQL files discovered: 2

> Static scans only indicate that code/schema patterns exist. They do not prove that screens or database workflows work. Complete every relevant manual test in SCRATCHPAD.md.

| ID | Result | Details |
|---|---|---|
| AUTO-01 | **PASS** | Expected folders: src=True, sql=True, lib=True. Run from repository root. |
| AUTO-02 | **PASS** | src/Main.java found. |
| AUTO-03 | **PASS** | Found 31 Java source file(s). |
| AUTO-04 | **PASS** | PostgreSQL JDBC JAR found: postgresql-42.7.3.jar |
| AUTO-05 | **PASS** | Java compilation succeeds (javac). |
| AUTO-06 | **PASS** | sql/setup_postgres.sql found. |
| AUTO-07 | **PASS** | User & role tables (residents, workers, admins) detected. |
| AUTO-08 | **PASS** | visitor/pass indicator found in SQL/source scan (static indicator only). |
| AUTO-09 | **PASS** | parking indicator found in SQL/source scan (static indicator only). |
| AUTO-10 | **PASS** | maintenance/complaints indicator found in SQL/source scan (static indicator only). |
| AUTO-11 | **PASS** | payments indicator found in SQL/source scan (static indicator only). |
| AUTO-12 | **PASS** | Primary-key declarations in SQL: 7. |
| AUTO-13 | **PASS** | Foreign-key/references declarations in SQL: 7. |
| AUTO-14 | **PASS** | Resident demo-account email found in SQL/Java sources. |
| AUTO-15 | **PASS** | Worker demo-account email found in SQL/Java sources. |
| AUTO-16 | **PASS** | visitor pass generation indicator found in Java source. |
| AUTO-17 | **PASS** | gate entry/check-in indicator found in Java source. |
| AUTO-18 | **PASS** | gate exit/check-out indicator found in Java source. |
| AUTO-19 | **PASS** | parking allocation/release indicator found in Java source. |
| AUTO-20 | **PASS** | maintenance complaint workflow indicator found in Java source. |
| AUTO-21 | **PASS** | payment record workflow indicator found in Java source. |

## Summary

- PASS: 21
- WARN: 0
- FAIL: 0
- BLOCKED: 0

## Required next actions
1. Fix any FAIL compilation result.
2. Review WARN results; some can be false negatives because names and patterns vary.
3. Run every manual case in `SCRATCHPAD.md` using a test database.
4. Record failures and evidence in the scratchpad. Do not claim testing is complete while any test is unchecked.
