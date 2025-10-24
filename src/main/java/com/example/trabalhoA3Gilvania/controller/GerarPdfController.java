package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.PdfRetiradaItens;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GerarPdfController (respeita larguras definidas no FXML)
 * - usa coluna de checkbox definida no FXML (fx:id = pdfColumnCheck)
 * - NÃO altera larguras nem política de resize em runtime (preserva Scene Builder)
 * - controla visibilidade das tabelas OS/Operacao/Itens e limpa seleções após geração
 */
public class GerarPdfController implements Initializable {

    @FXML private Button consultVoltarButton;
    @FXML private Button pdfGerarPdfButton;
    @FXML private ImageView voltarImage;

    @FXML private TableView<Item> pdfTableItem;
    @FXML private TableView<Operacao> pdfTableOperacao;
    @FXML private TableView<OrdemServico> pdfTableOs;

    @FXML private TableColumn<OrdemServico, String> pdfColumnOs;
    @FXML private TableColumn<Operacao, String> pdfColumnOperacao;

    // coluna de checkbox declarada no FXML - use este id
    @FXML private TableColumn<Item, Boolean> pdfColumnCheck;

    @FXML private TableColumn<Item, String> pdfColumnCodItem;
    @FXML private TableColumn<Item, String> pdfColumnDescricao;
    @FXML private TableColumn<Item, Integer> pdfColumnSolicitado;
    @FXML private TableColumn<Item, Integer> pdfColumnRetirado;
    @FXML private TableColumn<Item, Integer> pdfColumnMatricula;

    @FXML private AnchorPane paneItens;

    private final ObservableList<OrdemServico> listaOs = FXCollections.observableArrayList();
    private final ObservableList<Operacao> listaOperacoes = FXCollections.observableArrayList();
    private final ObservableList<Item> listaItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados;

