package pt.ul.fc.css.weatherwise.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

public enum DataSource {
    INSTANCE;

    private Connection connection;

    private static final String DB_URL = "jdbc:postgresql://pgserver:5432/testdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 3000;

    public DataSource connect() throws PersistenceException {
        // Verifica se já existe uma conexão válida
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return INSTANCE;
                }
            } catch (SQLException e) {
                // Conexão está fechada ou inválida, precisa reconectar
                connection = null;
            }
        }
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                System.out.println("Attempting to connect to database... (Attempt " + (attempt + 1) + ")");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection successful.");
                return INSTANCE;
            } catch (SQLException e) {
                attempt++;
                if (attempt < MAX_RETRIES) {
                    System.out.println("Connection failed. Retrying in " + RETRY_DELAY_MS / 1000 + " seconds...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("Could not connect to the database after " + MAX_RETRIES + " attempts.");
                    throw new PersistenceException("Cannot connect to database", e);
                }
            }
        }
        return INSTANCE;
    }

    public Connection getConnection() { return connection; }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) { /* Cannot do much */ }
        }
    }

    public PreparedStatement prepare(String sql) throws PersistenceException {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new PersistenceException("Error preparing comment", e);
        }
    }

    public PreparedStatement prepareGetGenKey(String sql) throws SQLException {
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public void beginTransaction() throws PersistenceException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenceException("Error starting DB transaction", e);
        }
    }

    public void commit() throws PersistenceException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new PersistenceException("Error on commit", e);
        }
        startAutoCommit();
    }

    public void rollback() throws PersistenceException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new PersistenceException("Error on rollback!", e);
        }
        startAutoCommit();
    }

    private void startAutoCommit() throws PersistenceException {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new PersistenceException("Error starting auto commit", e);
        }
    }
}
