package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.excelHandling.LeitorExcel;
import com.example.trabalhoA3Gilvania.Utils.OnFecharJanela;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task; // Para tarefas em background (não travar a UI)
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane; // Usado para o GIF de loading
import javafx.stage.Stage;
import javafx.event.ActionEvent;

// Importações de bibliotecas externas e do Java
import java.io.FileInputStream;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;
import lombok.Cleanup; // (do Lombok) Garante que o FileInputStream será fechado
import org.apache.poi.ss.usermodel.DataFormatter; // (do Apache POI) Para ler células do Excel
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // (do Apache POI) Para ler arquivos .xlsx

import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "ImportarOs.fxml".
 * Esta tela permite ao usuário selecionar um arquivo Excel (.xlsx),
 * pré-visualizar o conteúdo (OS -> Operações -> Itens) em tabelas
 * e, em seguida, confirmar a importação dos dados para o banco de dados.
 */
public class ImportarOsController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button importVoltar;
    @FXML private Button importSelecionarExcel;
    @FXML private AnchorPane ImportarOsAcnhorPane; // O painel principal (usado para o loading)
    @FXML private AnchorPane importOsAnchorPanelTable; // Painel que contém as tabelas
    @FXML private Label importLabelSelecionar;
    @FXML private TextField importOsPathField; // Campo que mostra o caminho do arquivo
    @FXML private TableView<OrdemServico> consultTableOrdemServico;
    @FXML private TableColumn<OrdemServico, String> constulTabelCodOrdemServico;
    @FXML private TableView<Operacao> consultTableOperacao;
    @FXML private TableColumn<Operacao, String> constulTabelCodOperacao;
    @FXML private TableView<Item> consultTableItem;
    @FXML private TableColumn<Item, String> consultTableCodItem;
    @FXML private TableColumn<Item, String> consultTableDescricaoItem;
    @FXML private TableColumn<Item, Integer> consultTablePedidoItem;
    @FXML private SplitPane imortarSplitPane;
    @FXML private AnchorPane importarOsTableViewOrdem;
    @FXML private AnchorPane importarOsTableViewOperacao;
    @FXML private AnchorPane importarOsTableViewItens;
    @FXML private ImageView importarOsVoltarImage;


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


    // --- Campos Privados ---
    // Interface para callback (avisar a tela anterior para se atualizar)
    private OnFecharJanela listener;
    // Classe utilitária para criar alertas (pop-ups)
    FormsUtil alerta = new FormsUtil();

    /**
     * Define o "ouvinte" (listener/callback) que será acionado quando esta janela for fechada.
     */
    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    // Listas que guardam os dados lidos do Excel e alimentam as tabelas
    private final ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private final ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private final ObservableList<OrdemServico> todasOrdensServico = FXCollections.observableArrayList();

    // Classe que lida com a lógica de negócio (selecionar arquivo e cadastrar no DB)
    LeitorExcel cadastrarOs = new LeitorExcel();
    // Armazena o arquivo Excel selecionado pelo usuário
    File filePath;


    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     * Configura as tabelas, listeners e o estado inicial da tela.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configura a imagem do botão "Voltar"
        URL importarOsVoltarImageURL = getClass().getResource("/imagens/close.png");
        Image importarOsVoltarImageImagem = new Image(importarOsVoltarImageURL.toExternalForm());
        importarOsVoltarImage.setImage(importarOsVoltarImageImagem);

        importOsPathField.setDisable(true);
        importOsPathField.setFocusTraversable(false);

        constulTabelCodOrdemServico.setCellValueFactory(new PropertyValueFactory<>("codOrdemServico"));
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));

        constulTabelCodOrdemServico.setStyle("-fx-alignment: CENTER;");
        constulTabelCodOperacao.setStyle("-fx-alignment: CENTER;");
        consultTableDescricaoItem.setStyle("-fx-alignment: CENTER;");
        consultTablePedidoItem.setStyle("-fx-alignment: CENTER;");
        consultTableCodItem.setStyle("-fx-alignment: CENTER-LEFT;");
        consultTableDescricaoItem.setStyle("-fx-alignment: CENTER-LEFT;");

        consultTableOrdemServico.setItems(todasOrdensServico);
        consultTableOperacao.setItems(FXCollections.observableArrayList());
        consultTableItem.setItems(FXCollections.observableArrayList());

        consultTableOperacao.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        consultTableOperacao.setFocusTraversable(true);

        // --- Listener: seleção de OS ---
        consultTableOrdemServico.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedOS) -> {
                    if (selectedOS != null) {
                        ObservableList<Operacao> operacoesFiltradas = todasOperacoes.filtered(
                                op -> todosItens.stream()
                                        .anyMatch(item -> item.getCodOperacao().equals(op.getCodOperacao())
                                                && item.getCodOs().equals(selectedOS.getCodOrdemServico()))
                        );
                        consultTableOperacao.setItems(operacoesFiltradas);
                        consultTableOperacao.getSelectionModel().clearSelection();
                        consultTableItem.getItems().clear();

                        importarOsTableViewOperacao.setVisible(true);
                        importarOsTableViewItens.setVisible(false);

                        // 🔹 NOVO: pré-seleciona a primeira operação automaticamente
                        if (!operacoesFiltradas.isEmpty()) {
                            consultTableOperacao.getSelectionModel().selectFirst();
                        }
                    } else {
                        consultTableOperacao.setItems(FXCollections.observableArrayList());
                        consultTableItem.setItems(FXCollections.observableArrayList());
                        importarOsTableViewOperacao.setVisible(false);
                        importarOsTableViewItens.setVisible(false);
                    }
                }
        );

        // --- Listener: seleção de Operação ---
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedOperacao) -> {
                    if (selectedOperacao != null) {
                        OrdemServico osSelecionada = consultTableOrdemServico.getSelectionModel().getSelectedItem();
                        if (osSelecionada != null) {
                            ObservableList<Item> itensFiltrados = todosItens.filtered(
                                    item -> item.getCodOs().equals(osSelecionada.getCodOrdemServico())
                                            && item.getCodOperacao().equals(selectedOperacao.getCodOperacao())
                            );
                            consultTableItem.setItems(itensFiltrados);
                            importarOsTableViewItens.setVisible(!itensFiltrados.isEmpty());
                        }
                    } else {
                        consultTableItem.setItems(FXCollections.observableArrayList());
                        importarOsTableViewItens.setVisible(false);
                    }
                }
        );

        Platform.runLater(() -> {
            Stage stage = (Stage) importOsAnchorPanelTable.getScene().getWindow();
            stage.setOnHidden(event -> {
                if (listener != null) listener.aoFecharJanela();
            });
        });

        ImageView fecharImagem = (ImageView) importVoltar.getGraphic();
        importVoltar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            importVoltar.setCursor(Cursor.HAND);
        });
        importVoltar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            importVoltar.setCursor(Cursor.DEFAULT);
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) importSelecionarExcel.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });

    }
