# A-partment Helper — Complete Test Scratchpad

Project features checked against the repository README: Java Swing + PostgreSQL/JDBC; resident and worker sign-in; resident dashboard; visitor pass generation; gate entry and exit; visitor parking; maintenance complaints; payment records. Use this file as the source of truth for test completion.

## How to mark
- `[ ]` Not tested
- `[x]` Passed — test was actually run and expected result observed
- `[!]` Failed — record the bug below
- `[-]` Blocked / feature not present — write why
Do not mark a test passed based on reading code alone. Run automated checks first, then work through every manual section. If a test does not apply, mark `[-]` and explain.

## 0. Automated checks
Run `python scripts/run_qa.py` from the repository root. Review `reports/automated-report.md`.
- [ ] AUTO-01 Correct project root detected (`src`, `sql`, `lib`)
- [ ] AUTO-02 `src/Main.java` found
- [ ] AUTO-03 Java source files discovered
- [ ] AUTO-04 PostgreSQL JDBC JAR found in `lib`
- [ ] AUTO-05 Java compilation succeeds
- [ ] AUTO-06 `sql/setup_postgres.sql` found
- [ ] AUTO-07 Expected table definitions checked
- [ ] AUTO-08 Primary-key declarations checked
- [ ] AUTO-09 Foreign-key declarations checked
- [ ] AUTO-10 Resident demo account seed checked
- [ ] AUTO-11 Worker demo account seed checked
- [ ] AUTO-12 Visitor-pass/hash-related implementation indicators checked
- [ ] AUTO-13 Gate entry and exit implementation indicators checked
- [ ] AUTO-14 Parking implementation indicators checked
- [ ] AUTO-15 Maintenance implementation indicators checked
- [ ] AUTO-16 Payment implementation indicators checked
- [ ] AUTO-17 Report generated and every warning reviewed

## 1. Setup and launch
- [ ] SETUP-01 Open the project in IntelliJ
- [ ] SETUP-02 JDK 17+ is selected
- [ ] SETUP-03 PostgreSQL JDBC driver is available to the project
- [ ] SETUP-04 Run `sql/setup_postgres.sql` against the intended test database
- [ ] SETUP-05 Setup script completes without SQL errors
- [ ] SETUP-06 Re-running setup script does not delete existing test records
- [ ] SETUP-07 Environment variables are configured for the test database
- [ ] SETUP-08 `src/Main.java` starts the application
- [ ] SETUP-09 Login window appears without an exception
- [ ] SETUP-10 Close and relaunch the application successfully
- [ ] SETUP-11 Empty/no-record tables do not crash the app
- [ ] SETUP-12 Database unavailable at startup gives a visible error rather than an unexplained crash

## 2. Login and role selection
Documented demo accounts: resident `akash@example.com` / `1234`; worker `worker@example.com` / `1234`.
- [ ] LOGIN-01 Resident demo login succeeds
- [ ] LOGIN-02 Resident login opens resident dashboard
- [ ] LOGIN-03 Worker demo login succeeds
- [ ] LOGIN-04 Worker login opens worker dashboard
- [ ] LOGIN-05 Wrong password is rejected
- [ ] LOGIN-06 Unknown email is rejected
- [ ] LOGIN-07 Blank email is handled
- [ ] LOGIN-08 Blank password is handled
- [ ] LOGIN-09 Leading/trailing spaces behave consistently
- [ ] LOGIN-10 Logout/back-to-login works, if implemented
- [ ] LOGIN-11 Closing a dashboard and reopening behaves correctly
- [ ] LOGIN-12 A resident session does not accidentally display worker dashboard controls
- [ ] LOGIN-13 A worker session does not accidentally display resident-only controls

## 3. Resident dashboard
- [ ] RES-01 Dashboard opens after resident login
- [ ] RES-02 Resident name/details are correct
- [ ] RES-03 Live parking total matches database data
- [ ] RES-04 Visitor total matches database data
- [ ] RES-05 Outstanding maintenance total matches database data
- [ ] RES-06 Counts refresh after related actions
- [ ] RES-07 Visitor pass/invitation screen opens
- [ ] RES-08 Maintenance complaint screen opens
- [ ] RES-09 Payment records screen opens
- [ ] RES-10 Navigation/back controls work
- [ ] RES-11 No-record/empty state is readable
- [ ] RES-12 Dashboard remains usable after repeated navigation

