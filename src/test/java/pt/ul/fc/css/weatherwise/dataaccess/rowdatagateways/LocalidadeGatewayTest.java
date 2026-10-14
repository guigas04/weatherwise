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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalidadeGateway Tests")
class LocalidadeGatewayTest {

    private Connection connection;
    private LocalidadeGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        gateway = new LocalidadeGateway();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Localidade (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("DELETE FROM Localidade");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should return empty Optional when location does not exist")
    void testFindIdByName_NonExistent() throws PersistenceException {
        Optional<Long> result = gateway.findIdByName(connection, "LocalidadeInexistente");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find existing location by name")
    void testFindIdByName_Existing() throws Exception {
        long insertedId;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO Localidade(nome) VALUES ('Lisboa')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                insertedId = rs.getLong(1);
            }
        }

        Optional<Long> result = gateway.findIdByName(connection, "Lisboa");
        assertTrue(result.isPresent());
        assertEquals(insertedId, result.get());
    }

    @Test
    @DisplayName("Should trim whitespace when finding location")
    void testFindIdByName_TrimsWhitespace() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO Localidade(nome) VALUES ('Porto')");
        }

        Optional<Long> result = gateway.findIdByName(connection, "  Porto  ");
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should insert new location and return ID")
    void testInsert_NewLocation() throws PersistenceException {
        long id = gateway.insert(connection, "Faro");
        assertTrue(id > 0);
        
        Optional<Long> found = gateway.findIdByName(connection, "Faro");
        assertTrue(found.isPresent());
        assertEquals(id, found.get());
    }

    @Test
    @DisplayName("Should trim whitespace when inserting location")
    void testInsert_TrimsWhitespace() throws PersistenceException {
        long id = gateway.insert(connection, "  Coimbra  ");
        
        Optional<Long> found = gateway.findIdByName(connection, "Coimbra");
        assertTrue(found.isPresent());
        assertEquals(id, found.get());
    }

    @Test
    @DisplayName("Should find existing location in findOrInsert")
    void testFindOrInsert_Existing() throws Exception {
        long originalId = gateway.insert(connection, "Braga");
        long resultId = gateway.findOrInsert(connection, "Braga");
        
        assertEquals(originalId, resultId);
        
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Localidade WHERE nome = 'Braga'");
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    @DisplayName("Should insert new location if not found in findOrInsert")
    void testFindOrInsert_NewLocation() throws PersistenceException {
        long id = gateway.findOrInsert(connection, "Aveiro");
        assertTrue(id > 0);
        
        Optional<Long> found = gateway.findIdByName(connection, "Aveiro");
        assertTrue(found.isPresent());
        assertEquals(id, found.get());
    }

    @Test
    @DisplayName("Should insert or return existing location using inserirSeNaoExistir")
    void testInserirSeNaoExistir_NewLocation() throws SQLException, PersistenceException {
        Long id = gateway.inserirSeNaoExistir(connection, "Viseu");
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("Should return existing location using inserirSeNaoExistir")
    void testInserirSeNaoExistir_Existing() throws Exception {
        long originalId = gateway.insert(connection, "Leiria");
        Long resultId = gateway.inserirSeNaoExistir(connection, "Leiria");
        
        assertEquals(originalId, resultId.longValue());
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error")
    void testFindIdByName_SQLError() throws Exception {
        connection.close();
        
        assertThrows(PersistenceException.class, 
            () -> gateway.findIdByName(connection, "Teste"));
    }

    @Test
    @DisplayName("Should throw PersistenceException on insert SQL error")
    void testInsert_SQLError() throws Exception {
        connection.close();
        
        assertThrows(PersistenceException.class, 
            () -> gateway.insert(connection, "Teste"));
    }
}

