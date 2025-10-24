package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


// Importações padrão do Java
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "ConsultarOs.fxml".
 * Esta tela permite ao usuário buscar uma Ordem de Serviço (OS) pelo número
 * e visualizar suas operações e os itens detalhados de cada operação.
 */
public class ConsultarOsController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button consultVoltarButton;
    @FXML private ImageView consultarBackImage;
    @FXML private TextField consultNumeroOs;
    @FXML private TableView<Item> consultTableItem;
    @FXML private TableView<Operacao> consultTableOperacao;
    @FXML private TableColumn<Operacao, String> constulTabelCodOperacao;
    @FXML private TableColumn<Operacao, String> consultTableOperacaoStatus;
    @FXML private TableColumn<Item, String> consultTableCodItem;
    @FXML private TableColumn<Item, String> consultTableItemSolicitado;
    @FXML private TableColumn<Item, String> consultTableItemEntregue;
    @FXML private TableColumn<Item, String> consultTableDescricaoItem;
    @FXML private TableColumn<Item, String> consultTablePedidoItem;
    @FXML private TableColumn<Item, String> consultTableRecebidoItem;
    @FXML private TableColumn<Item, String> consultTableItemStatus;
    @FXML private SplitPane consultarOsSplitPane;
    @FXML private AnchorPane consultarOsTableViewOperacao;
    @FXML private AnchorPane consultarOsTableViewItens;

    // --- Listas de Dados para as Tabelas ---

    // Lista principal que armazena todas as operações da OS buscada
    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    // Lista principal que armazena TODOS os itens da OS buscada
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();
    // Lista filtrada que será exibida na tabela de itens.
    // Ela é baseada na 'todosItens', mas filtrada pela operação selecionada.
    private FilteredList<Item> itensFiltrados;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Carrega a imagem "close.png" para o botão de voltar
        URL consultarBackImageURL = getClass().getResource("/imagens/close.png");
        Image consultarBack = new Image(consultarBackImageURL.toExternalForm());
        consultarBackImage.setImage(consultarBack);

        // --- Configuração das Colunas das Tabelas ---
        // Vincula as colunas às propriedades das classes Item e Operacao
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableItemSolicitado.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitado"));
        consultTableItemEntregue.setCellValueFactory(new PropertyValueFactory<>("qtdEntregue"));

        // Define o alinhamento centralizado para colunas específicas
        constulTabelCodOperacao.setStyle("-fx-alignment: CENTER;");
        consultTablePedidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableRecebidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableItemSolicitado.setStyle("-fx-alignment: CENTER;");
        consultTableItemEntregue.setStyle("-fx-alignment: CENTER;");

        // Define texto de placeholder (vazio)
        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));

        // Vincula a lista de operações à tabela
        consultTableOperacao.setItems(todasOperacoes);

        // --- Configuração de Filtro dos Itens ---
        // A tabela de itens usa a lista filtrada, que começa vazia (predicado `item -> false`)
        itensFiltrados = new FilteredList<>(todosItens, item -> false);
        consultTableItem.setItems(itensFiltrados);

        // --- Listener: seleção de operação ---
        // Ouve mudanças na seleção da tabela de operações
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Se uma nova operação for selecionada, mostra a tabela de itens
                consultarOsTableViewItens.setVisible(true);
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();

                // Atualiza o filtro da lista de itens para mostrar
                // apenas itens que pertencem à operação selecionada.
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));
            } else {
                // Se nenhuma operação for selecionada, esconde todos os itens
                itensFiltrados.setPredicate(item -> false);
            }
        });

        // --- Efeito visual no botão voltar ---
        ImageView fecharImagem = (ImageView) consultVoltarButton.getGraphic();
        // Efeito ao entrar com o mouse
        consultVoltarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            consultVoltarButton.setCursor(Cursor.HAND);
        });
        // Efeito ao sair com o mouse
        consultVoltarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            consultVoltarButton.setCursor(Cursor.DEFAULT);
        });

        // Define o Stage principal na classe utilitária
        Platform.runLater(() -> {
            Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    }
// Fim do initialize()


    /**
     * Ação do botão "Buscar" (lupa).
     * Primeiro valida se o campo OS foi preenchido, depois chama a busca no DB.
     */
    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        if (verificarNumeroOS()) {
            BuscarDB();
        }
    }

    /**
     * Ação do botão "Voltar" (X).
     * Fecha a janela (Stage) atual.
     */
    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        // Obtém a referência da janela (Stage) a partir do botão
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        // Fecha a janela
        stage.close();
    }

    /**
     * Método principal de busca de dados no Banco de Dados.
     * Chama a procedure 'consultar_os' que retorna TRÊS ResultSets.
     */
    public void BuscarDB() {
        // Esconde a tabela de itens antes de nova busca
        consultarOsTableViewItens.setVisible(false);

        // Listas temporárias para carregar os dados
        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        // **CORREÇÃO**: Adicionado .trim() para garantir que espaços
        //              não invalidem a busca no banco de dados.
        String numeroOs = consultNumeroOs.getText().trim();

        // Try-with-resources para garantir o fechamento da conexão e statement
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_os(?) }")) {

            cs.setString(1, numeroOs);
            boolean hasResults = cs.execute();

            // 1️⃣ Verifica se OS existe (Primeiro ResultSet)
            if (hasResults) {
                try (ResultSet rsCount = cs.getResultSet()) {
                    if (rsCount.next()) {
                        int total = rsCount.getInt("total");
                        if (total == 0) {
                            // Se a OS não existe, informa o usuário e para a execução
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "OS não encontrada").showAndWait();
                            consultarOsSplitPane.setVisible(false);
                            consultarOsTableViewOperacao.setVisible(false);
                            consultarOsTableViewItens.setVisible(false);
                            return;
                        }
                    }
                }
            }

            // 2️⃣ Carrega Itens (Segundo ResultSet)
            if (cs.getMoreResults()) {
                try (ResultSet rsItens = cs.getResultSet()) {
                    while (rsItens.next()) {
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
                        listaItens.add(item);
                    }
                }
                // Atualiza a lista principal de 'todosItens'
                todosItens.clear();
                todosItens.addAll(listaItens);
            }

            // 3️⃣ Carrega Operações (Terceiro ResultSet)
            if (cs.getMoreResults()) {
                try (ResultSet rsOperacoes = cs.getResultSet()) {
                    while (rsOperacoes.next()) {
                        Operacao operacao = new Operacao(
                                rsOperacoes.getInt("id"),
                                rsOperacoes.getString("cod_operacao"),
                                rsOperacoes.getString("status")
                        );
                        listaOperacao.add(operacao);
                    }
                }
                // Define as operações na tabela e na lista principal
                consultTableOperacao.setItems(listaOperacao);
                todasOperacoes.setAll(listaOperacao);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao buscar OS").showAndWait();
        }

        // Exibe os painéis com os resultados
        consultarOsSplitPane.setVisible(true);
        consultarOsTableViewOperacao.setVisible(true);

        // Pré-seleciona a primeira operação e carrega seus itens
        if (!todasOperacoes.isEmpty()) {
            consultTableOperacao.getSelectionModel().selectFirst();
            Operacao primeiraOperacao = consultTableOperacao.getSelectionModel().getSelectedItem();
            if (primeiraOperacao != null) {
                // Torna a tabela de itens visível (redundante, mas garante)
                consultarOsTableViewItens.setVisible(true);
                // Filtra os itens para a primeira operação
                String codOperacaoSelecionada = primeiraOperacao.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));
            }
        }
    }
