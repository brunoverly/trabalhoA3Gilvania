package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.Sessao;
import com.example.trabalhoA3Gilvania.screen.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class InicioController implements Initializable {
    @FXML private Button menuAddUser;
    @FXML private Button menuRemoveUser;
    @FXML private Button menuSair;
    @FXML private Button menuImportarOs;
    @FXML private Button menuConsultOs;
    @FXML private Button menuCloseOs;
    @FXML private Button menuRetiradaItem;
    @FXML private Button menuEntradaItem;
    @FXML private Button menuSolcitarItem;

    @FXML private ImageView iniciologo;
    @FXML private ImageView inicio1;
    @FXML private ImageView inicio2;
    @FXML private ImageView inicio3;
    @FXML private ImageView inicio4;
    @FXML private ImageView inicio5;
    @FXML private ImageView inicio6;
    @FXML private ImageView inicio7;
    @FXML private ImageView inicio8;
    @FXML private ImageView inicio9;


    public void initialize(URL url, ResourceBundle resourceBundle) {

        URL inicioLogoURL = getClass().getResource("/imagens/brand.png");
        Image inicioLogo = new Image(inicioLogoURL.toExternalForm());
        iniciologo.setImage(inicioLogo);

        URL inicio1URL = getClass().getResource("/imagens/inicio1.png");
        Image inicio1Img = new Image(inicio1URL.toExternalForm());
        inicio1.setImage(inicio1Img);

        URL inicio2URL = getClass().getResource("/imagens/remover1.png");
        Image inicio2Img = new Image(inicio2URL.toExternalForm());
        inicio2.setImage(inicio2Img);

        URL inicio3URL = getClass().getResource("/imagens/inicio3.png");
        Image inicio3Img = new Image(inicio3URL.toExternalForm());
        inicio3.setImage(inicio3Img);

        URL inicio4URL = getClass().getResource("/imagens/entrada1.png");
        Image inicio4Img = new Image(inicio4URL.toExternalForm());
        inicio4.setImage(inicio4Img);

        URL inicio5URL = getClass().getResource("/imagens/entrega1.png");
        Image inicio5Img = new Image(inicio5URL.toExternalForm());
        inicio5.setImage(inicio5Img);

        URL inicio6URL = getClass().getResource("/imagens/cadastro1.png");
        Image inicio6Img = new Image(inicio6URL.toExternalForm());
        inicio6.setImage(inicio6Img);

        URL inicio7URL = getClass().getResource("/imagens/remover3.png");
        Image inicio7Img = new Image(inicio7URL.toExternalForm());
        inicio7.setImage(inicio7Img);

        URL inicio8URL = getClass().getResource("/imagens/inicio8.png");
        Image inicio8Img = new Image(inicio7URL.toExternalForm());
        inicio8.setImage(inicio7Img);

        URL inicio9URL = getClass().getResource("/imagens/inicio9.png");
        Image inicio9Img = new Image(inicio7URL.toExternalForm());
        inicio9.setImage(inicio7Img);

        verificarUsuario();
    }

    private void verificarUsuario() {

        if (Sessao.getCargo().equals("Mecanico")) {
            menuEntradaItem.setVisible(false);
            menuEntradaItem.setManaged(false);

            menuImportarOs.setVisible(false);
            menuImportarOs.setManaged(false);

            menuRetiradaItem.setVisible(false);
            menuRetiradaItem.setManaged(false);

            menuRemoveUser.setVisible(false);
            menuRemoveUser.setManaged(false);

            menuAddUser.setVisible(false);
            menuAddUser.setManaged(false);

            menuCloseOs.setVisible(false);
            menuCloseOs.setManaged(false);
        }


    }

    public void menuRemoveUserOnAction(ActionEvent event){
        try{
            RemoverUsuario();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }


    public void menuCloseOsOnAction(ActionEvent event){
        try{
            FecharOs();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    public void menuConsultOsOnAction(ActionEvent event){
        try{
            ConsultarOs();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }
    public void menuSolcitarItemOnAction(ActionEvent event){
        try{
            menuSolcitarItem("solicitar");
        }
        catch (Exception e){}
    }


    public void menuAddUserOnAction(ActionEvent event){
        try{
            CadastroUsuario();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    public void menuEntradaItemOnAction(ActionEvent event){
        try{
            menuSolcitarItem("entrada");
        }
        catch (Exception e){}
    }

    public void menuRetiradaItemOnAction(ActionEvent event) throws Exception {
        menuSolcitarItem("retirar");
    }



    public void menuSairOnAction (ActionEvent event){
        try{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação");
            alert.setHeaderText(null);
            alert.setContentText("Encerrar sessao?");

            Optional<ButtonType> resultado = alert.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK){
                Stage stage = (Stage) menuSair.getScene().getWindow();
                stage.close();
                Sessao.getCargo();
                Login();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    public void menuImportarOsOnAction(ActionEvent event) throws Exception {
        ImportarOs();
    }

    public static void CadastroUsuario() throws Exception {
        CadastrarUsuarioScreen cadastroUsuario = new CadastrarUsuarioScreen();
        Stage stage = new Stage();
        cadastroUsuario.start(stage);
    }

    public static void RemoverUsuario() throws Exception {
        RemoverUsuarioScreen removerUsuario = new RemoverUsuarioScreen();
        Stage stage = new Stage();
        removerUsuario.start(stage);
    }

    public static void Login() throws Exception {
        LoginScreen login = new LoginScreen();
        Stage stage = new Stage();
        login.start(stage);
    }
    public static void ImportarOs() throws Exception {
        ImportarOsScreen importarOs = new ImportarOsScreen();
        Stage stage = new Stage();
        importarOs.start(stage);
    }
    public static void FecharOs() throws Exception {
        FecharOsScreen importarOs = new FecharOsScreen();
        Stage stage = new Stage();
        importarOs.start(stage);
    }

    public static void ConsultarOs() throws Exception {
        ConsultarOsScreen novaJanela = new ConsultarOsScreen();
        Stage stage = new Stage();
        novaJanela.start(stage);
    }
    public void menuSolcitarItem(String modo) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/trabalhoA3Gilvania/consultarItem.fxml"));
        Parent root = loader.load();

        // Obtém o controller e passa o parâmetro
        ConsultarItemController controller = loader.getController();
        controller.setModo(modo);
        controller.AtualizarTituloPorModo();


        Stage stage = new Stage();
        stage.setTitle("Consulta de Itens");
        stage.setScene(new Scene(root));
        stage.show();
    }

}
