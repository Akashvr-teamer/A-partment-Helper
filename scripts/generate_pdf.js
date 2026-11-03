import { createRequire } from "module";
const req = createRequire("/tmp/pdf-test/package.json");
const PDFDocument = req("pdfkit");
import fs from "fs";
import path from "path";

const outputPath = path.resolve(process.cwd(), "A-partment-Helper-Explanation-Guide.pdf");

// Initialize PDF with precise margins
const doc = new PDFDocument({
  size: "A4",
  margins: { top: 40, bottom: 42, left: 45, right: 45 },
  bufferPages: true,
  autoFirstPage: true
});

const writeStream = fs.createWriteStream(outputPath);
doc.pipe(writeStream);

// Register clean typography (Roboto & JetBrainsMono)
const fontDir = "/usr/share/fonts/TTF";
doc.registerFont("AppFont", path.join(fontDir, "Roboto-Regular.ttf"));
doc.registerFont("AppFont-Medium", path.join(fontDir, "Roboto-Medium.ttf"));
doc.registerFont("AppFont-Bold", path.join(fontDir, "Roboto-Bold.ttf"));
doc.registerFont("AppFont-Italic", path.join(fontDir, "Roboto-Italic.ttf"));
doc.registerFont("CodeFont", path.join(fontDir, "JetBrainsMonoNerdFont-Regular.ttf"));
doc.registerFont("CodeFont-Bold", path.join(fontDir, "JetBrainsMonoNerdFont-Bold.ttf"));

// Palette
const primaryColor = "#0F2942";    // Deep Navy
const secondaryColor = "#0284C7";  // Sky Blue
const textDark = "#1E293B";        // Slate dark
const textMuted = "#64748B";       // Slate muted
const bgCode = "#F8FAFC";          // Code block bg
const borderCode = "#E2E8F0";      // Code block border
const accentGreen = "#059669";     // Emerald
const accentAmber = "#D97706";     // Amber

function checkPageSpace(doc, neededHeight) {
  if (doc.y + neededHeight > doc.page.height - 50) {
    doc.addPage();
  }
}

function addTitleBanner(doc) {
  doc.rect(45, 40, 505, 5).fill(primaryColor);
  doc.y = 52;

  doc.font("AppFont-Bold").fontSize(19).fillColor(primaryColor)
     .text("A-PARTMENT HELPER", { align: "center" });

  doc.moveDown(0.12);
  doc.font("AppFont-Medium").fontSize(10.5).fillColor(secondaryColor)
     .text("Comprehensive Codebase Explanation, Java Syllabus Reference & Viva Guide", { align: "center" });

  doc.moveDown(0.18);
  doc.font("AppFont").fontSize(8).fillColor(textMuted)
     .text("TECH STACK: Java 17/21 • Java Swing (MVC GUI) • PostgreSQL / Supabase • Academic Modules 1 – 4", { align: "center" });

  doc.moveDown(0.35);
  doc.moveTo(45, doc.y).lineTo(550, doc.y).lineWidth(0.75).strokeColor("#E2E8F0").stroke();
  doc.moveDown(0.4);
}

function addPartHeader(doc, partTitle, description) {
  checkPageSpace(doc, 60);
  doc.moveDown(0.45);
  const startY = doc.y;
  doc.rect(45, startY, 505, 22).fill(primaryColor);
  doc.font("AppFont-Bold").fontSize(10.5).fillColor("#FFFFFF")
     .text(partTitle.toUpperCase(), 55, startY + 5.5);
  doc.y = startY + 26;

  if (description) {
    doc.font("AppFont-Italic").fontSize(8).fillColor(textMuted)
       .text(description, 45, doc.y, { align: "left", lineGap: 2 });
    doc.moveDown(0.25);
  }
}

function addSectionHeader(doc, title) {
  checkPageSpace(doc, 45);
  doc.moveDown(0.35);
  const y = doc.y;
  doc.rect(45, y, 3.5, 15).fill(secondaryColor);
  doc.font("AppFont-Bold").fontSize(11).fillColor(primaryColor)
     .text(`   ${title}`, 45, y + 2, { align: "left" });
  doc.moveDown(0.25);
}

function addSubHeader(doc, title) {
  checkPageSpace(doc, 32);
  doc.moveDown(0.25);
  doc.font("AppFont-Bold").fontSize(9.5).fillColor("#0F172A")
     .text(title, 45, doc.y, { align: "left" });
  doc.moveDown(0.15);
}