// Fim do initialize()


    /**
     * Ação do botão "Selecionar Excel".
     * Abre o seletor de arquivos e, se um arquivo for escolhido,
     * atualiza o campo de texto e chama a verificação/pré-visualização.
     */
    public void importSelecionarExcelOnAction(ActionEvent event){
        // Chama o método da classe GerenciadorOperacao que abre o FileChooser
        filePath = cadastrarOs.selecionarArquivo((Stage) importSelecionarExcel.getScene().getWindow());
        importOsPathField.setText(filePath.getAbsolutePath()); // Mostra o caminho no TextField
        verificarImport(); // Inicia a leitura do arquivo
    }

    /**
     * Ação do botão "Voltar".
     * Fecha a janela (Stage) atual.
     */
    public void importVoltarOnAction(ActionEvent event){
        Stage stage = (Stage) importVoltar.getScene().getWindow();
        stage.close(); // Isso aciona o listener 'setOnHidden' configurado no initialize()
    }

    /**
     * Verifica se um arquivo foi importado e inicia a tarefa de
     * pré-visualização (leitura do Excel) em uma thread separada (Task).
     */
    public void verificarImport(){
        if(filePath == null){
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione um arquivo para importar os dados")
                    .showAndWait();
        }
        else {
            // 1. Cria o painel de "loading" (GIF) e o sobrepõe à tela
            StackPane loadingPane = FormsUtil.createGifLoading();
            loadingPane.prefWidthProperty().bind(ImportarOsAcnhorPane.widthProperty());
            loadingPane.prefHeightProperty().bind(ImportarOsAcnhorPane.heightProperty());
            ImportarOsAcnhorPane.getChildren().add(loadingPane);

            // 2. Cria uma Task (tarefa em background) para ler o Excel
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    PreviewTable(filePath); // Chama o método pesado de leitura (Apache POI)
                    return null;
                }
            };

            // 3. Define o que fazer quando a Task terminar (na UI Thread)
            task.setOnSucceeded(event -> {
                ImportarOsAcnhorPane.getChildren().remove(loadingPane); // Remove o loading
                importOsAnchorPanelTable.setVisible(true); // Mostra as tabelas
                consultTableItem.setSelectionModel(null); // Desabilita seleção na tabela de itens
            });

            // 4. Define o que fazer se a Task falhar
            task.setOnFailed(event -> {
                ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                task.getException().printStackTrace(); // Mostra o erro no console
            });

            // 5. Inicia a Task em uma nova Thread
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Garante que a thread morra se a aplicação fechar
            thread.start();
        }
    }

    /**
     * Ação do botão "Importar".
     * Pega a OS selecionada e chama a lógica de negócio (em background)
     * para cadastrá-la no banco de dados.
     */
    public void importFazerImportOnAction(ActionEvent event) {
        // 1. Validação: Verifica se o arquivo foi selecionado
        if (filePath == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione um arquivo para importar os dados")
                    .showAndWait();
            return;
        }
        // 2. Validação: Verifica se uma OS na tabela foi selecionada
        else if (consultTableOrdemServico.getSelectionModel().getSelectedItem() == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione uma ordem de serviço da tabela para prosseguir")
                    .showAndWait();
            return;
        } else {
            // 3. Pede confirmação ao usuário
            OrdemServico ordemSelecionada = consultTableOrdemServico.getSelectionModel().getSelectedItem();
            if (ordemSelecionada != null) {
                String mensagem = "Tem certeza que deseja cadastrar a ordem de número: '"
                        + ordemSelecionada.getCodOrdemServico() + "' ?";
                boolean confirmar = alerta.criarAlertaConfirmacao("Aviso", mensagem);

                if (confirmar) {
                    String codOrdemSelecionada = ordemSelecionada.getCodOrdemServico();
                    try {
                        // 4. Cria o overlay de "loading"
                        StackPane loadingPane = FormsUtil.createGifLoading();
                        loadingPane.prefWidthProperty().bind(ImportarOsAcnhorPane.widthProperty());
                        loadingPane.prefHeightProperty().bind(ImportarOsAcnhorPane.heightProperty());
                        loadingPane.setStyle("""
                        -fx-background-color: rgba(0,0,0,0.15);
                        -fx-background-radius: 17.5;
                        -fx-border-radius: 17.5;
                        -fx-border-color: #c3c3c3;
                        -fx-border-width: 1.2;
                    """);
                        ImportarOsAcnhorPane.getChildren().add(loadingPane);

                        // 5. Cria a Task em background para o cadastro no DB
                        Task<Integer> task = new Task<>() {
                            @Override
                            protected Integer call() throws Exception {
                                LeitorExcel op = new LeitorExcel();
                                // Chama o método 'criar' que vai ao DB e retorna um código de status
                                return op.criar(codOrdemSelecionada, filePath);
                            }
                        };

                        // 6. Define o que fazer quando a Task terminar (na UI Thread)
                        task.setOnSucceeded(event2 -> {
                            ImportarOsAcnhorPane.getChildren().remove(loadingPane); // Remove o loading
                            int resultado = task.getValue(); // Pega o código de status (0, 1, 2, 3)

                            // ⚡ Garante que os alertas sejam exibidos na Thread da Aplicação
                            Platform.runLater(() -> {
                                // Trata os diferentes códigos de resultado
                                switch (resultado) {
                                    case 0:
                                        alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao processar a OS").showAndWait();
                                        break;
                                    case 1:
                                        alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Ordem já cadastrada").showAndWait();
                                        break;
                                    case 2:
                                        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Ordem cadastrada com sucesso").showAndWait();
                                        break;
                                    case 3:
                                        alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao finalizar o cadastro da OS").showAndWait();
                                        break;
                                }

                                // Reexibe as tabelas
                                importOsAnchorPanelTable.setVisible(true);
                                consultTableItem.setSelectionModel(null);
                            });
                        });

                        // 7. Define o que fazer se a Task falhar
                        task.setOnFailed(event2 -> {
                            ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                            Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado").showAndWait());
                        });

                        // 8. Inicia a Task
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();

                    } catch (Exception e) {
                        Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao cadastrar a ordem de serviço").showAndWait());
                    }
                }
            }
        }
    }
