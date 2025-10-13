package com.example.trabalhoA3Gilvania;
import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseConection {
    public Connection databaseLink;

    public Connection getConection(){
        String databaseName = "trabalho_a3_modelagem";
        String databaseUser = "root";
        String databasePassword = "1234";
        String url = "jdbc:mysql://localhost:3306/" + databaseName;

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
