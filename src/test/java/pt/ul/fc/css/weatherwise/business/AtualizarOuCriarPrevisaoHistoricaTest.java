package pt.ul.fc.css.weatherwise.business;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.RegistoMeterelogicoGateway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AtualizarOuCriarPrevisaoHistorica Tests")
class AtualizarOuCriarPrevisaoHistoricaTest {

    private Connection connection;
    private AtualizarOuCriarPrevisaoHistorica useCase;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        useCase = new AtualizarOuCriarPrevisaoHistorica();
        
        // Configura DataSource para usar a conexão de teste
        DataSource.INSTANCE.close();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Localidade (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS RegistoMeteorologico (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "localidadeId BIGINT NOT NULL, " +
                    "condicaoClimatica VARCHAR(50), " +
                    "data DATE NOT NULL, " +
                    "temperatura DECIMAL(5,2), " +
                    "FOREIGN KEY (localidadeId) REFERENCES Localidade(Id), " +
                    "UNIQUE(localidadeId, data))");
            
            stmt.execute("DELETE FROM RegistoMeteorologico");
            stmt.execute("DELETE FROM Localidade");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        DataSource.INSTANCE.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Should create new historical record")
    void testExecutar_NewRecord() throws Exception {
        LocalDate data = LocalDate.of(2025, 1, 15);
        String localidade = "Lisboa";
        
        // Mock DataSource connection
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        field.setAccessible(false);
        
        useCase.executar(localidade, data, "SOL", 25.5);
        
        // Verifica se foi inserido
        RegistoMeterelogicoGateway gateway = new RegistoMeterelogicoGateway();
        LocalidadeGateway localGateway = new LocalidadeGateway();
        long localidadeId = localGateway.findOrInsert(connection, localidade);
        
        assertTrue(gateway.existeRegistoCamel(connection, localidadeId, data));
    }

    @Test
    @DisplayName("Should update existing historical record")
    void testExecutar_UpdateRecord() throws Exception {
        LocalDate data = LocalDate.of(2025, 1, 15);
        String localidade = "Porto";
        
        // Mock DataSource connection
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        field.setAccessible(false);
        
        // Cria primeiro
        useCase.executar(localidade, data, "SOL", 25.5);
        
        // Atualiza
        useCase.executar(localidade, data, "CHUVA", 18.0);
        
        // Verifica atualização
        RegistoMeterelogicoGateway gateway = new RegistoMeterelogicoGateway();
        LocalidadeGateway localGateway = new LocalidadeGateway();
        long localidadeId = localGateway.findOrInsert(connection, localidade);
        
        var registoOpt = gateway.findAtualByLocalidadeId(connection, localidadeId);
        assertTrue(registoOpt.isPresent());
        assertEquals("CHUVA", registoOpt.get().condicao);
        assertEquals(18.0, registoOpt.get().temperatura.doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Should handle invalid location gracefully")
    void testExecutar_InvalidLocation() throws Exception {
        // Mock DataSource connection com conexão fechada para simular erro
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        Connection closedConn = DriverManager.getConnection("jdbc:h2:mem:closed;DB_CLOSE_DELAY=-1");
        closedConn.close();
        field.set(DataSource.INSTANCE, closedConn);
        field.setAccessible(false);
        
        // Não deve lançar exceção, apenas imprimir erro
        assertDoesNotThrow(() -> {
            useCase.executar("LocalidadeInexistente", LocalDate.now(), "SOL", 25.0);
        });
    }

    @Test
    @DisplayName("Should create location if it doesn't exist")
    void testExecutar_CreatesLocation() throws Exception {
        LocalDate data = LocalDate.of(2025, 1, 15);
        String localidade = "NovaLocalidade";
        
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        field.setAccessible(false);
        
        useCase.executar(localidade, data, "SOL", 25.5);
        
        // Verifica se localidade foi criada
        LocalidadeGateway localGateway = new LocalidadeGateway();
        var localOpt = localGateway.findIdByName(connection, localidade);
        assertTrue(localOpt.isPresent());
    }

    @Test
    @DisplayName("Should handle multiple locations")
    void testExecutar_MultipleLocations() throws Exception {
        LocalDate data = LocalDate.of(2025, 1, 15);
        
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        field.setAccessible(false);
        
        useCase.executar("Lisboa", data, "SOL", 25.5);
        useCase.executar("Porto", data, "CHUVA", 18.0);
        useCase.executar("Faro", data, "NUBLADO", 22.0);
        
        LocalidadeGateway localGateway = new LocalidadeGateway();
        RegistoMeterelogicoGateway gateway = new RegistoMeterelogicoGateway();
        
        assertTrue(gateway.existeRegistoCamel(connection, 
            localGateway.findOrInsert(connection, "Lisboa"), data));
        assertTrue(gateway.existeRegistoCamel(connection, 
            localGateway.findOrInsert(connection, "Porto"), data));
        assertTrue(gateway.existeRegistoCamel(connection, 
            localGateway.findOrInsert(connection, "Faro"), data));
    }
}

