package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

public class AutorGateway {

    public Optional<Long> findIdByNome(Connection conn, String nome) throws PersistenceException {
        final String sql = "SELECT Id FROM Autor WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong("Id"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao procurar autor: " + nome, e);
        }
    }

    public long insert(Connection conn, String nome) throws PersistenceException {
        final String sql = "INSERT INTO Autor(nome) VALUES (?) RETURNING Id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                throw new PersistenceException("Falha ao inserir autor: " + nome);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao inserir autor: " + nome, e);
        }
    }

    public long findOrInsert(Connection conn, String nome) throws PersistenceException {
        Optional<Long> existing = findIdByNome(conn, nome);
        if (existing.isPresent()) return existing.get();
        return insert(conn, nome);
    }
}
