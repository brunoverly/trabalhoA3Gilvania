package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do próprio projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Utils.Sessao;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.StageStyle;

// Importações padrão do Java
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador JavaFX para a tela "ConsultarItem.fxml".
 * Esta tela é multifuncional, permitindo:
 * 1. Consultar operações e itens de uma OS.
 * 2. Solicitar itens (Mecânico).
 * 3. Lançar entrada de itens (Aprovisionador/Admin).
 * 4. Lançar retirada/saída de itens (Aprovisionador/Admin).
 * A funcionalidade exata é determinada pela variável 'modo' e pelo 'cargo' do usuário na Sessao.
 */
public class ConsultarItemController implements Initializable{

    // --- Injeção de Componentes FXML ---
    // Estes campos são vinculados aos componentes definidos no arquivo .fxml
    @FXML private Button consultVoltarButton;
    @FXML private Label consultItemLabel; // Título da janela
    @FXML private TextField consultNumeroOs;
    @FXML private TableView<Item> consultTableItem;
    @FXML private TableView<Operacao> consultTableOperacao;
    @FXML private TableColumn<Operacao, String> constulTabelCodOperacao;
    @FXML private TableColumn<Operacao, String> consultTableOperacaoStatus;
    @FXML private TableColumn<Item, String> consultTableCodItem;
    @FXML private TableColumn<Item, String> consultTableDescricaoItem;
    @FXML private TableColumn<Item, String> consultTablePedidoItem;
    @FXML private TableColumn<Item, String> consultTableRecebidoItem;
    @FXML private TableColumn<Item, String> consultTableItemStatus;
    @FXML private TableColumn<Item, String> consultTableItemSolicitado;
    @FXML private TableColumn<Item, String> consultTableItemEntregue;
    @FXML private AnchorPane consultItemSplitPane;
    @FXML private AnchorPane consultItenTableViewOperacao;
    @FXML private AnchorPane consultItemTableViewItem;
    @FXML private SplitPane consultarItemSplitPane;
    @FXML private ImageView solicitarItemVoltarIImage;

    // --- Campos Privados ---
    private Stage janelaEntradaItem; // Referência para a janela (pop-up) de entrada
    private Stage janelaSaidaItem;
    private Stage janelaSolicitarItem; // Referência para a janela (pop-up) de saída

    // Listas de dados para as tabelas
    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados; // Lista especial para filtrar itens baseados na operação

    // Variáveis para armazenar dados passados para este controller ou lidos do DB
    private String modo; // Define o comportamento da tela ("Entrada", "Retirar", "Solicitar")
    private String codOperacao;
    private String codOs;
    private String codItem;
    private String descricaoItem;
    private String qtdPedido;
    private int idItem;
    private String localizacao;
    private String status;
    private int qtdRecebida;
    private int idOperacao;


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


    // Callback para notificar a tela anterior quando esta for fechada
    private OnFecharJanela onFecharJanela;

    // Variáveis para permitir arrastar a janela (sem borda)
    private double xOffset = 0;
    private double yOffset = 0;

    // Instância da classe utilitária para exibir alertas
    FormsUtil alerta = new FormsUtil();

    /**
     * Define um "ouvinte" (callback) que será chamado quando esta janela for fechada.
     * Usado para que a tela principal possa atualizar seus dados.
     */
    public void setOnFecharJanela(OnFecharJanela onFecharJanela) {
        this.onFecharJanela = onFecharJanela;
    }

    // --- Setters para passagem de dados ---
    // Métodos usados por outros controllers para "injetar" dados nesta tela
    public void setModo(String modo) {
        this.modo = modo;
    }
    public void setCodItem(String codItem) {
        this.codItem = codItem;
    }
    public void setCodOperacao(String codOperacao) {
        this.codOperacao = codOperacao;
    }
    public void setCodOs(String codOs) {
        this.codOs = codOs;
    }
    public void setDescricaoItem(String descricaoItem) {
        this.descricaoItem = descricaoItem;
    }
    public void setQtdPedido(int qtdPedido) {
        this.qtdPedido = String.valueOf(qtdPedido);
    }
    public void setIdItem(int idItem) {this.idItem = idItem;}
    public void setLocalizacao(String localizacao){this.localizacao = localizacao;}
    public void setStatus(String status){this.status = status;}
    public void setQtdRecebida(int qtdRecebida){this.qtdRecebida = qtdRecebida;}
    public void setIdOperacao(int idOperacao){this.idOperacao = idOperacao;}

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carrega a imagem do botão de fechar
        URL solicitarItemVoltarIImageURL = getClass().getResource("/imagens/close.png");
        Image solicitarItemVoltarIImagem = new Image(solicitarItemVoltarIImageURL.toExternalForm());
        solicitarItemVoltarIImage.setImage(solicitarItemVoltarIImagem);