## 4. Visitor pass creation
- [ ] VIS-01 Open visitor pass form
- [ ] VIS-02 Create pass with valid visitor name/details
- [ ] VIS-03 A pass/code is generated and shown to the resident
- [ ] VIS-04 Pass is associated with the logged-in resident
- [ ] VIS-05 Only the hash is stored (inspect database row; do not rely on UI alone)
- [ ] VIS-06 Two passes generated in succession are distinct
- [ ] VIS-07 Empty visitor name is handled
- [ ] VIS-08 Invalid/missing required values are handled
- [ ] VIS-09 Pass creation survives app restart
- [ ] VIS-10 Resident can identify the correct pass after creation
- [ ] VIS-11 Pass validity/status is shown consistently, if displayed
- [ ] VIS-12 Any cancel/revoke feature works, if implemented

## 5. Gate entry and visitor check-in
- [ ] GATE-01 Open gate/visitor entry workflow as worker
- [ ] GATE-02 Valid, unused visitor code is accepted
- [ ] GATE-03 Invalid code is rejected
- [ ] GATE-04 Random/garbage code is rejected without crashing
- [ ] GATE-05 Already-used pass is rejected
- [ ] GATE-06 Invalid or expired pass is rejected, if expiry is implemented
- [ ] GATE-07 Successful check-in changes pass/visitor status correctly
- [ ] GATE-08 Check-in is associated with the correct resident
- [ ] GATE-09 Walk-in visitor without a vehicle checks in without parking allocation
- [ ] GATE-10 Vehicle visitor receives the first available visitor parking slot
- [ ] GATE-11 When all visitor slots are occupied, the app gives a clear result
- [ ] GATE-12 Repeating the same check-in does not create a duplicate active visit
- [ ] GATE-13 Worker can see the expected visitor/check-in information
- [ ] GATE-14 Resident dashboard reflects relevant visitor count/status changes

## 6. Gate exit and parking release
- [ ] EXIT-01 Find a currently checked-in visitor
- [ ] EXIT-02 Check out a visitor successfully
- [ ] EXIT-03 Visitor status changes to exited/completed
- [ ] EXIT-04 Assigned visitor parking slot becomes available again
- [ ] EXIT-05 A second exit attempt is handled cleanly
- [ ] EXIT-06 Exiting a visitor without a vehicle does not alter parking
- [ ] EXIT-07 Exiting one visitor does not release another visitor's slot
- [ ] EXIT-08 Resident dashboard reflects updated counts after exit
- [ ] EXIT-09 Exit changes persist after restarting the app

## 7. Parking
- [ ] PARK-01 Available visitor slots are shown correctly
- [ ] PARK-02 A checked-in vehicle visitor gets an available slot
- [ ] PARK-03 Assigned slot is no longer shown as available
- [ ] PARK-04 Two active visitors do not receive the same slot
- [ ] PARK-05 Walk-in visitor without a vehicle uses no slot
- [ ] PARK-06 No-slot scenario is handled without corrupting check-in data
- [ ] PARK-07 Exit frees the correct slot
- [ ] PARK-08 Freed slot can be assigned to a later visitor
- [ ] PARK-09 Resident sees the correct visitor parking information
- [ ] PARK-10 Parking state remains correct after app restart

