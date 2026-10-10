package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** PostgreSQL connection. Set APARTMENT_DB_URL, APARTMENT_DB_USER and APARTMENT_DB_PASSWORD as environment variables. */
public final class DBConnection {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/postgres?sslmode=prefer";
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&currentSchema=\"A-partment\"";
        String user = "postgres.qjzczawhtqcewdjqbged";
        String password = "Thisisadbpas";
        if (password.isBlank()) {
            throw new SQLException("Database password is not configured. Set APARTMENT_DB_PASSWORD in your environment.");
        }
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("PostgreSQL JDBC driver is missing. Add lib/postgresql-42.7.3.jar to the project libraries.", e); }
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        return DriverManager.getConnection(url, props);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
