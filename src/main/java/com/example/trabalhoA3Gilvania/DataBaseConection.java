package com.example.trabalhoA3Gilvania;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