function addParagraph(doc, text) {
  checkPageSpace(doc, 22);
  doc.font("AppFont").fontSize(8.5).fillColor(textDark)
     .text(text, 45, doc.y, { align: "left", lineGap: 2.5 });
  doc.moveDown(0.25);
}

function addBullet(doc, boldLabel, text) {
  checkPageSpace(doc, 20);
  const currentY = doc.y;
  doc.font("AppFont-Bold").fontSize(8.5).fillColor(secondaryColor).text("• ", 50, currentY);
  doc.font("AppFont-Bold").fontSize(8.5).fillColor(textDark).text(boldLabel + ": ", 60, currentY, { continued: true });
  doc.font("AppFont").fontSize(8.5).fillColor(textDark).text(text, { align: "left", lineGap: 2 });
  doc.moveDown(0.18);
}

function addCodeBlock(doc, code) {
  doc.font("CodeFont").fontSize(7);
  const lines = code.split("\n");
  const lineHeight = 9.5;
  const padding = 6;
  const boxHeight = (lines.length * lineHeight) + (padding * 2);

  checkPageSpace(doc, boxHeight + 8);
  const startY = doc.y;
  doc.rect(45, startY, 505, boxHeight).fillAndStroke(bgCode, borderCode);
  
  doc.fillColor("#0F172A");
  let lineY = startY + padding;
  for (const line of lines) {
    doc.text(line, 55, lineY, { lineBreak: false });
    lineY += lineHeight;
  }
  doc.y = startY + boxHeight + 5;
}

function addCallout(doc, title, text, type = "info") {
  const barColor = type === "success" ? accentGreen : (type === "warning" ? accentAmber : secondaryColor);
  const bgColor = type === "success" ? "#ECFDF5" : (type === "warning" ? "#FFFBEB" : "#F0F9FF");
  
  doc.font("AppFont").fontSize(8);
  const textHeight = doc.heightOfString(text, { width: 480, lineGap: 2 }) + 22;
  checkPageSpace(doc, textHeight + 8);
  
  const boxY = doc.y;
  doc.rect(45, boxY, 505, textHeight).fill(bgColor);
  doc.rect(45, boxY, 3.5, textHeight).fill(barColor);
  
  doc.font("AppFont-Bold").fontSize(8.5).fillColor(barColor).text(title, 55, boxY + 5);
  doc.font("AppFont").fontSize(8).fillColor(textDark).text(text, 55, boxY + 16, { width: 485, align: "left", lineGap: 2 });
  doc.y = boxY + textHeight + 5;
}

function addTable(doc, headers, rows, colWidths) {
  checkPageSpace(doc, 45);
  const startX = 45;
  const rowHeight = 17;

  // Header
  const headerY = doc.y;
  doc.rect(startX, headerY, 505, rowHeight).fill(primaryColor);
  let curX = startX;
  doc.font("AppFont-Bold").fontSize(7.5).fillColor("#FFFFFF");
  for (let i = 0; i < headers.length; i++) {
    doc.text(headers[i], curX + 5, headerY + 4, { width: colWidths[i] - 10, align: "left" });
    curX += colWidths[i];
  }
  doc.y = headerY + rowHeight;

  // Rows
  let isAlt = false;
  for (const row of rows) {
    let maxHeight = rowHeight;
    doc.font("AppFont").fontSize(7.5);
    for (let i = 0; i < row.length; i++) {
      const h = doc.heightOfString(row[i], { width: colWidths[i] - 10, lineGap: 1.5 }) + 6;
      if (h > maxHeight) maxHeight = h;
    }

    checkPageSpace(doc, maxHeight);
    const rowY = doc.y;
    doc.rect(startX, rowY, 505, maxHeight).fillAndStroke(isAlt ? "#F8FAFC" : "#FFFFFF", "#E2E8F0");

    let cellX = startX;
    for (let i = 0; i < row.length; i++) {
      doc.font(i === 0 ? "AppFont-Bold" : "AppFont").fontSize(7.5).fillColor(textDark)
         .text(row[i], cellX + 5, rowY + 3.5, { width: colWidths[i] - 10, align: "left", lineGap: 1.5 });
      cellX += colWidths[i];
    }
    doc.y = rowY + maxHeight;
    isAlt = !isAlt;
  }
  doc.moveDown(0.3);
}

// ==========================================
// DOCUMENT GENERATION
// ==========================================

addTitleBanner(doc);

