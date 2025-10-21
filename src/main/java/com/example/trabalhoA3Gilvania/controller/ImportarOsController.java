package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.excelHandling.GerenciadorOperacao;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.FileInputStream;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.example.trabalhoA3Gilvania.OnFecharJanela;

import java.util.ResourceBundle;

public class ImportarOsController implements Initializable {
    @FXML private Button importVoltar;
    @FXML private Button importFazerImport;
    @FXML private Button importSelecionarExcel;
    @FXML private AnchorPane ImportarOsAcnhorPane;
    @FXML private AnchorPane importOsAnchorPanelTable;
    @FXML private Label importLabelSelecionar;
    @FXML private TextField importNumeroOs;
    @FXML private TextField importOsPathField;
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

    private OnFecharJanela listener;
    FormsUtil alerta = new FormsUtil();

    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    private final ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private final ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private final ObservableList<OrdemServico> todasOrdensServico = FXCollections.observableArrayList();

    GerenciadorOperacao cadastrarOs = new GerenciadorOperacao();
    File filePath;



    @Override    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configura imagens
        URL importarOsVoltarImageURL = getClass().getResource("/imagens/close.png");
        Image importarOsVoltarImageImagem = new Image(importarOsVoltarImageURL.toExternalForm());
        importarOsVoltarImage.setImage(importarOsVoltarImageImagem);

        // Campo de caminho desabilitado
        importOsPathField.setDisable(true);
        importOsPathField.setFocusTraversable(false);

