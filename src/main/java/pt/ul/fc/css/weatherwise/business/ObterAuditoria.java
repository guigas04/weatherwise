package pt.ul.fc.css.weatherwise.business;

import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AuditoriaGateway;
import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;
import java.util.List;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.AutorGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;

public class ObterAuditoria {
    public static class AuditoriaConsulta {
        public final String autor;
        public final String localidade;
        public final java.sql.Timestamp dataHora;
        public final String tipoConsulta;
        public AuditoriaConsulta(String autor, String localidade, String tipoConsulta, java.sql.Timestamp dataHora) {
            this.autor = autor;
            this.localidade = localidade;
            this.tipoConsulta = tipoConsulta;
            this.dataHora = dataHora;
        }
    }

    public List<AuditoriaGateway.AuditoriaConsulta> listarHistorico() throws PersistenceException {
        try {
            var conn = pt.ul.fc.css.weatherwise.dataaccess.DataSource.INSTANCE.getConnection();
            var autorGateway = new AutorGateway();
            var auditoriaGateway = new AuditoriaGateway();
            var localidadeGateway = new LocalidadeGateway();
            long autorId = autorGateway.findOrInsert(conn, "Sistema Automático");
            long localidadeId = localidadeGateway.findOrInsert(conn, "Global");
            auditoriaGateway.registarConsulta(conn, autorId, localidadeId, "historico");
        } catch (Exception e) {
        }
        return new AuditoriaGateway().listarHistorico(pt.ul.fc.css.weatherwise.dataaccess.DataSource.INSTANCE.getConnection());
    }

    public void imprimirHistorico() throws PersistenceException {
        var historico = listarHistorico();
        System.out.println("\n===== Histórico de Auditoria =====");
        for (var audit : historico) {
            System.out.println(String.format(
                "Autor: %s | Localidade: %s | Tipo: %s | DataHora: %s",
                audit.autor, audit.localidade, audit.tipoConsulta, audit.dataHora
            ));
        }
    }
}