// ==========================================
// PART I: HIGH-LEVEL ARCHITECTURE
// ==========================================
addPartHeader(doc, "Part I: System Overview & Architecture", "The Model-View-Controller design pattern and system communication flow");

addParagraph(doc, "The A-partment Helper system is an enterprise-grade desktop society management application engineered in Java using the Model-View-Controller (MVC) architectural pattern. MVC cleanly separates visual user presentation from application business rules, database queries, and data models.");

addCodeBlock(doc, 
`+-----------------------------------------------------------------------------------------+
|                                 VIEW LAYER (Java Swing)                                 |
|  WelcomeFrame | LoginFrame | ResidentDashboard | WorkerDashboard | AdminDashboard ...    |
+-----------------------------------------------------------------------------------------+
                                             |
                   User Actions (Button Click, Form Input, Event Trigger)
                                             v
+-----------------------------------------------------------------------------------------+
|                               CONTROLLER LAYER (Business Logic)                         |
|  AuthController | ResidentController | WorkerController | VisitorController | AdminCtrl  |
|  - Input Validation | Auto-Assignment Algorithm | SHA-256 Hashing | Fee Calculations    |
+-----------------------------------------------------------------------------------------+
                            |                                         ^
                JDBC Parameterized Queries                   ResultSets Mapped into
                  (PreparedStatements)                        Encapsulated POJOs
                            v                                         |
+------------------------------------------+       +--------------------------------------+
|       DATABASE LAYER (PostgreSQL)        |       |          MODEL LAYER (POJOs)         |
|  Cloud-hosted Supabase instance with     |       | Resident, Worker, Complaint, Payment,|
|  residents, complaints, payments, etc.   |       | Visitor, Parking, Metrics            |
+------------------------------------------+       +--------------------------------------+`);

addSubHeader(doc, "Architectural Benefits of MVC:");
addBullet(doc, "Decoupled Presentation", "Swing View components contain no database connection strings or SQL queries. Views strictly collect user input and render visual components.");
addBullet(doc, "Maintainable Business Logic", "Controller classes encapsulate business decisions, validation, and auto-assignment algorithms. Updating SQL queries or validation rules does not affect the UI design.");
addBullet(doc, "Data Integrity & Encapsulation", "Model classes (POJOs) encapsulate database records with private fields and public accessors, preventing unauthorized side effects.");

// ==========================================
// PART II: FILE-BY-FILE CODEBASE BREAKDOWN
// ==========================================
addPartHeader(doc, "Part II: Complete File-by-File Codebase Explanation (All 24 Files)", "Detailed breakdown of the role, responsibilities, and mechanics of every source file");

addSectionHeader(doc, "1. Entry Point & Core Configuration");
addBullet(doc, "src/Main.java", "The application bootstrapper. Configures OS-level font anti-aliasing via System.setProperty('awt.useSystemAAFontSettings', 'on') and 'swing.aatext' for sharp typography. Safely initializes WelcomeFrame on the Swing Event Dispatch Thread (EDT) using SwingUtilities.invokeLater().");

addSectionHeader(doc, "2. Database & Utility Classes");
addBullet(doc, "src/database/DBConnection.java", "Centralized database factory. Dynamically loads the PostgreSQL JDBC driver ('org.postgresql.Driver'), securely encapsulates Supabase credentials (URL, USER, PASSWORD), and provides the getConnection() factory method returning live java.sql.Connection instances.");
addBullet(doc, "src/util/CodeGenerator.java", "Cryptographic security utility. Generates 4-character random visitor pass codes (e.g., 'K7M2') and 3-character checkout codes ('7B2') using SecureRandom. Implements sha256(String) using Java's MessageDigest to hash pass codes so plaintext codes are never stored in the database.");

