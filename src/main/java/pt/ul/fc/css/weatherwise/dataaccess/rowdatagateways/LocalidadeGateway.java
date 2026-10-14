package pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways;

import java.sql.*;
import java.util.Optional;

import pt.ul.fc.css.weatherwise.dataaccess.exception.PersistenceException;

/**
 * Gateway responsável pelo acesso à tabela "location".
 * Fornece métodos para encontrar ou inserir localidades na base de dados.
 */
public class LocalidadeGateway {

    public Optional<Long> findIdByName(Connection conn, String name) throws PersistenceException {
        final String sql = "SELECT Id FROM Localidade WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong("id"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao procurar localidade: " + name, e);
        }
    }


    public long insert(Connection conn, String name) throws PersistenceException {
        final String sql = "INSERT INTO Localidade(nome) VALUES (?) RETURNING Id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                throw new PersistenceException("Falha ao inserir localidade: " + name);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao inserir localidade: " + name, e);
        }
    }


    public long findOrInsert(Connection conn, String name) throws PersistenceException {
        Optional<Long> existing = findIdByName(conn, name);
        if (existing.isPresent()) return existing.get();
        return insert(conn, name);
    }

    // Insere localidade se não existir; retorna sempre o id
    public Long inserirSeNaoExistir(Connection conn, String nome) throws SQLException, PersistenceException {
        Optional<Long> idOpt = findIdByName(conn, nome);
        if (idOpt.isPresent()) return idOpt.get();
        String insert = "INSERT INTO localidade (nome) VALUES (?) ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        Optional<Long> newIdOpt = findIdByName(conn, nome);
        if (newIdOpt.isPresent()) return newIdOpt.get();
        throw new PersistenceException("Falha ao inserir ou recuperar localidade: " + nome);
    }


    public Optional<String> findNameById(Connection conn, long id) throws PersistenceException {
        final String sql = "SELECT nome FROM Localidade WHERE Id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("nome"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao procurar localidade por ID: " + id, e);
        }
    }
}