// Fim do importFazerImportOnAction()

    /**
     * Lê o arquivo Excel (usando Apache POI) e popula as listas
     * 'todasOrdensServico', 'todasOperacoes' e 'todosItens'.
     * Este método é executado em background pela Task 'verificarImport'.
     *
     * @param fileSelected O arquivo .xlsx selecionado pelo usuário.
     */
    public void PreviewTable(File fileSelected) {
        try {
            DataFormatter formatter = new DataFormatter();
            todasOrdensServico.clear();
            todasOperacoes.clear();
            todosItens.clear();

            @Cleanup FileInputStream file = new FileInputStream(fileSelected);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String osString = formatter.formatCellValue(row.getCell(1));
                String operacaoString = formatter.formatCellValue(row.getCell(2));
                String codItem = row.getCell(4).getStringCellValue();
                String descricaoItem = row.getCell(5).getStringCellValue();
                int qtdItem = (int) row.getCell(6).getNumericCellValue();

                boolean existeOrdemServico = todasOrdensServico.stream()
                        .anyMatch(op -> op.getCodOrdemServico().equals(osString));
                if (!existeOrdemServico) todasOrdensServico.add(new OrdemServico(osString));

                Item item = new Item(codItem, operacaoString, descricaoItem, qtdItem, osString);
                if (item.getQtdPedido() != 0) todosItens.add(item);

                boolean existeOperacao = todasOperacoes.stream()
                        .anyMatch(op -> op.getCodOperacao().equals(operacaoString));
                if (!existeOperacao) todasOperacoes.add(new Operacao(operacaoString));
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso",
                                "Erro ao tentar ler o arquivo, certifique que o arquivo selecionado segue o modelo de importação")
                        .showAndWait();
                importOsPathField.setText("");
                importarOsTableViewOrdem.setVisible(false);
                importarOsTableViewOperacao.setVisible(false);
                importarOsTableViewItens.setVisible(false);
            });
            return;
        }

        Platform.runLater(() -> {
            importLabelSelecionar.setVisible(true);
            imortarSplitPane.setVisible(true);
            importarOsTableViewOrdem.setVisible(true);

            // 🔹 NOVO: pré-seleciona automaticamente a primeira OS e sua primeira operação
            if (!todasOrdensServico.isEmpty()) {
                consultTableOrdemServico.getSelectionModel().selectFirst();
                OrdemServico primeiraOS = consultTableOrdemServico.getSelectionModel().getSelectedItem();

                if (primeiraOS != null) {
                    ObservableList<Operacao> operacoesFiltradas = todasOperacoes.filtered(
                            op -> todosItens.stream()
                                    .anyMatch(item -> item.getCodOperacao().equals(op.getCodOperacao())
                                            && item.getCodOs().equals(primeiraOS.getCodOrdemServico()))
                    );
                    consultTableOperacao.setItems(operacoesFiltradas);

                    if (!operacoesFiltradas.isEmpty()) {
                        consultTableOperacao.getSelectionModel().selectFirst();
                        Operacao primeiraOperacao = consultTableOperacao.getSelectionModel().getSelectedItem();

                        if (primeiraOperacao != null) {
                            ObservableList<Item> itensFiltrados = todosItens.filtered(
                                    item -> item.getCodOs().equals(primeiraOS.getCodOrdemServico())
                                            && item.getCodOperacao().equals(primeiraOperacao.getCodOperacao())
                            );
                            consultTableItem.setItems(itensFiltrados);
                            importarOsTableViewItens.setVisible(!itensFiltrados.isEmpty());
                        }
                    }
                }
            }
        });
    }