addSectionHeader(doc, "3. Model Layer (src/model/ POJOs)");
addBullet(doc, "src/model/Resident.java", "Encapsulates apartment resident data: residentId, fullName, email, phone, apartmentNo, parkingSlot, and account status.");
addBullet(doc, "src/model/Worker.java", "Encapsulates maintenance staff: workerId, fullName, workerType (Plumbing, Electrical, Carpentry, Cleaning), phone, and activeComplaintsCount for workload balancing.");
addBullet(doc, "src/model/Admin.java", "Encapsulates administrative accounts with credentials, email, and administrative role privileges.");
addBullet(doc, "src/model/Complaint.java", "Encapsulates maintenance tickets: complaintId, residentId, assignedWorkerId, title, category, description, status (PENDING, ASSIGNED, IN_PROGRESS, COMPLETED), and resolutionNote.");
addBullet(doc, "src/model/Payment.java", "Encapsulates society billing and invoices: paymentId, residentId, title, amount, status (PENDING, PAID), dueDate, paidDate, and paymentMethod.");
addBullet(doc, "src/model/Visitor.java", "Encapsulates visitor passes: visitorId, residentId, name, phone, hasVehicle, parkingSlot, accessHash (SHA-256), exitCode, and entry/exit timestamps.");
addBullet(doc, "src/model/Parking.java", "Encapsulates physical parking bays: parkingId, slotNumber (R-xx for residents, V-xx for visitors), slotType, status (AVAILABLE, OCCUPIED), assigned residentId, or visitorId.");
addBullet(doc, "src/model/ResidentMetrics.java", "Aggregates summary statistics for a resident's dashboard (pending bills count, outstanding balance, open complaints, visitors today).");
addBullet(doc, "src/model/SocietyMetrics.java", "Aggregates macro KPIs for the administrative dashboard (total residents, active staff, open complaints, monthly collections, occupied parking).");

addSectionHeader(doc, "4. Controller Layer (src/controller/ Business Logic & JDBC)");
addBullet(doc, "src/controller/AuthController.java", "Authenticates login credentials across residents, workers, and admins. Enforces password checks, handles session routing to appropriate dashboard windows, and manages clean logout.");
addBullet(doc, "src/controller/ResidentController.java", "Coordinates resident operations: loads resident profile, aggregates dashboard metrics, creates visitor passes with SHA-256 hashing, registers complaints with auto-worker assignment, and processes bill payments.");
addBullet(doc, "src/controller/WorkerController.java", "Coordinates technician workflows: retrieves assigned and category-specific open tickets, claims unassigned complaints, updates status to IN_PROGRESS, records completion notes, and generates automated repair fee invoices.");
addBullet(doc, "src/controller/VisitorController.java", "Coordinates security gate check-in/out: verifies pass codes by hashing and matching access_hash, dynamically allocates visitor parking slots (V-01 to V-10), generates exit codes, and deallocates parking slots upon exit.");
addBullet(doc, "src/controller/AdminController.java", "Coordinates society administration: computes society-wide KPIs, executes full CRUD for residents and staff, synchronizes assigned resident parking slots, manually or automatically assigns complaints, and generates custom resident invoices.");

addSectionHeader(doc, "5. View Layer (src/view/ Java Swing User Interface)");
addBullet(doc, "src/view/WelcomeFrame.java", "The landing window with modern buttons directing users to either 'Login to Portal' or 'Visitor Entry / Exit' (security desk).");
addBullet(doc, "src/view/LoginFrame.java", "Authentication window featuring a role selector (RESIDENT, WORKER, ADMIN), email, and password fields, delegating validation to AuthController.");
addBullet(doc, "src/view/ResidentDashboard.java", "Multi-tab resident portal: Overview (KPI cards, quick actions), Visitors (invitation modal, past visitors), Complaints (ticket submission form, live status), Bills (unpaid bills with 'Pay Now' flow).");
addBullet(doc, "src/view/WorkerDashboard.java", "Technician workbench: active jobs queue, ticket claiming, status transitions (IN_PROGRESS, COMPLETED), repair note entry, and billing dialog.");
addBullet(doc, "src/view/AdminDashboard.java", "Administrative control panel: society overview KPIs, resident management with Add/Edit/Delete modals, worker workload balancer, complaint triage with auto-assign, and financial ledger.");
addBullet(doc, "src/view/VisitorEntryFrame.java", "Security guard workstation with two tabs: Gate Check-In (validates 4-character pass, displays allocated parking slot and exit code) and Gate Check-Out (releases visitor and parking slot).");
addBullet(doc, "src/view/InviteVisitorFrame.java", "Modal dialog launched from ResidentDashboard allowing residents to register a guest, select vehicle requirements, and display the generated 4-character pass code.");

// ==========================================
// PART III: JAVA SYLLABUS MAPPING (MODULES 1-4)
// ==========================================
addPartHeader(doc, "Part III: Comprehensive Java Syllabus & Academic Concept Mapping", "Module-by-Module mapping (Modules 1 – 4) across Core Java, OOP, Design Patterns & JDBC");

