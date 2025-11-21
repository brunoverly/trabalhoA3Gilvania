package com.example.trabalhoA3Gilvania.Utils;
import java.sql.*;

public class DataBaseConection {
    public Connection databaseLink;
    public Connection getConection(){
        String databaseName = "projeto_java_a3";
        String databaseUser = "root";
        String databasePassword = "S@Jr%ilh%754vV"; // sem \n
        String url = "jdbc:mysql://localhost:3306/"
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
