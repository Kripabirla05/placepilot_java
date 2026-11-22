package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gives a JDBC Connection to MySQL.
 * JDBC steps:  1) load driver  2) DriverManager.getConnection(url, user, password)
 *
 * !!! CHANGE PASSWORD below to your own MySQL root password !!!
 */
public class DBConnection {

    // jdbc:mysql://host:port/databaseName
    private static final String URL = "jdbc:mysql://localhost:3306/placepilot_db";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";   // <-- change this

    // static block runs once when the class is loaded: it loads MySQL Connector/J driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found. Add mysql-connector-j.jar in classpath (lib folder).");
        }
    }

    // Every DAO method calls this to get a fresh connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Used by Main at startup to check that database is working
    public static boolean testConnection() {
        try (Connection con = getConnection()) {   // try-with-resources closes con automatically
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return false;
        }
    }
}
