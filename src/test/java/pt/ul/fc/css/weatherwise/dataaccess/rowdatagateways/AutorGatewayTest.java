package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutorGateway Tests")
class AutorGatewayTest {

    private Connection connection;
    private AutorGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        // Usa H2 em memória para os testes
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        gateway = new AutorGateway();
        
        // Cria a tabela Autor para os testes
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Autor (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("DELETE FROM Autor"); // Limpa dados anteriores
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should return empty Optional when author does not exist")
    void testFindIdByNome_NonExistent() throws PersistenceException {
        Optional<Long> result = gateway.findIdByNome(connection, "AutorInexistente");
        
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent author");
    }

    @Test
    @DisplayName("Should find existing author by name")
    void testFindIdByNome_Existing() throws Exception {
        // Insere autor diretamente
        long insertedId;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO Autor(nome) VALUES ('Teste Autor')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                insertedId = rs.getLong(1);
            }
        }

        Optional<Long> result = gateway.findIdByNome(connection, "Teste Autor");
        
        assertTrue(result.isPresent(), "Should find existing author");
        assertEquals(insertedId, result.get(), "ID should match inserted author");
        assertTrue(result.get() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should trim whitespace when finding author")
    void testFindIdByNome_TrimsWhitespace() throws Exception {
        // Insere autor sem espaços
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO Autor(nome) VALUES ('Autor Com Espacos')");
        }

        // Procura com espaços - deve encontrar
        Optional<Long> result = gateway.findIdByNome(connection, "  Autor Com Espacos  ");
        
        assertTrue(result.isPresent(), "Should find author even with whitespace");
    }

    @Test
    @DisplayName("Should insert new author and return ID")
    void testInsert_NewAuthor() throws PersistenceException {
        long id = gateway.insert(connection, "Novo Autor");
        
        assertTrue(id > 0, "Should return positive ID");
        
        // Verifica se foi inserido
        Optional<Long> found = gateway.findIdByNome(connection, "Novo Autor");
        assertTrue(found.isPresent(), "Should find inserted author");
        assertEquals(id, found.get(), "ID should match");
    }

    @Test
    @DisplayName("Should trim whitespace when inserting author")
    void testInsert_TrimsWhitespace() throws PersistenceException {
        long id = gateway.insert(connection, "  Autor Com Espacos  ");
        
        // Deve encontrar com nome sem espaços
        Optional<Long> found = gateway.findIdByNome(connection, "Autor Com Espacos");
        assertTrue(found.isPresent(), "Should find author with trimmed name");
        assertEquals(id, found.get(), "ID should match");
    }

    @Test
    @DisplayName("Should find existing author in findOrInsert")
    void testFindOrInsert_Existing() throws Exception {
        // Insere autor primeiro
        long originalId = gateway.insert(connection, "Autor Existente");
        
        // Tenta encontrar ou inserir (deve encontrar)
        long resultId = gateway.findOrInsert(connection, "Autor Existente");
        
        assertEquals(originalId, resultId, "Should return existing author ID");
        
        // Verifica que não foi criado duplicado
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Autor WHERE nome = 'Autor Existente'");
            rs.next();
            assertEquals(1, rs.getInt(1), "Should have only one author with this name");
        }
    }

    @Test
    @DisplayName("Should insert new author if not found in findOrInsert")
    void testFindOrInsert_NewAuthor() throws PersistenceException {
        long id = gateway.findOrInsert(connection, "Autor Novo");
        
        assertTrue(id > 0, "Should return positive ID for new author");
        
        // Verifica se foi inserido
        Optional<Long> found = gateway.findIdByNome(connection, "Autor Novo");
        assertTrue(found.isPresent(), "Should find newly inserted author");
        assertEquals(id, found.get(), "ID should match");
    }

    @Test
    @DisplayName("Should handle findOrInsert with whitespace")
    void testFindOrInsert_TrimsWhitespace() throws PersistenceException {
        // Insere com espaços
        long id1 = gateway.findOrInsert(connection, "  Autor Teste  ");
        
        // Tenta encontrar/inserir com espaços diferentes
        long id2 = gateway.findOrInsert(connection, "Autor Teste");
        
        // Deve retornar o mesmo ID
        assertEquals(id1, id2, "Should return same ID for same author name (trimmed)");
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error in findIdByNome")
    void testFindIdByNome_SQLError() throws Exception {
        connection.close(); // Fecha conexão para forçar erro
        
        PersistenceException exception = assertThrows(PersistenceException.class, 
            () -> gateway.findIdByNome(connection, "Teste"),
            "Should throw PersistenceException on SQL error");
        
        assertTrue(exception.getMessage().contains("Erro ao procurar autor"),
            "Exception message should mention error finding author");
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error in insert")
    void testInsert_SQLError() throws Exception {
        connection.close(); // Fecha conexão para forçar erro
        
        PersistenceException exception = assertThrows(PersistenceException.class, 
            () -> gateway.insert(connection, "Teste"),
            "Should throw PersistenceException on SQL error");
        
        assertTrue(exception.getMessage().contains("Erro ao inserir autor"),
            "Exception message should mention error inserting author");
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error in findOrInsert")
    void testFindOrInsert_SQLError() throws Exception {
        connection.close(); // Fecha conexão para forçar erro
        
        assertThrows(PersistenceException.class, 
            () -> gateway.findOrInsert(connection, "Teste"),
            "Should throw PersistenceException on SQL error");
    }

    @Test
    @DisplayName("Should handle multiple authors correctly")
    void testMultipleAuthors() throws PersistenceException {
        long id1 = gateway.insert(connection, "Autor 1");
        long id2 = gateway.insert(connection, "Autor 2");
        long id3 = gateway.insert(connection, "Autor 3");
        
        assertNotEquals(id1, id2, "Different authors should have different IDs");
        assertNotEquals(id2, id3, "Different authors should have different IDs");
        
        Optional<Long> found1 = gateway.findIdByNome(connection, "Autor 1");
        Optional<Long> found2 = gateway.findIdByNome(connection, "Autor 2");
        Optional<Long> found3 = gateway.findIdByNome(connection, "Autor 3");
        
        assertTrue(found1.isPresent() && found1.get() == id1);
        assertTrue(found2.isPresent() && found2.get() == id2);
        assertTrue(found3.isPresent() && found3.get() == id3);
    }
}
