package pt.ul.fc.css.weatherwise.business;

import java.sql.Connection;

import pt.ul.fc.css.weatherwise.dataaccess.DataSource;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.RegistoMeterelogicoGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AutorGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AuditoriaGateway;

public class ConsultarPrevisaoAtual {

    public static final class Resultado {
        public final long localidadeId;

        public Resultado(long localidadeId) {
            this.localidadeId = localidadeId;
        }
    }

    public static final class AutorContagem {
        public final String autor;
        public final long total;
        public AutorContagem(String autor, long total) {
            this.autor = autor;
            this.total = total;
        }
    }

    private final LocalidadeGateway localidadeGateway = new LocalidadeGateway();

    public Resultado executar(String nomeLocalidade) throws PersistenceException {
        if (nomeLocalidade == null || nomeLocalidade.isBlank()) {
            throw new IllegalArgumentException("nomeLocalidade não pode ser vazio");
        }

        DataSource dataSource = DataSource.INSTANCE.connect();
        Connection conn = dataSource.getConnection();

        long localidadeId = localidadeGateway.findOrInsert(conn, nomeLocalidade);
        return new Resultado(localidadeId);
    }

    public void executarEImprimir(String nomeLocalidade) throws PersistenceException {
        Resultado res = executar(nomeLocalidade);
        Connection conn = DataSource.INSTANCE.getConnection();

        var registosGateway = new RegistoMeterelogicoGateway();
        var registoOpt = registosGateway.findAtualByLocalidadeId(conn, res.localidadeId);
        if (registoOpt.isPresent()) {
            var registo = registoOpt.get();
            System.out.println("\n===== Previsão =====");
            System.out.println("Localidade = " + nomeLocalidade);
            System.out.println("Condição Climática Atual: " + registo.condicao);
            System.out.println("Temperatura Atual: " + registo.temperatura + " ºC");
        } else {
            System.out.println("Localidade = " + nomeLocalidade);
            System.out.println("Não existe registo meteorológico para esta localidade.");
        }

        var autorGateway = new AutorGateway();
        var auditoriaGateway = new AuditoriaGateway();
        long autorId = autorGateway.findOrInsert(conn, "Sistema Automático");
        auditoriaGateway.registarConsulta(conn, autorId, res.localidadeId, "previsao_atual");

    }

    public long contarConsultas(String nomeLocalidade) throws PersistenceException {
        if (nomeLocalidade == null || nomeLocalidade.isBlank()) {
            throw new IllegalArgumentException("nomeLocalidade não pode ser vazio");
        }
        DataSource dataSource = DataSource.INSTANCE.connect();
        Connection conn = dataSource.getConnection();
        long localidadeId = localidadeGateway.findOrInsert(conn, nomeLocalidade);
        var auditoriaGateway = new AuditoriaGateway();
        long total = auditoriaGateway.contarConsultasAtualPorLocalidade(conn, localidadeId);
        System.out.println("Consultas de previsão para " + nomeLocalidade + ": " + total);
        return total;
    }

    public AutorContagem autorMaisConsultas(String nomeLocalidade) throws PersistenceException {
        if (nomeLocalidade == null || nomeLocalidade.isBlank()) {
            throw new IllegalArgumentException("nomeLocalidade não pode ser vazio");
        }
        DataSource dataSource = DataSource.INSTANCE.connect();
        Connection conn = dataSource.getConnection();
        long localidadeId = localidadeGateway.findOrInsert(conn, nomeLocalidade);
        var auditoriaGateway = new AuditoriaGateway();
        var top = auditoriaGateway.topAutorPorLocalidade(conn, localidadeId);
        System.out.println("Autor com mais consultas para " + nomeLocalidade + ": " + top.nomeAutor + " (" + top.totalConsultas + ")");
        return new AutorContagem(top.nomeAutor, top.totalConsultas);
    }
}
