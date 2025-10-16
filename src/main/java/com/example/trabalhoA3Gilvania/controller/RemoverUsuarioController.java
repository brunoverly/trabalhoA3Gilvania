package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;




public class RemoverUsuarioController implements Initializable {

    @FXML private ImageView remover1;
    @FXML private ImageView remover2;
    @FXML private ImageView remover3;


    @FXML private Button removeBuscarMatricula;
    @FXML private Button removeCancelarButton;
    @FXML private Button removeConfirmarButton;

    @FXML private AnchorPane removeUser;

    @FXML private TextField removeMatricula;

    @FXML private Label removeMotrarDadosUser;
    @FXML private Label removeNomeBuscado;
    @FXML private Label removeCodCargoBuscado;
    @FXML private Label removeMatriculaBuscado;
    @FXML private Label removeEmailBuscado;





    //Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL remover1ImageURL = getClass().getResource("/imagens/remover1.png");
        Image remover1Image = new Image(remover1ImageURL.toExternalForm());
        remover1.setImage(remover1Image);

        URL reomver2ImageURL = getClass().getResource("/imagens/remover2.png");
        Image remover2Image = new Image(reomver2ImageURL.toExternalForm());
        remover2.setImage(remover2Image);

        URL remover3ImageUrl = getClass().getResource("/imagens/remover3.png");
        Image remover3Image = new Image(remover3ImageUrl.toExternalForm());
        remover3.setImage(remover3Image);


    }

    public void removeCancelarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) removeCancelarButton.getScene().getWindow();
        stage.close();
    }

    public void removeBuscarMatriculaOnAction(ActionEvent event) {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connectDB = connectNow.getConection();

        int matricula = Integer.parseInt(removeMatricula.getText());

        String query = "SELECT nome, cod_cargo, email, senha FROM users WHERE matricula = " + matricula;

        try{
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) { // se encontrou resultado
                String nome = resultSet.getString("nome");
                int codCargo = resultSet.getInt("cod_cargo");
                String email = resultSet.getString("email");
                String senha = resultSet.getString("senha");

                removeMotrarDadosUser.setText("Dados do usuario selecionado: ");
                removeNomeBuscado.setText("Nome: " + nome);
                removeCodCargoBuscado.setText("Codigo do Cargo: " + codCargo);
                removeMatriculaBuscado.setText("Matricula: " + matricula);
                removeEmailBuscado.setText("Email: " + email);


            } else {
                removeMotrarDadosUser.setText("Usuário não encontrado!");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    @FXML
    private void removeConfirmarButtonOnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Remover Usuário");
        alert.setContentText("Tem certeza que deseja remover este usuário?");

        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            DataBaseConection connectNow = new DataBaseConection();
            Connection connectDB = connectNow.getConection();

            int matricula = Integer.parseInt(removeMatricula.getText());

            String query = "DELETE FROM users WHERE matricula = " + matricula;

            try{
                Statement statement = connectDB.createStatement();
                int linhasAfetadas = statement.executeUpdate(query);

                if (linhasAfetadas > 0 ) { // se encontrou resultado
                    Alert usuarioRemovido = new Alert(Alert.AlertType.INFORMATION);
                    usuarioRemovido.setTitle("Alerta");
                    usuarioRemovido.setHeaderText(null);
                    usuarioRemovido.setContentText("Usuario removido!");
                    usuarioRemovido.show();

                    removeMotrarDadosUser.setText("");
                    removeNomeBuscado.setText("");
                    removeCodCargoBuscado.setText("");
                    removeMatriculaBuscado.setText("");
                    removeEmailBuscado.setText("");

                } else {
                    removeMotrarDadosUser.setText("Usuario nao encontrado!");
                }
            }
            catch (Exception e){
                e.printStackTrace();
                e.getCause();
            }
        }
    }

}