        // --- Configuração das Colunas das Tabelas ---
        // Vincula as colunas da Tabela de Operações às propriedades da classe 'Operacao'
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        //consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Vincula as colunas da Tabela de Itens às propriedades da classe 'Item'
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableItemSolicitado.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitado"));
        consultTableItemEntregue.setCellValueFactory(new PropertyValueFactory<>("qtdEntregue"));


        constulTabelCodOperacao.setStyle("-fx-alignment: CENTER;");
        consultTablePedidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableRecebidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableItemSolicitado.setStyle("-fx-alignment: CENTER;");
        consultTableItemEntregue.setStyle("-fx-alignment: CENTER;");




        // Define um texto padrão para tabelas vazias (aqui, vazio)
        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));

        // Vincula as listas de dados às tabelas
        consultTableOperacao.setItems(todasOperacoes); // Tabela de operações usa a lista completa

        // Tabela de itens usa a lista filtrada, que começa vazia (predicado `item -> false`)
        itensFiltrados = new FilteredList<>(todosItens, item -> false);
        consultTableItem.setItems(itensFiltrados);

        // --- Efeitos de Hover (mouse) no botão de Voltar ---
        ImageView fecharImagem = (ImageView) consultVoltarButton.getGraphic();

        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        consultVoltarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            consultVoltarButton.setCursor(Cursor.HAND);
        });

        // Ao sair com o mouse: retorna ao normal
        consultVoltarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            consultVoltarButton.setCursor(Cursor.DEFAULT);
        });


        // --- Lógica de Interação das Tabelas ---

        // Listener para a SELEÇÃO na Tabela de Operações
        // Filtra a Tabela de Itens para mostrar apenas itens da operação selecionada.
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Quando uma operação é selecionada, atualiza o "predicado" (filtro) da lista de itens
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));
            } else {
                // Se nada for selecionado, o filtro esconde todos os itens
                itensFiltrados.setPredicate(item -> false);
            }
        });

        // Listener de CLIQUE na Tabela de Operações
        // Torna a Tabela de Itens visível ao clicar em uma operação
        consultTableOperacao.setOnMouseClicked(event -> {
            Operacao selecionada = consultTableOperacao.getSelectionModel().getSelectedItem();
            if (selecionada != null && event.getButton() == MouseButton.PRIMARY) {
                consultItemTableViewItem.setVisible(true); // Mostra o painel da tabela de itens
            }
        });

        // Configuração customizada das LINHAS da Tabela de Operações

        consultTableOperacao.setRowFactory(table -> {
                    TableRow<Operacao> row = new TableRow<>();

                    // Cria o menu de contexto
                    if (modo.equals("Entrada")) {
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem lancarEntradaTodos = new MenuItem("Lançar entrada para todos os itens");
                        contextMenu.getItems().add(lancarEntradaTodos);

                        // Define a ação do menu (chamará outro método que você vai criar)
                        lancarEntradaTodos.setOnAction(event -> {
                            Operacao selecionada = row.getItem();
                            if (selecionada != null && modo.equals("Entrada")) {
                                boolean confirmacao = alerta.criarAlertaConfirmacao("Confirmação",
                                        "Ao prosseguir sera lançada entrada TOTAL para TODOS os itens pendentes da operação: '" + selecionada.getCodOperacao() + "'. Deseja continuar?");
                                if (confirmacao) {
                                    if (consultNumeroOs.getText().isBlank() || consultNumeroOs == null) {
                                        alerta.criarAlerta(Alert.AlertType.ERROR, "Erro",
                                                        "Erro ao recuperar o numero da OS, tente procurar a ordem de serviço novamente")
                                                .showAndWait();
                                    } else {
                                        lancarEntradaTodosItens(consultNumeroOs.getText());
                                    }
                                } else {
                                    return;
                                }
                            }
                        });


                    // Só exibe o menu se a linha não estiver vazia
                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(contextMenu)
                    );
                }
            // Clique normal
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    consultItemTableViewItem.setVisible(true);
                }
            });

            return row;
        });

        // Configuração customizada das LINHAS da Tabela de Itens (Mais complexa)
        // Inclui ContextMenu (clique direito) e Duplo Clique
        consultTableItem.setRowFactory(table -> {
            TableRow<Item> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu(); // Cria um menu de clique direito

            // Listener para ATUALIZAR o menu de contexto dinamicamente
            // O menu muda dependendo do status do item e do "modo" da tela
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                configurarContextMenu(row, contextMenu);
            });

            // Vincula o menu de contexto à linha, mas só se a linha não estiver vazia
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null) // Se vazia, menu nulo
                            .otherwise(contextMenu)  // Se cheia, mostra o menu configurado
            );



            // --- Ação de DUPLO CLIQUE na Tabela de Itens ---
            row.setOnMouseClicked(event -> {
                // Verifica se foi um duplo clique primário em uma linha não vazia
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    Item selecionado = row.getItem(); // Pega o item da linha clicada

                    // Coleta todos os dados necessários do item selecionado e da tela
                    String codItemSelecionado = selecionado.getCodItem();
                    String codOperacaoItemSelecionado = selecionado.getCodOperacao();
                    String codOsItemSelecionado = consultNumeroOs.getText();
                    String descricaoItemSelecionado = selecionado.getDescricao();
                    int idOperacaoItemSelecionado = selecionado.getIdOperacao();
                    int qtdPedidoItemSelecionado = selecionado.getQtdPedido();
                    int idItemselecionado = selecionado.getIdItem();
                    int qtdRecebida = selecionado.getQtdRecebida();

                    try {
                        if (modo == null) return; // Se o modo não foi definido, não faz nada

                        // --- Lógica de Solicitação (Mecânico) ---
                        // Trava para item "Aguardando entrega"
                        if (!Sessao.getCargo().equals("Aprovisionador") && modo.equals("Solicitar") && selecionado.getStatus().equals(statusItem1)) {
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "A solicitação só pode ser realizada quando o item estiver com status 'Recebido parcial' ou 'Recebido integral'")
                                    .showAndWait();
                            return; // Interrompe
                        }

                        // Permissão: Não-Aprovisionador, Modo "Solicitar", Status "Recebido parcial" ou "Recebido integral"
                        if (!Sessao.getCargo().equals("Aprovisionador") && modo.equals("Solicitar") &&
                                (selecionado.getStatus().equals(statusItem2) || selecionado.getStatus().equals(statusItem3) || selecionado.getStatus().equals(statusItem4) || selecionado.getStatus().equals(statusItem6))) {
                            if (selecionado != null) {
                                SolicitarItem(codItemSelecionado, codOperacaoItemSelecionado, codOsItemSelecionado,
                                        descricaoItemSelecionado, qtdPedidoItemSelecionado, idItemselecionado,
                                        localizacao, status, qtdRecebida, idOperacaoItemSelecionado);
                            }
                        }

                        // --- Lógica de Entrada/Retirada (Admin/Aprovisionador) ---
                        // Permissão: Admin ou Aprovisionador, com status compatível
                        if (!Sessao.getCargo().equals("Mecânico")) {

                            // Verifica o "modo" da tela (definido na abertura)
                            switch (modo) {
                                case "Entrada":
                                    // Só pode dar entrada se estiver "Aguardando entrega" ou "Recebido parcial"
                                    // Bloqueia se for "Recebido integral"
                                    if (selecionado.getStatus().equals(statusItem1) || selecionado.getStatus().equals(statusItem2)) {
                                        // Abre a janela de Lançar Entrada
                                        LancarEntradaItem(codItemSelecionado, codOperacaoItemSelecionado,
                                                codOsItemSelecionado, descricaoItemSelecionado,
                                                qtdPedidoItemSelecionado, idItemselecionado, idOperacaoItemSelecionado, qtdRecebida);
                                    } else if (selecionado.getStatus().equals(statusItem3)) {
                                        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "O item já foi totalmente recebido e não pode ser lançado novamente.")
                                                .showAndWait();
                                        return;
                                    }
                                    break;

                                case "Retirar":
                                    // Só pode retirar se estiver "Solicitado entrega parcial" ou "Solicitado entrega integral"
                                    if (selecionado.getStatus().equals(statusItem4) || selecionado.getStatus().equals(statusItem5) || selecionado.getStatus().equals(statusItem6)) {

                                        // Busca dados atualizados (localização, etc.) antes de abrir a janela
                                        try (Connection connectDB = new DataBaseConection().getConection()) {
                                            CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_item_att_saida(?) }");
                                            cs.setInt(1, idItemselecionado);

                                            try (ResultSet rs = cs.executeQuery()) {
                                                if (rs.next()) {
                                                    localizacao = rs.getString("localizacao");
                                                    status = rs.getString("status");
                                                    qtdRecebida = rs.getInt("qtd_recebida");
                                                }
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }

                                        // Abre a janela de Lançar Saída/Retirada
                                        LancaSaidaItem(codItemSelecionado, codOperacaoItemSelecionado, codOsItemSelecionado,
                                                descricaoItemSelecionado, qtdPedidoItemSelecionado, idItemselecionado,
                                                localizacao, status, qtdRecebida, idOperacaoItemSelecionado);
                                    }
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return row; // Retorna a linha configurada
        });

    } // Fim do método initialize()

    private void lancarEntradaTodosItens(String os) {
        Operacao operacaoSelecionada = consultTableOperacao.getSelectionModel().getSelectedItem();
        if (operacaoSelecionada == null) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Nenhuma operação selecionada.").showAndWait();
            return;
        }

        String codOperacaoSelecionada = operacaoSelecionada.getCodOperacao();

        // 1. Pegar a lista de itens que serão atualizados.
        // Usamos a 'itensFiltrados' que já mostra os itens da operação selecionada.
        // Adicionamos um filtro extra para pegar APENAS os que estão "Aguardando entrega".
        ObservableList<Item> itensParaAtualizar = itensFiltrados.stream()
                .filter(item -> item.getStatus().equals(statusItem1))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Cria uma nova lista

        if (itensParaAtualizar.isEmpty()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Não há itens 'Aguardando entrega' para esta operação.").showAndWait();
            return;
        }

        // 2. Abre a conexão UMA VEZ, fora do loop
        try (Connection conn = new DataBaseConection().getConection()) {
            String sql = "{CALL lancar_entrada_itens_por_operacao(?, ?, ?, ?, ?, ?, ?, ?)}";

            // 3. Faz o LOOP (para cada item da lista)
            for (Item item : itensParaAtualizar) {

                // 4. Prepara e executa a procedure PARA ESTE ITEM
                try (CallableStatement stmt = conn.prepareCall(sql)) {

                    // AGORA SIM você pode usar os dados do item:

                    stmt.setInt(1, item.getIdItem()); // p_id <- USA O ID DO ITEM
                    stmt.setString(2, os); // p_cod_os
                    stmt.setString(3, codOperacaoSelecionada); // p_cod_operacao
                    stmt.setString(4, statusItem3); // p_status
                    stmt.setString(5, "Item"); // p_tipo

                    // --- AQUI ESTÁ A SUA SOLUÇÃO ---
                    stmt.setString(6, "Item recebido (Integral) na base"); // p_descricao

                    stmt.setInt(7, Sessao.getMatricula()); // p_matricula

                    // --- E AQUI ---
                    stmt.setString(8, item.getCodItem()); // p_coditem

                    stmt.execute();
                }
                // O 'try-with-resources' fecha o 'stmt' aqui
            }
            // O loop termina

            // 5. Após o loop, atualiza a tela
            BuscarDB(os);
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Entrada lançada com sucesso para " + itensParaAtualizar.size() + " itens.").showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado ao lançar entrada para todos os itens: " + e.getMessage()).showAndWait();
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    }



    /**
     * Ação do botão "Buscar" (lupa).
     * Chama o método principal de busca no banco.
     */
    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        BuscarDB(consultNumeroOs.getText());
    }

    /**
     * Ação do botão "Voltar" (X).
     * Fecha a janela atual e aciona o callback 'onFecharJanela' (se existir).
     */
    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        // Mostra um GIF de loading (opcional)
        StackPane loadingPane = FormsUtil.createGifLoading();
        loadingPane.prefWidthProperty().bind(consultItemSplitPane.widthProperty());
        loadingPane.prefHeightProperty().bind(consultItemSplitPane.heightProperty());
        consultItemSplitPane.getChildren().add(loadingPane);

        // Se a tela anterior registrou um "ouvinte", chama ele agora
        if (onFecharJanela != null) {
            onFecharJanela.aoFecharJanela();
        }

        // Fecha a janela atual
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();

        // Remove o GIF de loading
        consultItemSplitPane.getChildren().remove(loadingPane);
    }

    /**
     * Método central de busca de dados no Banco de Dados.
     * Chama a procedure 'consultar_item' que retorna DOIS ResultSets.
     * @param numeroOs O número da OS a ser consultada.
     */
    public void BuscarDB(String numeroOs) {
        // Listas temporárias para armazenar os resultados da busca
        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        // Try-with-resources para garantir o fechamento da conexão
        try (Connection connectDB = new DataBaseConection().getConection()) {
            // Prepara a chamada da Stored Procedure
            CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_item(?) }");
            cs.setString(1, numeroOs);

            // Executa a procedure
            boolean hasResults = cs.execute();

            // --- Leitura do Primeiro ResultSet (Itens) ---
            if (hasResults) {
                try (ResultSet rsItens = cs.getResultSet()) {
                    while (rsItens.next()) {
                        // Tratamento especial: a procedure pode retornar 'resultado = 0' se a OS não existir
                        try {
                            int resultado = rsItens.getInt("resultado");
                            if (resultado == 0) {
                                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                                                "Não foi localizada ordem de serviço aberta com número informado")
                                        .showAndWait();
                                return; // Interrompe a busca
                            }
                        } catch (SQLException ignored) {
                            // Se der erro ao pegar 'resultado', é porque é uma linha de item normal.
                            // Ignora o erro e continua para carregar o item.
                            Item item = new Item(
                                    rsItens.getInt("id"),
                                    rsItens.getString("cod_item"),
                                    rsItens.getInt("id_operacao"),
                                    rsItens.getString("cod_operacao"),
                                    rsItens.getString("descricao"),
                                    rsItens.getInt("qtd_pedido"),
                                    rsItens.getInt("qtd_recebida"),
                                    rsItens.getString("status"),
                                    rsItens.getInt("qtd_solicitada"),
                                    rsItens.getInt("qtd_retirada")
                            );
                            listaItens.add(item); // Adiciona o item na lista temporária
                        }
                    }
                }
            }

            // --- Leitura do Segundo ResultSet (Operações) ---
            if (cs.getMoreResults()) {
                try (ResultSet rsOperacoes = cs.getResultSet()) {
                    while (rsOperacoes.next()) {
                        Operacao operacao = new Operacao(
                                rsOperacoes.getInt("id"),
                                rsOperacoes.getString("cod_operacao"),
                                rsOperacoes.getString("status")
                        );
                        listaOperacao.add(operacao); // Adiciona a operação na lista temporária
                    }
                }
            }

            // --- Atualização das Tabelas ---
            todosItens.clear();
            todosItens.addAll(listaItens);

            consultTableOperacao.setItems(listaOperacao);

            // Torna os painéis visíveis
            consultItenTableViewOperacao.setVisible(true);
            consultarItemSplitPane.setVisible(true);

            // Se houver operações, seleciona a primeira automaticamente
            if (!listaOperacao.isEmpty()) {
                Operacao primeiraOperacao = listaOperacao.get(0);
                consultTableOperacao.getSelectionModel().select(primeiraOperacao);

                // Atualiza o filtro da tabela de itens para mostrar apenas os itens da primeira operação
                String codOperacaoSelecionada = primeiraOperacao.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));

                // Mostra a tabela de itens
                consultItemTableViewItem.setVisible(true);
            } else {
                // Se não houver operações, esconde a tabela de itens
                consultItemTableViewItem.setVisible(false);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Classe de Modelo (POJO) estática para 'Item'.
     * Contém as propriedades JavaFX (SimpleStringProperty, etc.)
     * necessárias para o funcionamento do TableView.
     */
    public static class Item {
        // Propriedades JavaFX
        private SimpleIntegerProperty idItem;
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;
        private SimpleIntegerProperty qtdSolicitado;
        private SimpleIntegerProperty qtdEntregue;


        public Item() {
        }

        // Construtor principal
        public Item(int idItem, String codItem, int idOperacao, String codOperacao, String descricao, int qtdPedido, int qtdRecebida, String status, int qtdSolicitado, int qtdEntregue) {
            this.idItem = new SimpleIntegerProperty(idItem);
            this.codItem = new SimpleStringProperty(codItem);
            this.idOperacao = new SimpleIntegerProperty(idOperacao);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.descricao = new SimpleStringProperty(descricao);
            this.qtdPedido = new SimpleIntegerProperty(qtdPedido);
            this.qtdRecebida = new SimpleIntegerProperty(qtdRecebida);
            this.status = new SimpleStringProperty(status);
            this.qtdSolicitado = new SimpleIntegerProperty(qtdSolicitado);
            this.qtdEntregue = new SimpleIntegerProperty(qtdEntregue);
        }

        // --- Getters ---
        public int getIdItem() {
            return idItem.get();
        }
        public String getCodItem() {
            return codItem.get();
        }
        public int getIdOperacao() {
            return idOperacao.get();
        }
        public String getCodOperacao() {
            return codOperacao.get();
        }
        public String getDescricao() {
            return descricao.get();
        }
        public int getQtdPedido() {
            return qtdPedido.get();
        }
        public int getQtdRecebida() {
            return qtdRecebida.get();
        }
        public String getStatus() {
            return status.get();
        }

        // --- Setters ---
        public void setIdItem(int idItem) {
            this.idItem.set(idItem);
        }
        public void setCodItem(String codItem) {
            this.codItem.set(codItem);
        }
        public void setIdOperacao(int idOperacao) {
            this.idOperacao.set(idOperacao);
        }
        public void setCodOperacao(String codOperacao) {
            this.codOperacao.set(codOperacao);
        }
        public void setDescricao(String descricao) {
            this.descricao.set(descricao);
        }
        public void setQtdPedido(int qtdPedido) {
            this.qtdPedido.set(qtdPedido);
        }
        public void setQtdRecebida(int qtdRecebida) {
            this.qtdRecebida.set(qtdRecebida);
        }
        public void setStatus(String status) {
            this.status.set(status);
        }
        public int getQtdSolicitado() {
            return qtdSolicitado.get();
        }
        public int getQtdEntregue() {
            return qtdEntregue.get();
        }

        // --- Métodos Property (Necessários para o TableView) ---
        public SimpleStringProperty codItemProperty() {
            return codItem;
        }
        public SimpleStringProperty descricaoProperty() {
            return descricao;
        }
        public SimpleIntegerProperty qtdPedidoProperty() {
            return qtdPedido;
        }
        public SimpleIntegerProperty qtdRecebidaProperty() {
            return qtdRecebida;
        }
        public SimpleStringProperty statusProperty() {
            return status;
        }
        public void setQtdSolicitado(int qtdSolicitado) {
            this.qtdSolicitado.set(qtdSolicitado);
        }
        public void setQtdEntregue(int qtdEntregue) {
            this.qtdEntregue.set(qtdEntregue);
        }
    }

    /**
     * Classe de Modelo (POJO) estática para 'Operacao'.
     */
    public static class Operacao {
        private SimpleIntegerProperty id;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty status;


        public Operacao() {
        }

        public Operacao(int id,String codOperacao, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.status = new SimpleStringProperty(status);
        }

        // --- Getters ---
        public int getId() {
            return id.get();
        }
        public String getCodOperacao() {
            return codOperacao.get();
        }
        public String getStatus() {
            return status.get();
        }

        // --- Setters ---
        public void setId(int id) {
            this.id.set(id);
        }
        public void setCodOperacao(String codOperacao) {
            this.codOperacao.set(codOperacao);
        }
        public void setStatus(String status) {
            this.status.set(status);
        }

        // --- Métodos Property (Necessários para o TableView) ---
        public SimpleStringProperty codOperacaoProperty() {
            return codOperacao;
        }
        public SimpleStringProperty statusProperty() {
            return status;
        }
    }


    /**
     * Abre a janela (Stage) de "Entrada de Item" (entradaItem.fxml).
     * @param codItem Dados do item selecionado
     * @param codOperacao Dados do item selecionado
     * @param codOs Dados do item selecionado
     * @param descricaoItem Dados do item selecionado
     * @param qtdPedido Dados do item selecionado
     * @param idItem Dados do item selecionado
     * @param idOperacao Dados do item selecionado
     */
    public void LancarEntradaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, int idOperacao, int qtdRecebidaParcial) throws Exception {
        // Se a janela já estiver aberta, fecha a instância antiga
        if (janelaEntradaItem != null) {
            janelaEntradaItem.close();
            janelaEntradaItem = null;
        }

        try {
            // Carrega o arquivo FXML da nova janela
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/entradaItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            janelaEntradaItem = new Stage(); // Cria o novo Stage (janela)

            // Carrega as fontes customizadas
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Configura a cena para ser transparente
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            // Configura o Stage para ser transparente (sem bordas do Windows)
            janelaEntradaItem.initStyle(StageStyle.TRANSPARENT);
            janelaEntradaItem.setScene(scene);

            // Adiciona o ícone da aplicação
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaEntradaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // --- Habilita o arraste da janela (já que não tem borda) ---
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaEntradaItem.setX(event.getScreenX() - xOffset);
                janelaEntradaItem.setY(event.getScreenY() - yOffset);
            });
            // --------------------------------------------------------

            // Carrega o arquivo CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // Pega o Controller da janela que acabou de ser carregada
            EntradaItemController controller = fxmlLoader.getController();

            // "Injeta" os dados do item selecionado no controller da nova janela
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.setIdOperacao(idOperacao);
            controller.setqtdRecebidaParcial(qtdRecebidaParcial);
            controller.carregaDados(); // Chama o método do outro controller para ele popular os campos

            // Define o callback: Quando a janela 'EntradaItem' fechar,
            // ela vai chamar este código, que atualiza a tabela principal.
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    BuscarDB(consultNumeroOs.getText());
                }
            });


            // Configura e mostra a nova janela
            janelaEntradaItem.setTitle("Entrada de item");
            janelaEntradaItem.setResizable(false);
            janelaEntradaItem.show();

            // Foca o campo de quantidade automaticamente
            TextField tf = (TextField) root.lookup("#entradaQtdRecebida");
            tf.requestFocus();

            // Limpa a referência da janela quando ela for fechada
            janelaEntradaItem.setOnHidden(event -> janelaEntradaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre a janela (Stage) de "Saída de Item" (saidaItem.fxml).
     * A lógica é quase idêntica à de LancarEntradaItem.
     */
    public void LancaSaidaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, String localizacao, String status, int qtdRecebida, int idOperacao) throws Exception {
        // Garante que apenas uma janela de saída esteja aberta
        if (janelaSaidaItem != null) {
            janelaSaidaItem.close();
            janelaSaidaItem = null;
        }

        try {
            // Carrega o FXML da janela de saída
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/saidaItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            janelaSaidaItem = new Stage(); // Cria o novo Stage

            // Carrega fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Cena e Stage transparentes (sem borda)
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            janelaSaidaItem.initStyle(StageStyle.TRANSPARENT);
            janelaSaidaItem.setScene(scene);

            // Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaSaidaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Habilita o arraste da janela
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaSaidaItem.setX(event.getScreenX() - xOffset);
                janelaSaidaItem.setY(event.getScreenY() - yOffset);
            });

            // Carrega CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // Pega o controller da nova janela (SaidaItemController)
            SaidaItemController controller = fxmlLoader.getController();

            // "Injeta" os dados do item no novo controller
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.setLocalizacao(localizacao);
            controller.setStatus(status);
            controller.setQtdRecebida(qtdRecebida);
            controller.setIdOperacao(idOperacao);
            controller.carregaDados();
            controller.buscarQtdRetida();// Popula os campos da tela de saída

            // Define o callback para atualizar esta tela quando a janela de saída fechar
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    BuscarDB(consultNumeroOs.getText());
                }
            });

            // Configura e mostra a janela
            janelaSaidaItem.setTitle("Lançar entrega de item");
            janelaSaidaItem.setResizable(false);
            janelaSaidaItem.setScene(scene);
            janelaSaidaItem.show();


            // Limpa a referência da janela ao fechar
            janelaSaidaItem.setOnHidden(event -> janelaSaidaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SolicitarItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, String localizacao, String status, int qtdRecebida, int idOperacao) throws Exception {
        // Garante que apenas uma janela de saída esteja aberta
        if (janelaSolicitarItem != null) {
            janelaSolicitarItem.close();
            janelaSolicitarItem = null;
        }

        try {
            // Carrega o FXML da janela de saída
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/solicitarItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            janelaSolicitarItem = new Stage(); // Cria o novo Stage

            // Carrega fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Cena e Stage transparentes (sem borda)
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            janelaSolicitarItem.initStyle(StageStyle.TRANSPARENT);
            janelaSolicitarItem.setScene(scene);

            // Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaSolicitarItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Habilita o arraste da janela
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                janelaSolicitarItem.setX(event.getScreenX() - xOffset);
                janelaSolicitarItem.setY(event.getScreenY() - yOffset);
            });

            // Carrega CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // Pega o controller da nova janela (SaidaItemController)
            SolicitarItemController controller = fxmlLoader.getController();

            // "Injeta" os dados do item no novo controller
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.setLocalizacao(localizacao);
            controller.setStatus(status);
            controller.setQtdRecebida(qtdRecebida);
            controller.setIdOperacao(idOperacao);
            controller.verificarDisponibilidadeItem();
            controller.carregaDados(); // Popula os campos da tela de saída

            // Define o callback para atualizar esta tela quando a janela de saída fechar
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    BuscarDB(consultNumeroOs.getText());
                }
            });

            // Configura e mostra a janela
            janelaSolicitarItem.setTitle("Lançar entrega de item");
            janelaSolicitarItem.setResizable(false);
            janelaSolicitarItem.setScene(scene);
            janelaSolicitarItem.show();


            // Limpa a referência da janela ao fechar
            janelaSolicitarItem.setOnHidden(event -> janelaSolicitarItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Atualiza o Label (título) da janela com base no cargo do usuário e no "modo".
     * Este método é chamado pela tela anterior (ex: DashboardController)
     * *depois* que o 'modo' é definido.
     */
    public void AtualizarTituloPorModo() {
        // Se for Mecânico, o modo é sempre "Solicitar"
        if (Sessao.getCargo().equals("Mecânico")) {
            consultItemLabel.setText("Solicitar item");
        } else if (modo != null) {
            // Para outros cargos, verifica o modo
            switch (modo) {
                case "Entrada":
                    consultItemLabel.setText("Lançar entrada de item");
                    break;
                case "Retirar":
                    consultItemLabel.setText("Lançar retirada de item");
                    break;
                default:
                    consultItemLabel.setText("Solicitar item"); // Modo "Solicitar" ou padrão
            }
        }
    }

    /**
     * Configura dinamicamente o menu de contexto (clique direito) para uma linha da tabela de itens.
     * Este método é chamado sempre que a linha é atualizada.
     * @param row A linha da tabela (TableRow)
     * @param contextMenu O menu de contexto associado a esta linha
     */
    public void configurarContextMenu(TableRow<Item> row, ContextMenu contextMenu) {
        // Limpa os itens do menu anterior para reconstruí-lo
        contextMenu.getItems().clear();

        if (modo == null) return; // Se o modo não foi definido, não mostra menu

        Item selecionado = row.getItem(); // Pega o item da linha


        if (selecionado == null) return; // Se a linha estiver vazia, não mostra menu

        // --- Lógica de Solicitação (Mecânico ou outros) ---
        // 1. Modo "Solicitar" e item "Aguardando entrega"
        if (modo.equals("Solicitar") && selecionado.getStatus().equals(statusItem1)) {
            MenuItem solicitarItem = new MenuItem("Requisitar item");
            contextMenu.getItems().add(solicitarItem);

            // Ação: Mostra um alerta informando que não pode solicitar
            solicitarItem.setOnAction(event -> {
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "O item selecionado ainda consta como 'Aguardando entrega', a solicitação só pode ser realizada quando o item estiver com status 'Recebido'")
                        .showAndWait();
                return;
            });
        }
        // 2. Modo "Solicitar", status "Recebido" e usuário NÃO é Aprovisionador
        else if (modo.equals("Solicitar") && (selecionado.getStatus().equals(statusItem2) || selecionado.getStatus().equals(statusItem3) || selecionado.getStatus().equals(statusItem4) || selecionado.getStatus().equals(statusItem6))&& !Sessao.getCargo().equals("Aprovisionador")) {
            MenuItem solicitarItem = new MenuItem("Requisitar item");
            contextMenu.getItems().add(solicitarItem);

            // Ação: Confirma e chama a procedure 'solicitar_item'
            solicitarItem.setOnAction(event -> {
                try {
                    SolicitarItem(selecionado.getCodItem(), selecionado.getCodOperacao(), consultNumeroOs.getText(),
                            selecionado.getDescricao(), selecionado.getQtdPedido(), selecionado.getIdItem(),
                            localizacao, status, selecionado.getQtdRecebida(), selecionado.getIdOperacao());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // --- Lógica de Entrada/Retirada (Admin/Aprovisionador) ---
        // 3. Usuário NÃO é Mecânico (ou seja, Admin ou Aprovisionador)
        else if (!Sessao.getCargo().equals("Mecânico")) {
            // 3a. Modo "Entrada" e status "Aguardando entrega"
            if (modo.equals("Entrada") && (selecionado.getStatus().equals(statusItem1) || selecionado.getStatus().equals(statusItem4))){
                MenuItem lancarEntrada = new MenuItem("Lançar entrada");
                contextMenu.getItems().add(lancarEntrada);

                // Ação: Abre a janela de Lançar Entrada
                lancarEntrada.setOnAction(event -> {
                    try {
                        LancarEntradaItem(
                                selecionado.getCodItem(),
                                selecionado.getCodOperacao(),
                                consultNumeroOs.getText(),
                                selecionado.getDescricao(),
                                selecionado.getQtdPedido(),
                                selecionado.getIdItem(),
                                selecionado.getIdOperacao(),
                                selecionado.getQtdRecebida()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // 3b. Modo "Retirar" e status "Recebido" ou "Solicitado"
            else if (modo.equals("Retirar") && (selecionado.getStatus().equals(statusItem4) || selecionado.getStatus().equals(statusItem5) || selecionado.getStatus().equals(statusItem6))) {
                MenuItem lancarSaida = new MenuItem("Lançar retirada");
                contextMenu.getItems().add(lancarSaida);

                // Ação: Abre a janela de Lançar Saída
                lancarSaida.setOnAction(event -> {
                    // Primeiro, busca dados atualizados do item (localização, etc.)
                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        // NOTA: O nome da procedure aqui está '..._att_saida',
                        // no duplo clique estava '..._atualizardadossaida'
                        CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_item_att_saida(?) }");
                        cs.setInt(1, selecionado.getIdItem());

                        try (ResultSet rs = cs.executeQuery()) {
                            if (rs.next()) {
                                localizacao = rs.getString("localizacao");
                                status = rs.getString("status");
                                qtdRecebida = rs.getInt("qtd_recebida");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Agora, abre a janela de saída com os dados atualizados
                    try {
                        LancaSaidaItem(
                                selecionado.getCodItem(),
                                selecionado.getCodOperacao(),
                                consultNumeroOs.getText(),
                                selecionado.getDescricao(),
                                selecionado.getQtdPedido(),
                                selecionado.getIdItem(),
                                localizacao,
                                status,
                                qtdRecebida,
                                selecionado.getIdOperacao()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    } // Fim de configurarContextMenu()

} // Fim da classe