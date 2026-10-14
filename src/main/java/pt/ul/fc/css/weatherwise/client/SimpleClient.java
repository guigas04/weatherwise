package pt.ul.fc.css.weatherwise.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ul.fc.css.weatherwise.dbutils.SetupDatabase;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;
import java.time.LocalDate;
import pt.ul.fc.css.weatherwise.business.AtualizarOuCriarPrevisaoHistorica;
import pt.ul.fc.css.weatherwise.business.ConsultarPrevisaoAtual;
import pt.ul.fc.css.weatherwise.business.ObterAuditoria;
import pt.ul.fc.css.weatherwise.business.EstimarPrevisaoFuturaUC;


@SpringBootApplication
public class SimpleClient {

    public static void main(String[] args) {
        try {
            SetupDatabase.run();
        } catch (PersistenceException e) {
            System.err.println("FATAL: Could not setup the database.");
            e.printStackTrace();
            return;
        }
        SpringApplication.run(SimpleClient.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return (args) -> {

            // Dados de teste
            
            String localidade = "Lisboa";
            LocalDate data = LocalDate.of(2025, 10, 29);
            String condicao = "SOL";
            double temperatura = 23.7;


            AtualizarOuCriarPrevisaoHistorica ts = new AtualizarOuCriarPrevisaoHistorica();
            ts.executar(localidade, data, condicao, temperatura);
            
            new ObterAuditoria().imprimirHistorico();

            var useCase2 = new ConsultarPrevisaoAtual();
            useCase2.autorMaisConsultas("Lisboa");

            var useCase = new ConsultarPrevisaoAtual();
            useCase.executarEImprimir("Faro");
            useCase.contarConsultas("Faro");
            
            EstimarPrevisaoFuturaUC estimarUC = new EstimarPrevisaoFuturaUC();
            estimarUC.executar("Aveiro", 10);

            EstimarPrevisaoFuturaUC estimarUC2 = new EstimarPrevisaoFuturaUC();
            estimarUC2.executar("Porto", 10);
            
        };
    }
}