    private final FormsUtil alerta = new FormsUtil();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // --- Configura colunas (as existentes) ---
        pdfColumnOs.setCellValueFactory(new PropertyValueFactory<>("codOs"));
        pdfColumnOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));

        pdfColumnCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        pdfColumnDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        pdfColumnSolicitado.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitada"));
        pdfColumnRetirado.setCellValueFactory(new PropertyValueFactory<>("qtdRetirada"));
        pdfColumnMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaSolicitador"));

        // --- Inicializa tabelas ---
        pdfTableOs.setItems(listaOs);
        pdfTableOperacao.setItems(listaOperacoes);
        itensFiltrados = new FilteredList<>(listaItens, item -> false);
        pdfTableItem.setItems(itensFiltrados);

        // --- Configura coluna de seleção (declarada no FXML) ---
        if (pdfColumnCheck != null) {
            pdfColumnCheck.setCellValueFactory(cell -> {
                BooleanProperty prop = cell.getValue().selecionadoProperty();
                return prop;
            });
            pdfColumnCheck.setCellFactory(CheckBoxTableCell.forTableColumn(pdfColumnCheck));
            pdfColumnCheck.setEditable(true);
            // Não definimos larguras aqui — deixe as larguras no Scene Builder.
            pdfColumnCheck.setStyle("-fx-alignment: CENTER;");
        }

        // --- Permitir seleção múltipla nas tabelas ---
        pdfTableOperacao.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pdfTableItem.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pdfTableOs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // --- tornar a tabela editável (necessário para que o CheckBoxTableCell funcione ao clicar) ---
        pdfTableItem.setEditable(true);

        // NÃO alteramos aqui a política de resize nem as larguras — respeitamos o FXML/Scene Builder.
        // Se quiser alterar a política (mostrar scrollbar horizontal), defina no FXML:
        // <TableView ... columnResizePolicy="UNCONSTRAINED_RESIZE_POLICY"> ...

        // --- Inicialmente: somente a tabela de OS visível; operações e itens escondidas ---
        setOperacoesVisible(false);
        setItensVisible(false);

        // --- Listener de seleção de OS: filtra operações que pertencem à OS e mostra a tabela de operações ---
        pdfTableOs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String codOsSelecionada = newSelection.getCodOs();

                FilteredList<Operacao> operacoesFiltradas = new FilteredList<>(listaOperacoes, op ->
                        op.getCodOs() != null && op.getCodOs().equals(codOsSelecionada));
                pdfTableOperacao.setItems(operacoesFiltradas);

                // mostrar operações e limpar seleção anterior
                setOperacoesVisible(true);
                pdfTableOperacao.getSelectionModel().clearSelection();

                // limpar itens exibidos e esconder tabela de itens
                itensFiltrados.setPredicate(item -> false);
                setItensVisible(false);
            } else {
                pdfTableOperacao.setItems(FXCollections.observableArrayList());
                itensFiltrados.setPredicate(item -> false);
                setOperacoesVisible(false);
                setItensVisible(false);
            }
        });

        // --- Listener de seleção de Operações: quando houver 1+ ops selecionadas, exibe itens dessas ops ---
        pdfTableOperacao.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Operacao>) change -> {
            ObservableList<Operacao> opsSelecionadas = pdfTableOperacao.getSelectionModel().getSelectedItems();
            if (opsSelecionadas == null || opsSelecionadas.isEmpty()) {
                itensFiltrados.setPredicate(item -> false);
                setItensVisible(false);
            } else {
                Set<String> codOps = opsSelecionadas.stream()
                        .map(Operacao::getCodOperacao)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null && codOps.contains(item.getCodOperacao()));
                // mostra tabela de itens quando existir pelo menos uma operação selecionada
                setItensVisible(true);
                // garante que a tabela respeite o layout já configurado no FXML
                Platform.runLater(() -> pdfTableItem.refresh());
            }
        });

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

        // --- Carrega dados ao abrir ---
        carregarDados();
    }

    private void setOperacoesVisible(boolean visible) {
        pdfTableOperacao.setVisible(visible);
        pdfTableOperacao.setManaged(visible);
    }

    private void setItensVisible(boolean visible) {
        pdfTableItem.setVisible(visible);
        pdfTableItem.setManaged(visible);
        if (paneItens != null) {
            paneItens.setVisible(visible);
            paneItens.setManaged(visible);
        }
    }

    @FXML
    private void consultVoltarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Gera PDF com os itens selecionados (checkbox tem prioridade).
     */
    @FXML
    private void pdfGerarPdfButtonOnAction(ActionEvent event) {
        // 1) Coletar todos os itens DO MODELO que têm checkbox marcado
        List<Item> itensMarcados = listaItens.stream()
                .filter(Item::isSelecionado)
                .collect(Collectors.toList());

        if (itensMarcados.isEmpty()) {
            List<Item> selecionadosNativos = pdfTableItem.getSelectionModel().getSelectedItems().stream()
                    .collect(Collectors.toList());
            if (!selecionadosNativos.isEmpty()) {
                itensMarcados = selecionadosNativos;
            } else {
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Nenhum item",
                        "Nenhum item marcado (checkbox) para gerar PDF").showAndWait();
                return;
            }
        }

        // 2) Validar que todas as seleções têm a mesma matrícula
        Set<Integer> matriculas = itensMarcados.stream()
                .map(Item::getMatriculaSolicitador)
                .collect(Collectors.toSet());
        if (matriculas.size() > 1) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Matrículas diferentes",
                    "Selecione apenas itens com a mesma matrícula.").showAndWait();
            return;
        }
        int matricula = matriculas.iterator().next();
        String matriculaTexto = String.valueOf(matricula);

        // 3) Mapear para PdfRetiradaItens.Item
        List<com.example.trabalhoA3Gilvania.PdfRetiradaItens.Item> itensParaPdf = itensMarcados.stream()
                .map(i -> new com.example.trabalhoA3Gilvania.PdfRetiradaItens.Item(
                        i.getCodOs(),
                        i.getCodOperacao(),
                        i.getCodItem(),
                        i.getDescricao(),
                        String.valueOf(i.getQtdSolicitada()),
                        String.valueOf(i.getQtdRetirada()),
                        String.valueOf(i.getMatriculaSolicitador())
                ))
                .collect(Collectors.toList());

        // 4) Preparar caminho/nome do arquivo
        String userDesktop = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Retiradas";
        File pastaRetiradas = new File(userDesktop);
        if (!pastaRetiradas.exists()) pastaRetiradas.mkdirs();

        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataPorExtenso = hoje.format(formatter);

        Task<Void> gerarTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int numeroRegistroPdf = 0;

                try (Connection conn = new DataBaseConection().getConection()) {
                    // --- Cadastrar emissão no banco e pegar novo ID ---
                    String sql = "{CALL projeto_java_a3.registrar_pdf(?, ?, ?)}"; // 2 IN + 1 OUT
                    try (CallableStatement cs = conn.prepareCall(sql)) {
                        cs.setInt(1, Sessao.getMatricula()); // emitido_por
                        cs.setInt(2, matricula);             // emitido_para
                        cs.registerOutParameter(3, java.sql.Types.INTEGER); // p_id_pdf como OUT

                        cs.execute(); // executa o procedure

                        // Recupera o ID gerado
                        numeroRegistroPdf = cs.getInt(3);
                    }
                }

                // --- Preparar arquivo PDF ---
                String nomeArquivo = "Retirada_" + matriculaTexto + "_" + numeroRegistroPdf + ".pdf";
                String caminhoPdf = pastaRetiradas.getAbsolutePath() + File.separator + nomeArquivo;

                // --- Gerar PDF ---
                com.example.trabalhoA3Gilvania.PdfRetiradaItens.gerarPdf(
                        caminhoPdf,
                        String.valueOf(numeroRegistroPdf),
                        dataPorExtenso,
                        Sessao.getNome(),
                        "Almoxarife",
                        itensParaPdf,
                        Sessao.getNome(),
                        "Matr: " + matriculaTexto
                );

                // --- Abrir PDF gerado automaticamente ---
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(caminhoPdf));
                }

                return null;
            }
        };

        gerarTask.setOnSucceeded(evt -> Platform.runLater(() -> {
            clearSelectionsAfterGenerate();
        }));

        gerarTask.setOnFailed(evt -> {
            Throwable ex = gerarTask.getException();
            ex.printStackTrace();
            Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao gerar PDF: " + (ex != null ? ex.getMessage() : "erro desconhecido")).showAndWait());
        });

        Thread t = new Thread(gerarTask, "GerarPdfThread");
        t.setDaemon(true);
        t.start();
    }

    private void clearSelectionsAfterGenerate() {
        // limpar checkboxes no modelo
        listaItens.forEach(item -> {
            if (item.isSelecionado()) item.setSelecionado(false);
        });

        // limpar seleções nas tabelas de operações e itens
        pdfTableOperacao.getSelectionModel().clearSelection();
        pdfTableItem.getSelectionModel().clearSelection();

        // refresh para garantir atualização visual dos checkboxes
        Platform.runLater(() -> pdfTableItem.refresh());

        // esconder tabela de itens até nova seleção de operação
        itensFiltrados.setPredicate(item -> false);
        setItensVisible(false);
    }

    private void carregarDados() {
        String sql = "{CALL projeto_java_a3.buscar_itens_para_pdf()}";

        try (Connection conn = new DataBaseConection().getConection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            listaItens.clear();
            listaOperacoes.clear();
            listaOs.clear();

            while (rs.next()) {
                String codOs = rs.getString("cod_os");
                String codOperacao = rs.getString("cod_operacao");

                Item item = new Item(
                        rs.getInt("id_item"),
                        rs.getString("cod_item"),
                        rs.getInt("id_operacao"),
                        codOperacao,
                        rs.getString("descricao"),
                        rs.getInt("qtd_solicitada"),
                        rs.getInt("qtd_retirada"),
                        rs.getInt("matricula_solicitador"),
                        codOs
                );

                item.selecionadoProperty().addListener((obs, oldVal, newVal) ->
                        System.out.println("Item " + item.getIdItem() + " selecionado mudou: " + oldVal + " -> " + newVal)
                );

                listaItens.add(item);

                boolean existe = listaOperacoes.stream()
                        .anyMatch(op -> Objects.equals(op.getCodOperacao(), codOperacao));
                if (!existe) {
                    listaOperacoes.add(new Operacao(item.getIdOperacao(), codOperacao, "Em andamento", codOs));
                }

                boolean osExiste = listaOs.stream()
                        .anyMatch(os -> Objects.equals(os.getCodOs(), codOs));
                if (!osExiste) {
                    listaOs.add(new OrdemServico(codOs));
                }
            }

            pdfTableOs.setItems(listaOs);
            pdfTableOperacao.setItems(listaOperacoes);

            // garante atualização visual e preserva larguras do FXML
            Platform.runLater(() -> pdfTableItem.refresh());

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao carregar dados").showAndWait();
        }
    }

    // --- Classe Item ---
    public static class Item {
        private javafx.beans.property.SimpleIntegerProperty idItem;
        private javafx.beans.property.SimpleStringProperty codItem;
        private javafx.beans.property.SimpleIntegerProperty idOperacao;
        private javafx.beans.property.SimpleIntegerProperty qtdSolicitada;
        private javafx.beans.property.SimpleIntegerProperty qtdRetirada;
        private javafx.beans.property.SimpleIntegerProperty matriculaSolicitador;
        private javafx.beans.property.SimpleStringProperty descricao;
        private javafx.beans.property.SimpleStringProperty codOperacao;
        private javafx.beans.property.SimpleStringProperty codOs;
        private javafx.beans.property.BooleanProperty selecionado = new javafx.beans.property.SimpleBooleanProperty(false);

        public Item(int idItem, String codItem, int idOperacao, String codOperacao,
                    String descricao, int qtdSolicitada, int qtdRetirada, int matriculaSolicitador,
                    String codOs) {
            this.idItem = new javafx.beans.property.SimpleIntegerProperty(idItem);
            this.codItem = new javafx.beans.property.SimpleStringProperty(codItem);
            this.idOperacao = new javafx.beans.property.SimpleIntegerProperty(idOperacao);
            this.codOperacao = new javafx.beans.property.SimpleStringProperty(codOperacao);
            this.descricao = new javafx.beans.property.SimpleStringProperty(descricao);
            this.qtdSolicitada = new javafx.beans.property.SimpleIntegerProperty(qtdSolicitada);
            this.qtdRetirada = new javafx.beans.property.SimpleIntegerProperty(qtdRetirada);
            this.matriculaSolicitador = new javafx.beans.property.SimpleIntegerProperty(matriculaSolicitador);
            this.codOs = new javafx.beans.property.SimpleStringProperty(codOs);
            this.selecionado = new javafx.beans.property.SimpleBooleanProperty(false);
        }

        public int getIdItem() { return idItem.get(); }
        public String getCodItem() { return codItem.get(); }
        public int getIdOperacao() { return idOperacao.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public String getDescricao() { return descricao.get(); }
        public int getQtdSolicitada() { return qtdSolicitada.get(); }
        public int getQtdRetirada() { return qtdRetirada.get(); }
        public int getMatriculaSolicitador() { return matriculaSolicitador.get(); }
        public String getCodOs() { return codOs.get(); }

        public javafx.beans.property.BooleanProperty selecionadoProperty() { return selecionado; }
        public boolean isSelecionado() { return selecionado.get(); }
        public void setSelecionado(boolean s) { selecionado.set(s); }
    }

    // --- Classe Operacao ---
    public static class Operacao {
        private javafx.beans.property.SimpleIntegerProperty idOperacao;
        private javafx.beans.property.SimpleStringProperty codOperacao;
        private javafx.beans.property.SimpleStringProperty status;
        private javafx.beans.property.SimpleStringProperty codOs;

        public Operacao(int id, String codOperacao, String status, String codOs) {
            this.idOperacao = new javafx.beans.property.SimpleIntegerProperty(id);
            this.codOperacao = new javafx.beans.property.SimpleStringProperty(codOperacao);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.codOs = new javafx.beans.property.SimpleStringProperty(codOs);
        }

        public int getIdOperacao() { return idOperacao.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public SimpleStringProperty getStatus() { return status; }
        public String getCodOs() { return codOs.get(); }
    }

    // --- Classe OrdemServico ---
    public static class OrdemServico {
        private javafx.beans.property.SimpleStringProperty codOs;

        public OrdemServico(String codOs) {
            this.codOs = new javafx.beans.property.SimpleStringProperty(codOs);
        }

        public String getCodOs() { return codOs.get(); }
    }
}