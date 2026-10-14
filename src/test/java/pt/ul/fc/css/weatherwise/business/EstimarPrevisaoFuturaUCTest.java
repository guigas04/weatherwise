package pt.ul.fc.css.weatherwise.business;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EstimarPrevisaoFuturaUC Tests")
class EstimarPrevisaoFuturaUCTest {

    private Connection connection;
    private EstimarPrevisaoFuturaUC useCase;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        useCase = new EstimarPrevisaoFuturaUC();
        
        DataSource.INSTANCE.close();
        
        try (Statement stmt = connection.createStatement()) {
            // Criar tabelas necessárias
            stmt.execute("CREATE TABLE IF NOT EXISTS Localidade (" +
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(255) NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS RegistoMeteorologico (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "localidadeId BIGINT NOT NULL, " +
                    "condicaoClimatica VARCHAR(50) NOT NULL, " +
                    "data DATE NOT NULL, " +
                    "temperatura DECIMAL(5,2) NOT NULL, " +
                    "FOREIGN KEY (localidadeId) REFERENCES Localidade(Id))");
            
            // Limpar dados
            stmt.execute("DELETE FROM RegistoMeteorologico");
            stmt.execute("DELETE FROM Localidade");
            
            // Inserir localidade de teste
            stmt.execute("INSERT INTO Localidade (nome) VALUES ('Lisboa')");
            
            // Inserir dados históricos de teste para diferentes condições
            long localidadeId = 1;
            LocalDate hoje = LocalDate.now();
            
            // Inserir 10 dias de SOL (maioria)
            for (int i = 1; i <= 10; i++) {
                stmt.execute(String.format(
                    "INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'SOL', DATE '%s', 25.0)",
                    localidadeId, hoje.minusDays(i)
                ));
            }
            
            // Inserir 5 dias de CHUVA (minoria)
            for (int i = 11; i <= 15; i++) {
                stmt.execute(String.format(
                    "INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'CHUVA', DATE '%s', 18.0)",
                    localidadeId, hoje.minusDays(i)
                ));
            }
            
            // Inserir 3 dias de NUBLADO
            for (int i = 16; i <= 18; i++) {
                stmt.execute(String.format(
                    "INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'NUBLADO', DATE '%s', 20.0)",
                    localidadeId, hoje.minusDays(i)
                ));
            }
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
    @DisplayName("Should estimate most probable weather condition (mode)")
    void testEstimarPrevisaoFutura() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        // Testar com 15 dias - SOL deve ser a moda (10 dias SOL vs 5 dias CHUVA)
        String previsao = useCase.executar(1L, 15);
        
        assertNotNull(previsao);
        assertEquals("SOL", previsao);
    }

    @Test
    @DisplayName("Should handle different historical periods")
    void testEstimarPrevisaoFutura_DifferentPeriods() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        // Testar com 8 dias - ainda deve ser SOL
        String previsao8Dias = useCase.executar(1L, 8);
        assertEquals("SOL", previsao8Dias);
        
        // Testar com 13 dias - deve ser SOL (10 SOL vs 3 CHUVA nos últimos 13 dias)
        String previsao13Dias = useCase.executar(1L, 13);
        assertEquals("SOL", previsao13Dias);
    }

    @Test
    @DisplayName("Should throw exception for invalid localidadeId")
    void testEstimarPrevisaoFutura_InvalidLocalidade() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar(-1L, 15));
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar(0L, 15));
    }

    @Test
    @DisplayName("Should throw exception for invalid days")
    void testEstimarPrevisaoFutura_InvalidDays() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar(1L, -5));
        
        assertThrows(IllegalArgumentException.class, 
            () -> useCase.executar(1L, 0));
    }

    @Test
    @DisplayName("Should throw exception when no historical data")
    void testEstimarPrevisaoFutura_NoData() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        // Tentar com localidade que não existe
        assertThrows(PersistenceException.class, 
            () -> useCase.executar(999L, 15));
    }

    @Test
    @DisplayName("Should handle database connection errors")
    void testEstimarPrevisaoFutura_DatabaseError() throws Exception {
        // Fecha conexão para forçar erro
        connection.close();
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        assertThrows(PersistenceException.class, 
            () -> useCase.executar(1L, 15));
    }

    @Test
    @DisplayName("Should calculate correct mode with tie scenario")
    void testEstimarPrevisaoFutura_TieScenario() throws Exception {
        java.lang.reflect.Field field = DataSource.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(DataSource.INSTANCE, connection);
        
        // Limpar e criar cenário de empate
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM RegistoMeteorologico");
            
            long localidadeId = 1;
            LocalDate hoje = LocalDate.now();
            
            // Inserir 5 dias de SOL
            for (int i = 1; i <= 5; i++) {
                stmt.execute(String.format(
                    "INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'SOL', DATE '%s', 25.0)",
                    localidadeId, hoje.minusDays(i)
                ));
            }
            
            // Inserir 5 dias de CHUVA (empate)
            for (int i = 6; i <= 10; i++) {
                stmt.execute(String.format(
                    "INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) " +
                    "VALUES (%d, 'CHUVA', DATE '%s', 18.0)",
                    localidadeId, hoje.minusDays(i)
                ));
            }
        }
        
        // Deve retornar uma das condições (a primeira encontrada com frequência máxima)
        String previsao = useCase.executar(1L, 10);
        assertNotNull(previsao);
        assertTrue(previsao.equals("SOL") || previsao.equals("CHUVA"));
    }
}