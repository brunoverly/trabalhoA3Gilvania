package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Utils.Sessao;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * Controlador JavaFX para a tela "FecharOs.fxml".
 * Esta tela permite buscar uma OS, exibir seus dados (operações e itens)
 * de forma não interativa (apenas visualização) e, em seguida,
 * confirmar o fechamento (encerramento) dessa OS.
 */
public class FecharOsController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button consultVoltarButton;
    @FXML private Label consultLabelOsBuscada;
    @FXML private ImageView fecharOsVoltar;
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
    @FXML private AnchorPane fecharAnchorPane; // Painel que contém as tabelas de resultado

    // Constantes de Status
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


    // --- Listas de Dados para as Tabelas ---
    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();

    // Interface usada como "callback" para notificar a tela anterior quando esta fechar.
    private OnFecharJanela listener;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    /**
     * Define o "ouvinte" (listener/callback) que será acionado quando esta janela for fechada.
     * @param listener A implementação da interface (geralmente vinda da tela anterior).
     */
    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega imagem do botão voltar
        URL fecharOsVoltarURL = getClass().getResource("/imagens/close.png");
        Image fecharOsVoltarImage = new Image(fecharOsVoltarURL.toExternalForm());
        fecharOsVoltar.setImage(fecharOsVoltarImage);

        // --- Configuração das colunas da tabela de Operações ---
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        //consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // --- Configuração das colunas da tabela de Itens ---
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableItemEntregue.setCellValueFactory(new PropertyValueFactory<>("qtdEntregue"));
        consultTableItemSolicitado.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitado"));

        // Define alinhamento centralizado para colunas
        constulTabelCodOperacao.setStyle("-fx-alignment: CENTER;");
        consultTablePedidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableRecebidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableItemEntregue.setStyle("-fx-alignment: CENTER;");
        consultTableItemSolicitado.setStyle("-fx-alignment: CENTER;");


        // --- Configuração das Listas e Tabelas ---
        consultTableOperacao.setItems(todasOperacoes);

        // Lista filtrada de itens (atualiza quando muda a seleção da operação)
        ObservableList<Item> itensFiltrados = FXCollections.observableArrayList();
        consultTableItem.setItems(itensFiltrados);

        // Listener: filtra os itens conforme a operação selecionada
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldOp, novaOp) -> {
            itensFiltrados.clear();
            if (novaOp != null) {
                // Mostra apenas os itens cujo ID da operação corresponde ao da operação selecionada
                for (Item item : todosItens) {
                    if (item.getIdOperacao() == novaOp.getId()) {
                        itensFiltrados.add(item);
                    }
                }
            }
        });

        // (Nota: Havia um listener duplicado aqui, foi removido na revisão)

        // Listener para selecionar automaticamente a primeira operação após carregar dados
        consultTableOperacao.getItems().addListener((javafx.collections.ListChangeListener<Operacao>) change -> {
            if (!consultTableOperacao.getItems().isEmpty()) {
                consultTableOperacao.getSelectionModel().selectFirst();
                Operacao primeira = consultTableOperacao.getSelectionModel().getSelectedItem();
                if (primeira != null) {
                    // Filtra os itens para a primeira operação selecionada
                    itensFiltrados.clear();
                    for (Item item : todosItens) {
                        if (item.getIdOperacao() == primeira.getId()) {
                            itensFiltrados.add(item);
                        }
                    }
                }
            }
        });


        // Configuração visual de placeholders
        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));

        // Configuração do Callback de Fechamento da Janela
        Platform.runLater(() -> {
            Stage stage = (Stage) fecharAnchorPane.getScene().getWindow();
            // Define o que acontece quando a janela é fechada
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela(); // Notifica a tela anterior
                }
            });
        });

        // Efeitos hover no botão voltar
        ImageView fecharImagem = (ImageView) consultVoltarButton.getGraphic();
        consultVoltarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            consultVoltarButton.setCursor(Cursor.HAND);
        });
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
     * Valida o campo de OS e, se OK, chama a busca no banco.
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
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close(); // Ao fechar, o 'stage.setOnHidden' (do initialize) será acionado
    }


    /**
     * Ação do botão "Confirmar Fechamento".
     * Chama a Stored Procedure 'encerrar_os' para efetivar o fechamento.
     */
    @FXML
    public void confirmCloseOsButton(ActionEvent event) {
        // 1. Validação: Verifica se o campo de OS está preenchido
        if (consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe o número da ordem de serviço")
                    .showAndWait();
            return; // Interrompe a ação
        }

        // Pede confirmação ao usuário
        boolean confirmar = alerta.criarAlertaConfirmacao("Confirmar", "Tem certeza que deseja encerrar esta ordem de serviço?");
        if (confirmar) {
            // **CORREÇÃO**: Adicionado .trim() para limpar a entrada
            String numeroOs = consultNumeroOs.getText().trim();

            // Try-with-resources para garantir o fechamento da conexão
            try (Connection connectDB = new DataBaseConection().getConection()) {

                // 2. Chama a procedure 'encerrar_os' (que realiza a AÇÃO de fechar)
                int resultadoEncerramento = 0;
                try (CallableStatement csEncerrar = connectDB.prepareCall("{ CALL projeto_java_a3.encerrar_os(?, ?, ?, ?) }")) {
                    // Define os parâmetros de entrada (IN)
                    csEncerrar.setString(1, numeroOs);
                    csEncerrar.setString(2, "Ordem de serviço"); // p_tipo (para log)
                    csEncerrar.setString(3, "Ordem de serviço encerrada"); // p_descricao (para log)
                    csEncerrar.setInt(4, Sessao.getMatricula()); // p_matricula (quem fechou)

                    // Executa a procedure e verifica se ela retornou um ResultSet
                    boolean hasRS = csEncerrar.execute();
                    if (hasRS) {
                        try (ResultSet rs = csEncerrar.getResultSet()) {
                            if (rs.next()) {
                                // Captura o código de resultado retornado pela procedure
                                resultadoEncerramento = rs.getInt("resultado");
                            }
                        }
                    }
                }

                // 3. Trata o código de resultado
                switch (resultadoEncerramento) {
                    case 0:
                        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço não encontrada").showAndWait();
                        break;
                    case 1:
                        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem já se encontra encerrada").showAndWait();
                        break;
                    case 2:
                        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço encerrada com sucesso").showAndWait();
                        break;
                    default:
                        alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro desconhecido ao encerrar OS").showAndWait();
                        break;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao conectar com banco de dados").showAndWait();
            }

            // 4. Limpa e esconde os campos/painéis após a tentativa de fechamento
            consultNumeroOs.setText("");
            consultLabelOsBuscada.setVisible(false);
            fecharAnchorPane.setVisible(false);
        }
        else{
            return; // Usuário clicou em "Cancelar" na confirmação
        }
    }




    /**
     * Método de busca no Banco de Dados para *exibir* os dados da OS (antes de fechar).
     * Chama a Stored Procedure 'encerrar_os_dados'.
     */
    public void BuscarDB() {
        // **CORREÇÃO**: Adicionado .trim() para limpar a entrada
        String numeroOs = consultNumeroOs.getText().trim();

        // Try-with-resources para garantir o fechamento da conexão
        try (Connection connectDB = new DataBaseConection().getConection()) {
            // Chama a procedure 'encerrar_os_dados' (que apenas CONSULTA)
            CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.encerrar_os_dados(?) }");
            cs.setString(1, numeroOs); // Define o parâmetro de entrada (IN)

            boolean hasResults = cs.execute();

            // --- 1️⃣ Leitura do Primeiro ResultSet (Verificação) ---
            // A procedure primeiro checa se a OS existe e está aberta
            if (hasResults) {
                try (ResultSet rsResultado = cs.getResultSet()) {
                    if (rsResultado.next()) {
                        int resultado = rsResultado.getInt("resultado");
                        if (resultado == 0) {
                            // Se resultado = 0, OS não encontrada ou já encerrada
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço não encontrada ou já encerrada")
                                    .showAndWait();
                            return; // Interrompe a execução (não há o que mostrar)
                        }
                    }
                }
            }

            // Se a OS existe, processa os próximos ResultSets
            ObservableList<Item> listaItens = FXCollections.observableArrayList();
            ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

            // --- 2️⃣ Leitura do Segundo ResultSet (Itens) ---
            if (cs.getMoreResults()) { // Avança para o próximo resultado
                try (ResultSet rsItens = cs.getResultSet()) {
                    while (rsItens.next()) {
                        // Cria um objeto Item para cada linha retornada
                        Item item = new Item(
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
                        listaItens.add(item); // Adiciona na lista temporária
                    }
                    // Atualiza a lista principal de itens
                    todosItens.clear();
                    todosItens.addAll(listaItens);
                }
            }

            // --- 3️⃣ Leitura do Terceiro ResultSet (Operações) ---
            if (cs.getMoreResults()) { // Avança para o último resultado
                try (ResultSet rsOperacoes = cs.getResultSet()) {
                    while (rsOperacoes.next()) {
                        // Cria um objeto Operacao para cada linha
                        Operacao operacao = new Operacao(
                                rsOperacoes.getInt("id"),
                                rsOperacoes.getString("cod_operacao"),
                                rsOperacoes.getString("status")
                        );
                        listaOperacao.add(operacao); // Adiciona na lista temporária
                    }
                    // Atualiza a lista principal de operações (ligada à tabela)
                    todasOperacoes.clear();
                    todasOperacoes.addAll(listaOperacao);
                }
            }

            // (As tabelas são atualizadas automaticamente pelos listeners
            //  e data binding configurados no 'initialize')

        } catch (SQLException ex) {
            ex.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao conectar com banco de dados").showAndWait();
        }

        // Se a busca foi bem-sucedida, exibe o painel de resultados
        consultLabelOsBuscada.setVisible(true);
        fecharAnchorPane.setVisible(true);

        // (Bloco duplicado removido na revisão)

        // Garante que a primeira operação será selecionada e seus itens filtrados
        if (!todasOperacoes.isEmpty()) {
            consultTableOperacao.getSelectionModel().selectFirst();
            Operacao primeira = consultTableOperacao.getSelectionModel().getSelectedItem();
            if (primeira != null) {
                // Filtra os itens para a primeira operação
                ObservableList<Item> itensFiltrados = consultTableItem.getItems();
                itensFiltrados.clear();
                for (Item item : todosItens) {
                    if (item.getIdOperacao() == primeira.getId()) {
                        itensFiltrados.add(item);
                    }
                }
            }
        }
    }


    /**
     * Valida se o campo de texto da Ordem de Serviço foi preenchido.
     * @return true se preenchido, false se estiver vazio ou nulo.
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
     * (Nota: Esta versão da classe Item não inclui 'idItem', pois é apenas para exibição).
     */
    public static class Item {
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;
        private SimpleIntegerProperty qtdSolicitado;
        private SimpleIntegerProperty qtdEntregue;

        // Construtor vazio
        public Item() {
        }

        // Construtor principal
        public Item(String codItem, int idOperacao, String codOperacao, String descricao, int qtdPedido, int qtdRecebida, String status, int qtdSolicitado, int qtdEntregue) {
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

        // --- Métodos property() (Necessários para o TableView) ---
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
        private SimpleIntegerProperty id;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty status;

        // Construtor vazio
        public Operacao() {
        }

        // Construtor principal
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

        // --- Métodos property() (Necessários para o TableView) ---
        public SimpleStringProperty codOperacaoProperty() {
            return codOperacao;
        }
        public SimpleStringProperty statusProperty() {
            return status;
        }
    } // Fim da classe Operacao

} // Fim da classe FecharOsController