// Fim do BuscarDB()


    /**
     * Valida se o campo de texto da Ordem de Serviço foi preenchido.
     * (Nota: isBlank() já verifica se é nulo, vazio ou só tem espaços em branco).
     * @return true se preenchido, false se estiver vazio.
     */
    public boolean verificarNumeroOS() {
        boolean retorno = true;
        // isBlank() cobre nulo, "", " "
        if(consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe o número da ordem de serviço")
                    .showAndWait();
            retorno = false; // Define o retorno como falso se o campo estiver vazio
        }
        return retorno; // Retorna o status da validação
    }

    /**
     * Classe de Modelo (POJO) estática para 'Item'.
     * Contém as propriedades JavaFX (SimpleStringProperty, etc.)
     * necessárias para o funcionamento do TableView de Itens.
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

        // Construtor vazio (potencialmente não utilizado, mas mantido)
        public Item(String codItem, String operacaoString, String descricaoItem, int qtdItem) {
        }

        // Construtor principal usado para popular a lista de itens
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

        // --- Getters (usados pelos PropertyValueFactory) ---
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
        public int getQtdSolicitado() {
            return qtdSolicitado.get();
        }
        public int getQtdEntregue() {
            return qtdEntregue.get();
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
        public void setQtdSolicitado(int qtdSolicitado) {
            this.qtdSolicitado.set(qtdSolicitado);
        }
        public void setQtdEntregue(int qtdEntregue) {
            this.qtdEntregue.set(qtdEntregue);
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
        public SimpleStringProperty codOperacaoProperty() {
            return codOperacao;
        }
        public SimpleIntegerProperty idOperacaoProperty() {
            return idOperacao;
        }
        public SimpleIntegerProperty qtdSolicitadoProperty() {
            return qtdSolicitado;
        }
        public SimpleIntegerProperty qtdEntregueProperty() {
            return qtdEntregue;
        }
    } // Fim da classe Item

    /**
     * Classe de Modelo (POJO) estática para 'Operacao'.
     * Contém as propriedades JavaFX necessárias para o
     * funcionamento do TableView de Operações.
     */
    public static class Operacao {
        // Propriedades JavaFX
        private SimpleIntegerProperty id;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty status;

        // Construtor vazio (potencialmente não utilizado, mas mantido)
        public Operacao(String operacaoString) {
        }

        // Construtor principal usado para popular a lista de operações
        public Operacao(int id,String codOperacao, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.status = new SimpleStringProperty(status);
        }

        // --- Getters (usados pelos PropertyValueFactory) ---
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
    } // Fim da classe Operacao

} // Fim da classe ConsultarOsController