addSectionHeader(doc, "Module 1: Introduction to Java & OOP Concepts");
addSubHeader(doc, "1. Structure of a Simple Java Program & Execution Environment");
addBullet(doc, "Entry Point", "Main.java defines public static void main(String[] args), the standardized JVM entry point.");
addBullet(doc, "Compiler & Bytecode", "Source code is compiled by javac into architecture-neutral .class bytecode files in out/.");
addBullet(doc, "Java Virtual Machine (JVM)", "The JVM interprets bytecode using java -cp 'lib/*:out' Main, resolving classpath dependencies like lib/postgresql-42.7.3.jar.");

addSubHeader(doc, "2. Primitive Data Types vs. Wrapper Types, Autoboxing & Casting");
addBullet(doc, "Primitive Types", "int for entity IDs and counters; double for financial amounts (Payment.amount); boolean for flags (hasVehicle); char and byte[] for SHA-256 byte digests in CodeGenerator.");
addBullet(doc, "Wrapper Types & Nullability", "Integer and Double wrappers are utilized for nullable database foreign keys: Integer workerId in Complaint.java (null when unassigned), Integer residentId in Parking.java, and Double repairFee in WorkerController.java.");
addBullet(doc, "Autoboxing & Unboxing", "In ResidentController.java, SQL nullable columns are mapped using: Integer workerId = rs.getObject('worker_id') != null ? rs.getInt('worker_id') : null;");
addBullet(doc, "Type Casting", "Graphics2D g2 = (Graphics2D) g.create(); in ResidentDashboard.java for custom painting; (DefaultTableModel) table.getModel() for table data manipulation.");

addSubHeader(doc, "3. Arrays, Strings, Vector, Operators & Control Flow");
addBullet(doc, "Arrays", "Table column headers (String[] cols = {'ID', 'Title', 'Status'};) and table row arrays (model.addRow(new Object[]{...});).");
addBullet(doc, "Strings & StringBuilder", "StringBuilder used in CodeGenerator for thread-safe random code assembly; String.format('%.2f', amount) for currency; methods like isBlank(), trim(), equals().");
addBullet(doc, "Vector Class", "While modern collections (ArrayList) are used in controllers, Swing's DefaultTableModel internally uses java.util.Vector to manage rows and column names.");
addBullet(doc, "Control Flow", "Ternary operator (hasVehicle ? 'YES' : 'NO'); switch statements in AuthController for role dispatch; while (rs.next()) for database cursor traversal; enhanced for loops (for (Complaint c : list)).");

addSectionHeader(doc, "Module 2: Polymorphism, Inheritance & Advanced Class Features");
addSubHeader(doc, "1. Polymorphism & Method Overloading");
addBullet(doc, "Compile-Time Polymorphism", "WorkerController overloads completeComplaint(): a 4-parameter version for standard closing, and a 6-parameter version that simultaneously records notes and generates a billing invoice.");
addBullet(doc, "Constructor Overloading", "Resident.java and Worker.java implement both default no-arg constructors (new Worker()) and fully parameterized constructors for instantiation flexibility.");

addSubHeader(doc, "2. Static Members, Final Variables & Inner Classes");
addBullet(doc, "Static Methods & State", "DBConnection.getConnection() and CodeGenerator.generateCode() are static utility methods callable without object instantiation. DBConnection holds static connection credentials.");
addBullet(doc, "Final Constants", "private static final String CHARS in CodeGenerator and private static final String URL in DBConnection enforce immutability.");
addBullet(doc, "Static Nested Classes", "AdminDashboard.WorkerItem and ResidentItem are static nested helper classes used in dropdown selectors.");
addBullet(doc, "Anonymous Inner Classes", "Mouse adapters for table click detection (table.addMouseListener(new MouseAdapter() { public void mouseClicked(...) { ... } })) and custom table renderers.");

addSubHeader(doc, "3. Inheritance, Method Overriding & Dynamic Dispatch");
addBullet(doc, "Superclass & Subclass Hierarchy", "javax.swing.JFrame is the Superclass. All 7 GUI views (AdminDashboard, ResidentDashboard, WorkerDashboard, LoginFrame, etc.) are Subclasses extending JFrame.");
addBullet(doc, "Method Overriding (@Override)", "Worker.java and Resident.java override Object.toString() so JComboBox components display formatted names and IDs. Table cell renderers override getTableCellRendererComponent().");
addBullet(doc, "Dynamic Method Dispatch", "When Swing repaints components, it invokes overridden paintIcon() and render methods dynamically at runtime through base class/interface references.");
addBullet(doc, "Constructor Execution Order", "When new ResidentDashboard() is invoked, Java automatically executes the superclass JFrame constructor before executing ResidentDashboard constructor body.");