// Fim do PreviewTable()


    /**
     * Método auxiliar de filtro.
     * (Nota: Este método parece não estar sendo usado, pois a lógica
     * de filtro principal está implementada nos Listeners do initialize().)
     */
    private void filtrarOperacoesEItens(String codOS) {
        try {
            // Filtra Operações relacionadas à OS
            ObservableList<Operacao> operacoesFiltradas = todasOperacoes.filtered(
                    op -> todosItens.stream()
                            .anyMatch(item -> item.getCodOperacao().equals(op.getCodOperacao())
                                    && item.getCodOs().equals(codOS))
            );
            consultTableOperacao.setItems(operacoesFiltradas);

            // Filtra Itens relacionados à OS
            ObservableList<Item> itensFiltrados = todosItens.filtered(
                    item -> item.getCodOs().equals(codOS)
            );
            consultTableItem.setItems(itensFiltrados);

        } catch (Exception e) {
            // (O bloco catch original estava quebrado, foi corrigido para dentro do catch)
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Erro ao tentar ler o arquivo, certifique que o arquivo selecionado segue o modelo de importação")
                    .showAndWait();

            importarOsTableViewOrdem.setVisible(false);
            importarOsTableViewOperacao.setVisible(false);
            importarOsTableViewItens.setVisible(false);
            return;
        }
    } // Fim do filtrarOperacoesEItens()


    // ===================================================
    // === CLASSES INTERNAS DE MODELO (POJOs com Props) ===
    // ===================================================

    /**
     * Classe de Modelo (POJO) estática para 'Item'.
     * Contém as propriedades JavaFX para o TableView de Itens.
     */
    public static class Item {
        private SimpleStringProperty codItem;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleObjectProperty<Integer> qtdPedido; // Usado Object para permitir 'Integer'
        private SimpleStringProperty status;
        private SimpleStringProperty codOs; // <--- Adicionado para vincular o item à OS

        // Construtor principal
        public Item(String codItem, String codOperacao, String descricao, Integer qtdPedido, String codOs) {
            this.codItem = new SimpleStringProperty(codItem != null ? codItem : "");
            this.codOperacao = new SimpleStringProperty(codOperacao != null ? codOperacao : "");
            this.descricao = new SimpleStringProperty(descricao != null ? descricao : "");
            this.qtdPedido = new SimpleObjectProperty<>(qtdPedido);
            this.status = new SimpleStringProperty(""); // Status inicializado como vazio
            this.codOs = new SimpleStringProperty(codOs != null ? codOs : "");
        }

        // --- Getters (usados pelo PropertyValueFactory) ---
        public String getCodItem() { return codItem.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public String getDescricao() { return descricao.get(); }
        public Integer getQtdPedido() { return qtdPedido.get(); }
        public String getStatus() { return status.get(); }
        public String getCodOs() { return codOs.get(); }

        // --- Setters ---
        public void setCodItem(String codItem) { this.codItem.set(codItem); }
        public void setCodOperacao(String codOperacao) { this.codOperacao.set(codOperacao); }
        public void setDescricao(String descricao) { this.descricao.set(descricao); }
        public void setQtdPedido(Integer qtdPedido) { this.qtdPedido.set(qtdPedido); }
        public void setStatus(String status) { this.status.set(status); }
        public void setCodOs(String codOs) { this.codOs.set(codOs); }

        // --- Métodos property() (Necessários para o TableView) ---
        public SimpleStringProperty codItemProperty() { return codItem; }
        public SimpleStringProperty codOperacaoProperty() { return codOperacao; }
        public SimpleStringProperty descricaoProperty() { return descricao; }
        public SimpleObjectProperty<Integer> qtdPedidoProperty() { return qtdPedido; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty codOsProperty() { return codOs; }
    }


    /**
     * Classe de Modelo (POJO) estática para 'OrdemServico'.
     * Contém as propriedades JavaFX para o TableView de Ordens de Serviço.
     */
    public static class OrdemServico {
        private final SimpleStringProperty codOrdemServico;

        // Construtor vazio
        public OrdemServico() {
            this.codOrdemServico = new SimpleStringProperty("");
        }

        // Construtor principal
        public OrdemServico(String codOrdemServico) {
            this.codOrdemServico = new SimpleStringProperty(codOrdemServico);
        }

        // --- Getter, Setter e Property ---
        public String getCodOrdemServico() { return codOrdemServico.get(); }
        public void setCodOrdemServico(String codOrdemServico) { this.codOrdemServico.set(codOrdemServico); }
        public SimpleStringProperty codOrdemServicoProperty() { return codOrdemServico; }
    }

    /**
     * Classe de Modelo (POJO) estática para 'Operacao'.
     * Contém as propriedades JavaFX para o TableView de Operações.
     */
    public static class Operacao {
        private SimpleStringProperty codOperacao;

        // Construtor vazio
        public Operacao() {}

        // Construtor principal
        public Operacao(String codOperacao) {
            this.codOperacao = new SimpleStringProperty(codOperacao);
        }

        // --- Getter, Setter e Property ---
        public String getCodOperacao() { return codOperacao.get(); }
        public void setCodOperacao(String codOperacao) { this.codOperacao.set(codOperacao); }
        public SimpleStringProperty codOperacaoProperty() { return codOperacao; }
    }

} // Fim da classe ImportarOsController