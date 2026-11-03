#!/usr/bin/env python3
"""
Automated QA Verification Script for A-partment Helper
Checks directory structure, compilation, SQL schemas, demo accounts, and MVC implementation indicators.
Generates/updates reports/automated-report.md.
"""

import os
import re
import sys
import subprocess
from datetime import datetime

def run_qa():
    project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    os.chdir(project_root)

    results = []

    # AUTO-01: Correct project root
    has_src = os.path.isdir("src")
    has_sql = os.path.isdir("sql")
    has_lib = os.path.isdir("lib")
    auto_01 = has_src and has_sql and has_lib
    results.append(("AUTO-01", "PASS" if auto_01 else "FAIL", f"Expected folders: src={has_src}, sql={has_sql}, lib={has_lib}. Run from repository root."))

    # AUTO-02: Main.java found
    main_found = os.path.isfile(os.path.join("src", "Main.java"))
    results.append(("AUTO-02", "PASS" if main_found else "FAIL", "src/Main.java found." if main_found else "src/Main.java missing."))

    # AUTO-03: Java source files discovered
    java_files = []
    for root, _, files in os.walk("src"):
        for f in files:
            if f.endswith(".java"):
                java_files.append(os.path.join(root, f))
    results.append(("AUTO-03", "PASS" if len(java_files) > 0 else "FAIL", f"Found {len(java_files)} Java source file(s)."))

    # AUTO-04: PostgreSQL JDBC JAR found in lib
    jar_found = None
    if has_lib:
        for f in os.listdir("lib"):
            if f.startswith("postgresql-") and f.endswith(".jar"):
                jar_found = f
                break
    results.append(("AUTO-04", "PASS" if jar_found else "FAIL", f"PostgreSQL JDBC JAR found: {jar_found}" if jar_found else "No postgresql jar found in lib/"))

    # AUTO-05: Java compilation succeeds
    try:
        cp = f"lib/{jar_found}" if jar_found else "lib/*"
        compile_cmd = ["javac", "-cp", cp, "-d", "out"] + java_files
        res = subprocess.run(compile_cmd, capture_output=True, text=True)
        if res.returncode == 0:
            results.append(("AUTO-05", "PASS", "Java compilation succeeds (javac)."))
        else:
            results.append(("AUTO-05", "FAIL", f"Java compilation failed: {res.stderr[:200]}"))
    except FileNotFoundError:
        results.append(("AUTO-05", "BLOCKED", "javac not found. Install/configure JDK 17+ and rerun."))

    # AUTO-06: sql/setup_postgres.sql found
    sql_setup = os.path.isfile(os.path.join("sql", "setup_postgres.sql"))
    results.append(("AUTO-06", "PASS" if sql_setup else "FAIL", "sql/setup_postgres.sql found." if sql_setup else "sql/setup_postgres.sql not found."))

    # Read SQL files and Java sources for indicator checks
    sql_text = ""
    sql_files = []
    if os.path.isdir("sql"):
        for f in os.listdir("sql"):
            if f.endswith(".sql"):
                sql_files.append(f)
                with open(os.path.join("sql", f), "r", encoding="utf-8", errors="ignore") as sf:
                    sql_text += sf.read() + "\n"

    java_text = ""
    for jf in java_files:
        with open(jf, "r", encoding="utf-8", errors="ignore") as f:
            java_text += f.read() + "\n"

    combined_text = sql_text + "\n" + java_text

    # AUTO-07: Expected table definitions / users indicator
    has_residents = "residents" in combined_text.lower()
    has_workers = "workers" in combined_text.lower()
    has_admins = "admins" in combined_text.lower()
    has_users = has_residents and has_workers
    results.append(("AUTO-07", "PASS" if has_users else "WARN", "User & role tables (residents, workers, admins) detected." if has_users else "Users indicator missing."))

    # AUTO-08: Visitor / pass indicator
    has_vis = "visitors" in combined_text.lower() or "visitor" in combined_text.lower()
    results.append(("AUTO-08", "PASS" if has_vis else "FAIL", "visitor/pass indicator found in SQL/source scan (static indicator only)."))

    # AUTO-09: Parking indicator
    has_park = "parking" in combined_text.lower()
    results.append(("AUTO-09", "PASS" if has_park else "FAIL", "parking indicator found in SQL/source scan (static indicator only)."))

    # AUTO-10: Maintenance indicator
    has_maint = "complaints" in combined_text.lower() or "maintenance" in combined_text.lower()
    results.append(("AUTO-10", "PASS" if has_maint else "FAIL", "maintenance/complaints indicator found in SQL/source scan (static indicator only)."))

    # AUTO-11: Payments indicator
    has_pay = "payments" in combined_text.lower()
    results.append(("AUTO-11", "PASS" if has_pay else "FAIL", "payments indicator found in SQL/source scan (static indicator only)."))

    # AUTO-12: Primary keys in SQL
    pk_count = len(re.findall(r"PRIMARY\s+KEY", sql_text, re.IGNORECASE))
    results.append(("AUTO-12", "PASS" if pk_count > 0 else "FAIL", f"Primary-key declarations in SQL: {pk_count}."))

    # AUTO-13: Foreign keys in SQL
    fk_count = len(re.findall(r"FOREIGN\s+KEY|REFERENCES", sql_text, re.IGNORECASE))
    results.append(("AUTO-13", "PASS" if fk_count > 0 else "FAIL", f"Foreign-key/references declarations in SQL: {fk_count}."))

    # AUTO-14: Resident demo email
    res_email = ("akash@gmail.com" in combined_text.lower()) or ("akash@example.com" in combined_text.lower())
    results.append(("AUTO-14", "PASS" if res_email else "FAIL", "Resident demo-account email found in SQL/Java sources."))

    # AUTO-15: Worker demo email
    worker_email = ("suresh@apartment.com" in combined_text.lower()) or ("worker@example.com" in combined_text.lower())
    results.append(("AUTO-15", "PASS" if worker_email else "FAIL", "Worker demo-account email found in SQL/Java sources."))

    # AUTO-16: Visitor pass generation indicator
    pass_gen = ("generatecode" in java_text.lower()) or ("sha256" in java_text.lower()) or ("access_hash" in combined_text.lower())
    results.append(("AUTO-16", "PASS" if pass_gen else "FAIL", "visitor pass generation indicator found in Java source."))

    # AUTO-17: Gate entry/check-in indicator
    gate_entry = ("processentry" in java_text.lower()) or ("visitorentry" in java_text.lower())
    results.append(("AUTO-17", "PASS" if gate_entry else "FAIL", "gate entry/check-in indicator found in Java source."))

    # AUTO-18: Gate exit/check-out indicator
    gate_exit = ("processexit" in java_text.lower()) or ("markexited" in java_text.lower())
    results.append(("AUTO-18", "PASS" if gate_exit else "FAIL", "gate exit/check-out indicator found in Java source."))

    # AUTO-19: Parking allocation/release indicator
    park_alloc = ("allocatevisitorparking" in java_text.lower()) and ("freevisitorparking" in java_text.lower())
    results.append(("AUTO-19", "PASS" if park_alloc else "FAIL", "parking allocation/release indicator found in Java source."))

    # AUTO-20: Maintenance workflow indicator
    comp_wf = ("claimcomplaint" in java_text.lower()) and ("completecomplaint" in java_text.lower())
    results.append(("AUTO-20", "PASS" if comp_wf else "FAIL", "maintenance complaint workflow indicator found in Java source."))

    # AUTO-21: Payment record workflow indicator
    pay_wf = ("markaspaid" in java_text.lower()) or ("paybill" in java_text.lower())
    results.append(("AUTO-21", "PASS" if pay_wf else "FAIL", "payment record workflow indicator found in Java source."))

    # Generate Report
    os.makedirs("reports", exist_ok=True)
    report_file = os.path.join("reports", "automated-report.md")

    pass_count = sum(1 for _, res, _ in results if res == "PASS")
    warn_count = sum(1 for _, res, _ in results if res == "WARN")
    fail_count = sum(1 for _, res, _ in results if res == "FAIL")
    blocked_count = sum(1 for _, res, _ in results if res == "BLOCKED")

    now_str = datetime.now().astimezone().isoformat()

    report_lines = [
        "# A-partment Helper — Automated QA Report",
        "",
        f"- Run time: `{now_str}`",
        f"- Working directory: `{project_root}`",
        f"- Java files discovered: {len(java_files)}",
        f"- SQL files discovered: {len(sql_files)}",
        "",
        "> Static scans only indicate that code/schema patterns exist. They do not prove that screens or database workflows work. Complete every relevant manual test in SCRATCHPAD.md.",
        "",
        "| ID | Result | Details |",
        "|---|---|---|",
    ]

    for item_id, res, detail in results:
        status_md = f"**{res}**"
        report_lines.append(f"| {item_id} | {status_md} | {detail} |")

    report_lines.extend([
        "",
        "## Summary",
        "",
        f"- PASS: {pass_count}",
        f"- WARN: {warn_count}",
        f"- FAIL: {fail_count}",
        f"- BLOCKED: {blocked_count}",
        "",
        "## Required next actions",
        "1. Fix any FAIL compilation result.",
        "2. Review WARN results; some can be false negatives because names and patterns vary.",
        "3. Run every manual case in `SCRATCHPAD.md` using a test database.",
        "4. Record failures and evidence in the scratchpad. Do not claim testing is complete while any test is unchecked.",
        ""
    ])

    report_content = "\n".join(report_lines)
    with open(report_file, "w", encoding="utf-8") as f:
        f.write(report_content)

    print(f"Report generated at {report_file}")
    print(f"PASS: {pass_count}, WARN: {warn_count}, FAIL: {fail_count}, BLOCKED: {blocked_count}")

if __name__ == "__main__":
    run_qa()
