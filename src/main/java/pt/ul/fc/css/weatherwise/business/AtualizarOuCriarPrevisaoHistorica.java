package pt.ul.fc.css.weatherwise.business;

import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.LocalidadeGateway;
import pt.ul.fc.css.weatherwise.dataaccess.rowdatagateways.RegistoMeterelogicoGateway;
import pt.ul.fc.css.weatherwise.dataaccess.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AtualizarOuCriarPrevisaoHistorica {

    public void executar(String nomeLocalidade, LocalDate data, String condicao, double temperatura) {
        Connection conn = null;
        try {
            // Garantir ligação ativa e transação
            DataSource.INSTANCE.connect();
            conn = DataSource.INSTANCE.getConnection();
            conn.setAutoCommit(false);

            LocalidadeGateway localidadeGateway = new LocalidadeGateway();
            Long localidadeId = localidadeGateway.inserirSeNaoExistir(conn, nomeLocalidade);
            if (localidadeId == null) {
                System.err.println("Erro: Localidade não encontrada/criada: " + nomeLocalidade);
                if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
                return;
            }

            RegistoMeterelogicoGateway registoGateway = new RegistoMeterelogicoGateway();
            if (registoGateway.existeRegistoCamel(conn, localidadeId, data)) {
                registoGateway.atualizarCamel(conn, localidadeId, data, condicao, temperatura);
                System.out.println("Registo histórico atualizado com sucesso.");
            } else {
                registoGateway.inserirCamel(conn, localidadeId, data, condicao, temperatura);
                System.out.println("Registo histórico criado com sucesso.");
            }

            conn.commit();
        } catch (Exception e) {
            System.err.println("Erro ao executar o caso de uso (AtualizarOuCriarPrevisaoHistorica): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { 
                    conn.rollback(); 
                } catch (SQLException ignored) {}
            }
        } finally {
            // Restaura auto-commit mas NÃO fecha a conexão singleton
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }
}
