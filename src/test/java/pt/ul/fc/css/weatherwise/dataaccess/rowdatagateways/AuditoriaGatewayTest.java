package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditoriaGateway Tests")
class AuditoriaGatewayTest {

    private Connection connection;
    private AuditoriaGateway gateway;
    private long autorId;
    private long localidadeId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        gateway = new AuditoriaGateway();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Autor (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Localidade (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Auditoria (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "autorId BIGINT NOT NULL, " +
                    "localidadeId BIGINT NOT NULL, " +
                    "tipoConsulta VARCHAR(50) NOT NULL, " +
                    "dataHora TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (autorId) REFERENCES Autor(Id), " +
                    "FOREIGN KEY (localidadeId) REFERENCES Localidade(Id))");
            
            stmt.execute("DELETE FROM Auditoria");
            stmt.execute("DELETE FROM Autor");
            stmt.execute("DELETE FROM Localidade");
            
            // Insere autor e localidade de teste
            stmt.executeUpdate("INSERT INTO Autor(nome) VALUES ('Teste Autor')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                autorId = rs.getLong(1);
            }
            
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
    @DisplayName("Should register consultation successfully")
    void testRegistarConsulta_Success() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        
        List<AuditoriaGateway.AuditoriaConsulta> historico = gateway.listarHistorico(connection);
        assertEquals(1, historico.size());
        assertEquals("previsao_atual", historico.get(0).tipoConsulta);
    }

    @Test
    @DisplayName("Should preserve case as provided")
    void testRegistarConsulta_CasePreserved() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        
        List<AuditoriaGateway.AuditoriaConsulta> historico = gateway.listarHistorico(connection);
        assertEquals(1, historico.size());
        assertEquals("previsao_atual", historico.get(0).tipoConsulta);
    }

    @Test
    @DisplayName("Should trim tipoConsulta")
    void testRegistarConsulta_Trims() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "  previsao_atual  ");
        
        List<AuditoriaGateway.AuditoriaConsulta> historico = gateway.listarHistorico(connection);
        assertEquals(1, historico.size());
        assertEquals("previsao_atual", historico.get(0).tipoConsulta);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null tipoConsulta")
    void testRegistarConsulta_Null() {
        assertThrows(IllegalArgumentException.class, 
            () -> gateway.registarConsulta(connection, autorId, localidadeId, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid tipoConsulta")
    void testRegistarConsulta_Invalid() {
        assertThrows(IllegalArgumentException.class, 
            () -> gateway.registarConsulta(connection, autorId, localidadeId, "INVALIDO"));
    }

    @Test
    @DisplayName("Should accept valid tipos: previsao_atual, estimar_previsao, historico")
    void testRegistarConsulta_ValidTypes() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId, localidadeId, "estimar_previsao");
        gateway.registarConsulta(connection, autorId, localidadeId, "historico");
        
        List<AuditoriaGateway.AuditoriaConsulta> historico = gateway.listarHistorico(connection);
        assertEquals(3, historico.size());
    }

    @Test
    @DisplayName("Should list all audit history")
    void testListarHistorico() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId, localidadeId, "historico");
        
        List<AuditoriaGateway.AuditoriaConsulta> historico = gateway.listarHistorico(connection);
        
        assertEquals(2, historico.size());
        assertNotNull(historico.get(0).autor);
        assertNotNull(historico.get(0).localidade);
        assertNotNull(historico.get(0).dataHora);
    }

    @Test
    @DisplayName("Should count consultations for location")
    void testContarConsultasAtualPorLocalidade() throws PersistenceException {
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId, localidadeId, "historico");
        
        long count = gateway.contarConsultasAtualPorLocalidade(connection, localidadeId);
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should return zero when no consultations")
    void testContarConsultasAtualPorLocalidade_Zero() throws PersistenceException {
        long count = gateway.contarConsultasAtualPorLocalidade(connection, localidadeId);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should find top author by location")
    void testTopAutorPorLocalidade() throws Exception {
        long autorId2;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO Autor(nome) VALUES ('Autor 2')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                autorId2 = rs.getLong(1);
            }
        }
        
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual");
        gateway.registarConsulta(connection, autorId2, localidadeId, "previsao_atual");
        
        AuditoriaGateway.TopAutor top = gateway.topAutorPorLocalidade(connection, localidadeId);
        assertNotNull(top);
        assertEquals("Teste Autor", top.nomeAutor);
        assertEquals(2, top.totalConsultas);
    }

    @Test
    @DisplayName("Should return default when no author found")
    void testTopAutorPorLocalidade_NoAuthor() throws PersistenceException {
        AuditoriaGateway.TopAutor top = gateway.topAutorPorLocalidade(connection, localidadeId);
        assertNotNull(top);
        assertNull(top.nomeAutor);
        assertEquals(0, top.totalConsultas);
    }

    @Test
    @DisplayName("Should throw PersistenceException on SQL error")
    void testRegistarConsulta_SQLError() throws Exception {
        connection.close();
        
        assertThrows(PersistenceException.class, 
            () -> gateway.registarConsulta(connection, autorId, localidadeId, "previsao_atual"));
    }

    @Test
    @DisplayName("Should throw PersistenceException on listarHistorico SQL error")
    void testListarHistorico_SQLError() throws Exception {
        connection.close();
        
        assertThrows(PersistenceException.class, 
            () -> gateway.listarHistorico(connection));
    }
}