## 8. Maintenance complaints
- [ ] MAINT-01 Resident opens maintenance screen
- [ ] MAINT-02 Resident submits a valid complaint
- [ ] MAINT-03 New complaint appears in the resident's list
- [ ] MAINT-04 Complaint is associated with the correct resident
- [ ] MAINT-05 Empty complaint is handled
- [ ] MAINT-06 Worker can view available complaints
- [ ] MAINT-07 Worker can claim a complaint
- [ ] MAINT-08 Claimed complaint status becomes Assigned
- [ ] MAINT-09 Worker changes status to In Progress
- [ ] MAINT-10 Worker changes status to Completed
- [ ] MAINT-11 Resident sees the updated status after refresh/reopen
- [ ] MAINT-12 Completed complaint is not incorrectly shown as outstanding
- [ ] MAINT-13 Status changes persist after app restart
- [ ] MAINT-14 Two complaints can be managed independently
- [ ] MAINT-15 Empty complaint list displays cleanly

## 9. Payment records
Note: this is a record-keeping feature; it does not process real money.
- [ ] PAY-01 Resident opens payment records
- [ ] PAY-02 Correct resident's payment records are displayed
- [ ] PAY-03 Outstanding/unpaid record is displayed correctly
- [ ] PAY-04 Marking a payment as paid asks for confirmation, if designed to
- [ ] PAY-05 Confirming marks the correct record as paid
- [ ] PAY-06 Cancelling confirmation leaves the record unchanged
- [ ] PAY-07 Paid state persists after app restart
- [ ] PAY-08 Repeated clicks do not corrupt the payment record
- [ ] PAY-09 Empty payment list displays cleanly
- [ ] PAY-10 Payment update refreshes relevant dashboard totals, if applicable

## 10. Database consistency (use a test database)
- [ ] DB-01 Tables exist after setup
- [ ] DB-02 Demo resident row exists
- [ ] DB-03 Demo worker row exists
- [ ] DB-04 Resident/visitor association is correct
- [ ] DB-05 Visitor pass status changes after check-in
- [ ] DB-06 Parking assignment links to the correct active visitor
- [ ] DB-07 Exit releases only the associated slot
- [ ] DB-08 Complaint status matches what the UI displays
- [ ] DB-09 Payment status matches what the UI displays
- [ ] DB-10 Saved records persist after app restart
- [ ] DB-11 No orphaned visitor/parking/complaint/payment rows appear during normal workflows
- [ ] DB-12 App handles an empty table without error
- [ ] DB-13 Failed action does not leave half-completed data (e.g., check-in without expected parking state)

## 11. UI and usability (manual only)
- [ ] UI-01 Resident dashboard elements align properly
- [ ] UI-02 Worker dashboard elements align properly
- [ ] UI-03 Buttons and labels are not clipped
- [ ] UI-04 Tables handle long names and text
- [ ] UI-05 Empty tables have a useful empty state
- [ ] UI-06 Error messages are understandable
- [ ] UI-07 Success messages appear for successful actions
- [ ] UI-08 Back/navigation buttons return to the expected screen
- [ ] UI-09 Resize window and confirm content remains usable
- [ ] UI-10 Test at laptop resolution (e.g. 1366×768 or 1920×1080)
- [ ] UI-11 Repeatedly switch screens; no duplicated windows or stuck UI
- [ ] UI-12 Database operations do not make the UI unresponsive for an unreasonable time
- [ ] UI-13 Text is readable at normal Windows display scaling
- [ ] UI-14 Keyboard tab order and Enter/Escape behavior are acceptable

## 12. Regression pass
After fixing any bug, repeat the affected test and related workflow.
- [ ] REG-01 Login for resident and worker
- [ ] REG-02 Create visitor pass → gate entry → parking allocation → gate exit → slot release
- [ ] REG-03 Submit complaint → worker claim → In Progress → Completed → resident verifies
- [ ] REG-04 View payment → mark paid → reopen app → verify paid state
- [ ] REG-05 Re-run automatic test script
- [ ] REG-06 Every failed test has a bug-log entry
- [ ] REG-07 Every blocked/N/A test has a reason
- [ ] REG-08 No unchecked test remains before declaring testing complete

## Execution log
| Date/time | Tester | Test IDs / section | Result | Evidence / notes |
|---|---|---|---|---|
| | | | | |

## Bug log
| Bug ID | Test ID | Steps to reproduce | Expected result | Actual result | Severity | Status |
|---|---|---|---|---|---|---|
| | | | | | | |
