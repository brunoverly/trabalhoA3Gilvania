package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.PdfRetiradaItens;
import com.example.trabalhoA3Gilvania.Utils.Sessao;
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
import javafx.scene.control.Label;
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
 * Controlador JavaFX para a tela "GerarPdf.fxml".
 * Esta tela carrega Ordens de Serviço, Operações e Itens que necessitam de retirada.
 * Permite ao usuário selecionar itens específicos (via checkbox) e gerar um
 * PDF de comprovante de retirada, registrando a emissão no banco de dados.
 */
public class GerarPdfController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button consultVoltarButton;
    @FXML private Button pdfGerarPdfButton;
    @FXML private ImageView voltarImage;
    @FXML Label pdfLabel;

    @FXML private TableView<Item> pdfTableItem;
    @FXML private TableView<Operacao> pdfTableOperacao;
    @FXML private TableView<OrdemServico> pdfTableOs;

    @FXML private TableColumn<OrdemServico, String> pdfColumnOs;
    @FXML private TableColumn<Operacao, String> pdfColumnOperacao;

    // Coluna de checkbox declarada no FXML
    @FXML private TableColumn<Item, Boolean> pdfColumnCheck;

    @FXML private TableColumn<Item, String> pdfColumnCodItem;
    @FXML private TableColumn<Item, String> pdfColumnDescricao;
    @FXML private TableColumn<Item, Integer> pdfColumnSolicitado;
    @FXML private TableColumn<Item, Integer> pdfColumnRetirado;
    @FXML private TableColumn<Item, Integer> pdfColumnMatricula;

    @FXML private AnchorPane paneItens;

    // --- Listas de Dados para as Tabelas ---
    private final ObservableList<OrdemServico> listaOs = FXCollections.observableArrayList();
    private final ObservableList<Operacao> listaOperacoes = FXCollections.observableArrayList();
    private final ObservableList<Item> listaItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados; // Lista filtrada para a tabela de itens

    // Instância da classe utilitária para exibir pop-ups de alerta
    private final FormsUtil alerta = new FormsUtil();

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     * Configura tabelas, colunas, listeners e carrega os dados iniciais.
     */
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

        // Define alinhamento centralizado
        pdfColumnOs.setStyle("-fx-alignment: CENTER;");
        pdfColumnOperacao.setStyle("-fx-alignment: CENTER;");
        pdfColumnSolicitado.setStyle("-fx-alignment: CENTER;");
        pdfColumnRetirado.setStyle("-fx-alignment: CENTER;");
        pdfColumnMatricula.setStyle("-fx-alignment: CENTER;");


        // --- Inicializa tabelas ---
        pdfTableOs.setItems(listaOs);
        pdfTableOperacao.setItems(listaOperacoes);
        itensFiltrados = new FilteredList<>(listaItens, item -> false); // Começa vazia
        pdfTableItem.setItems(itensFiltrados);


        // --- Configura coluna de seleção (CheckBox) ---
        if (pdfColumnCheck != null) {
            // Vincula a coluna à propriedade 'selecionadoProperty' do Item
            pdfColumnCheck.setCellValueFactory(cell -> cell.getValue().selecionadoProperty());
            pdfColumnCheck.setCellFactory(CheckBoxTableCell.forTableColumn(pdfColumnCheck));
            pdfColumnCheck.setEditable(true);
            pdfColumnCheck.setStyle("-fx-alignment: CENTER;");
        }

        // --- Permitir seleção múltipla nas tabelas ---
        pdfTableOperacao.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pdfTableItem.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pdfTableOs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // --- Tornar a tabela editável (necessário para o CheckBoxTableCell) ---
        pdfTableItem.setEditable(true);

        // As larguras e política de resize são mantidas conforme definido no FXML.

        // --- Inicialmente: tabelas de operações e itens ficam escondidas ---
        setOperacoesVisible(false);
        setItensVisible(false);

        // --- Listener de seleção de OS ---
        // Filtra operações que pertencem à OS e mostra a tabela de operações
        pdfTableOs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String codOsSelecionada = newSelection.getCodOs();

                // Cria um filtro para as operações dessa OS
                FilteredList<Operacao> operacoesFiltradas = new FilteredList<>(listaOperacoes, op ->
                        op.getCodOs() != null && op.getCodOs().equals(codOsSelecionada));
                pdfTableOperacao.setItems(operacoesFiltradas);

                // Mostra operações e limpa seleção anterior
                setOperacoesVisible(true);
                pdfTableOperacao.getSelectionModel().clearSelection();

                // Limpa itens exibidos e esconde tabela de itens
                itensFiltrados.setPredicate(item -> false);
                setItensVisible(false);
            } else {
                // Se nenhuma OS for selecionada, limpa e esconde tudo
                pdfTableOperacao.setItems(FXCollections.observableArrayList());
                itensFiltrados.setPredicate(item -> false);
                setOperacoesVisible(false);
                setItensVisible(false);
            }
        });

        // --- Listener de seleção de Operações ---
        // Quando 1+ operações são selecionadas, exibe os itens dessas operações
        pdfTableOperacao.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Operacao>) change -> {
            ObservableList<Operacao> opsSelecionadas = pdfTableOperacao.getSelectionModel().getSelectedItems();
            if (opsSelecionadas == null || opsSelecionadas.isEmpty()) {
                // Se nada selecionado, esconde itens
                itensFiltrados.setPredicate(item -> false);
                setItensVisible(false);
            } else {
                // Pega o código de todas as operações selecionadas
                Set<String> codOps = opsSelecionadas.stream()
                        .map(Operacao::getCodOperacao)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                // Filtra os itens que pertencem a qualquer uma das operações selecionadas
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null && codOps.contains(item.getCodOperacao()));
                setItensVisible(true); // Mostra a tabela de itens
                Platform.runLater(() -> pdfTableItem.refresh());
            }
        });

        // --- Efeitos de Hover (mouse) no botão Voltar ---
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

        // Seleciona o primeiro item das tabelas
        Platform.runLater(() -> {
            // Se existir pelo menos uma OS, seleciona a primeira
            if (!listaOs.isEmpty()) {
                pdfTableOs.getSelectionModel().select(0);
                OrdemServico primeiraOs = listaOs.get(0);

                // Filtra as operações da primeira OS
                FilteredList<Operacao> operacoesFiltradas = new FilteredList<>(listaOperacoes, op ->
                        op.getCodOs() != null && op.getCodOs().equals(primeiraOs.getCodOs()));
                pdfTableOperacao.setItems(operacoesFiltradas);

                // Se existir pelo menos uma operação, seleciona a primeira
                if (!operacoesFiltradas.isEmpty()) {
                    pdfTableOperacao.getSelectionModel().select(0);
                    Operacao primeiraOperacao = operacoesFiltradas.get(0);

                    // Atualiza o filtro dos itens para mostrar apenas os da primeira operação
                    itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                            item.getCodOperacao().equals(primeiraOperacao.getCodOperacao()));
                    setItensVisible(true);
                }
            }
        });

        // Foca o Label principal (para remover o foco de outros componentes)
        Platform.runLater(() -> {
            if (pdfLabel != null) {
                pdfLabel.requestFocus();
            }
        });

        // Define o Stage principal na classe utilitária
        Platform.runLater(() -> {
            Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    }

    /**
     * Controla a visibilidade da tabela de operações.
     * @param visible true para mostrar, false para esconder.
     */
    private void setOperacoesVisible(boolean visible) {
        pdfTableOperacao.setVisible(visible);
        pdfTableOperacao.setManaged(visible);
    }

    /**
     * Controla a visibilidade da tabela de itens e seu painel container.
     * @param visible true para mostrar, false para esconder.
     */
    private void setItensVisible(boolean visible) {
        pdfTableItem.setVisible(visible);
        pdfTableItem.setManaged(visible);
        if (paneItens != null) {
            paneItens.setVisible(visible);
            paneItens.setManaged(visible);
        }
    }

    /**
     * Ação do botão "Voltar" (X).
     * Fecha a janela (Stage) atual.
     */
    @FXML
    private void consultVoltarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Ação do botão "Gerar PDF".
     * Coleta itens selecionados, valida, registra no banco e gera o arquivo PDF.
     */
    @FXML
    private void pdfGerarPdfButtonOnAction(ActionEvent event) {
        // 1) Coletar todos os itens DO MODELO que têm checkbox marcado
        List<Item> itensMarcados = listaItens.stream()
                .filter(Item::isSelecionado)
                .collect(Collectors.toList());

        // Se nenhum checkbox foi marcado, tenta usar a seleção nativa da tabela
        if (itensMarcados.isEmpty()) {
            List<Item> selecionadosNativos = new ArrayList<>(pdfTableItem.getSelectionModel().getSelectedItems());
            if (!selecionadosNativos.isEmpty()) {
                itensMarcados = selecionadosNativos;
            } else {
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Nenhum item",
                        "Nenhum item marcado (checkbox) ou selecionado para gerar PDF").showAndWait();
                return;
            }
        }

        // 2) Validar que todas as seleções têm a mesma matrícula de solicitador
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

        // 3) Mapear os itens do controlador para o formato esperado pelo gerador de PDF
        List<PdfRetiradaItens.Item> itensParaPdf = itensMarcados.stream()
                .map(i -> new PdfRetiradaItens.Item(
                        i.getCodOs(),
                        i.getCodOperacao(),
                        i.getCodItem(),
                        i.getDescricao(),
                        String.valueOf(i.getQtdSolicitada()),
                        String.valueOf(i.getQtdRetirada()),
                        String.valueOf(i.getMatriculaSolicitador())
                ))
                .collect(Collectors.toList());

        // 4) Preparar caminho/nome do arquivo na pasta "Retiradas" no Desktop
        String userDesktop = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Retiradas";
        File pastaRetiradas = new File(userDesktop);
        if (!pastaRetiradas.exists()) pastaRetiradas.mkdirs(); // Cria a pasta se não existir

        LocalDate hoje = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataPorExtenso = hoje.format(formatter);

        // Cria uma Task para gerar o PDF em background (evita travar a UI)
        Task<Void> gerarTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int numeroRegistroPdf = 0;

                // --- 5) Cadastrar emissão no banco e pegar novo ID ---
                try (Connection conn = new DataBaseConection().getConection()) {
                    String sql = "{CALL projeto_java_a3.registrar_pdf(?, ?, ?)}"; // 2 IN + 1 OUT
                    try (CallableStatement cs = conn.prepareCall(sql)) {
                        cs.setInt(1, Sessao.getMatricula()); // emitido_por (usuário logado)
                        cs.setInt(2, matricula);             // emitido_para (solicitador)
                        cs.registerOutParameter(3, java.sql.Types.INTEGER); // p_id_pdf (OUT)

                        cs.execute();
                        numeroRegistroPdf = cs.getInt(3); // Recupera o ID gerado
                    }
                }

                // --- 6) Preparar arquivo PDF ---
                String nomeArquivo = "Retirada_" + matriculaTexto + "_" + numeroRegistroPdf + ".pdf";
                String caminhoPdf = pastaRetiradas.getAbsolutePath() + File.separator + nomeArquivo;

                // --- 7) Gerar PDF ---
                PdfRetiradaItens.gerarPdf(
                        caminhoPdf,
                        String.valueOf(numeroRegistroPdf), // ID do registro
                        dataPorExtenso,                    // Data
                        Sessao.getNome(),                  // Emitente (Almoxarife)
                        "Almoxarife",                      // Cargo Emitente
                        itensParaPdf,                      // Lista de Itens
                        Sessao.getNome(),                  // Recebedor (TODO: Ajustar se for outra pessoa)
                        "Matr: " + matriculaTexto          // Matrícula Recebedor
                );

                // --- 8) Abrir PDF gerado automaticamente ---
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(caminhoPdf));
                }

                return null;
            }
        };

        // O que fazer quando a Task terminar com sucesso
        gerarTask.setOnSucceeded(evt -> Platform.runLater(() -> {
            clearSelectionsAfterGenerate(); // Limpa a tela
        }));

        // O que fazer se a Task falhar
        gerarTask.setOnFailed(evt -> {
            Throwable ex = gerarTask.getException();
            ex.printStackTrace();
            Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao gerar PDF: " + (ex != null ? ex.getMessage() : "erro desconhecido")).showAndWait());
        });

        // Inicia a Task em uma nova Thread
        Thread t = new Thread(gerarTask, "GerarPdfThread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Limpa as seleções (checkboxes e seleção de tabela) após o PDF ser gerado
     * e esconde a tabela de itens.
     */
    private void clearSelectionsAfterGenerate() {
        // 1. Limpar checkboxes no modelo de dados
        listaItens.forEach(item -> {
            if (item.isSelecionado()) item.setSelecionado(false);
        });

        // 2. Limpar seleções visuais nas tabelas
        pdfTableOperacao.getSelectionModel().clearSelection();
        pdfTableItem.getSelectionModel().clearSelection();

        // 3. Forçar atualização visual dos checkboxes
        Platform.runLater(() -> pdfTableItem.refresh());

        // 4. Esconder tabela de itens até nova seleção de operação
        itensFiltrados.setPredicate(item -> false);
        setItensVisible(false);
    }

    /**
     * Carrega todos os dados necessários (OS, Operações e Itens pendentes)
     * do banco de dados usando a procedure 'buscar_itens_para_pdf'.
     */
    private void carregarDados() {
        String sql = "{CALL projeto_java_a3.buscar_itens_para_pdf()}";

        try (Connection conn = new DataBaseConection().getConection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            // Limpa listas antes de carregar
            listaItens.clear();
            listaOperacoes.clear();
            listaOs.clear();

            // Processa o ResultSet
            while (rs.next()) {
                String codOs = rs.getString("cod_os");
                String codOperacao = rs.getString("cod_operacao");

                // Cria o objeto Item
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

                // (Listener de debug, pode ser removido)
                item.selecionadoProperty().addListener((obs, oldVal, newVal) ->
                        System.out.println("Item " + item.getIdItem() + " selecionado mudou: " + oldVal + " -> " + newVal)
                );

                listaItens.add(item);

                // Adiciona a Operação (apenas se ainda não existir na lista)
                boolean existe = listaOperacoes.stream()
                        .anyMatch(op -> Objects.equals(op.getCodOperacao(), codOperacao));
                if (!existe) {
                    listaOperacoes.add(new Operacao(item.getIdOperacao(), codOperacao, "Em andamento", codOs));
                }

                // Adiciona a OS (apenas se ainda não existir na lista)
                boolean osExiste = listaOs.stream()
                        .anyMatch(os -> Objects.equals(os.getCodOs(), codOs));
                if (!osExiste) {
                    listaOs.add(new OrdemServico(codOs));
                }
            }

            // Define as listas nas tabelas (embora já definidas, isso reforça)
            pdfTableOs.setItems(listaOs);
            pdfTableOperacao.setItems(listaOperacoes);

            // Garante atualização visual
            Platform.runLater(() -> pdfTableItem.refresh());

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao carregar dados").showAndWait();
        }
    }

    /**
     * Classe de Modelo (POJO) estática para 'Item'.
     * Representa um item na tabela de itens para seleção do PDF.
     */
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
        // Propriedade para o CheckBox
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

        // --- Getters ---
        public int getIdItem() { return idItem.get(); }
        public String getCodItem() { return codItem.get(); }
        public int getIdOperacao() { return idOperacao.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public String getDescricao() { return descricao.get(); }
        public int getQtdSolicitada() { return qtdSolicitada.get(); }
        public int getQtdRetirada() { return qtdRetirada.get(); }
        public int getMatriculaSolicitador() { return matriculaSolicitador.get(); }
        public String getCodOs() { return codOs.get(); }

        // --- Métodos para o CheckBox ---
        public javafx.beans.property.BooleanProperty selecionadoProperty() { return selecionado; }
        public boolean isSelecionado() { return selecionado.get(); }
        public void setSelecionado(boolean s) { selecionado.set(s); }
    }

    /**
     * Classe de Modelo (POJO) estática para 'Operacao'.
     * Representa uma operação na tabela de operações.
     */
    public static class Operacao {
        private javafx.beans.property.SimpleIntegerProperty idOperacao;
        private javafx.beans.property.SimpleStringProperty codOperacao;
        private javafx.beans.property.SimpleStringProperty status;
        private javafx.beans.property.SimpleStringProperty codOs; // Referência à OS pai

        public Operacao(int id, String codOperacao, String status, String codOs) {
            this.idOperacao = new javafx.beans.property.SimpleIntegerProperty(id);
            this.codOperacao = new javafx.beans.property.SimpleStringProperty(codOperacao);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.codOs = new javafx.beans.property.SimpleStringProperty(codOs);
        }

        // --- Getters ---
        public int getIdOperacao() { return idOperacao.get(); }
        public String getCodOperacao() { return codOperacao.get(); }
        public SimpleStringProperty getStatus() { return status; }
        public String getCodOs() { return codOs.get(); }
    }

    /**
     * Classe de Modelo (POJO) estática para 'OrdemServico'.
     * Representa uma OS na tabela de Ordens de Serviço.
     */
    public static class OrdemServico {
        private javafx.beans.property.SimpleStringProperty codOs;

        public OrdemServico(String codOs) {
            this.codOs = new javafx.beans.property.SimpleStringProperty(codOs);
        }

        // --- Getter ---
        public String getCodOs() { return codOs.get(); }
    }
}
