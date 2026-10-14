package pt.ul.fc.css.weatherwise.business;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AuditoriaGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.RegistoMeterelogicoGateway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsultarPrevisaoAtual Tests")
class ConsultarPrevisaoAtualTest {

    private Connection connection;
    private ConsultarPrevisaoAtual useCase;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        useCase = new ConsultarPrevisaoAtual();
        
        DataSource.INSTANCE.close();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Autor (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
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
            stmt.execute("CREATE TABLE IF NOT EXISTS Auditoria (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "autorId BIGINT NOT NULL, " +
                    "localidadeId BIGINT NOT NULL, " +
                    "tipoConsulta VARCHAR(50) NOT NULL, " +
                    "dataHora TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (autorId) REFERENCES Autor(Id), " +
                    "FOREIGN KEY (localidadeId) REFERENCES Localidade(Id))");
            
            stmt.execute("DELETE FROM Auditoria");
            stmt.execute("DELETE FROM RegistoMeteorologico");
            stmt.execute("DELETE FROM Autor");
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
    @DisplayName("Should throw IllegalArgumentException for null nomeLocalidade")
    void testExecutar_Null() {
        java.lang.reflect.Field field = null;
        try {
            field = DataSource.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(DataSource.INSTANCE, connection);
        } catch (Exception e) {
            // Ignore
        }
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank nomeLocalidade")
    void testExecutar_Blank() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar("   "));
    }

    @Test
    @DisplayName("Should return localidadeId for valid location")
    void testExecutar_ValidLocation() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        ConsultarPrevisaoAtual.Resultado resultado = useCase.executar("Lisboa");
        
        assertNotNull(resultado);
        assertTrue(resultado.localidadeId > 0);
    }

    @Test
    @DisplayName("Should create location if it doesn't exist")
    void testExecutar_CreatesLocation() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        useCase.executar("NovaLocalidade");
        
        LocalidadeGateway gateway = new LocalidadeGateway();
        var localOpt = gateway.findIdByName(connection, "NovaLocalidade");
        assertTrue(localOpt.isPresent());
    }

    @Test
    @DisplayName("Should register consultation in auditoria")
    void testExecutarEImprimir_RegistersAudit() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        // Insere registo meteorológico
        LocalidadeGateway localGateway = new LocalidadeGateway();
        long localidadeId = localGateway.findOrInsert(connection, "Lisboa");
        RegistoMeterelogicoGateway regGateway = new RegistoMeterelogicoGateway();
        regGateway.inserirCamel(connection, localidadeId, LocalDate.now(), "SOL", 25.0);
        
        useCase.executarEImprimir("Lisboa");
        
        AuditoriaGateway auditGateway = new AuditoriaGateway();
        long count = auditGateway.contarConsultasAtualPorLocalidade(connection, localidadeId);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should count consultations correctly")
    void testContarConsultas() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        String localidade = "Porto";
        
        // Cria algumas consultas
        useCase.executarEImprimir(localidade);
        useCase.executarEImprimir(localidade);
        
        long count = useCase.contarConsultas(localidade);
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should find top author for location")
    void testAutorMaisConsultas() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        String localidade = "Faro";
        
        // Cria consultas
        useCase.executarEImprimir(localidade);
        useCase.executarEImprimir(localidade);
        
        ConsultarPrevisaoAtual.AutorContagem result = useCase.autorMaisConsultas(localidade);
        
        assertNotNull(result);
        assertNotNull(result.autor);
        assertEquals(2, result.total);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for contarConsultas with null")
    void testContarConsultas_Null() {
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.contarConsultas(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for autorMaisConsultas with blank")
    void testAutorMaisConsultas_Blank() {
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.autorMaisConsultas("   "));
    }
}

