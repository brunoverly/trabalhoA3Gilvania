package com.example.trabalhoA3Gilvania;
import java.sql.*;

public class DataBaseConection {
    public Connection databaseLink;
    public Connection getConection(){
        String databaseName = "projeto_java_a3";
        String databaseUser = "master";
        String databasePassword = "bJb%2up0vHcSE^"; // sem \n
        String url = "jdbc:mysql://azuremysqlbruno.mysql.database.azure.com:3306/"
                + databaseName
                + "?useSSL=true&requireSSL=true&serverTimezone=UTC";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        }
        catch(Exception e){
            e.printStackTrace();
            e.getCause();
        }

        return databaseLink;
    }
    public void AtualizarBanco(String tipo, String cod_os, String descricao, int matricula) {
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySQL = """
                        INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula)
                        VALUES (?, ?, ?, ?)
                    """;
            try (PreparedStatement statement = connectDB.prepareStatement(querySQL)) {
                statement.setString(1, tipo);
                statement.setString(2, cod_os);
                statement.setString(3, descricao);
                statement.setInt(4, matricula);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public void AtualizarStatusPorSolicitacao(int idOperacao) {
        int totalSolicitado = 0;
        int totalRecebido = 0;
        int totalEntregue = 0;
        int totalEmEspera = 0;

        try (Connection connectDB = new DataBaseConection().getConection()) {

            // Busca itens solicitados
            String sqlBuscaOperacao = """
            SELECT COUNT(*) AS total 
            FROM item 
            WHERE status = ? 
            AND id_operacao = ?
        """;

            try (PreparedStatement statement = connectDB.prepareStatement(sqlBuscaOperacao)) {
                statement.setString(1, "Solicitado na oficina");
                statement.setInt(2, idOperacao);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        totalSolicitado = rs.getInt("total");
                    }
                }
            }

            // Se encontrou algum item solicitado, atualiza o status da operação
            if (totalSolicitado > 0) {
                try (PreparedStatement statement2 = connectDB.prepareStatement(
                        "UPDATE operacao SET status = 'Item(s) solicitados' WHERE id = ?")) {
                    statement2.setInt(1, idOperacao);
                    statement2.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar total de itens solicitados", e);
        }

        // Se não há itens solicitados, verifica se estão aguardando entrega
        if (totalSolicitado == 0) {
            try (Connection connectDB = new DataBaseConection().getConection()) {
                String sqlBuscaOperacao = """
                    SELECT COUNT(*) AS total
                    FROM item
                    WHERE id_operacao = ?
                    AND (status = 'Recebido' OR status = 'Aguardando entrega')
                """;

                try (PreparedStatement statement = connectDB.prepareStatement(sqlBuscaOperacao)) {
                    statement.setInt(1, idOperacao);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            totalEmEspera = rs.getInt("total");
                        }
                    }
                }

                // Se não há itens solicitados nem aguardando entrega, define como entregue
                if (totalEmEspera == 0 && totalSolicitado == 0) {
                    try (PreparedStatement statement = connectDB.prepareStatement(
                            "UPDATE operacao SET status = 'Item(s) entregue(s)' WHERE id = ?")) {
                        statement.setInt(1, idOperacao);
                        statement.executeUpdate();
                    }
                }
                else{
                    try (PreparedStatement statement = connectDB.prepareStatement(
                            "UPDATE operacao SET status = 'Em espera' WHERE id = ?")) {
                        statement.setInt(1, idOperacao);
                        statement.executeUpdate();
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Erro ao procurar total itens em aguardo", e);
            }
        }
    }


}
