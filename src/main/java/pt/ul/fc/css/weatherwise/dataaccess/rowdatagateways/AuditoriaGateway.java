package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

public class AuditoriaGateway {

    private static final Set<String> TIPOS_VALIDOS =
        Set.of("previsao_atual", "estimar_previsao", "historico");

    public void registarConsulta(Connection conn, long autorId, long localidadeId, String tipoConsulta)
            throws PersistenceException {
        if (tipoConsulta == null) {
            throw new IllegalArgumentException("tipoConsulta não pode ser nulo");
        }
        String tipo = tipoConsulta.trim();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("tipoConsulta inválido: " + tipoConsulta);
        }

        final String sql = "INSERT INTO Auditoria(autorId, localidadeId, tipoConsulta, dataHora) VALUES (?,?,?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, autorId);
            ps.setLong(2, localidadeId);
            ps.setString(3, tipo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao registar auditoria de consulta", e);
        }
    }

    public static class AuditoriaConsulta {
        public final String autor;
        public final String localidade;
        public final String tipoConsulta;
        public final java.sql.Timestamp dataHora;
        public AuditoriaConsulta(String autor, String localidade, String tipoConsulta, java.sql.Timestamp dataHora) {
            this.autor = autor;
            this.localidade = localidade;
            this.tipoConsulta = tipoConsulta;
            this.dataHora = dataHora;
        }
    }

    public List<AuditoriaConsulta> listarHistorico(Connection conn) throws PersistenceException {
        List<AuditoriaConsulta> result = new ArrayList<>();
        String sql = "SELECT a.nome as autor, l.nome as localidade, au.tipoConsulta, au.dataHora " +
                     "FROM Auditoria au " +
                     "JOIN Autor a ON au.autorId = a.Id " +
                     "JOIN Localidade l ON au.localidadeId = l.Id " +
                     "ORDER BY au.dataHora DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new AuditoriaConsulta(
                        rs.getString("autor"),
                        rs.getString("localidade"),
                        rs.getString("tipoConsulta"),
                        rs.getTimestamp("dataHora")
                    ));
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Erro ao obter histórico de auditoria", e);
        }
        return result;
    }

    public long contarConsultasAtualPorLocalidade(Connection conn, long localidadeId) throws PersistenceException {
        final String sql = "SELECT COUNT(*) FROM Auditoria WHERE localidadeId = ? AND tipoConsulta = 'previsao_atual'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                return 0L;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao contar consultas ATUAL para localidade " + localidadeId, e);
        }
    }

    public static class TopAutor {
        public final String nomeAutor;
        public final long totalConsultas;
        public TopAutor(String nomeAutor, long totalConsultas) {
            this.nomeAutor = nomeAutor;
            this.totalConsultas = totalConsultas;
        }
    }

    public TopAutor topAutorPorLocalidade(Connection conn, long localidadeId) throws PersistenceException {
        final String sql =
            "SELECT a.nome, COUNT(*) AS total " +
            "FROM Auditoria au JOIN Autor a ON au.autorId = a.Id " +
            "WHERE au.localidadeId = ? " +
            "  AND au.tipoConsulta = 'previsao_atual' " +
            "GROUP BY a.nome " +
            "ORDER BY total DESC, a.nome ASC " +
            "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TopAutor(rs.getString(1), rs.getLong(2));
                }
                return new TopAutor(null, 0L);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao obter autor com mais consultas para localidade " + localidadeId, e);
        }
    }
}
