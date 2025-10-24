package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;

// Importações de classes do JavaFX
import javafx.geometry.Insets;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

// Importações padrão do Java
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
    @FXML private Button menuAddUser;
    @FXML private Button menuRemoveUser;
    @FXML private Button menuSair;
    @FXML private Button menuImportarOs;
    @FXML private Button menuPdf;
    @FXML private Button menuMenu;
    @FXML private Button menuCloseOs;
    @FXML private Button menuRetiradaItem;
    @FXML private Button menuEntradaItem;
    @FXML private Button menuSolcitarItem;
    @FXML private Button inicioButtonMaximizar;
    @FXML private Button inicioButtonMinimizar;
    @FXML private Button menuConsultOs;
    @FXML private Button menuConsultarRetiradas;
    @FXML private Button menuConsultarSolicitacoes;
    @FXML private AnchorPane inicioPane;
    @FXML private Label inicioLabelOsAbertas;
    @FXML private Label inicioLabelOsEncerrada;
    @FXML private Label inicioLabelOsEmAndamento;
    @FXML private Label inicioLabelBemVindo;
    @FXML private Label inicioLabelData;
    @FXML private ImageView inicioLogo;
    @FXML private ImageView inicio1;
    @FXML private ImageView inicio2;
    @FXML private ImageView inicio3;
    @FXML private ImageView inicio4;
    @FXML private ImageView inicio5;
    @FXML private ImageView inicio6;
    @FXML private ImageView inicio7;
    @FXML private ImageView inicio8;
    @FXML private ImageView inicioSolcitarItem;
    @FXML private ImageView inicio11;
    @FXML private ImageView inicio10;
    @FXML private ImageView inicioImagemMaximizar;
    @FXML private ImageView inicioImagemMinimizar;
    @FXML private ImageView inicio15;
    @FXML private Pane inicioPaneMenu;
    @FXML private VBox menuButtonVBox;
    @FXML private Pane menuBackgroundPane;
    @FXML private Pane inicioPanelInvisivel;
    @FXML private Button inicioButtonFecharJanela;
    @FXML private ImageView inicioImagemFechar;
    @FXML private TableView<Atualizacao> inicioTableView;
    @FXML private TableColumn<Atualizacao, String> inicioTableData;
    @FXML private TableColumn<Atualizacao, String> inicioTableTipo;
    @FXML private TableColumn<Atualizacao, String> inicioTableOs;
    @FXML private TableColumn<Atualizacao, String> inicioTableDescricao;
    @FXML private TableColumn<Atualizacao, String> inicioTableUsuario;
    @FXML private TableColumn<Atualizacao, String> inicioTableCodItem;

    // --- Variáveis de Instância ---
    @FXML private Stage janelaImportarOs;
    @FXML private Stage janelaCadastroUsuario;
    @FXML private Stage janelaRemoverUsuario;
    @FXML private Stage janelaFecharOs;
    @FXML private Stage janelaGerarPdf;
    @FXML private Stage janelaConsultarOs;
    @FXML private Stage janelaConsultarHistorico;
    private double xOffset = 0;
    private double yOffset = 0;
    private ObservableList<Atualizacao> listaAtualizacoes = FXCollections.observableArrayList();
    FormsUtil alerta = new FormsUtil();

    private String statusItem1 = "Aguardando entrega";
    private String statusItem2 = "Recebido (parcial)";
    private String statusItem3 = "Recebido (integral)";
    private String statusItem4 = "Solicitado (parcial)";
    private String statusItem5 = "Solicitado (integral)";
    private String statusItem6 = "Entregue (parcial)";
    private String statusItem7 = "Entregue (integral)";
    private String statusOrdemServico1 = "Aberta";
    private String statusOrdemServico2 = "Em andamento";
    private String statusOrdemServico3 = "Encerrada";
    private String statusOperacao1 = "Em espera";
    private String statusOperacao2 = "Item(s) solicitados";
    private String statusOperacao3 = "Itens entregues (Parcial)";
    private String statusOperacao4 = "Itens entregues (Integral)";


    // --- Variáveis de Estado do Menu ---
    private List<Button> botoesDoMenu;
    private boolean menuEstaAberto = false;

    // --- Constantes de Animação do Menu (Baseado no FXML CORRIGIDO) ---

    // Este é o único valor que você deve alterar para ajustar o tamanho final
    private final double LARGURA_ABERTA_PARENT = 160.0; // <<< AJUSTE O TAMANHO FINAL AQUI

    // Valores do FXML (estado fechado)
    // Assumindo que você está usando o FXML corrigido (com larguras de 56.0)
    private final double LARGURA_FECHADA_PARENT = 63.0; // (inicioPaneMenu prefWidth)
    private final double LARGURA_FECHADA_BG = 56.0;     // (menuBackgroundPane prefWidth)
    private final double LARGURA_FECHADA_VBOX = 56.0;   // (menuButtonVBox prefWidth)
    private final double LARGURA_LOGO = 52.0;           // (inicioLogo fitWidth)

    // --- ATUALIZAÇÃO: Posições X da Logo ---
    // Posição Fechada: O valor 7.0 que você indicou ser o centro visual
    private final double LOGO_LAYOUTX_FECHADO = 7.0;

    // Valores Calculados (estado aberto)
    private final double OFFSET_PARENT_BG = LARGURA_FECHADA_PARENT - LARGURA_FECHADA_BG; // 63 - 56 = 7.0
    private final double LARGURA_ABERTA_BG = LARGURA_ABERTA_PARENT - OFFSET_PARENT_BG;   // 160 - 7 = 153.0
    private final double LARGURA_ABERTA_VBOX = LARGURA_ABERTA_BG;

    // --- ATUALIZAÇÃO: Posição X Aberta da Logo ---
    // Posição Aberta: Centralizado na nova largura do fundo
    private final double LOGO_LAYOUTX_ABERTO = (LARGURA_ABERTA_BG - LARGURA_LOGO) / 2.0; // (153-52)/2 = 50.5


    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */

/**
 * Método de inicialização, chamado automaticamente pelo JavaFX
 * após o FXML ser carregado.
 */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // --- 1. Carregamento das Imagens/Ícones ---
        // (Seu código de carregamento de imagens original - INTACTO)
        URL inicio1URL = getClass().getResource("/imagens/inicio1.png");
        Image inicio1Img = new Image(inicio1URL.toExternalForm());
        inicio1.setImage(inicio1Img);
        URL inicio2URL = getClass().getResource("/imagens/inicio23.png");
        Image inicio2Img = new Image(inicio2URL.toExternalForm());
        inicio2.setImage(inicio2Img);
        URL inicio3URL = getClass().getResource("/imagens/inicio24.png");
        Image inicio3Img = new Image(inicio3URL.toExternalForm());
        inicio3.setImage(inicio3Img);
        URL inicio4URL = getClass().getResource("/imagens/inicio22.png");
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
        URL inicioSolcitarItemURL = getClass().getResource("/imagens/menu20.png");
        Image inicioSolcitarItemImage = new Image(inicioSolcitarItemURL.toExternalForm());
        inicioSolcitarItem.setImage(inicioSolcitarItemImage);
        URL inicioImagemFecharURL = getClass().getResource("/imagens/close.png");
        Image inicioImagemFecharImage = new Image(inicioImagemFecharURL.toExternalForm());
        inicioImagemFechar.setImage(inicioImagemFecharImage);
        URL inicioLogoURL = getClass().getResource("/imagens/logo.png");
        Image inicioLogoImagem = new Image(inicioLogoURL.toExternalForm());
        inicioLogo.setImage(inicioLogoImagem);
        URL inicio11URL = getClass().getResource("/imagens/inicio26.png");
        Image inicio11Image = new Image(inicio11URL.toExternalForm());
        inicio11.setImage(inicio11Image);
        URL inicio10URL = getClass().getResource("/imagens/menu21.png");
        Image inicio10Image = new Image(inicio10URL.toExternalForm());
        inicio10.setImage(inicio10Image);
        URL inicioImagemMaximizarURL = getClass().getResource("/imagens/maximize.png");
        Image inicioImagemMaximizarImage = new Image(inicioImagemMaximizarURL.toExternalForm());
        inicioImagemMaximizar.setImage(inicioImagemMaximizarImage);
        URL inicioImagemMinimizarURL = getClass().getResource("/imagens/minimize.png");
        Image inicioImagemMinimizarImage = new Image(inicioImagemMinimizarURL.toExternalForm());
        inicioImagemMinimizar.setImage(inicioImagemMinimizarImage);
        URL inicio15URL = getClass().getResource("/imagens/pdf.png");
        Image inicio15Image = new Image(inicio15URL.toExternalForm());
        inicio15.setImage(inicio15Image);

        // --- 2. Controle de Acesso (seu código original - INTACTO) ---
        verificarUsuario();

        // --- 3. Configuração da Tabela de Atualizações ---
        inicioTableData.setCellValueFactory(new PropertyValueFactory<>("datahora"));
        inicioTableTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        inicioTableOs.setCellValueFactory(new PropertyValueFactory<>("os"));
        inicioTableDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao")); // <-- CORRIGIDO
        inicioTableUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        inicioTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        inicioTableView.setItems(listaAtualizacoes);
        inicioTableView.setSelectionModel(null);

        // --- 4. Carregamento dos Dados (seu código original - INTACTO) ---
        carregarAtualizacoes();

        // --- 5. Configuração dos Labels de Boas-Vindas e Data (seu código original - INTACTO) ---
        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataPorExtenso = hoje.format(formatter);
        inicioLabelBemVindo.setText("Bem vindo de volta, " + Sessao.getNome());
        inicioLabelData.setText(dataPorExtenso);

        // --- 6. Efeitos de Hover (seu código original - INTACTO) ---
        ImageView fecharImagem = (ImageView) inicioButtonFecharJanela.getGraphic();
        inicioButtonFecharJanela.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            inicioButtonFecharJanela.setCursor(Cursor.HAND);
        });
        inicioButtonFecharJanela.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            inicioButtonFecharJanela.setCursor(Cursor.DEFAULT);
        });
        ImageView maximizarImagem = (ImageView) inicioButtonMaximizar.getGraphic();
        inicioButtonMaximizar.setOnMouseEntered(e -> {
            maximizarImagem.setScaleX(1.2);
            maximizarImagem.setScaleY(1.2);
            inicioButtonMaximizar.setCursor(Cursor.HAND);
        });
        inicioButtonMaximizar.setOnMouseExited(e -> {
            maximizarImagem.setScaleX(1.0);
            maximizarImagem.setScaleY(1.0);
            inicioButtonMaximizar.setCursor(Cursor.DEFAULT);
        });
        ImageView minimizarImagem = (ImageView) inicioButtonMinimizar.getGraphic();
        inicioButtonMinimizar.setOnMouseEntered(e -> {
            minimizarImagem.setScaleX(1.2);
            minimizarImagem.setScaleY(1.2);
            inicioButtonMinimizar.setCursor(Cursor.HAND);
        });
        minimizarImagem.setOnMouseExited(e -> {
            minimizarImagem.setScaleX(1.0);
            minimizarImagem.setScaleY(1.0);
            minimizarImagem.setCursor(Cursor.DEFAULT);
        });

        // --- 7. Configuração da Animação do Menu (ATUALIZADO) ---

        // Adiciona TODOS os botões do menu à lista para tratamento unificado
        botoesDoMenu = Arrays.asList(
                menuMenu,
                menuImportarOs, menuConsultOs, menuCloseOs, menuSolcitarItem,
                menuEntradaItem, menuRetiradaItem, menuAddUser, menuRemoveUser,
                menuConsultarRetiradas, menuConsultarSolicitacoes, menuPdf ,menuSair
        );

        // ** ATUALIZAÇÃO: Força a posição correta da logo (ignora o 2.0 do FXML)
        inicioLogo.setLayoutX(LOGO_LAYOUTX_FECHADO); // Usa o 7.0

        // =================================================================
        // *** ATUALIZAÇÃO FEITA AQUI ***
        // Alinha a VBox no topo. O 'spacing' do FXML cuidará do espaçamento.
        // =================================================================

        definirEstiloBotoes(false); // Esconde textos e ajusta larguras
        menuEstaAberto = false;

        // Aplica o "Clip" (Recorte)
        Rectangle clip = new Rectangle();
        // ** ATUALIZAÇÃO: O clipe agora recorta o 'menuBackgroundPane', não o 'inicioPaneMenu'
        clip.widthProperty().bind(menuBackgroundPane.widthProperty());
        clip.heightProperty().bind(menuBackgroundPane.heightProperty());
        clip.setArcWidth(42.0);  // Raio (21 * 2)
        clip.setArcHeight(42.0); // Raio (21 * 2)
        // ** ATUALIZAÇÃO: O clipe agora é aplicado DIRETAMENTE no painel de fundo
        menuBackgroundPane.setClip(clip);

    } // Fim do initialize()
    /**
     * Método auxiliar para aplicar efeito hover nos botões da janela
     */
    private void setupHoverEffect(Button botao, ImageView imagem) {
        botao.setOnMouseEntered(e -> {
            imagem.setScaleX(1.2);
            imagem.setScaleY(1.2);
            botao.setCursor(Cursor.HAND);
        });
        botao.setOnMouseExited(e -> {
            imagem.setScaleX(1.0);
            imagem.setScaleY(1.0);
            botao.setCursor(Cursor.DEFAULT);
        });
    }
