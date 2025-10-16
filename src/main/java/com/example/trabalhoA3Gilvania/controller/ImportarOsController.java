package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.Sessao;
import com.example.trabalhoA3Gilvania.excelHandling.GerenciadorOperacao;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;
import lombok.Cleanup;
import lombok.Data;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ImportarOsController implements Initializable {
    @FXML private Button importVoltar;
    @FXML private Button importFazerImport;
    @FXML private Button importSelecionarExcel;
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

    @FXML private ImageView importar1;
    @FXML private ImageView importar3;

    private final ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private final ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private final ObservableList<OrdemServico> todasOrdensServico = FXCollections.observableArrayList();

    GerenciadorOperacao cadastrarOs = new GerenciadorOperacao();
    File filePath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL importar1ImageURL = getClass().getResource("/imagens/importar1.png");
        Image importar1Image = new Image(importar1ImageURL.toExternalForm());
        importar1.setImage(importar1Image);

        URL importar3ImageURL = getClass().getResource("/imagens/importar3.png");
        Image importar3Image = new Image(importar3ImageURL.toExternalForm());
        importar3.setImage(importar3Image);

        importOsPathField.setDisable(true);
        importOsPathField.setFocusTraversable(false);

        // Configuração das colunas
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

        // Inicializa Operações e Itens com linhas vazias
        consultTableOperacao.setItems(FXCollections.observableArrayList(new Operacao("")));
        consultTableItem.setItems(FXCollections.observableArrayList(
                new Item("", "", "", null, "")
        ));


        // Listener para filtrar Operações e Itens ao selecionar OS
        consultTableOrdemServico.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedOS) -> {
                    if (selectedOS != null) {
                        filtrarOperacoesEItens(selectedOS.getCodOrdemServico());
                    } else {
                        consultTableOperacao.setItems(FXCollections.observableArrayList());
                        consultTableItem.setItems(FXCollections.observableArrayList());
                    }
                }
        );
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Selecione o arquivo");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
        }
        else {
            //String numeroOsDigitado = importNumeroOs.getText();
            //cadastrarOs.criar(numeroOsDigitado, filePath);

            try {
                PreviewTable(filePath);
                importLabelSelecionar.setVisible(true);
                importOsAnchorPanelTable.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                e.getCause();
            }
        }
    }

    public void importFazerImportOnAction(ActionEvent event) {
        OrdemServico ordemSelecionada = consultTableOrdemServico.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja cadstrar a ordem de numero: '" + ordemSelecionada.getCodOrdemServico() +"' ?");
        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
        stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));

        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {


            if (ordemSelecionada != null) {
                String codOrdemSelecionada = ordemSelecionada.getCodOrdemServico();
                try {
                    GerenciadorOperacao cadastrarOs = new GerenciadorOperacao();
                    cadastrarOs.criar(codOrdemSelecionada, filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    e.getCause();
                }
                DataBaseConection registarAtualizacao = new DataBaseConection();
                registarAtualizacao.AtualizarBanco(
                        "Ordem de Servico",
                        ordemSelecionada.getCodOrdemServico(),
                        "Cadastro de nova Ordem de Servico",
                        Sessao.getMatricula()
                );
            }
        }
    }

    public void PreviewTable(File fileSelected) throws IOException {
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
            todosItens.add(item);

            // Adiciona Operação (se ainda não existir)
            boolean existeOperacao = todasOperacoes.stream()
                    .anyMatch(op -> op.getCodOperacao().equals(operacaoString));
            if (!existeOperacao) {
                Operacao operacao = new Operacao(operacaoString);
                todasOperacoes.add(operacao);
            }
        }
    }

    // =================== NOVO MÉTODO DE FILTRO ===================
    private void filtrarOperacoesEItens(String codOS) {
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
