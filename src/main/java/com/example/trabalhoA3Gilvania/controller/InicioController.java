package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.Sessao;
import com.example.trabalhoA3Gilvania.screen.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
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
    @FXML private AnchorPane inicioPane;
    @FXML private Label inicioLabelOsAbertas;
    @FXML private Label inicioLabelOsEncerrada;
    @FXML private Label inicioLabelOsEmAndamento;
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
    @FXML private TableView<Atualizacao> inicioTableView;
    @FXML private TableColumn<Atualizacao, String> inicioTableData;
    @FXML private TableColumn<Atualizacao, String> inicioTableTipo;
    @FXML private TableColumn<Atualizacao, String> inicioTableOs;
    @FXML private TableColumn<Atualizacao, String> inicioTableDescricao;
    @FXML private TableColumn<Atualizacao, String> inicioTableUsuario;
    private ObservableList<Atualizacao> listaAtualizacoes = FXCollections.observableArrayList();



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

        Platform.runLater(() -> {
            inicioPane.requestFocus(); // força o foco para o pane após a tela ser exibida
        });

        verificarUsuario();
            inicioTableData.setCellValueFactory(new PropertyValueFactory<>("datahora"));
            inicioTableTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
            inicioTableOs.setCellValueFactory(new PropertyValueFactory<>("os"));
            inicioTableDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
            inicioTableUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));

            inicioTableView.setItems(listaAtualizacoes);
            inicioTableView.setSelectionModel(null);
            carregarAtualizacoes();
            atualizarDashBoard();


    }

    private void atualizarDashBoard() {
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String query = "SELECT COUNT(*) FROM ordem_servico " +
                    "WHERE status = 'aberta'";
            try (PreparedStatement stmt = connectDB.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    inicioLabelOsAbertas.setText(String.valueOf(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String query = "SELECT COUNT(*) FROM ordem_servico " +
                    "WHERE status = 'encerrada'";
            try (PreparedStatement stmt = connectDB.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    inicioLabelOsEmAndamento.setText(String.valueOf(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String query = "SELECT COUNT(DISTINCT cod_os) FROM operacao " +
                    "WHERE status = 'item(s) solicitados'";
            try (PreparedStatement stmt = connectDB.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    inicioLabelOsEncerrada.setText(String.valueOf(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void carregarAtualizacoes() {
        listaAtualizacoes.clear();

        try (Connection connectDB = new DataBaseConection().getConection()) {
            String query = "SELECT datahora, tipo, cod_os, descricao, matricula FROM atualizacoes ORDER BY datahora DESC";
            try (PreparedStatement stmt = connectDB.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                int contador = 1;
                while (rs.next() && contador <= 20) {
                        Timestamp datahora = rs.getTimestamp("datahora");
                        String tipo = rs.getString("tipo");
                        String os = rs.getString("cod_os");
                        String descricao = rs.getString("descricao");
                        String usuario = String.valueOf(rs.getInt("matricula")); // ou busque nome de outra tabela

                        listaAtualizacoes.add(new Atualizacao(datahora, tipo, os, descricao, usuario));
                        contador++;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        if (Sessao.getCargo().equals("Aprovisionador")) {
            menuRemoveUser.setVisible(false);
            menuRemoveUser.setManaged(false);

            menuAddUser.setVisible(false);
            menuAddUser.setManaged(false);

            menuSolcitarItem.setVisible(false);
            menuSolcitarItem.setManaged(false);
        }

    }

    public void menuRemoveUserOnAction(ActionEvent event){
            RemoverUsuario();
    }


    public void menuCloseOsOnAction(ActionEvent event){
            FecharOs();

    }

    public void menuConsultOsOnAction(ActionEvent event){
            ConsultarOs();

    }
    public void menuSolcitarItemOnAction(ActionEvent event){
            menuSolcitarItem("solicitar");

    }

    public void menuAddUserOnAction(ActionEvent event){
            CadastroUsuario();
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
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));

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

    public void menuImportarOsOnAction(ActionEvent event){
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/importarOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));



            // Configurar stage
            stage.setTitle("Importar Ordem de Servico");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#importNumeroOs"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void CadastroUsuario(){
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/cadastrarUsuario.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));



            // Configurar stage
            stage.setTitle("Cadastro de usuario");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#cadastroNome"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RemoverUsuario()  {
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/removerUsuario.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Configurar stage
            stage.setTitle("Remover Usuario");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#removeMatricula"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Login() {
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/login.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));



            // Configurar stage
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#enterUserNameField"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void FecharOs() {
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/fecharOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Configurar stage
            stage.setTitle("Fechar Ordem de Servico");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#consultNumeroOs"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void ConsultarOs(){
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Configurar stage
            stage.setTitle("Consultar Ordem de Servico");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#consultNumeroOs"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void menuSolcitarItem(String modo){
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            Stage stage = new Stage();
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Obtém o controller e passa o parâmetro
            ConsultarItemController controller = fxmlLoader.getController();
            controller.setModo(modo);
            controller.AtualizarTituloPorModo();


            // Configurar stage
            stage.setTitle("Consultar Item");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#consultNumeroOs"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Atualizacao {
        private Timestamp datahora;
        private String tipo;
        private String os;
        private String descricao;
        private String usuario;

        public Atualizacao(Timestamp datahora, String tipo, String os, String descricao, String usuario) {
            this.datahora = datahora;
            this.tipo = tipo;
            this.os = os;
            this.descricao = descricao;
            this.usuario = usuario;
        }

        // Getters e setters
        public Timestamp getDatahora() {
            return datahora;
        }

        public String getTipo() {
            return tipo;
        }

        public String getOs() {
            return os;
        }

        public String getDescricao() {
            return descricao;
        }

        public String getUsuario() {
            return usuario;
        }
    }

}



