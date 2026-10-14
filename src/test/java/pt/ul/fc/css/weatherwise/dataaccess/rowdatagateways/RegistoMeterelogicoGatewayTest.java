package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegistoMeterelogicoGateway Tests")
class RegistoMeterelogicoGatewayTest {

    private Connection connection;
    private RegistoMeterelogicoGateway gateway;
    private long localidadeId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        gateway = new RegistoMeterelogicoGateway();
        
        try (Statement stmt = connection.createStatement()) {
            // Cria tabelas necessárias
            stmt.execute("CREATE TABLE IF NOT EXISTS Localidade (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS RegistoMeteorologico (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "localidadeId BIGINT NOT NULL, " +
                    "condicaoClimatica VARCHAR(50), " +
                    "data DATE NOT NULL, " +
                    "temperatura DECIMAL(5,2), " +
                    "FOREIGN KEY (localidadeId) REFERENCES Localidade(Id))");
            
            stmt.execute("DELETE FROM RegistoMeteorologico");
            stmt.execute("DELETE FROM Localidade");
            
            // Insere localidade de teste
            stmt.executeUpdate("INSERT INTO Localidade(nome) VALUES ('Lisboa')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                localidadeId = rs.getLong(1);
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should return empty Optional when no record exists")
    void testFindAtualByLocalidadeId_NoRecord() throws PersistenceException {
        Optional<RegistoMeterelogicoGateway.Registo> result = 
            gateway.findAtualByLocalidadeId(connection, localidadeId);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find most recent record by localidade")
    void testFindAtualByLocalidadeId_WithRecords() throws Exception {
        LocalDate data1 = LocalDate.of(2025, 1, 15);
        LocalDate data2 = LocalDate.of(2025, 1, 20);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(String.format(
                "INSERT INTO RegistoMeteorologico(localidadeId, condicaoClimatica, data, temperatura) " +
                "VALUES (%d, 'SOL', DATE '%s', 25.5)", localidadeId, data1));
            stmt.executeUpdate(String.format(
                "INSERT INTO RegistoMeteorologico(localidadeId, condicaoClimatica, data, temperatura) " +
                "VALUES (%d, 'CHUVA', DATE '%s', 18.0)", localidadeId, data2));
        }

        Optional<RegistoMeterelogicoGateway.Registo> result = 
            gateway.findAtualByLocalidadeId(connection, localidadeId);
        
        assertTrue(result.isPresent());
        assertEquals("CHUVA", result.get().condicao);
        assertEquals(18.0, result.get().temperatura.doubleValue(), 0.01);
        assertEquals(data2, result.get().data.toLocalDate());
    }

    @Test
    @DisplayName("Should check if record exists")
    void testExisteRegistoCamel_Exists() throws SQLException {
        LocalDate data = LocalDate.of(2025, 1, 15);
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(String.format(
                "INSERT INTO RegistoMeteorologico(localidadeId, condicaoClimatica, data, temperatura) " +
                "VALUES (%d, 'SOL', DATE '%s', 25.5)", localidadeId, data));
        }

        boolean exists = gateway.existeRegistoCamel(connection, localidadeId, data);
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when record does not exist")
    void testExisteRegistoCamel_NotExists() throws SQLException {
        LocalDate data = LocalDate.of(2025, 1, 15);
        boolean exists = gateway.existeRegistoCamel(connection, localidadeId, data);
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should insert new record")
    void testInserirCamel_NewRecord() throws SQLException {
        LocalDate data = LocalDate.of(2025, 1, 15);
        gateway.inserirCamel(connection, localidadeId, data, "SOL", 25.5);
        
        boolean exists = gateway.existeRegistoCamel(connection, localidadeId, data);
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should update existing record")
    void testAtualizarCamel_ExistingRecord() throws SQLException, PersistenceException {
        LocalDate data = LocalDate.of(2025, 1, 15);
        
        // Insere primeiro
        gateway.inserirCamel(connection, localidadeId, data, "SOL", 25.5);
        
        // Atualiza
        gateway.atualizarCamel(connection, localidadeId, data, "CHUVA", 18.0);
        
        // Verifica
        Optional<RegistoMeterelogicoGateway.Registo> result = 
            gateway.findAtualByLocalidadeId(connection, localidadeId);
        assertTrue(result.isPresent());
        assertEquals("CHUVA", result.get().condicao);
        assertEquals(18.0, result.get().temperatura.doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Should find most recent record")
    void testFindMaisRecenteByLocalidadeId() throws Exception {
        LocalDate data1 = LocalDate.of(2025, 1, 10);
        LocalDate data2 = LocalDate.of(2025, 1, 20);
        
        try (Statement stmt = connection.createStatement()) {
            try {
                stmt.executeUpdate(String.format(
                    "INSERT INTO RegistoMeteorologico(localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'SOL', DATE '%s', 25.5)", localidadeId, data1));
                stmt.executeUpdate(String.format(
                    "INSERT INTO RegistoMeteorologico(localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'NUBLADO', DATE '%s', 20.0)", localidadeId, data2));
            } catch (SQLException e) {
                // Ignore
            }
        }

        Optional<RegistoMeterelogicoGateway.Registo> result = 
            gateway.findMaisRecenteByLocalidadeId(connection, localidadeId);
        
        assertTrue(result.isPresent());
        assertEquals(data2, result.get().data.toLocalDate());
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error")
    void testFindAtualByLocalidadeId_SQLError() throws Exception {
        connection.close();
        
        assertThrows(PersistenceException.class, 
            () -> gateway.findAtualByLocalidadeId(connection, localidadeId));
    }

    @Test
    @DisplayName("Should throw SQLException on insert error")
    void testInserirCamel_SQLError() throws Exception {
        connection.close();
        
        assertThrows(SQLException.class, 
            () -> gateway.inserirCamel(connection, localidadeId, LocalDate.now(), "SOL", 25.0));
    }
}