addSectionHeader(doc, "Module 3: Packages, Interfaces, Exception Handling & Design Patterns");
addSubHeader(doc, "1. Package Architecture & CLASSPATH");
addBullet(doc, "Package Organization", "The codebase is cleanly separated into 5 packages: package model, package controller, package view, package database, and package util.");
addBullet(doc, "Access Protection", "public classes/methods for APIs; private fields for POJOs; protected methods inherited from JFrame.");

addSubHeader(doc, "2. Interfaces vs. Abstract Classes");
addBullet(doc, "Interface Implementation", "SwingNavIcon implements javax.swing.Icon. JDBC interfaces (Connection, PreparedStatement, ResultSet) in java.sql are interfaces whose implementations are supplied by the PostgreSQL driver.");
addBullet(doc, "Abstract Classes", "java.awt.Component and javax.swing.JComponent are root abstract classes for all UI elements. java.awt.event.MouseAdapter is an abstract adapter class.");

addSubHeader(doc, "3. Exception Handling Architecture");
addBullet(doc, "Checked Exceptions", "SQLException and ClassNotFoundException must be handled or declared. Handled via try-catch blocks and declared with throws SQLException in controller methods.");
addBullet(doc, "Unchecked Exceptions", "Validation logic explicitly throws IllegalArgumentException (e.g., duplicate email) or NumberFormatException (e.g., negative billing amount).");
addBullet(doc, "Try-with-Resources", "Used across every single JDBC method to automatically close Connection, PreparedStatement, and ResultSet instances, guaranteeing zero memory or connection pool leaks.");

addSubHeader(doc, "4. Design Patterns: Singleton & Adapter");
addBullet(doc, "Singleton Pattern", "DBConnection.java acts as a centralized connection provider managing static database parameters and connection lifecycles.");
addBullet(doc, "Adapter Pattern", "MouseAdapter adapts the 5-method MouseListener interface, allowing AdminDashboard to implement only mouseClicked(). SwingNavIcon adapts procedural 2D vector drawing to Swing's Icon interface.");

addSectionHeader(doc, "Module 4: SOLID Principles, Swing GUI & JDBC");
addSubHeader(doc, "1. SOLID Principles in Practice");
addBullet(doc, "S - Single Responsibility", "model/ only stores state; controller/ only handles business logic and DB; view/ only renders GUI; DBConnection only manages connection lifecycles.");
addBullet(doc, "O - Open/Closed Principle", "New complaint categories (e.g., 'Elevator') or payment types can be added without modifying the core controller logic or database schema.");
addBullet(doc, "L - Liskov Substitution", "Any view subclassing JFrame can be substituted wherever a top-level Swing window is expected.");
addBullet(doc, "I - Interface Segregation", "Using MouseAdapter prevents classes from being forced to implement dummy methods for mouseEntered, mouseExited, etc.");
addBullet(doc, "D - Dependency Inversion", "Views depend on high-level controller abstractions (workerController.getAllComplaints()) rather than low-level JDBC query implementations.");

addSubHeader(doc, "2. Swing GUI Fundamentals & Layout Managers");
addBullet(doc, "AWT vs. Swing", "AWT provides underlying OS text anti-aliasing; lightweight Swing components (JFrame, JPanel, JButton, JTable, JScrollPane) provide flexible cross-platform UI.");
addBullet(doc, "Layout Managers Used", "BorderLayout for root frames; CardLayout for in-place tab switching without opening new OS windows; GridLayout for dashboard KPI cards; BoxLayout for vertical sidebars; FlowLayout for action button bars.");

addSubHeader(doc, "3. Event Handling & Delegation Event Model");
addBullet(doc, "Delegation Mechanism", "Event sources (JButton, JTable) generate events (ActionEvent, MouseEvent) that are delegated to registered listeners (ActionListener, MouseListener).");
addBullet(doc, "Lambda Listeners", "Modern concise lambda syntax: loginBtn.addActionListener(e -> handleLogin());");
addBullet(doc, "Thread Safety (EDT)", "Application is launched safely on the Swing Event Dispatch Thread (EDT) via SwingUtilities.invokeLater() in Main.java.");

