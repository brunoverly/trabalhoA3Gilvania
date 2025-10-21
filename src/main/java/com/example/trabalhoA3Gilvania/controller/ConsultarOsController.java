package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;

// Importações de classes do JavaFX
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
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Carrega a imagem "close.png" para o botão de voltar
        URL consultarBackImageURL = getClass().getResource("/imagens/close.png");
        Image consultarBack = new Image(consultarBackImageURL.toExternalForm());
        consultarBackImage.setImage(consultarBack);

        // --- Configuração das Colunas das Tabelas ---
        // Vincula as colunas da Tabela de Operações às propriedades da classe 'Operacao'
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Vincula as colunas da Tabela de Itens às propriedades da classe 'Item'
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Define um texto padrão para tabelas vazias (aqui, vazio)
        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));

        // Vincula a lista de operações à Tabela de Operações
        consultTableOperacao.setItems(todasOperacoes);


        // --- Configuração das Listas e Filtros ---

        // Inicializa a lista de itens filtrados (baseada na 'todosItens'),
        // começando com um filtro que não mostra nada (item -> false)
        itensFiltrados = new FilteredList<>(todosItens, item -> false);
        // Vincula a lista filtrada à Tabela de Itens
        consultTableItem.setItems(itensFiltrados);

        // --- Listener de Seleção (Tabela de Operações) ---
        // Este é o "coração" da tela: filtra a tabela de itens
        // com base na operação selecionada na tabela de operações.
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Se uma nova operação for selecionada:
                // 1. Torna a tabela de itens visível
                consultarOsTableViewItens.setVisible(true);

                // 2. Define o filtro (predicado) da 'itensFiltrados'
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));

            } else {
                // Se nenhuma operação for selecionada (ex: limpar seleção),
                // o filtro esconde todos os itens.
                itensFiltrados.setPredicate(item -> false);
            }
        });

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
    } // Fim do initialize()


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
        // Esconde a tabela de itens no início de cada nova busca
        consultarOsTableViewItens.setVisible(false);

        // Listas temporárias para armazenar os resultados da busca
        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        // Pega o número da OS do campo de texto
        String numeroOs = consultNumeroOs.getText();

        // Try-with-resources para garantir que a conexão e o statement sejam fechados
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_os(?) }")) {

            cs.setString(1, numeroOs); // Define o parâmetro de entrada (IN) da procedure
            boolean hasResults = cs.execute(); // Executa a procedure

            // --- 1️⃣ Leitura do Primeiro ResultSet (COUNT) ---
            // A procedure primeiro retorna um 'SELECT COUNT(*)' para verificar se a OS existe.
            if (hasResults) {
                try (ResultSet rsCount = cs.getResultSet()) {
                    if (rsCount.next()) {
                        int total = rsCount.getInt("total");
                        if (total == 0) {
                            // Se a contagem for 0, a OS não foi encontrada
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "OS não encontrada").showAndWait();
                            // Esconde os painéis de resultado
                            consultarOsSplitPane.setVisible(false);
                            consultarOsTableViewOperacao.setVisible(false);
                            consultarOsTableViewItens.setVisible(false);
                            return; // Interrompe a execução do método
                        }
                    }
                }
            }

            // --- 2️⃣ Leitura do Segundo ResultSet (Itens) ---
            // Avança para o próximo resultado (SELECT * FROM itens...)
            if (cs.getMoreResults()) {
                try (ResultSet rsItens = cs.getResultSet()) {
                    // Itera sobre todos os itens retornados
                    while (rsItens.next()) {
                        Item item = new Item(
                                rsItens.getInt("id"),
                                rsItens.getString("cod_item"),
                                rsItens.getInt("id_operacao"),
                                rsItens.getString("cod_operacao"),
                                rsItens.getString("descricao"),
                                rsItens.getInt("qtd_pedido"),
                                rsItens.getInt("qtd_recebida"),
                                rsItens.getString("status")
                        );
                        listaItens.add(item); // Adiciona na lista temporária
                    }
                }
                // Atualiza a lista principal (que é a fonte do FilteredList)
                todosItens.clear();
                todosItens.addAll(listaItens);
            }

            // --- 3️⃣ Leitura do Terceiro ResultSet (Operações) ---
            // Avança para o último resultado (SELECT ... FROM operacoes GROUP BY...)
            if (cs.getMoreResults()) {
                try (ResultSet rsOperacoes = cs.getResultSet()) {
                    // Itera sobre todas as operações retornadas
                    while (rsOperacoes.next()) {
                        Operacao operacao = new Operacao(
                                rsOperacoes.getInt("id"),
                                rsOperacoes.getString("cod_operacao"),
                                rsOperacoes.getString("status")
                        );
                        listaOperacao.add(operacao); // Adiciona na lista temporária
                    }
                }
                // Define a lista de operações na tabela (esta é a 'todasOperacoes')
                consultTableOperacao.setItems(listaOperacao);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao buscar OS").showAndWait();
        }

        // Se a busca foi bem-sucedida (não saiu no 'return' do 'total == 0'),
        // torna os painéis de resultado visíveis.
        consultarOsSplitPane.setVisible(true);
        consultarOsTableViewOperacao.setVisible(true);
    } // Fim do BuscarDB()


    /**
     * Valida se o campo de texto da Ordem de Serviço foi preenchido.
     * @return true se preenchido, false se estiver vazio ou nulo.
     */
    public boolean verificarNumeroOS() {
        boolean retorno = true;
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

        // Construtor vazio (potencialmente não utilizado, mas mantido)
        public Item(String codItem, String operacaoString, String descricaoItem, int qtdItem) {
        }

        // Construtor principal usado para popular a lista de itens
        public Item(int idItem, String codItem, int idOperacao, String codOperacao, String descricao, int qtdPedido, int qtdRecebida, String status) {
            this.idItem = new SimpleIntegerProperty(idItem);
            this.codItem = new SimpleStringProperty(codItem);
            this.idOperacao = new SimpleIntegerProperty(idOperacao);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.descricao = new SimpleStringProperty(descricao);
            this.qtdPedido = new SimpleIntegerProperty(qtdPedido);
            this.qtdRecebida = new SimpleIntegerProperty(qtdRecebida);
            this.status = new SimpleStringProperty(status);
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