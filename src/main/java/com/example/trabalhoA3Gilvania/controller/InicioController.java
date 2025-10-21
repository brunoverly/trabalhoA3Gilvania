package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
import com.example.trabalhoA3Gilvania.screen.*; // (Importa classes de modelo, ex: Atualizacao)

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.StageStyle;

// Importações padrão do Java
import java.awt.*; // (Nota: Este import 'java.awt.*' parece não estar sendo usado)
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela principal (Dashboard) "Inicio.fxml".
 * Esta é a tela central da aplicação após o login, exibindo estatísticas,
 * atualizações recentes e os menus de navegação.
 */
public class InicioController implements Initializable {

    // --- Injeção de Componentes FXML ---
    // Botões do Menu Lateral
    @FXML private Button menuAddUser;
    @FXML private Button menuRemoveUser;
    @FXML private Button menuSair;
    @FXML private Button menuImportarOs;
    @FXML private Button menuCloseOs;
    @FXML private Button menuRetiradaItem;
    @FXML private Button menuEntradaItem;
    @FXML private Button menuSolcitarItem;

    // Layout e Labels
    @FXML private AnchorPane inicioPane;
    @FXML private Label inicioLabelOsAbertas;
    @FXML private Label inicioLabelOsEncerrada;
    @FXML private Label inicioLabelOsEmAndamento;
    @FXML private Label inicioLabelBemVindo;
    @FXML private Label inicioLabelData;

    // Imagens e Ícones
    @FXML private ImageView inicioLogo;
    @FXML private ImageView inicio1;
    @FXML private ImageView inicio2;
    @FXML private ImageView inicio3;
    @FXML private ImageView inicio4;
    @FXML private ImageView inicio5;
    @FXML private ImageView inicio6;
    @FXML private ImageView inicio7;
    @FXML private ImageView inicio8;
    @FXML private ImageView inicio9;

    // Componentes Funcionais
    @FXML private Pane inicioPanelInvisivel;
    @FXML private Button inicioButtonFecharJanela;
    @FXML private ImageView inicioImagemFechar;

    // Tabela de Atualizações
    @FXML private TableView<Atualizacao> inicioTableView;
    @FXML private TableColumn<Atualizacao, String> inicioTableData;
    @FXML private TableColumn<Atualizacao, String> inicioTableTipo;
    @FXML private TableColumn<Atualizacao, String> inicioTableOs;
    @FXML private TableColumn<Atualizacao, String> inicioTableDescricao;
    @FXML private TableColumn<Atualizacao, String> inicioTableUsuario;

    // --- Variáveis de Instância ---

    // Referências de Stage para as janelas filhas (usadas para evitar abrir duplicatas)
    @FXML private Stage janelaImportarOs;
    @FXML private Stage janelaCadastroUsuario;
    @FXML private Stage janelaRemoverUsuario;
    @FXML private Stage janelaFecharOs;
    @FXML private Stage janelaConsultarOs;

    // Variáveis para permitir arrastar a janela (quando é transparente/sem borda)
    private double xOffset = 0;
    private double yOffset = 0;

    // Lista de dados principal para a Tabela de Atualizações (Logs)
    private ObservableList<Atualizacao> listaAtualizacoes = FXCollections.observableArrayList();

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();


    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // --- 1. Carregamento das Imagens/Ícones ---
        // (Carrega todas as imagens estáticas da tela)
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

        URL inicioImagemFecharURL = getClass().getResource("/imagens/close.png");
        Image inicioImagemFecharImage = new Image(inicioImagemFecharURL.toExternalForm());
        inicioImagemFechar.setImage(inicioImagemFecharImage);

        URL inicioLogoURL = getClass().getResource("/imagens/brand.png");
        Image inicioLogoImagem = new Image(inicioLogoURL.toExternalForm());
        inicioLogo.setImage(inicioLogoImagem);

        // --- 2. Controle de Acesso (esconde botões) ---
        verificarUsuario(); // Chama o método que ajusta a UI com base no cargo

