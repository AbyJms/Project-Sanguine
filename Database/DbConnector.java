package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnector {

    // --- Configuration Constants ---
    private static final String ipHome = "192.168.1.6";
    private static final String ipAbhi = "172.30.200.44";
    private static final String ipAby = "10.83.31.44";
    private static final String DB_URL = "jdbc:mysql://"+ipAbhi+":3307/Sanguine?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    private static final String USER = "root"; 
    private static final String PASS = "projectSanguine"; 

    // --- Get Connection Method (for SanguineApp, Admin, Hospital, etc.) ---
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }

    // --- Optional: quick test method ---
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected successfully to database!");
            } else {
                System.out.println("Failed to connect to database!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
