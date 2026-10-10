# A-partment Helper — Smart Apartment Society Management System

A Java Swing + PostgreSQL/JDBC desktop application for resident, visitor, worker, parking, maintenance and payment workflows. The UI uses a coffee-brown, amber and warm-cream palette.

## Features in this version

- Resident/worker sign-in
- Resident dashboard with live parking, visitor and outstanding maintenance totals
- Generate visitor access passes; only the SHA-256 hash is stored
- Gate entry checks pass validity and status; vehicle visitors receive the first available visitor slot
- Gate exit releases a visitor parking slot
- Residents submit maintenance complaints and view their status
- Workers claim complaints and update them through Assigned → In Progress → Completed
- Residents view payment records and can record a payment as paid (this is a record only; no money is processed)

## Requirements

- JDK 17 or newer recommended
- IntelliJ IDEA
- PostgreSQL or a Supabase PostgreSQL project
- Included PostgreSQL JDBC driver: `lib/postgresql-42.7.3.jar`

## 1. Prepare the database

1. Open the Supabase SQL Editor (or a PostgreSQL SQL client).
2. Run **`sql/setup_postgres.sql`**. It is designed to be rerun and does not drop tables.
3. Demo login records are inserted if they don't already exist:
   - Resident: `akash@example.com` / `1234`
   - Worker: `worker@example.com` / `1234`
4. Change the demo passwords before using this with real people. The simple login currently compares the `password` column directly to keep the project compatible with the existing sample schema. This is for a classroom demo only, not production authentication.

> Do not run old scripts that start with `DROP TABLE` or `DROP DATABASE` unless you deliberately want to delete data. `sql/main.sql` is the legacy MySQL script; this project uses PostgreSQL, so use `setup_postgres.sql` instead.

## 2. Configure the database connection

The application reads these environment variables. It does not keep a database password in source code.

| Variable | Example / meaning |
|---|---|
| `APARTMENT_DB_URL` | `jdbc:postgresql://YOUR-HOST:5432/postgres?sslmode=require` |
| `APARTMENT_DB_USER` | Your PostgreSQL database user, for Supabase often `postgres.<project-ref>` when using the pooler |
| `APARTMENT_DB_PASSWORD` | Your database password |

### Configure in IntelliJ (Windows)

1. Go to **Run → Edit Configurations…** and select the `Main` application configuration.
2. In **Environment variables**, add `APARTMENT_DB_URL`, `APARTMENT_DB_USER`, and `APARTMENT_DB_PASSWORD` using your own database connection details.
3. For Supabase, copy the host, port, database name and username from **Project Settings → Database → Connection string**. Use the correct connection mode (direct or pooler) for your network.
4. Never commit these environment values or share screenshots containing them.

You can also set the variables in PowerShell before launching from that terminal:

```powershell
$env:APARTMENT_DB_URL="jdbc:postgresql://YOUR-HOST:5432/postgres?sslmode=require"
$env:APARTMENT_DB_USER="YOUR-DB-USER"
$env:APARTMENT_DB_PASSWORD="YOUR-DB-PASSWORD"
```

## 3. Open and run in IntelliJ

1. Extract the ZIP.
2. Open the extracted `A-partment-Helper-main` folder in IntelliJ.
3. If prompted, configure a JDK (17+ recommended).
4. Add `lib/postgresql-42.7.3.jar` as a project library if IntelliJ has not detected it: **File → Project Structure → Modules → Dependencies → + → JARs or Directories**.
5. Run `src/Main.java`.

## Notes and limitations

- All database table queries explicitly use the quoted PostgreSQL schema name `"A-partment"`.
- Visitor parking is allocated when a valid vehicle visitor checks in, not when the invite is created. Walk-in visitors do not consume parking slots.
- The payment screen is not connected to a payment gateway. It only updates the local payment record after user confirmation.
- Passwords are currently plain text for compatibility with the starter project. Do not use this authentication design in production; use a password hash (e.g. Argon2id or BCrypt) and server-side authorization for a deployed system.
- Keep the JDBC driver in `lib/` and do not commit database credentials.
