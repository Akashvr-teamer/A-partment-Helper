# A-partment Helper — Student Code Explanation & Viva Guide

This guide is designed for **students learning the basics of Java** to easily explain the project, architecture, and code during demonstrations, vivas, and code reviews.

---

## 1. High-Level Architecture: MVC Pattern

The project is structured following the **MVC (Model-View-Controller)** design pattern. This is a standard software design practice that separates responsibilities:

```
[ View (Java Swing) ] 
       │  User clicks button / enters text
       ▼
[ Controller ] 
       │  Validates input, applies business logic
       ▼
[ DAO (Data Access Object) ] 
       │  Executes SQL queries via JDBC
       ▼
[ PostgreSQL Database (Supabase) ]
       │  Returns data
       ▼
[ Model (POJO) ]  ─── Passed back ───►  [ View displays data ]
```

### Why use MVC instead of writing SQL inside the GUI?
1. **Clean & Organized**: GUI classes focus only on display (buttons, tables, labels).
2. **Easy to Maintain**: If the database query changes, you only change the DAO file, not the UI.
3. **Reusability**: Multiple screens or controllers can reuse the same DAO methods.

---

## 2. The 4 Layers Explained in Simple Terms

### 1. Model Layer (`src/model/`)
- **What it is**: Plain Old Java Objects (**POJOs**) representing the entities in the system (`Resident`, `Worker`, `Admin`, `Complaint`, `Payment`, `Visitor`, `Parking`).
- **Core Java Principle: Encapsulation**:
  - All variables are declared `private` (e.g. `private int residentId;`).
  - Public **getters and setters** provide controlled access to read and update values.

### 2. DAO Layer (`src/model/dao/`)
- **What it is**: **Data Access Objects** that talk to the PostgreSQL database using **JDBC**.
- **Every DAO follows the same 5-step JDBC pattern**:
  1. **Define the SQL query** using `?` placeholders.
  2. **Get Connection**: `DBConnection.getConnection()`.
  3. **Prepare Statement**: `con.prepareStatement(sql)` (protects against SQL injection).
  4. **Set parameters**: `ps.setString(1, email); ps.setInt(2, id);`.
  5. **Execute query**:
     - `ps.executeQuery()` for `SELECT` (returns a `ResultSet`).
     - `ps.executeUpdate()` for `INSERT`, `UPDATE`, `DELETE` (returns number of rows affected).

### 3. Controller Layer (`src/controller/`)
- **What it is**: The mediator between the View and the DAO (`AuthController`, `ResidentController`, `WorkerController`, `VisitorController`, `AdminController`).
- **What it does**:
  1. Checks user input (e.g. "Is the email field blank?").
  2. Calls the appropriate DAO to fetch or save data.
  3. Returns a clean result or model object back to the View.

### 4. View Layer (`src/view/`)
- **What it is**: Java Swing graphical user interface (`LoginFrame`, `ResidentDashboard`, `WorkerDashboard`, `AdminDashboard`, `VisitorEntryFrame`, `InviteVisitorFrame`).
- **Key Swing components**:
  - `JFrame`: Main window.
  - `JPanel`: Container for grouping components.
  - `JButton`: Clickable buttons with `ActionListener`.
  - `JTable` & `DefaultTableModel`: Displays rows and columns of data.
  - `JOptionPane`: Standard popups for alerts and confirmations.

---

## 3. Step-by-Step Walkthrough of Core Features

### Feature 1: Login & Role Selection
- **Files**: [LoginFrame.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/LoginFrame.java), [AuthController.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/controller/AuthController.java)
- **Explanation**:
  1. User enters email and password, selects role (`RESIDENT`, `WORKER`, or `ADMIN`), and clicks **Login**.
  2. `AuthController.login()` checks if the email or password is empty.
  3. Depending on the role, it calls `residentDAO.authenticate()`, `workerDAO.authenticate()`, or `adminDAO.authenticate()`.
  4. If a matching record is found, it opens the corresponding dashboard (`ResidentDashboard`, `WorkerDashboard`, or `AdminDashboard`) and disposes of the login window.
  5. If invalid, it shows an error popup using `JOptionPane.showMessageDialog()`.

