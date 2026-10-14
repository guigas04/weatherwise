package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

public class CondicaoClimaticaGateway {

    public boolean exists(Connection conn, String condicao) throws PersistenceException {
        if (condicao == null || condicao.isBlank()) return false;
        final String sql =
            "SELECT EXISTS (" +
            "  SELECT 1 FROM pg_type t " +
            "  JOIN pg_enum e ON t.oid = e.enumtypid " +
            "  WHERE t.typname = 'CondicaoClimatica' AND e.enumlabel = ?" +
            ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, condicao.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao validar condicao_climatica_enum: " + condicao, e);
        }
    }

    public List<String> listAll(Connection conn) throws PersistenceException {
        final String sql =
            "SELECT e.enumlabel " +
            "FROM pg_type t JOIN pg_enum e ON t.oid = e.enumtypid " +
            "WHERE t.typname = 'CondicaoClimatica' " +
            "ORDER BY e.enumsortorder";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) result.add(rs.getString(1));
                return result;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao listar condicao_climatica_enum", e);
        }
    }
}
