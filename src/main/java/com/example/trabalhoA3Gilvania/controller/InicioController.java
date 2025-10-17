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
    @FXML private Stage janelaImportarOs;
    @FXML private Stage janelaSolcitarItem;
    @FXML private Stage janelaCadastroUsuario;
    @FXML private Stage janelaRemoverUsuario;
    @FXML private Stage janelaLogin;
    @FXML private Stage janelaFecharOs;
    @FXML private Stage janelaConsultarOs;










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
        Image inicio8Img = new Image(inicio8URL.toExternalForm());
        inicio8.setImage(inicio8Img);

        URL inicio9URL = getClass().getResource("/imagens/inicio9.png");
        Image inicio9Img = new Image(inicio9URL.toExternalForm());
        inicio9.setImage(inicio9Img);

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
                    "WHERE status = 'Aberta'";
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
                    "WHERE status = 'Encerrada'";
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
                    "WHERE status = 'Item(s) solicitados'";
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

        if (Sessao.getCargo().equals("Mecânico")) {
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
            menuSolcitarItem("Solicitar");

    }

    public void menuAddUserOnAction(ActionEvent event){
            CadastroUsuario();
    }

    public void menuEntradaItemOnAction(ActionEvent event){
        try{
            menuSolcitarItem("Entrada");
        }
        catch (Exception e){}
    }

    public void menuRetiradaItemOnAction(ActionEvent event) throws Exception {
        menuSolcitarItem("Retirar");
    }



    public void menuSairOnAction (ActionEvent event){
        try{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação");
            alert.setHeaderText(null);
            alert.setContentText("Encerrar sessão?");
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
        if(janelaImportarOs == null) {
            janelaImportarOs = new Stage();

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
                janelaImportarOs.getIcons().add(new Image(logoUrl.toExternalForm()));


                // Configurar stage
                janelaImportarOs.setTitle("Importar ordem de serviço");
                janelaImportarOs.setResizable(false);
                janelaImportarOs.setScene(scene);
                janelaImportarOs.setOnHidden(event2-> janelaImportarOs = null);
                janelaImportarOs.show();

                TextField tf = (TextField) root.lookup("#importNumeroOs"); // seu TextField pelo id
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Janela já existe → traz pra frente
            if (janelaImportarOs.isIconified()) {
                janelaImportarOs.setIconified(false); // desminimiza
            }
            janelaImportarOs.toFront();
        }

    }

    public void CadastroUsuario() {
        if (janelaCadastroUsuario == null) {
            janelaCadastroUsuario = new Stage();
            try {
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/cadastrarUsuario.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaCadastroUsuario.getIcons().add(new Image(logoUrl.toExternalForm()));

                janelaCadastroUsuario.setTitle("Cadastro de usuário");
                janelaCadastroUsuario.setResizable(false);
                janelaCadastroUsuario.setScene(scene);
                janelaCadastroUsuario.setOnHidden(e -> janelaCadastroUsuario = null);
                janelaCadastroUsuario.show();

                TextField tf = (TextField) root.lookup("#cadastroNome");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (janelaCadastroUsuario.isIconified()) janelaCadastroUsuario.setIconified(false);
            janelaCadastroUsuario.toFront();
        }
    }

    public void RemoverUsuario() {
        if (janelaRemoverUsuario == null) {
            janelaRemoverUsuario = new Stage();
            try {
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/removerUsuario.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaRemoverUsuario.getIcons().add(new Image(logoUrl.toExternalForm()));

                janelaRemoverUsuario.setTitle("Remover usuário");
                janelaRemoverUsuario.setResizable(false);
                janelaRemoverUsuario.setScene(scene);
                janelaRemoverUsuario.setOnHidden(e -> janelaRemoverUsuario = null);
                janelaRemoverUsuario.show();

                TextField tf = (TextField) root.lookup("#removeMatricula");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (janelaRemoverUsuario.isIconified()) janelaRemoverUsuario.setIconified(false);
            janelaRemoverUsuario.toFront();
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
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#enterUserNameField"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void FecharOs() {
        if (janelaFecharOs == null) {
            janelaFecharOs = new Stage();
            try {
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/fecharOs.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaFecharOs.getIcons().add(new Image(logoUrl.toExternalForm()));

                janelaFecharOs.setTitle("Fechar ordem de serviço");
                janelaFecharOs.setResizable(false);
                janelaFecharOs.setScene(scene);
                janelaFecharOs.setOnHidden(e -> janelaFecharOs = null);
                janelaFecharOs.show();

                TextField tf = (TextField) root.lookup("#consultNumeroOs");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (janelaFecharOs.isIconified()) janelaFecharOs.setIconified(false);
            janelaFecharOs.toFront();
        }
    }

    public void ConsultarOs() {
        if (janelaConsultarOs == null) {
            janelaConsultarOs = new Stage();
            try {
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarOs.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                Scene scene = new Scene(root);
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaConsultarOs.getIcons().add(new Image(logoUrl.toExternalForm()));

                janelaConsultarOs.setTitle("Consultar ordem de serviço");
                janelaConsultarOs.setResizable(false);
                janelaConsultarOs.setScene(scene);
                janelaConsultarOs.setOnHidden(e -> janelaConsultarOs = null);
                janelaConsultarOs.show();

                TextField tf = (TextField) root.lookup("#consultNumeroOs");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (janelaConsultarOs.isIconified()) janelaConsultarOs.setIconified(false);
            janelaConsultarOs.toFront();
        }
    }
    private Stage janelaSolicitarItem;

    public void menuSolcitarItem(String modo){
        // Se já existir, fecha a janela antes de abrir nova
        if (janelaSolicitarItem != null) {
            janelaSolicitarItem.close();
            janelaSolicitarItem = null;
        }

        janelaSolicitarItem = new Stage();
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaSolicitarItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            ConsultarItemController controller = fxmlLoader.getController();
            controller.setModo(modo);
            controller.AtualizarTituloPorModo();

            janelaSolicitarItem.setTitle("Consultar Item");
            janelaSolicitarItem.setResizable(false);
            janelaSolicitarItem.setScene(scene);
            janelaSolicitarItem.show();

            TextField tf = (TextField) root.lookup("#consultNumeroOs");
            tf.requestFocus();

            janelaSolicitarItem.setOnHidden(event -> janelaSolicitarItem = null);

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



