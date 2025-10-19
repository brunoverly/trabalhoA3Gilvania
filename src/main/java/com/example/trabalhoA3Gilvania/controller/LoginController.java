package com.example.trabalhoA3Gilvania.controller;
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.screen.InicioScreen;
import com.example.trabalhoA3Gilvania.Sessao;
import com.mysql.cj.xdevapi.Warning;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.scene.image.Image;
import java.sql.*;
import java.util.ResourceBundle;


public class LoginController implements Initializable {
    @FXML private Button loginButton;
    @FXML private Label loginErrorMessage;
    @FXML private ImageView brand;
    @FXML private ImageView login1;
    @FXML private ImageView login2;
    @FXML private ImageView login3;
    @FXML private TextField enterUserNameField;
    @FXML private TextField enterPasswordField;
    @FXML private Button loginButtonFechar;
    @FXML private ImageView loginImagemFechar;

    FormsUtil alerta = new FormsUtil();

    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Carreegar imagens da janela
        URL imagemPrincipalURL = getClass().getResource("/imagens/login1.jpg");
        Image imagemPrincipal = new Image(imagemPrincipalURL.toExternalForm());
        login1.setImage(imagemPrincipal);

        URL imagemUsuarioURL = getClass().getResource("/imagens/login2.png");
        Image imagemUsuario = new Image(imagemUsuarioURL.toExternalForm());
        login2.setImage(imagemUsuario);

        URL imagemPinURL = getClass().getResource("/imagens/login3.png");
        Image imagemPin = new Image(imagemPinURL.toExternalForm());
        login3.setImage(imagemPin);

        URL imagemBrandURL = getClass().getResource("/imagens/brand.png");
        Image imagemBrand = new Image(imagemBrandURL.toExternalForm());
        brand.setImage(imagemBrand);

        URL loginImagemFecharURL = getClass().getResource("/imagens/close.png");
        Image loginImagemFecharImagem = new Image(loginImagemFecharURL.toExternalForm());
        loginImagemFechar.setImage(loginImagemFecharImagem);

        //Aumentar o icone ao passar o mouse
        ImageView fecharImagem = (ImageView) loginButtonFechar.getGraphic();
        loginButtonFechar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.1);
            fecharImagem.setScaleY(1.1);
            loginButtonFechar.setCursor(Cursor.HAND); // cursor muda para mão
        });
        loginButtonFechar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            loginButtonFechar.setCursor(Cursor.DEFAULT);
        });

    }

    //Acao ao clicar em fechar janela
    public void loginButtonFecharOnAction(ActionEvent event){
    Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
    //Acao ao clicar me login
    public void LoginButtonOnAction(ActionEvent event) {
        if ((enterUserNameField.getText().isBlank() == false) && (enterPasswordField.getText().isBlank() == false)) {
            validateLogin();
        } else {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe usuário e PIN para prosseguir")
                    .showAndWait();
        }
    }

    //Validar o login conectando ao banco de dados
    public void validateLogin() {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connectDB = connectNow.getConection();
        try {
            String querySqlUser = """
                        SELECT nome, matricula, cargo
                        FROM users
                        WHERE matricula = ? AND pin = ?
                    """;
            try (PreparedStatement buscaUsuario = connectDB.prepareStatement(querySqlUser)) {
                buscaUsuario.setInt(1, Integer.parseInt(enterUserNameField.getText().trim()));
                buscaUsuario.setInt(2, Integer.parseInt(enterPasswordField.getText().trim()));
                ResultSet rs = buscaUsuario.executeQuery();
                if (rs.next()) {
                    Sessao.setUsuario(
                            rs.getInt("matricula"),
                            rs.getString("nome"),
                            rs.getString("cargo")
                    );
                    TelaInicial();
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.close();

                }
                else{
                    alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Usuário ou PIN inválidos")
                            .showAndWait();
                }
            }
            catch (NumberFormatException e){
                alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Insira valores númericos para matrícula e PIN")
                        .showAndWait();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
    }
    //Chama a tela inicial
    public static void TelaInicial() {
        InicioScreen telaInicial = new InicioScreen();
        Stage stage = new Stage();
        telaInicial.start(stage);
    }
}

