package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultarItemController implements Initializable{
    @FXML private Button consultVoltarButton;
    @FXML private Button consultBuscarOs;

    @FXML private Label consultItemLabel;
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
    @FXML private ImageView consultarItem1;
    @FXML private AnchorPane consultarItemAnchorPane;
    @FXML private AnchorPane consultItenTableViewOperacao;
    @FXML private AnchorPane consultItemTableViewItem;
    @FXML private SplitPane consultarItemSplitPane;
    @FXML private ImageView solicitarItemVoltarIImage;


    private Stage janelaEntradaItem;
    private Stage janelaSaidaItem;

    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados;

    private String modo;
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

    private OnFecharJanela onFecharJanela;
    private double xOffset = 0;
    private double yOffset = 0;

    FormsUtil alerta = new FormsUtil();

    public void setOnFecharJanela(OnFecharJanela onFecharJanela) {
        this.onFecharJanela = onFecharJanela;
    }


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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        URL solicitarItemVoltarIImageURL = getClass().getResource("/imagens/close.png");
        Image solicitarItemVoltarIImagem = new Image(solicitarItemVoltarIImageURL.toExternalForm());
        solicitarItemVoltarIImage.setImage(solicitarItemVoltarIImagem);

        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));
        consultTableOperacao.setItems(todasOperacoes); // tabela de operações
        itensFiltrados = new FilteredList<>(todosItens, item -> false);
        consultTableItem.setItems(itensFiltrados); // tabela de itens

        ImageView fecharImagem = (ImageView) consultVoltarButton.getGraphic();

        // Hover (mouse entrou)
        consultVoltarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            consultVoltarButton.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        consultVoltarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            consultVoltarButton.setCursor(Cursor.DEFAULT);
        });



        // Filtrar itens de acordo com a operação selecionada
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));
            } else {
                itensFiltrados.setPredicate(item -> false);
            }
        });

        // NOVO: ao clicar numa linha da tabela de operações, mostrar a tabela de itens
        consultTableOperacao.setOnMouseClicked(event -> {
            Operacao selecionada = consultTableOperacao.getSelectionModel().getSelectedItem();
            if (selecionada != null && event.getButton() == MouseButton.PRIMARY) {
                consultItemTableViewItem.setVisible(true);
            }
        });

        // Configuração do TableRow da tabela de operações para clique e duplo clique (se necessário)
        consultTableOperacao.setRowFactory(table -> {
            TableRow<Operacao> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    consultItemTableViewItem.setVisible(true);
                    // carregarItensDaOperacao(row.getItem().getCodOperacao());
                }
            });
            return row;
        });

        // Configuração do TableRow com ContextMenu da tabela de itens
        consultTableItem.setRowFactory(table -> {
            TableRow<Item> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // atualiza o menu sempre que o item da linha mudar
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                configurarContextMenu(row, contextMenu);
            });

            // só mostrar o menu em linhas não vazias
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            // Clique duplo para entrada/saída
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    Item selecionado = row.getItem();
                    String codItemSelecionado = selecionado.getCodItem();
                    String codOperacaoItemSelecionado = selecionado.getCodOperacao();
                    String codOsItemSelecionado = consultNumeroOs.getText();
                    String descricaoItemSelecionado = selecionado.getDescricao();
                    int idOperacaoItemSelecionado = selecionado.getIdOperacao();
                    int qtdPedidoItemSelecionado = selecionado.getQtdPedido();
                    int idItemselecionado = selecionado.getIdItem();

                    try {
                        if (modo == null) return;
                        if (modo.equals("Solicitar") && selecionado.getStatus().equals("Aguardando entrega")) {
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "O item selecionado ainda consta como 'Aguardando entrega', a solicitação só pode ser realizada quando o item estiver com status 'Recebido'")
                            .showAndWait();
                            return;
                        }

                        if(!Sessao.getCargo().equals("Aprovisionador") && modo.equals("Solicitar") && selecionado.getStatus().equals("Recebido")) {
                            if (selecionado != null) {
                                String mensagem = "Deseja solicitar a entrega do item: '"+ selecionado.getDescricao() +"' na oficina?";
                                boolean confirmacao = alerta.criarAlertaConfirmacao("Aviso", mensagem);
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                                if (confirmacao) {
                                    mensagem = "Requisitado a entrega do item: '" + selecionado.getDescricao() + "'" ;
                                    alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", mensagem)
                                            .showAndWait();

                                    // Atualiza status do item
                                    try (Connection connectDB = new DataBaseConection().getConection()) {
                                        String querySqlItem = "UPDATE item SET status = 'Solicitado na oficina' WHERE id = ?";
                                        String querySqlOperacao = "UPDATE operacao SET status = 'Item(s) solicitados' WHERE id = ?";
                                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                                            statement.setInt(1, selecionado.getIdItem());
                                            statement.executeUpdate();
                                        }
                                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                                            statement.setInt(1, selecionado.getIdOperacao());
                                            statement.executeUpdate();
                                        }
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    BuscarDB(consultNumeroOs.getText());

                                    DataBaseConection registarAtualizacao = new DataBaseConection();
                                    registarAtualizacao.AtualizarStatusPorSolicitacao(idOperacao);
                                    registarAtualizacao.AtualizarBanco(
                                            "Operação",
                                            codOsItemSelecionado,
                                            "Item solicitado na oficina",
                                            Sessao.getMatricula()
                                    );

                                    // Insere registro na tabela de controle de solicitações
                                    try (Connection connectDB = new DataBaseConection().getConection()) {
                                        String querySqlSolicitacao = "INSERT INTO controle_solicitacao_item (solicitador_por, id_item) VALUES (?, ?)";
                                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlSolicitacao)) {
                                            statement.setInt(1, Sessao.getMatricula());
                                            statement.setInt(2, selecionado.getIdItem());
                                            statement.executeUpdate();
                                        }
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }

                        if ((Sessao.getCargo().equals("Administrador") || Sessao.getCargo().equals("Aprovisionador")) &&
                                (selecionado.getStatus().equals("Aguardando entrega") || selecionado.getStatus().equals("Recebido") || selecionado.getStatus().equals("Solicitado na oficina"))) {

                            switch (modo) {
                                case "Entrada":
                                    if (selecionado.getStatus().equals("Aguardando entrega")) {
                                        LancarEntradaItem(codItemSelecionado, codOperacaoItemSelecionado,
                                                codOsItemSelecionado, descricaoItemSelecionado,
                                                qtdPedidoItemSelecionado, idItemselecionado,idOperacaoItemSelecionado);
                                    }
                                    break;

                                case "Retirar":
                                    if (selecionado.getStatus().equals("Solicitado na oficina") || selecionado.getStatus().equals("Recebido")) {
                                        try (Connection connectDB = new DataBaseConection().getConection()) {
                                            String querySqlItem = """
                    SELECT localizacao, status, qtd_recebida
                    FROM item
                    WHERE id = ?
                """;
                                            try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                                                statement.setInt(1, idItemselecionado);
                                                ResultSet rs = statement.executeQuery();
                                                if (rs.next()) {
                                                    localizacao = rs.getString("localizacao");
                                                    status = rs.getString("status");
                                                    qtdRecebida = rs.getInt("qtd_recebida");
                                                }
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }

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

            return row;
        });

    }




    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        if (verificarNumeroOS()) {
            BuscarDB(consultNumeroOs.getText());

        }
    }

    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        // Chama o callback ANTES de fechar
        if (onFecharJanela != null) {
            onFecharJanela.aoFecharJanela();
        }

        // Fecha a janela normalmente
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    public void BuscarDB(String numeroOs) {

        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlItem = """
                        SELECT item.id,
                               item.cod_item,
                               item.id_operacao,
                               operacao.cod_operacao,
                               item.descricao,
                               item.qtd_pedido,
                               item.qtd_recebida,
                               item.status
                        FROM item
                        JOIN operacao ON operacao.id = item.id_operacao
                        WHERE operacao.cod_os = ?
                    """;

            try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                statement.setString(1, numeroOs);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    Item item = new Item(
                            rs.getInt("id"),
                            rs.getString("cod_item"),
                            rs.getInt("id_operacao"),
                            rs.getString("cod_operacao"),
                            rs.getString("descricao"),
                            rs.getInt("qtd_pedido"),
                            rs.getInt("qtd_recebida"),
                            rs.getString("status")
                    );
                    listaItens.add(item);
                }
            }
            todosItens.clear();
            todosItens.addAll(listaItens);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlOperacao = """
                SELECT cod_operacao, MAX(id) AS id, MAX(status) AS status
                FROM operacao
                WHERE cod_os = ?
                GROUP BY cod_operacao
            """;

            try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                statement.setString(1, numeroOs);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    Operacao operacao = new Operacao(
                            rs.getInt("id"),
                            rs.getString("cod_operacao"),
                            rs.getString("status")
                    );
                    listaOperacao.add(operacao);
                }
            }

            consultTableOperacao.setItems(listaOperacao);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        consultItenTableViewOperacao.setVisible(true);
        consultarItemSplitPane.setVisible(true);
        consultItemTableViewItem.setVisible(false);
    }


    public boolean verificarNumeroOS() {
        boolean retorno = true;
        if(consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Informe o número da ordem de serviço")
                    .showAndWait();
            retorno = false;
        }
        else{
            try (Connection connectDB = new DataBaseConection().getConection()) {

                String verifcarCadastroBanco = "SELECT COUNT(*) FROM ordem_servico WHERE cod_os = ? AND status <> 'Encerrada'";
                try (PreparedStatement statement1 = connectDB.prepareStatement(verifcarCadastroBanco)) {
                    statement1.setString(1, consultNumeroOs.getText());
                    ResultSet resultadoBuscaOs = statement1.executeQuery();

                    if (resultadoBuscaOs.next()) {
                        int count = resultadoBuscaOs.getInt(1);
                        if (count == 0) {
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Não foi localizada ordem de serviço aberta com  número informado")
                                    .showAndWait();
                            retorno =  false;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return retorno;
    }

    public static class Item {
        private SimpleIntegerProperty idItem;
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;


        public Item() {
        }

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

        // Getters
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

        // Setters
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

        // Métodos property() para TableView bindings
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
    }

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

        // Getters
        public int getId() {
            return id.get();
        }
        public String getCodOperacao() {
            return codOperacao.get();
        }

        public String getStatus() {
            return status.get();
        }

        // Setters
        public void setId(int id) {
            this.id.set(id);
        }
        public void setCodOperacao(String codOperacao) {
            this.codOperacao.set(codOperacao);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        // Métodos property() para bindings (TableView, etc.)
        public SimpleStringProperty codOperacaoProperty() {
            return codOperacao;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }
    }


    public void LancarEntradaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, int idOperacao) throws Exception {
        // Se já existir, fecha a janela antes de abrir nova
        if (janelaEntradaItem != null) {
            janelaEntradaItem.close();
            janelaEntradaItem = null;
        }

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/entradaItem.fxml");
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
            janelaEntradaItem = new Stage();
            janelaEntradaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Obtém o controller e passa o parâmetro
            EntradaItemController controller = fxmlLoader.getController();
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.setIdOperacao(idOperacao);
            controller.carregaDados();
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    BuscarDB(consultNumeroOs.getText());
                }
            });



            // Configurar stage
            janelaEntradaItem.setTitle("Entrada de item");
            janelaEntradaItem.setResizable(false);
            janelaEntradaItem.setScene(scene);
            janelaEntradaItem.show();

            TextField tf = (TextField) root.lookup("#entradaQtdRecebida");
            tf.requestFocus();

            janelaEntradaItem.setOnHidden(event -> janelaEntradaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LancaSaidaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, String localizacao, String status, int qtdRecebida, int idOperacao) throws Exception {
        // Se já existir, fecha a janela antes de abrir nova
        if (janelaSaidaItem != null) {
            janelaSaidaItem.close();
            janelaSaidaItem = null;
        }

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/saidaItem.fxml");
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
            janelaSaidaItem = new Stage();
            janelaSaidaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            SaidaItemController controller = fxmlLoader.getController();
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
            controller.setOnFecharJanela(new OnFecharJanela() {
                @Override
                public void aoFecharJanela() {
                    BuscarDB(consultNumeroOs.getText());
                }
            });

            // Configurar stage
            janelaSaidaItem.setTitle("Lançar entrega de item");
            janelaSaidaItem.setResizable(false);
            janelaSaidaItem.setScene(scene);
            janelaSaidaItem.show();

            TextField tf = (TextField) root.lookup("#retirarMatriculaMecanico");
            tf.requestFocus();

            janelaSaidaItem.setOnHidden(event -> janelaSaidaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AtualizarTituloPorModo() {
        if (Sessao.getCargo().equals("Mecânico")) {
            consultItemLabel.setText("Solicitar item");
        } else if (modo != null) {

            switch (modo) {
                case "Entrada":
                    consultItemLabel.setText("Lançar entrada de item");
                    break;
                case "Retirar":
                    consultItemLabel.setText("Lançar retirada de item");
                    break;
                default:
                    consultItemLabel.setText("Solicitar item");
            }
        }
    }
    public void configurarContextMenu(TableRow<Item> row, ContextMenu contextMenu) {
        // Limpa os itens anteriores
        contextMenu.getItems().clear();

        if (modo == null) return;

        Item selecionado = row.getItem();
        if (selecionado == null) return;

        // 🔹 Caso o usuário nao seja aprovisionador e o modo seja "solicitar"
        if (modo.equals("Solicitar") && (selecionado.getStatus().equals("Recebido") || selecionado.getStatus().equals("Solicitado na oficina")  )&& !Sessao.getCargo().equals("Aprovisionador")) {
            MenuItem solicitarItem = new MenuItem("Requisitar item");
            contextMenu.getItems().add(solicitarItem);

            solicitarItem.setOnAction(event -> {
                String mensagem = "Deseja solicitar a entrega do item: '" + selecionado.getDescricao() + "' na oficina?";
                boolean confirmar = alerta.criarAlertaConfirmacao("Confirmar", mensagem );

                if (confirmar) {
                    mensagem = "Requisitado a entrega do item: '" + selecionado.getDescricao() + "'";
                    alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",mensagem)
                            .showAndWait();

                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlItem = "UPDATE item SET status = 'Solicitado na oficina' WHERE id = ?";
                        String querySqlOperacao = "UPDATE operacao SET status = 'Item(s) solicitados' WHERE id = ?";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                            statement.setInt(1, selecionado.getIdItem());
                            statement.executeUpdate();
                        }
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                            statement.setInt(1, selecionado.getIdOperacao());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    BuscarDB(consultNumeroOs.getText());

                    // Registrar atualização no banco
                    DataBaseConection registarAtualizacao = new DataBaseConection();
                    registarAtualizacao.AtualizarStatusPorSolicitacao(idOperacao);
                    registarAtualizacao.AtualizarBanco(
                            "Operação",
                            selecionado.getCodOperacao(),
                            "Item solicitado na oficina",
                            Sessao.getMatricula()
                    );

                    // Inserir registro de solicitação
                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlSolicitacao = "INSERT INTO controle_solicitacao_item (solicitador_por, id_item) VALUES (?, ?)";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlSolicitacao)) {
                            statement.setInt(1, Sessao.getMatricula());
                            statement.setInt(2, selecionado.getIdItem());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // 🔹 Caso o usuário seja ADMINISTRADOR ou APROVISIONADOR
        else if (!Sessao.getCargo().equals("Mecânico")) {
            if (modo.equals("Entrada") && selecionado.getStatus().equals("Aguardando entrega")) {
                MenuItem lancarEntrada = new MenuItem("Lançar entrada");
                contextMenu.getItems().add(lancarEntrada);

                lancarEntrada.setOnAction(event -> {
                    try {
                        LancarEntradaItem(
                                selecionado.getCodItem(),
                                selecionado.getCodOperacao(),
                                consultNumeroOs.getText(),
                                selecionado.getDescricao(),
                                selecionado.getQtdPedido(),
                                selecionado.getIdItem(),
                                selecionado.getIdOperacao()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // 👉 Se o modo for "retirar"
            else if (modo.equals("Retirar") && (selecionado.getStatus().equals("Recebido") || selecionado.getStatus().equals("Solicitado na oficina") )) {
                MenuItem lancarSaida = new MenuItem("Lançar retirada");
                contextMenu.getItems().add(lancarSaida);

                lancarSaida.setOnAction(event -> {
                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlItem = "SELECT localizacao, status, qtd_recebida FROM item WHERE id = ?";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                            statement.setInt(1, selecionado.getIdItem());
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                localizacao = rs.getString("localizacao");
                                status = rs.getString("status");
                                qtdRecebida = rs.getInt("qtd_recebida");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

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
    }

}