### Feature 2: Visitor Pass Generation & SHA-256 Hashing
- **Files**: [InviteVisitorFrame.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/InviteVisitorFrame.java), [ResidentController.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/controller/ResidentController.java), [CodeGenerator.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/util/CodeGenerator.java)
- **Explanation**:
  1. Resident fills in visitor name, phone, and selects whether the visitor has a vehicle.
  2. `ResidentController.createVisitorPass()` calls `CodeGenerator.generateCode()` to produce a 6-character alphanumeric pass code (e.g. `HFO123`).
  3. For security, the system does not store the raw code in the database. Instead, it computes a **SHA-256 cryptographic hash** using `CodeGenerator.sha256()`.
  4. The hash is saved in `visitors.access_hash` via `VisitorDAO.insert()`.
  5. The plaintext code is displayed once to the resident to share with the visitor.

### Feature 3: Gate Entry & Parking Allocation
- **Files**: [VisitorEntryFrame.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/VisitorEntryFrame.java), [VisitorController.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/controller/VisitorController.java), [ParkingDAO.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/model/dao/ParkingDAO.java)
- **Explanation**:
  1. Guard/worker enters the visitor's pass code.
  2. `VisitorController.processEntry()` computes the SHA-256 hash of the input and checks if a record exists with status `PENDING`.
  3. If the visitor brings a vehicle, `ParkingDAO.allocateVisitorParking()` queries for an available visitor slot (`status = 'AVAILABLE'`), sets it to `'OCCUPIED'`, and returns the slot number (e.g. `V-06`).
  4. A 3-character checkout exit code is generated (e.g. `TEL`), visitor status is updated to `ENTERED`, and the guard sees the assigned slot and exit code.

### Feature 4: Gate Exit & Parking Release
- **Files**: [VisitorEntryFrame.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/VisitorEntryFrame.java), [VisitorController.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/controller/VisitorController.java), [ParkingDAO.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/model/dao/ParkingDAO.java)
- **Explanation**:
  1. Guard enters the 3-character exit code (or original pass code).
  2. `VisitorController.processExit()` finds the entered visitor and marks them as `EXITED`.
  3. If the visitor had an allocated parking slot, `ParkingDAO.freeVisitorParking()` sets the slot back to `AVAILABLE` and clears the `visitor_id`.

### Feature 5: Maintenance Complaints Lifecycle
- **Files**: [ResidentDashboard.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/ResidentDashboard.java), [WorkerDashboard.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/view/WorkerDashboard.java), [ComplaintDAO.java](file:///home/anair/Desktop/Coding/A-partment-Helper/src/model/dao/ComplaintDAO.java)
- **Status Lifecycle**: `PENDING` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `COMPLETED`
- **Explanation**:
  1. Resident submits a complaint (e.g., Category: "Plumbing", Title: "Pipe Leak").
  2. The system checks for an available worker matching the category; if found, it auto-assigns the worker and sets status to `ASSIGNED`.
  3. Workers view complaints in `WorkerDashboard`.
  4. Worker marks status to `IN_PROGRESS` when work begins.
  5. Upon completion, worker marks `COMPLETED`, adds repair notes, and can generate an invoice for repairs.

---

## 4. Top Viva Questions & Model Answers

### Q1: What is JDBC and how does it connect to PostgreSQL?
> **Answer**: JDBC (Java Database Connectivity) is a Java API that connects Java applications to relational databases. In `DBConnection.java`, we first load the driver class `org.postgresql.Driver` using `Class.forName()`, and then establish the connection using `DriverManager.getConnection(url, user, password)`.

### Q2: Why did you use `PreparedStatement` instead of `Statement`?
> **Answer**:
> 1. **Security**: `PreparedStatement` uses parameterized queries (`?`), which automatically sanitizes user input and prevents **SQL Injection** attacks.
> 2. **Performance**: Prepared statements are pre-compiled by the database engine, making repeated execution faster.

### Q3: What is "try-with-resources" in Java?
> **Answer**: Introduced in Java 7, `try-with-resources` automatically closes any resource that implements `java.lang.AutoCloseable` (such as `Connection`, `PreparedStatement`, and `ResultSet`) when the try block ends, even if an exception occurs. This prevents database connection leaks without requiring manual `close()` calls in a `finally` block.

### Q4: What is the benefit of separating Model and DAO?
> **Answer**: A **Model** is a pure data carrier (POJO with attributes and getters/setters), while a **DAO** handles data persistence (SQL queries and database operations). This follows the **Single Responsibility Principle** from OOP.

### Q5: How is security handled for visitor access codes?
> **Answer**: Rather than storing plaintext pass codes in the database, the system hashes them with **SHA-256** (`CodeGenerator.sha256()`). When a visitor checks in, the entered code is hashed and compared against the stored hash in the database.

---

## 5. Verification Commands

To compile all files:
```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```

To run the automated QA checks:
```bash
python scripts/run_qa.py
```

To launch the desktop application:
```bash
java -cp "lib/*:out" Main
```
