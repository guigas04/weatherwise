package pt.ul.fc.css.weatherwise.business;

import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.RegistoMeterelogicoGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AutorGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AuditoriaGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstimarPrevisaoFuturaUC {

    private final RegistoMeterelogicoGateway registoGateway = new RegistoMeterelogicoGateway();

    public String executar(String nomeLocalidade, int diasHistorico) throws PersistenceException {
        if (nomeLocalidade == null || nomeLocalidade.isBlank()) {
            throw new IllegalArgumentException("nomeLocalidade não pode ser vazio");
        }

        DataSource dataSource = DataSource.INSTANCE.connect();
        Connection conn = dataSource.getConnection();

        var localidadeGateway = new LocalidadeGateway();
        long localidadeId = localidadeGateway.findOrInsert(conn, nomeLocalidade);

        return executar(localidadeId, diasHistorico);
    }

    public String executar(long localidadeId, int diasHistorico) throws PersistenceException {
        if (localidadeId <= 0) {
            throw new IllegalArgumentException("ID da localidade deve ser positivo");
        }
        
        if (diasHistorico <= 0) {
            throw new IllegalArgumentException("Número de dias históricos deve ser positivo");
        }

        DataSource dataSource = DataSource.INSTANCE.connect();
        Connection conn = dataSource.getConnection();

        LocalDate dataLimite = LocalDate.now().minusDays(diasHistorico);
        List<String> condicoesHistoricas = registoGateway.findCondicoesHistoricasByLocalidadeId(conn, localidadeId, dataLimite);

        if (condicoesHistoricas.isEmpty()) {
            throw new PersistenceException(
                "Não existem dados históricos para a localidade " + localidadeId + 
                " nos últimos " + diasHistorico + " dias");
        }

        String moda = calcularModa(condicoesHistoricas);

        // Registrar na auditoria
        var autorGateway = new AutorGateway();
        var auditoriaGateway = new AuditoriaGateway();
        long autorId = autorGateway.findOrInsert(conn, "Sistema Automático");
        auditoriaGateway.registarConsulta(conn, autorId, localidadeId, "estimar_previsao");

        // Obter nome da localidade para impressão
        var localidadeGateway = new LocalidadeGateway();
        var nomeLocalidadeOpt = localidadeGateway.findNameById(conn, localidadeId);
        String nomeLocalidade = nomeLocalidadeOpt.isPresent() ? nomeLocalidadeOpt.get() : "Localidade " + localidadeId;

        // Imprimir resultado
        System.out.println("\n===== Previsão Futura (Estimada) =====");
        System.out.println("Localidade = " + nomeLocalidade);
        System.out.println("Condição Climática Estimada: " + moda);

        return moda;
    }

    private String calcularModa(List<String> condicoesHistoricas) {
        Map<String, Integer> frequencias = new HashMap<>();
        
        for (String condicao : condicoesHistoricas) {
            frequencias.put(condicao, frequencias.getOrDefault(condicao, 0) + 1);
        }
        
        String condicaoMaisFrequente = null;
        int maxFrequencia = 0;
        
        for (Map.Entry<String, Integer> entry : frequencias.entrySet()) {
            if (entry.getValue() > maxFrequencia) {
                maxFrequencia = entry.getValue();
                condicaoMaisFrequente = entry.getKey();
            }
        }
        
        return condicaoMaisFrequente;
    }
}