addSubHeader(doc, "4. Database Applications using JDBC");
addBullet(doc, "Full CRUD Operations", "Create: INSERT INTO residents, complaints; Read: SELECT with JOINs (joining complaints with residents and workers); Update: UPDATE complaints SET status = 'COMPLETED'; Delete: DELETE FROM residents WHERE resident_id = ?.");
addBullet(doc, "ResultSet Traversal", "rs.next() with strongly-typed accessors: rs.getInt(), rs.getString(), rs.getDouble(), rs.getTimestamp().");
addBullet(doc, "SQL Injection Prevention", "Exclusive use of PreparedStatement with parameterized placeholders (?) across every single controller method.");

addSubHeader(doc, "5. Quick-Reference OOP & Academic Concepts Summary Table");
addTable(doc, 
  ["Concept", "Definition & Academic Meaning", "Concrete Project Implementation"],
  [
    ["Encapsulation", "Data hiding with private fields + public getters/setters.", "All src/model/ POJOs; private credentials in DBConnection.java."],
    ["Inheritance", "Deriving new classes from superclasses (extends).", "All 7 GUI views extend JFrame; cell renderers extend DefaultTableCellRenderer."],
    ["Polymorphism (Overload)", "Same method name with distinct parameter signatures.", "WorkerController.completeComplaint() (4 vs 6 params); Model constructors."],
    ["Polymorphism (Override)", "Redefining superclass behavior (@Override).", "toString() in Worker & Resident; getTableCellRendererComponent() in tables."],
    ["Interface Polymorphism", "Programming to abstract interface contracts.", "List<T> = new ArrayList<>(); JDBC Connection, PreparedStatement, ResultSet."],
    ["Abstraction", "Exposing clean APIs while hiding low-level complexity.", "Controllers abstract SQL/DB from Views; CodeGenerator abstracts crypto hashing."],
    ["Singleton Pattern", "Single global access point for resource management.", "DBConnection provides centralized static connection factory getConnection()."],
    ["Adapter Pattern", "Converting incompatible interfaces into expected types.", "MouseAdapter adapts MouseListener; SwingNavIcon adapts vector 2D drawing."]
  ],
  [95, 185, 225]
);

// ==========================================
// PART IV: WORKFLOWS & LIFECYCLES
// ==========================================
addPartHeader(doc, "Part IV: End-to-End Workflows & Security Lifecycles", "Detailed execution flows for authentication, visitor tracking, parking, complaints, and billing");

addSubHeader(doc, "1. Role-Based Authentication & Session Handoff");
addParagraph(doc, "LoginFrame captures the user's email, password, and chosen role (RESIDENT, WORKER, ADMIN). AuthController.login() queries the respective table. Upon successful match, it instantiates the corresponding dashboard (passing authenticated ID) and disposes of the login frame. Invalid credentials trigger an error dialog.");

addSubHeader(doc, "2. Visitor Pass Generation & SHA-256 Hashing");
addParagraph(doc, "When a resident invites a guest via InviteVisitorFrame, ResidentController.createVisitorPass() generates a 4-character random code (e.g. 'A9K2') via CodeGenerator.generateCode(). To prevent exposure in case of database leaks, the raw code is hashed using SHA-256 before being stored in visitors.access_hash. The plaintext code is displayed once to the resident to share with the guest.");

addSubHeader(doc, "3. Gate Check-In & Dynamic Parking Bay Allocation");
addParagraph(doc, "At the gate, the guard enters the visitor's 4-character code into VisitorEntryFrame. VisitorController.processEntry() hashes the input with SHA-256 and searches for a PENDING record. If the visitor brings a vehicle, allocateVisitorParking() finds an available visitor parking bay (V-01 to V-10), marks it OCCUPIED, links it to the visitor, and generates a 3-character checkout code ('TEL'). Status transitions to ENTERED.");

addSubHeader(doc, "4. Gate Check-Out & Automatic Parking Bay Deallocation");
addParagraph(doc, "Upon departure, the guard enters the 3-character exit code. VisitorController.processExit() locates the entered visitor, sets status to EXITED, records exit timestamp, and automatically resets the assigned visitor parking bay back to AVAILABLE.");

addSubHeader(doc, "5. Maintenance Complaint Lifecycle & Auto-Assignment Algorithm");
addParagraph(doc, "The maintenance ticket lifecycle follows a strict state transition: PENDING -> ASSIGNED -> IN_PROGRESS -> COMPLETED.");
addBullet(doc, "Triage & Auto-Assignment", "When a resident submits a complaint (e.g. 'Plumbing'), ResidentController.fileComplaint() queries all active plumbing workers, counts their active tickets, and automatically assigns the technician with the lightest workload.");
addBullet(doc, "Execution & Status Update", "The technician opens WorkerDashboard, claims or starts work, transitioning status to IN_PROGRESS.");
addBullet(doc, "Completion & Billing", "Upon repair completion, the worker records notes and an optional repair fee. WorkerController.completeComplaint() marks status COMPLETED and automatically inserts an invoice into the payments table.");

