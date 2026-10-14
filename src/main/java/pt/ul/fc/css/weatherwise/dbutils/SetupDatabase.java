package pt.ul.fc.css.weatherwise.dbutils;

import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SetupDatabase {

    /**
     * Reads and executes SQL scripts to initialize the database.
     * @throws PersistenceException if any script cannot be run.
     */
    public static void run() throws PersistenceException {
        System.out.println("Attempting to set up database schema...");
        try {
            DataSource.INSTANCE.connect();
            Connection conn = DataSource.INSTANCE.getConnection();

            // --- Exemplos de ficheiros SQL
            String[] schemaCommands = getCommands("/schema-postgres.sql");
            String[] dataCommands = getCommands("/data.sql");

            // Executa comandos do schema
            for (String command : schemaCommands) {
                if (command.trim().isEmpty()) continue;
                try (Statement statement = conn.createStatement()) {
                    statement.execute(command);
                }
            }
            System.out.println("Database schema setup successful.");

            // Executa comandos de dados
            for (String command : dataCommands) {
                if (command.trim().isEmpty()) continue;
                try (Statement statement = conn.createStatement()) {
                    statement.execute(command);
                }
            }
            System.out.println("Data population successful.");

        } catch (Exception e) {
            throw new PersistenceException("Failed to run database setup scripts.", e);
        } finally {
            DataSource.INSTANCE.close();
        }
    }

    /**
     * Reads an SQL script from the classpath and splits it into individual commands.
     * @param resourcePath path of the SQL file in resources (e.g., "/schema-postgres.sql")
     * @return array of SQL commands
     * @throws PersistenceException if the resource cannot be found
     */
    private static String[] getCommands(String resourcePath) throws PersistenceException {
        try (InputStream is = SetupDatabase.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new PersistenceException("Cannot find SQL resource: " + resourcePath);
            }

            String script;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                script = reader.lines().collect(Collectors.joining("\n"));
            }

            // Divide o conteúdo em comandos SQL separados por ponto e vírgula
            return script.split(";");
        } catch (IOException e) {
            throw new PersistenceException("Error reading SQL resource: " + resourcePath, e);
        }
    }
}
