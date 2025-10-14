package com.example.trabalhoA3Gilvania;
import java.sql.Connection;
import java.sql.DriverManager;

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

}
