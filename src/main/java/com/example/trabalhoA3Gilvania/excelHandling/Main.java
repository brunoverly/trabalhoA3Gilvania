package com.example.trabalhoA3Gilvania.excelHandling;

import com.example.trabalhoA3Gilvania.screen.InicioScreen;
import com.example.trabalhoA3Gilvania.screen.LoginScreen;
import javafx.stage.Stage;

public class Main {

    public static void main (String[] args){
        LoginScreen telaLogin = new LoginScreen();
        Stage stage = new Stage();
        telaLogin.start(stage);

    }
}