        // --- 3. Configuração da Tabela de Atualizações ---
        // Vincula as colunas da Tabela às propriedades da classe 'Atualizacao'
        inicioTableData.setCellValueFactory(new PropertyValueFactory<>("datahora"));
        inicioTableTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        inicioTableOs.setCellValueFactory(new PropertyValueFactory<>("os"));
        inicioTableDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        inicioTableUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        inicioTableView.setItems(listaAtualizacoes); // Define a lista de dados
        inicioTableView.setSelectionModel(null); // Desabilita a seleção de linhas

        // --- 4. Carregamento dos Dados (Dashboard e Tabela) ---
        carregarAtualizacoes(); // Busca os dados no banco de dados

        // --- 5. Configuração dos Labels de Boas-Vindas e Data ---
        LocalDate hoje = LocalDate.now();
        // Formata a data para o padrão brasileiro (ex: "terça-feira, 21 de outubro de 2025")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataPorExtenso = hoje.format(formatter);

        // Define o texto de boas-vindas usando o nome salvo na Sessao
        inicioLabelBemVindo.setText("Bem vindo de volta, "+ Sessao.getNome());
        inicioLabelData.setText(dataPorExtenso);

        // --- 6. Efeitos de Hover (mouse) no botão de Fechar App ---
        ImageView fecharImagem = (ImageView) inicioButtonFecharJanela.getGraphic();
        inicioButtonFecharJanela.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2); // Aumenta o ícone
            fecharImagem.setScaleY(1.2);
            inicioButtonFecharJanela.setCursor(Cursor.HAND); // Muda o cursor
        });
        inicioButtonFecharJanela.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0); // Retorna ao normal
            fecharImagem.setScaleY(1.0);
            inicioButtonFecharJanela.setCursor(Cursor.DEFAULT);
        });

        // Painel de layout, marcado como transparente a cliques (não interfere com o mouse)
        inicioPanelInvisivel.setMouseTransparent(true);

    } // Fim do initialize()

    /**
     * Ação do botão (X) de fechar a aplicação (canto superior direito).
     */
    public void inicioButtonFecharJanelaOnAction(ActionEvent event){
        // Pede confirmação ao usuário antes de encerrar
        boolean confirmacao = alerta.criarAlertaConfirmacao("Confirmação", "Deseja encerrar a aplicação?");
        if(confirmacao) {
            Stage stage = (Stage) inicioButtonFecharJanela.getScene().getWindow();
            stage.close(); // Fecha a janela principal (encerra a aplicação)
        }
    }

    /**
     * Carrega os contadores do dashboard (OS Abertas, Encerradas, etc.)
     * e a tabela de atualizações (logs) a partir do banco de dados.
     */
    public void carregarAtualizacoes() {
        // Try-with-resources para garantir o fechamento da conexão
        try (Connection connectDB = new DataBaseConection().getConection()) {
            // Chama a Stored Procedure que busca todos os dados do dashboard
            CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.inicio_dashboard_tableview() }");

            boolean hasResult = cs.execute();

            // --- 1. Leitura do Primeiro ResultSet (Contadores do Dashboard) ---
            if (hasResult) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        // Popula os labels de estatísticas
                        inicioLabelOsAbertas.setText(String.valueOf(rs.getInt("abertas")));
                        inicioLabelOsEncerrada.setText(String.valueOf(rs.getInt("encerradas")));
                        inicioLabelOsEmAndamento.setText(String.valueOf(rs.getInt("em_andamento")));
                    }
                }
            }

            // --- 2. Leitura do Segundo ResultSet (Tabela de Logs) ---
            if (cs.getMoreResults()) { // Avança para o próximo resultado
                try (ResultSet rs = cs.getResultSet()) {
                    listaAtualizacoes.clear(); // Limpa a lista antes de adicionar novos dados
                    while (rs.next()) {
                        // Coleta os dados de cada linha do log
                        Timestamp datahora = rs.getTimestamp("datahora");
                        String tipo = rs.getString("tipo");
                        String os = rs.getString("cod_os");
                        String descricao = rs.getString("descricao");
                        String usuario = String.valueOf(rs.getInt("matricula"));

                        // Adiciona um novo objeto 'Atualizacao' na lista (que atualiza a tabela)
                        listaAtualizacoes.add(new Atualizacao(datahora, tipo, os, descricao, usuario));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Esconde e desabilita (gerenciamento de layout) botões do menu
     * com base no cargo do usuário logado (armazenado na Sessao).
     */
    private void verificarUsuario() {

        // --- Regras para "Mecânico" ---
        // Mecânico só pode solicitar item e consultar OS.
        if (Sessao.getCargo().equals("Mecânico")) {
            menuEntradaItem.setVisible(false);
            menuEntradaItem.setManaged(false); // Remove do cálculo de layout (não deixa buraco)
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
        // --- Regras para "Aprovisionador" ---
        // Aprovisionador não pode gerenciar usuários e não solicita itens (só dá entrada/saída).
        if (Sessao.getCargo().equals("Aprovisionador")) {
            menuRemoveUser.setVisible(false);
            menuRemoveUser.setManaged(false);
            menuAddUser.setVisible(false);
            menuAddUser.setManaged(false);
            menuSolcitarItem.setVisible(false);
            menuSolcitarItem.setManaged(false);
        }
        // (Administrador vê tudo, por isso não há 'if' para ele)
    }

    // --- Métodos de Ação do Menu (Wrappers) ---
    // Estes métodos são chamados diretamente pelos botões no FXML
    // e apenas redirecionam para os métodos que abrem as janelas.

    /** Ação do botão "Remover Usuário" */
    public void menuRemoveUserOnAction(ActionEvent event){
        RemoverUsuario();
    }
    /** Ação do botão "Fechar OS" */
    public void menuCloseOsOnAction(ActionEvent event){
        FecharOs();

    }
    /** Ação do botão "Consultar OS" */
    public void menuConsultOsOnAction(ActionEvent event){
        ConsultarOs();

    }
    /** Ação do botão "Solicitar Item" */
    public void menuSolcitarItemOnAction(ActionEvent event){
        // Reutiliza a tela 'consultarItem' passando o modo "Solicitar"
        menuSolcitarItem("Solicitar");

    }
    /** Ação do botão "Adicionar Usuário" */
    public void menuAddUserOnAction(ActionEvent event){
        CadastroUsuario();
    }

    /** Ação do botão "Entrada de Item" */
    public void menuEntradaItemOnAction(ActionEvent event){
        try{
            // Reutiliza a tela 'consultarItem' passando o modo "Entrada"
            menuSolcitarItem("Entrada");
        }
        catch (Exception e){}
    }

    /** Ação do botão "Retirada de Item" */
    public void menuRetiradaItemOnAction(ActionEvent event) throws Exception {
        // Reutiliza a tela 'consultarItem' passando o modo "Retirar"
        menuSolcitarItem("Retirar");
    }


    /**
     * Ação de Log Off (Botão "Sair").
     * Fecha o dashboard e reabre a tela de Login.
     */
    public void menuSairOnAction (ActionEvent event){
        try{
            // Pede confirmação para encerrar a sessão
            boolean confirmacao = alerta.criarAlertaConfirmacao("Confirmação", "Encerrar sessão?");
            if(confirmacao){
                Stage stage = (Stage) menuSair.getScene().getWindow();
                stage.close(); // Fecha a janela do dashboard
                Sessao.getCargo(); // (Esta linha parece não ter efeito, talvez fosse para limpar a sessão?)
                Login(); // Abre a tela de Login novamente
            }
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    /**
     * Abre a janela de "Importar OS" (importarOs.fxml).
     * Gerencia a instância da janela para evitar duplicatas.
     */
    public void menuImportarOsOnAction(ActionEvent event) {
        try {
            // 1. Verifica se a janela já está aberta
            if (janelaImportarOs != null && janelaImportarOs.isShowing()) {
                janelaImportarOs.toFront(); // Traz para frente
                if (janelaImportarOs.isIconified()) {
                    janelaImportarOs.setIconified(false); // Desminimiza se estiver minimizada
                }
                return; // Interrompe a abertura de uma nova
            }

            // 2. Se não estiver aberta, cria uma nova
            janelaImportarOs = new Stage();
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/ImportarOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // 3. Carrega fontes personalizadas
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // 4. Pega o controller da nova janela
            ImportarOsController controller = fxmlLoader.getController();
            // 5. Define o "callback": o que fazer quando a janela fechar
            controller.setOnFecharJanela(() -> {
                carregarAtualizacoes(); // Atualiza os dados do dashboard
            });

            // 6. Configura a cena e o Stage para serem transparentes (sem borda)
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            janelaImportarOs.initStyle(StageStyle.TRANSPARENT);
            janelaImportarOs.setScene(scene);

            // 7. Adiciona ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaImportarOs.getIcons().add(new Image(logoUrl.toExternalForm()));

            // 8. --- Bloco para arrastar a janela transparente ---
            root.setOnMousePressed(event2 -> {
                xOffset = event2.getSceneX();
                yOffset = event2.getSceneY();
            });
            root.setOnMouseDragged(event2 -> {
                janelaImportarOs.setX(event2.getScreenX() - xOffset);
                janelaImportarOs.setY(event2.getScreenY() - yOffset);
            });
            // -------------------------------------------------

            // 9. Carrega o CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 10. Configura e mostra a janela
            janelaImportarOs.setTitle("Importar ordem de serviço");
            janelaImportarOs.setResizable(false);
            janelaImportarOs.show();

            // (As linhas abaixo parecem repetidas, mas não causam erro)
            janelaImportarOs.setTitle("Importar ordem de serviço");
            janelaImportarOs.setResizable(false);
            janelaImportarOs.setScene(scene);

            // 11. Limpa a referência da janela quando ela for fechada
            janelaImportarOs.setOnHidden(e -> janelaImportarOs = null);

            janelaImportarOs.show();

            // 12. Foca o campo principal (melhoria de UX)
            TextField tf = (TextField) root.lookup("#importNumeroOs");
            if (tf != null) tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Fim do menuImportarOsOnAction()


    /**
     * Abre a janela de "Cadastrar Usuário" (cadastrarUsuario.fxml).
     * Gerencia a instância da janela para evitar duplicatas.
     */
    public void CadastroUsuario() {
        // 1. Verifica se a janela já existe (se é nula)
        if (janelaCadastroUsuario == null) {
            janelaCadastroUsuario = new Stage(); // Cria a nova janela
            try {
                // 2. Carrega FXML
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/cadastrarUsuario.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                // 3. Carrega fontes
                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                // 4. Configura cena transparente
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);

                // 5. Carrega CSS
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                // 6. Configura Stage transparente (sem borda)
                janelaCadastroUsuario.initStyle(StageStyle.TRANSPARENT);
                janelaCadastroUsuario.setScene(scene);

                // 7. Adiciona Ícone
                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaCadastroUsuario.getIcons().add(new Image(logoUrl.toExternalForm()));

                // 8. Habilita o arraste da janela
                root.setOnMousePressed(event2 -> {
                    xOffset = event2.getSceneX();
                    yOffset = event2.getSceneY();
                });
                root.setOnMouseDragged(event2 -> {
                    janelaCadastroUsuario.setX(event2.getScreenX() - xOffset);
                    janelaCadastroUsuario.setY(event2.getScreenY() - yOffset);
                });

                // 9. Configura e mostra a janela
                janelaCadastroUsuario.setTitle("Cadastro de usuário");
                janelaCadastroUsuario.setResizable(false);
                // 10. Limpa a referência ao fechar
                janelaCadastroUsuario.setOnHidden(e -> janelaCadastroUsuario = null);
                janelaCadastroUsuario.show();

                // 11. Foca o primeiro campo de entrada
                TextField tf = (TextField) root.lookup("#cadastroNome");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Se a janela já existe, apenas a traz para frente
            if (janelaCadastroUsuario.isIconified()) janelaCadastroUsuario.setIconified(false);
            janelaCadastroUsuario.toFront();
        }
    } // Fim do CadastroUsuario()

    /**
     * Abre a janela de "Remover Usuário" (removerUsuario.fxml).
     * Gerencia a instância da janela para evitar duplicatas.
     */
    public void RemoverUsuario() {
        // (A lógica é idêntica à de CadastroUsuario(), apenas muda o FXML)

        // 1. Verifica se a janela já existe
        if (janelaRemoverUsuario == null) {
            janelaRemoverUsuario = new Stage();
            try {
                // 2. Carrega FXML
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/removerUsuario.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                // 3. Carrega fontes
                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                // 4. Configura cena transparente
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);

                // 6. Configura Stage transparente
                janelaRemoverUsuario.initStyle(StageStyle.TRANSPARENT);
                janelaRemoverUsuario.setScene(scene);

                // 7. Adiciona Ícone
                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaRemoverUsuario.getIcons().add(new Image(logoUrl.toExternalForm()));

                // 8. Habilita o arraste
                root.setOnMousePressed(event2 -> {
                    xOffset = event2.getSceneX();
                    yOffset = event2.getSceneY();
                });
                root.setOnMouseDragged(event2 -> {
                    janelaRemoverUsuario.setX(event2.getScreenX() - xOffset);
                    janelaRemoverUsuario.setY(event2.getScreenY() - yOffset);
                });

                // 5. Carrega CSS
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                // 9. Configura e mostra
                janelaRemoverUsuario.setTitle("Remover usuário");
                janelaRemoverUsuario.setResizable(false);
                // 10. Limpa referência ao fechar
                janelaRemoverUsuario.setOnHidden(e -> janelaRemoverUsuario = null);
                janelaRemoverUsuario.show();

                // 11. Foca o campo de matrícula
                TextField tf = (TextField) root.lookup("#removeMatricula");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Se já existe, traz para frente
            if (janelaRemoverUsuario.isIconified()) janelaRemoverUsuario.setIconified(false);
            janelaRemoverUsuario.toFront();
        }
    } // Fim do RemoverUsuario()

    /**
     * Abre a janela de "Login" (login.fxml).
     * Chamado pelo método de Log Off (menuSairOnAction).
     */
    public void Login() {
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/login.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            // Carregar fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            // Adicionar o ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // (A linha abaixo está duplicada com a de baixo, mas não causa erro)
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            // Configura Stage e Cena para transparentes (sem borda)
            stage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre a janela de "Fechar OS" (fecharOs.fxml).
     * Gerencia a instância da janela para evitar duplicatas.
     */
    public void FecharOs() {
        try {
            // 1. Verifica se a janela já está aberta
            if (janelaFecharOs != null && janelaFecharOs.isShowing()) {
                janelaFecharOs.toFront(); // Traz para frente
                return; // Interrompe
            }

            // 2. Cria nova janela
            janelaFecharOs = new Stage();
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/fecharOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // 3. Carrega fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // 4. Configura cena transparente
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            // 5. Carrega CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 6. Configura Stage transparente
            janelaFecharOs.initStyle(StageStyle.TRANSPARENT);
            janelaFecharOs.setScene(scene);

            // 7. Adiciona Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaFecharOs.getIcons().add(new Image(logoUrl.toExternalForm()));

            // 8. Habilita o arraste
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaFecharOs.setX(event.getScreenX() - xOffset);
                janelaFecharOs.setY(event.getScreenY() - yOffset);
            });

            // 9. Pega o controller da nova janela
            FecharOsController controller = fxmlLoader.getController();
            // 10. Define o "callback" de fechamento
            controller.setOnFecharJanela(() -> {
                carregarAtualizacoes(); // Atualiza o dashboard
            });

            // 11. Limpa a referência ao fechar
            janelaFecharOs.setOnHidden(e -> {
                janelaFecharOs = null;
            });

            // 12. Configura e mostra
            janelaFecharOs.setTitle("Fechar ordem de serviço");
            janelaFecharOs.setResizable(false);
            janelaFecharOs.show();

            // 13. Foca o campo principal
            TextField tf = (TextField) root.lookup("#consultNumeroOs");
            if (tf != null) tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Fim do FecharOs()


    /**
     * Abre a janela de "Consultar OS" (consultarOs.fxml).
     * Gerencia a instância da janela para evitar duplicatas.
     */
    public void ConsultarOs() {
        // (Lógica idêntica aos outros métodos de abertura de janela)

        // 1. Verifica se a janela já existe
        if (janelaConsultarOs == null) {
            janelaConsultarOs = new Stage();
            try {
                // 2. Carrega FXML
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarOs.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                Parent root = fxmlLoader.load();

                // 3. Carrega fontes
                String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
                for (String fontFile : fonts) {
                    Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
                }

                // 4. Configura cena transparente e CSS
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                URL cssUrl = getClass().getResource("/css/style.css");
                scene.getStylesheets().add(cssUrl.toExternalForm());

                // 6. Configura Stage transparente
                janelaConsultarOs.initStyle(StageStyle.TRANSPARENT);
                janelaConsultarOs.setScene(scene);

                // 7. Adiciona Ícone
                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaConsultarOs.getIcons().add(new Image(logoUrl.toExternalForm()));

                // 8. Habilita o arraste
                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    janelaConsultarOs.setX(event.getScreenX() - xOffset);
                    janelaConsultarOs.setY(event.getScreenY() - yOffset);
                });

                // 9. Configura e mostra
                janelaConsultarOs.setTitle("Consultar ordem de serviço");
                janelaConsultarOs.setResizable(false);
                // 10. Limpa referência ao fechar
                janelaConsultarOs.setOnHidden(e -> janelaConsultarOs = null);
                janelaConsultarOs.show();

                // 11. Foca o campo principal
                TextField tf = (TextField) root.lookup("#consultNumeroOs");
                tf.requestFocus();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // (Faltou o 'else { toFront() }' aqui, mas mantido como no original)
    }

    // Referência da janela "Consultar Item" (que é multifuncional)
    private Stage janelaSolicitarItem;

    /**
     * Abre a janela "Consultar Item" (consultarItem.fxml) de forma multifuncional.
     * O comportamento da janela é definido pelo parâmetro "modo".
     *
     * @param modo O modo de operação: "Solicitar", "Entrada" ou "Retirar".
     */
    public void menuSolcitarItem(String modo){
        // 1. Se já existir, fecha a janela anterior para garantir que a nova abra com o modo correto
        if (janelaSolicitarItem != null) {
            janelaSolicitarItem.close();
            janelaSolicitarItem = null;
        }

        janelaSolicitarItem = new Stage();
        try {
            // 2. Carrega FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // 3. Carrega fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // 4. Configura cena transparente e CSS
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            janelaSolicitarItem.initStyle(StageStyle.TRANSPARENT);
            janelaSolicitarItem.setScene(scene);
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 7. Adiciona Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaSolicitarItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // 8. Habilita o arraste
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaSolicitarItem.setX(event.getScreenX() - xOffset);
                janelaSolicitarItem.setY(event.getScreenY() - yOffset);
            });

            // 9. Pega o controller da nova janela
            ConsultarItemController controller = fxmlLoader.getController();
            // 10. *** Lógica Principal: Passa o "modo" para o controller ***
            controller.setModo(modo);
            // Pede ao controller para atualizar seu próprio título com base no modo
            controller.AtualizarTituloPorModo();
            // Define o callback de fechamento
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    carregarAtualizacoes(); // Atualiza o dashboard
                }
            });

            // 11. Configura e mostra
            janelaSolicitarItem.setTitle("Consultar Item");
            janelaSolicitarItem.setResizable(false);
            janelaSolicitarItem.show();

            // 12. Foca o campo principal
            TextField tf = (TextField) root.lookup("#consultNumeroOs");
            tf.requestFocus();

            // 13. Limpa a referência ao fechar
            janelaSolicitarItem.setOnHidden(event -> janelaSolicitarItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Fim do menuSolcitarItem()

    /**
     * Classe de Modelo (POJO) interna para a Tabela de Atualizações (Logs).
     * (Nota: O código original declara esta classe *dentro* de `InicioController`,
     * mas não como `static`. Isso funciona, mas é incomum. Mantido como está.)
     */
    public class Atualizacao {
        private Timestamp datahora;
        private String tipo;
        private String os;
        private String descricao;
        private String usuario;

        // Construtor
        public Atualizacao(Timestamp datahora, String tipo, String os, String descricao, String usuario) {
            this.datahora = datahora;
            this.tipo = tipo;
            this.os = os;
            this.descricao = descricao;
            this.usuario = usuario;
        }

        // --- Getters (usados pelos PropertyValueFactory) ---
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
    } // Fim da classe Atualizacao

} // Fim da classe InicioController