// Fim do initialize()


    /**
     * Chamado pelo onAction do 'menuMenu'.
     * Controla a animação de expansão e retração do menu lateral.
     * (ATUALIZADO)
     */
    @FXML
    private void menuMenuOnAction(ActionEvent event) {
        menuEstaAberto = !menuEstaAberto;

        // Define as larguras "alvo" (abertas ou fechadas)
        double targetWidthParent = menuEstaAberto ? LARGURA_ABERTA_PARENT : LARGURA_FECHADA_PARENT;
        double targetWidthBackground = menuEstaAberto ? LARGURA_ABERTA_BG : LARGURA_FECHADA_BG;
        double targetWidthVBox = menuEstaAberto ? LARGURA_ABERTA_VBOX : LARGURA_FECHADA_VBOX;

        // ** ATUALIZAÇÃO: Define a posição "alvo" da logo (Fechado: 7.0, Aberto: 50.5) **
        double targetLogoLayoutX = menuEstaAberto ? LOGO_LAYOUTX_ABERTO : LOGO_LAYOUTX_FECHADO;

        // Cria a animação de Timeline
        Timeline timeline = new Timeline();

        // Adiciona KeyValues para animar TUDO de uma vez
        KeyValue kvParent = new KeyValue(inicioPaneMenu.prefWidthProperty(), targetWidthParent);
        KeyValue kvBackground = new KeyValue(menuBackgroundPane.prefWidthProperty(), targetWidthBackground);
        KeyValue kvVBox = new KeyValue(menuButtonVBox.prefWidthProperty(), targetWidthVBox);
        KeyValue kvLogo = new KeyValue(inicioLogo.layoutXProperty(), targetLogoLayoutX);

        KeyFrame kf = new KeyFrame(Duration.millis(350), kvParent, kvBackground, kvVBox, kvLogo);
        timeline.getKeyFrames().add(kf);

        if (menuEstaAberto) {
            definirEstiloBotoes(true);
        } else {
            timeline.setOnFinished(e -> {
                definirEstiloBotoes(false);
            });
        }

        timeline.play();
    }

    /**
     * Método helper para definir o estilo de TODOS os botões do menu.
     * (ATUALIZADO E CORRIGIDO - Única versão deste método)
     */
    private void definirEstiloBotoes(boolean mostrar) {
        // Define a nova largura que os botões devem ter
        double newButtonWidth = menuEstaAberto ? LARGURA_ABERTA_VBOX : LARGURA_FECHADA_VBOX;

        // Define o alinhamento INTERNO do botão (Esquerda quando aberto, Centro quando fechado)
        Pos buttonAlignment = mostrar ? Pos.CENTER_LEFT : Pos.CENTER;

        // Define o alinhamento da VBox (para centralizar os botões quando fechado)
        Pos vBoxAlignment = mostrar ? Pos.TOP_LEFT : Pos.CENTER; // Alinha no topo quando aberto

        // Define o display (Icone+Texto quando aberto, só Icone quando fechado)
        ContentDisplay display = mostrar ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY;

        // ** ATUALIZAÇÃO: Define o Padding (espaçamento interno) **
        // Adiciona 10px à esquerda quando aberto, 0 quando fechado
        Insets padding = mostrar ? new Insets(0, 0, 0, 10) : Insets.EMPTY;

        // Aplica o alinhamento na VBox pai
        menuButtonVBox.setAlignment(vBoxAlignment);

        // Lista de textos (incluindo " Menu")
        String[] textos = {
                " Menu",
                " Importar OS", " Consultar OS", " Fechar OS", " Solicitar Item",
                " Entrada Item", " Retirada Item", " Add Usuário", " Rem. Usuário",
                " Cons. Retiradas", " Cons. Solicitações", "Gerar PDF"," Sair"
        };

        for (int i = 0; i < botoesDoMenu.size(); i++) {
            Button btn = botoesDoMenu.get(i);
            String texto = mostrar ? textos[i] : "";

            btn.setText(texto);
            btn.setPrefWidth(newButtonWidth);
            btn.setAlignment(buttonAlignment);
            btn.setContentDisplay(display);
            btn.setGraphicTextGap(10);

            // ** ATUALIZAÇÃO: Aplica o Padding **
            btn.setPadding(padding);
        }
    }


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
    @FXML
    private void inicioButtonMaximizarOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void inicioButtonMinimizarOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    public void menuConsultarRetiradasOnAction(ActionEvent event){

        menuConsultarHistorico("Retiradas");
    }
    public void menuConsultarSolicitacoesOnAction(ActionEvent event){

        menuConsultarHistorico("Solicitações");
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                    while (rs.next()) {
                        // Coleta os dados de cada linha do log
                        LocalDateTime ldt = rs.getTimestamp("datahora").toLocalDateTime();
                        String datahoraFormatada = ldt.format(formatter);
                        String tipo = rs.getString("tipo");
                        String os = rs.getString("cod_os");
                        String descricao = rs.getString("descricao");
                        String usuario = String.valueOf(rs.getInt("matricula"));
                        String codItem = rs.getString("cod_item"); // (Não usado na tabela)


                        // Adiciona um novo objeto 'Atualizacao' na lista (que atualiza a tabela)
                        listaAtualizacoes.add(new Atualizacao(datahoraFormatada, tipo, os, descricao, usuario, codItem));
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
            menuPdf.setVisible(false);
            menuPdf.setManaged(false);
            menuConsultarRetiradas.setVisible(false);
            menuConsultarRetiradas.setManaged(false);
            menuConsultarSolicitacoes.setVisible(false);
            menuConsultarSolicitacoes.setManaged(false);
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
    /// ///////////////////////////////////////////////////////////////////////

    public void menuConsultarHistorico(String modo){
        // 1. Se já existir, fecha a janela anterior para garantir que a nova abra com o modo correto
        if (janelaConsultarHistorico != null) {
            janelaConsultarHistorico.close();
            janelaConsultarHistorico = null;
        }

        janelaConsultarHistorico = new Stage();
        try {
            // 2. Carrega FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/consultarHistorico.fxml");
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
            janelaConsultarHistorico.initStyle(StageStyle.TRANSPARENT);
            janelaConsultarHistorico.setScene(scene);
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 7. Adiciona Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaConsultarHistorico.getIcons().add(new Image(logoUrl.toExternalForm()));

            // 8. Habilita o arraste
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaConsultarHistorico.setX(event.getScreenX() - xOffset);
                janelaConsultarHistorico.setY(event.getScreenY() - yOffset);
            });

            // 9. Pega o controller da nova janela
            ConsultarHistoricoController controller = fxmlLoader.getController();
            // 10. *** Lógica Principal: Passa o "modo" para o controller ***
            controller.setModo(modo);
            // Pede ao controller para atualizar seu próprio título com base no modo
            controller.AtualizarTituloPorModo();
            janelaConsultarHistorico.setTitle("Consultar Histórico");
            janelaConsultarHistorico.setResizable(false);
            janelaConsultarHistorico.show();

            janelaConsultarHistorico.setOnHidden(event -> janelaConsultarHistorico = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void menuPdfOnAction() {
        // (Lógica idêntica aos outros métodos de abertura de janela)

        // 1. Verifica se a janela já existe
        if (janelaGerarPdf == null) {
            janelaGerarPdf = new Stage();
            try {
                // 2. Carrega FXML
                URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/gerarPdf.fxml");
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
                janelaGerarPdf.initStyle(StageStyle.TRANSPARENT);
                janelaGerarPdf.setScene(scene);

                // 7. Adiciona Ícone
                URL logoUrl = getClass().getResource("/imagens/logo.png");
                janelaGerarPdf.getIcons().add(new Image(logoUrl.toExternalForm()));

                // 8. Habilita o arraste
                root.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                root.setOnMouseDragged(event -> {
                    janelaGerarPdf.setX(event.getScreenX() - xOffset);
                    janelaGerarPdf.setY(event.getScreenY() - yOffset);
                });

                // 9. Configura e mostra
                janelaGerarPdf.setTitle("Gerar PDF");
                janelaGerarPdf.setResizable(false);
                // 10. Limpa referência ao fechar
                janelaGerarPdf.setOnHidden(e -> janelaGerarPdf = null);
                janelaGerarPdf.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // (Faltou o 'else { toFront() }' aqui, mas mantido como no original)
    }


    /**
     * Classe de Modelo (POJO) interna para a Tabela de Atualizações (Logs).
     * (Nota: O código original declara esta classe *dentro* de `InicioController`,
     * mas não como `static`. Isso funciona, mas é incomum. Mantido como está.)
     */
    public class Atualizacao {
        private String datahora;
        private String tipo;
        private String os;
        private String descricao;
        private String usuario;
        private String codItem;

        // Construtor
        public Atualizacao(String datahora, String tipo, String os, String descricao, String usuario, String codItem) {
            this.datahora = datahora;
            this.tipo = tipo;
            this.os = os;
            this.descricao = descricao;
            this.usuario = usuario;
            this.codItem = codItem;
        }

        // --- Getters (usados pelos PropertyValueFactory) ---
        public String getDatahora() {
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
        public String getCodItem() {
            return codItem;
        }
    } // Fim da classe Atualizacao

} // Fim da classe InicioController