        // Configuração das colunas
        constulTabelCodOrdemServico.setCellValueFactory(new PropertyValueFactory<>("codOrdemServico"));
        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));

        // Ordem de Serviço inicia populada
        consultTableOrdemServico.setItems(todasOrdensServico);

        // Operações e Itens iniciam vazias
        consultTableOperacao.setItems(FXCollections.observableArrayList());
        consultTableItem.setItems(FXCollections.observableArrayList());

        // Habilitar seleção na tabela de operações
        consultTableOperacao.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        consultTableOperacao.setFocusTraversable(true);

        // Listener para filtrar Operações ao selecionar OS
        consultTableOrdemServico.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedOS) -> {
                    if (selectedOS != null) {
                        // Filtra operações relacionadas à OS
                        ObservableList<Operacao> operacoesFiltradas = todasOperacoes.filtered(
                                op -> todosItens.stream()
                                        .anyMatch(item -> item.getCodOperacao().equals(op.getCodOperacao())
                                                && item.getCodOs().equals(selectedOS.getCodOrdemServico()))
                        );
                        consultTableOperacao.setItems(operacoesFiltradas);

                        // 🔹 Resetar seleção da operação ao trocar de OS
                        consultTableOperacao.getSelectionModel().clearSelection();

                        // 🔹 Limpa itens ao trocar de OS
                        consultTableItem.getItems().clear();

                        importarOsTableViewOperacao.setVisible(true); // Mostrar tabela de operações
                        importarOsTableViewItens.setVisible(false);   // Esconder tabela de itens até selecionar operação
                    } else {
                        consultTableOperacao.setItems(FXCollections.observableArrayList());
                        consultTableItem.setItems(FXCollections.observableArrayList());
                        importarOsTableViewOperacao.setVisible(false);
                        importarOsTableViewItens.setVisible(false);
                    }
                }
        );

        // Listener para filtrar Itens ao selecionar Operação
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedOperacao) -> {
                    if (selectedOperacao != null) {
                        OrdemServico osSelecionada = consultTableOrdemServico.getSelectionModel().getSelectedItem();
                        if (osSelecionada != null) {
                            // Filtra itens relacionados à operação e OS
                            ObservableList<Item> itensFiltrados = todosItens.filtered(
                                    item -> item.getCodOs().equals(osSelecionada.getCodOrdemServico()) &&
                                            item.getCodOperacao().equals(selectedOperacao.getCodOperacao())
                            );

                            consultTableItem.setItems(itensFiltrados);
                            importarOsTableViewItens.setVisible(!itensFiltrados.isEmpty());
                        }
                    } else {
                        // 🔹 Se nenhuma operação selecionada, limpa itens
                        consultTableItem.setItems(FXCollections.observableArrayList());
                        importarOsTableViewItens.setVisible(false);
                    }
                }
        );

        // Callback ao fechar a janela
        Platform.runLater(() -> {
            Stage stage = (Stage) importOsAnchorPanelTable.getScene().getWindow();
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela();
                }
            });
        });

        ImageView fecharImagem = (ImageView) importVoltar.getGraphic();

        // Hover (mouse entrou)
        importVoltar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            importVoltar.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        importVoltar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            importVoltar.setCursor(Cursor.DEFAULT);
        });
    }


    public void importSelecionarExcelOnAction(ActionEvent event){
        filePath = cadastrarOs.selecionarArquivo((Stage) importSelecionarExcel.getScene().getWindow());
        importOsPathField.setText(filePath.getAbsolutePath());
        verificarImport();
    }

    public void importVoltarOnAction(ActionEvent event){
        Stage stage = (Stage) importVoltar.getScene().getWindow();
        stage.close();
    }

    public void verificarImport(){
        if(filePath == null){
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione um arquivo para importar os dados")
            .showAndWait();
        }
        else {
            StackPane loadingPane = FormsUtil.createGifLoading();
            loadingPane.prefWidthProperty().bind(ImportarOsAcnhorPane.widthProperty());
            loadingPane.prefHeightProperty().bind(ImportarOsAcnhorPane.heightProperty());
            ImportarOsAcnhorPane.getChildren().add(loadingPane);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    PreviewTable(filePath); // processamento pesado
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                importOsAnchorPanelTable.setVisible(true);
                consultTableItem.setSelectionModel(null);
            });
            task.setOnFailed(event -> {
                ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                task.getException().printStackTrace();
            });
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void importFazerImportOnAction(ActionEvent event) {
        if (filePath == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione um arquivo para importar os dados")
                    .showAndWait();
            return;
            }
            else if (consultTableOrdemServico.getSelectionModel().getSelectedItem() == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Selecione uma ordem de serviço da tabela para prosseguir")
                    .showAndWait();
                    return;
                } else {
            OrdemServico ordemSelecionada = consultTableOrdemServico.getSelectionModel().getSelectedItem();
            if (ordemSelecionada != null) {
                String mensagem = "Tem certeza que deseja cadastrar a ordem de número: '"
                        + ordemSelecionada.getCodOrdemServico() + "' ?";
                boolean confirmar = alerta.criarAlertaConfirmacao("Aviso", mensagem);

                if (confirmar) {
                    String codOrdemSelecionada = ordemSelecionada.getCodOrdemServico();
                    try {
                        // 🔹 Criar overlay com GIF de loading
                        StackPane loadingPane = FormsUtil.createGifLoading();
                        loadingPane.prefWidthProperty().bind(ImportarOsAcnhorPane.widthProperty());
                        loadingPane.prefHeightProperty().bind(ImportarOsAcnhorPane.heightProperty());
                        loadingPane.setStyle("-fx-background-color: rgba(0,0,0,0.15);"); // leve transparência
                        ImportarOsAcnhorPane.getChildren().add(loadingPane);

                        // 🔹 Task em background
                        Task<Integer> task = new Task<>() {
                            @Override
                            protected Integer call() throws Exception {
                                GerenciadorOperacao op = new GerenciadorOperacao();
                                return op.criar(codOrdemSelecionada, filePath); // retorna int
                            }
                        };

                        // 🔹 Ao terminar a Task
                        task.setOnSucceeded(event2 -> {
                            ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                            int resultado = task.getValue();

                            // ⚡ Atualiza UI na Application Thread
                            Platform.runLater(() -> {
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

                                // Atualiza tabela e seleção
                                importOsAnchorPanelTable.setVisible(true);
                                consultTableItem.setSelectionModel(null);
                            });
                        });

                        task.setOnFailed(event2 -> {
                            ImportarOsAcnhorPane.getChildren().remove(loadingPane);
                            Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado").showAndWait());
                        });

                        // 🔹 Inicia Task
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();

                    } catch (Exception e) {
                        Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR,"Erro", "Erro ao cadastrar a ordem de serviço").showAndWait());
                    }
                }
            }
        }

    }

    public void PreviewTable(File fileSelected) {
        try {
            DataFormatter formatter = new DataFormatter();

            // Limpa listas antes de adicionar novos dados
            todasOrdensServico.clear();
            todasOperacoes.clear();
            todosItens.clear();

            // Acessando o arquivo
            @Cleanup FileInputStream file = new FileInputStream(fileSelected);
            Workbook workbook = new XSSFWorkbook(file);

            // Seleciona a primeira aba
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String osString = formatter.formatCellValue(row.getCell(1)); // número da OS
                String operacaoString = formatter.formatCellValue(row.getCell(2)); // código da operação
                String codItem = row.getCell(4).getStringCellValue(); // código do item
                String descricaoItem = row.getCell(5).getStringCellValue(); // descrição do item
                int qtdItem = (int) row.getCell(6).getNumericCellValue(); // quantidade

                // Adiciona Ordem de Serviço (se ainda não existir)
                boolean existeOrdemServico = todasOrdensServico.stream()
                        .anyMatch(op -> op.getCodOrdemServico().equals(osString));
                if (!existeOrdemServico) {
                    OrdemServico ordemServicoItem = new OrdemServico(osString);
                    todasOrdensServico.add(ordemServicoItem);
                }

                // Adiciona Item (agora com codOS)
                Item item = new Item(codItem, operacaoString, descricaoItem, qtdItem, osString);
                if (item.getQtdPedido() != 0) {
                    todosItens.add(item);
                }

                // Adiciona Operação (se ainda não existir)
                boolean existeOperacao = todasOperacoes.stream()
                        .anyMatch(op -> op.getCodOperacao().equals(operacaoString));
                if (!existeOperacao) {
                    Operacao operacao = new Operacao(operacaoString);
                    todasOperacoes.add(operacao);
                }
            }
        } catch (Exception e) {
            // 🔹 Corrigido: chamar alert e atualizar UI na Application Thread
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

        // 🔹 Atualização de UI na Application Thread
        Platform.runLater(() -> {
            importLabelSelecionar.setVisible(true);
            imortarSplitPane.setVisible(true);
            importarOsTableViewOrdem.setVisible(true);
        });
    }


    // =================== NOVO MÉTODO DE FILTRO ===================
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
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Erro ao tentar ler o arquivo, certifique que o arquivo selecionado segue o modelo de importação")
                    .showAndWait();

        importarOsTableViewOrdem.setVisible(false);
        importarOsTableViewOperacao.setVisible(false);
        importarOsTableViewItens.setVisible(false);
        return;
    }
    }


    // =================== CLASSE ITEM ===================

    public static class Item {
        private SimpleStringProperty codItem;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleObjectProperty<Integer> qtdPedido;
        private SimpleStringProperty status;
        private SimpleStringProperty codOs; // <--- Adicionado

        public Item(String codItem, String codOperacao, String descricao, Integer qtdPedido, String codOs) {
            this.codItem = new SimpleStringProperty(codItem != null ? codItem : "");
            this.codOperacao = new SimpleStringProperty(codOperacao != null ? codOperacao : "");
            this.descricao = new SimpleStringProperty(descricao != null ? descricao : "");
            this.qtdPedido = new SimpleObjectProperty<>(qtdPedido); // pode ser null
            this.status = new SimpleStringProperty(""); // status inicial vazio
            this.codOs = new SimpleStringProperty(codOs != null ? codOs : "");
        }

        public String getCodItem() { return codItem.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public String getDescricao() { return descricao.get(); }
        public Integer getQtdPedido() { return qtdPedido.get(); }
        public String getStatus() { return status.get(); }
        public String getCodOs() { return codOs.get(); }

        public void setCodItem(String codItem) { this.codItem.set(codItem); }
        public void setCodOperacao(String codOperacao) { this.codOperacao.set(codOperacao); }
        public void setDescricao(String descricao) { this.descricao.set(descricao); }
        public void setQtdPedido(Integer qtdPedido) { this.qtdPedido.set(qtdPedido); }
        public void setStatus(String status) { this.status.set(status); }
        public void setCodOs(String codOs) { this.codOs.set(codOs); }

        public SimpleStringProperty codItemProperty() { return codItem; }
        public SimpleStringProperty codOperacaoProperty() { return codOperacao; }
        public SimpleStringProperty descricaoProperty() { return descricao; }
        public SimpleObjectProperty<Integer> qtdPedidoProperty() { return qtdPedido; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty codOsProperty() { return codOs; }
    }


    // =================== CLASSE ORDEM DE SERVIÇO ===================
    public static class OrdemServico {
        private final SimpleStringProperty codOrdemServico;

        public OrdemServico() {
            this.codOrdemServico = new SimpleStringProperty("");
        }

        public OrdemServico(String codOrdemServico) {
            this.codOrdemServico = new SimpleStringProperty(codOrdemServico);
        }

        public String getCodOrdemServico() { return codOrdemServico.get(); }
        public void setCodOrdemServico(String codOrdemServico) { this.codOrdemServico.set(codOrdemServico); }
        public SimpleStringProperty codOrdemServicoProperty() { return codOrdemServico; }
    }

    // =================== CLASSE OPERAÇÃO ===================
    public static class Operacao {
        private SimpleStringProperty codOperacao;

        public Operacao() {}

        public Operacao(String codOperacao) {
            this.codOperacao = new SimpleStringProperty(codOperacao);
        }

        public String getCodOperacao() { return codOperacao.get(); }
        public void setCodOperacao(String codOperacao) { this.codOperacao.set(codOperacao); }
        public SimpleStringProperty codOperacaoProperty() { return codOperacao; }
    }




}