// ==========================================
// PART V: TOP VIVA & TECHNICAL INTERVIEW QUESTIONS
// ==========================================
addPartHeader(doc, "Part V: Top Viva & Technical Interview Questions", "Concise, expert model answers for examinations and project defenses");

addCallout(doc, "Q1: What is JDBC and how does it connect to the PostgreSQL database?", 
  "Answer: JDBC (Java Database Connectivity) is a standard Java API that connects Java applications to relational databases. In DBConnection.java, we load the driver class 'org.postgresql.Driver' via Class.forName() and establish a secure SSL connection using DriverManager.getConnection(url, user, password).", "info");

addCallout(doc, "Q2: Why did you use PreparedStatement instead of Statement?", 
  "Answer:\n1. Security: PreparedStatement uses parameterized queries with '?' placeholders, completely preventing SQL Injection attacks by automatically escaping user inputs.\n2. Performance: PreparedStatement is pre-compiled by the database engine, allowing faster repeated execution.", "info");

addCallout(doc, "Q3: What is 'try-with-resources' and why is it important?", 
  "Answer: Introduced in Java 7, try-with-resources automatically closes any resource implementing AutoCloseable (Connection, PreparedStatement, ResultSet) when the block finishes, even if an exception is thrown. This guarantees zero database connection leaks.", "info");

addCallout(doc, "Q4: How does visitor pass verification maintain security?", 
  "Answer: Plaintext pass codes are never stored in the database. When created, the pass code is hashed using SHA-256 (CodeGenerator.sha256()) and stored as access_hash. At the gate, the guard's input is hashed and compared against access_hash in the database.", "info");

addCallout(doc, "Q5: How does the auto-assignment algorithm distribute maintenance work?", 
  "Answer: findBestWorkerForCategory() filters active workers matching the complaint category (e.g. Electrical), counts their current active tickets (ASSIGNED/IN_PROGRESS), and assigns the ticket to the technician with the lightest current workload.", "info");

addCallout(doc, "Q6: How are Design Patterns applied in this project?", 
  "Answer:\n1. Singleton Pattern: In DBConnection.java for centralized connection access.\n2. Adapter Pattern: MouseAdapter simplifies mouse event handling; SwingNavIcon adapts vector drawing to the Icon interface.\n3. MVC Pattern: Architectural pattern separating Model (data), View (GUI), and Controller (logic).", "info");

// ==========================================
// PART VI: COMMANDS
// ==========================================
addPartHeader(doc, "Part VI: Verification, Build & Execution Commands", "Standard terminal commands for compilation, testing, and deployment");

addBullet(doc, "Compile All Source Code", "javac -cp \"lib/*\" -d out $(find src -name \"*.java\")");
addBullet(doc, "Run Automated QA Test Suite", "python scripts/run_qa.py");
addBullet(doc, "Launch Desktop Application", "java -cp \"lib/*:out\" Main");

// ==========================================
// FOOTER & PAGE NUMBERING
// ==========================================
const pageRange = doc.bufferedPageRange();
for (let i = pageRange.start; i < pageRange.start + pageRange.count; i++) {
  doc.switchToPage(i);
  
  // Footer rule
  doc.moveTo(45, doc.page.height - 32).lineTo(550, doc.page.height - 32).lineWidth(0.5).strokeColor("#CBD5E1").stroke();
  
  // Left footer text
  doc.font("AppFont").fontSize(7.5).fillColor(textMuted)
     .text("A-partment Helper • Comprehensive Codebase, Java Syllabus & Viva Guide", 45, doc.page.height - 25);
     
  // Right footer text
  doc.font("AppFont-Bold").fontSize(7.5).fillColor(primaryColor)
     .text(`Page ${i + 1} of ${pageRange.count}`, 45, doc.page.height - 25, { align: "right" });
}

doc.end();

writeStream.on("finish", () => {
  const stats = fs.statSync(outputPath);
  console.log(`Successfully generated master PDF with clean typography: ${outputPath} (${(stats.size / 1024).toFixed(1)} KB, ${pageRange.count} pages)`);
});
