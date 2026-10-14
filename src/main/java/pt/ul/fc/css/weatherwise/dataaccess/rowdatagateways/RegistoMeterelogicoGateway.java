package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

public class RegistoMeterelogicoGateway {

    public static final class Registo {
        public final long id;
        public final long localidadeId;
        public final String condicao; 
        public final Date data;
        public final BigDecimal temperatura;
        public final boolean ehAtual;

        public Registo(long id, long localidadeId, String condicao, Date data, BigDecimal temperatura, boolean ehAtual) {
            this.id = id;
            this.localidadeId = localidadeId;
            this.condicao = condicao;
            this.data = data;
            this.temperatura = temperatura;
            this.ehAtual = ehAtual;
        }
    }

    public Optional<Registo> findAtualByLocalidadeId(Connection conn, long localidadeId) throws PersistenceException {
        final String sql = "SELECT id, localidadeId, condicaoClimatica, data, temperatura " +
                           "FROM RegistoMeteorologico " +
                           "WHERE localidadeId = ? " +
                           "ORDER BY data DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao obter registo atual da localidade " + localidadeId, e);
        }
    }

    public Optional<Registo> findMaisRecenteByLocalidadeId(Connection conn, long localidadeId) throws PersistenceException {
        final String sql = "SELECT id, localidadeId, condicaoClimatica, data, temperatura " +
                           "FROM RegistoMeteorologico " +
                           "WHERE localidadeId = ? " +
                           "ORDER BY data DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao obter registo mais recente da localidade " + localidadeId, e);
        }
    }



    // Verifica se já existe registo para localidade e data (schema camel case)
    public boolean existeRegistoCamel(Connection conn, long localidadeId, java.time.LocalDate data) throws SQLException {
        String sql = "SELECT 1 FROM RegistoMeteorologico WHERE localidadeId = ? AND data = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            ps.setDate(2, java.sql.Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Atualiza registo existente
    public void atualizarCamel(Connection conn, long localidadeId, java.time.LocalDate data, String condicaoClimatica, double temperatura) throws SQLException {
        String sql = """
            UPDATE RegistoMeteorologico
               SET condicaoClimatica = ?::CondicaoClimatica,
                   temperatura = ?
             WHERE localidadeId = ? AND data = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, condicaoClimatica);
            ps.setDouble(2, temperatura);
            ps.setLong(3, localidadeId);
            ps.setDate(4, java.sql.Date.valueOf(data));
            ps.executeUpdate();
        }
    }

    // Insere novo registo
    public void inserirCamel(Connection conn, long localidadeId, java.time.LocalDate data, String condicaoClimatica, double temperatura) throws SQLException {
        String sql = """
            INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura)
            VALUES (?, ?::CondicaoClimatica, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            ps.setString(2, condicaoClimatica);
            ps.setDate(3, java.sql.Date.valueOf(data));
            ps.setDouble(4, temperatura);
            ps.executeUpdate();
        }
    }

    // Obtém condições climáticas históricas a partir de uma data limite
    public List<String> findCondicoesHistoricasByLocalidadeId(Connection conn, long localidadeId, LocalDate dataLimite) throws PersistenceException {
        final String sql = """
            SELECT condicaoClimatica 
            FROM RegistoMeteorologico 
            WHERE localidadeId = ? AND data >= ? 
            ORDER BY data DESC
        """;
        List<String> condicoes = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, localidadeId);
            ps.setDate(2, java.sql.Date.valueOf(dataLimite));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    condicoes.add(rs.getString("condicaoClimatica"));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao obter condições históricas da localidade " + localidadeId, e);
        }
        return condicoes;
    }


    private Registo map(ResultSet rs) throws SQLException {
        return new Registo(
            rs.getLong("id"),
            rs.getLong("localidadeId"),
            rs.getString("condicaoClimatica"),
            rs.getDate("data"),
            rs.getBigDecimal("temperatura"),
            false // ehAtual desativado
        );
    }
}
