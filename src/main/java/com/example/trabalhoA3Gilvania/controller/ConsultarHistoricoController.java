package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto

import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "ConsultarOs.fxml".
 * Esta tela permite ao usuário buscar uma Ordem de Serviço (OS) pelo número
 * e visualizar suas operações e os itens detalhados de cada operação.
 */
public class ConsultarHistoricoController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button historicoConsultar;
    @FXML private Button historicoCloseButton;
    @FXML private DatePicker historicoDataInicio;
    @FXML private DatePicker historicoDataFim;
    @FXML private Label historicoTitulo;
    @FXML private AnchorPane historicoAnchoTable;
    @FXML private TableView <Historico> historicoTableView;
    @FXML private TableColumn<Historico, String> historicoTableColumnData;
    @FXML private TableColumn <Historico, String> historicoTableColumnOs;
    @FXML private TableColumn <Historico, String> historicoTableColumnOperacao;
    @FXML private TableColumn <Historico, String> historicoTableColumnCodItem;
    @FXML private TableColumn <Historico, String> historicoTableColumnDescricaoItem;
    @FXML private TableColumn <Historico, Integer> historicoTableColumnQtdItem;
    @FXML private TableColumn <Historico, Integer> historicoTableColumnMatricula;
    @FXML private ImageView historicoCloseImage;

    // Variáveis de status (parecem não utilizadas neste controller, mas mantidas)
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


    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();
    private String modo; // Armazena o modo ("Retiradas" ou "Solicitações")
    // Lista observável que preenche a TableView
    private ObservableList<Historico> listabuscada = FXCollections.observableArrayList();

    /**
     * Define o modo de operação da tela (ex: "Retiradas" ou "Solicitações").
     * Isso é usado para alterar o título e a consulta ao banco.
     * @param modo O modo de operação.
     */
    public void setModo(String modo) {
        this.modo = modo;
    }


    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Carrega a imagem "X" do botão de fechar
        URL historicoCloseImageURL = getClass().getResource("/imagens/close.png");
        Image historicoCloseImageImage = new Image(historicoCloseImageURL.toExternalForm());
        historicoCloseImage.setImage(historicoCloseImageImage);

        // --- Efeitos de Hover (mouse) no botão de Fechar ---
        ImageView fecharImagem = (ImageView) historicoCloseButton.getGraphic();

        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        historicoCloseButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            historicoCloseButton.setCursor(Cursor.HAND);
        });

        // Ao sair com o mouse: retorna ao normal
        historicoCloseButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            historicoCloseButton.setCursor(Cursor.DEFAULT);
        });

        // Configura as colunas da tabela

        // Formatação especial para a coluna de Data/Hora (LocalDateTime)
        historicoTableColumnData.setCellValueFactory(cellData -> {
            LocalDateTime data = cellData.getValue().getData();
            String formatada = (data != null)
                    ? data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
            return new SimpleStringProperty(formatada);
        });

        // Configuração padrão para as outras colunas usando PropertyValueFactory
        historicoTableColumnOs.setCellValueFactory(new PropertyValueFactory<>("ordem"));
        historicoTableColumnOperacao.setCellValueFactory(new PropertyValueFactory<>("operacao"));
        historicoTableColumnCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        historicoTableColumnQtdItem.setCellValueFactory(new PropertyValueFactory<>("qtdItem"));
        historicoTableColumnDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricaoItem"));
        historicoTableColumnMatricula.setCellValueFactory(new PropertyValueFactory<>("entregue_por"));

        // Centraliza o texto em colunas específicas
        historicoTableColumnOs.setStyle("-fx-alignment: CENTER;");
        historicoTableColumnOperacao.setStyle("-fx-alignment: CENTER;");
        historicoTableColumnQtdItem.setStyle("-fx-alignment: CENTER;");
        historicoTableColumnMatricula.setStyle("-fx-alignment: CENTER;");


        // Vincula a lista observável à TableView
        historicoTableView.setItems(listabuscada);

        // Define o Stage principal na classe utilitária
        Platform.runLater(() -> {
            Stage stage = (Stage) historicoCloseButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });

        // Trava os DatePickers para evitar digitação manual e forçar o uso do popup
        historicoDataInicio.setEditable(false);
        historicoDataFim.setEditable(false);
        historicoDataInicio.getEditor().setDisable(true);
        historicoDataFim.getEditor().setDisable(true);
    }

    /**
     * Ação do botão "Consultar".
     * Valida as datas e chama o método de busca apropriado (Solicitações ou Retiradas).
     */
    @FXML
    public void historicoConsultarOnAction(ActionEvent event) {
        // Valida se as datas foram preenchidas
        if(historicoDataInicio.getValue() == null || historicoDataFim.getValue() == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Por favor, preencha ambas as datas.").showAndWait();
            return;
        }
        else{
            // Chama a busca no DB de acordo com o modo definido
            if(modo.equals("Retiradas"))
                BuscarDB_retiradas();
            else if(modo.equals("Solicitações"))
                BuscarDB_solicitacoes();
        }
    }

    /**
     * Ação do botão "X" (Fechar).
     * Fecha a janela atual.
     */
    @FXML
    public void historicoCloseButtonOnAction (ActionEvent event) {
        // Obtém a referência da janela (Stage) a partir do botão
        Stage stage = (Stage) historicoCloseButton.getScene().getWindow();
        // Fecha a janela
        stage.close();
    }

    /**
     * Busca no banco de dados o histórico de SOLICITAÇÕES
     * com base no intervalo de datas e preenche a TableView.
     */
    public void BuscarDB_solicitacoes() {
        listabuscada.clear(); // Limpa a lista antes de uma nova busca

        // Try-with-resources para garantir o fechamento da conexão e do statement
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_historico_solicitacao(?,?) }")) {

            // Define os parâmetros da Stored Procedure (data inicial e data final)
            cs.setTimestamp(1, Timestamp.valueOf(historicoDataInicio.getValue().atStartOfDay())); // Início do dia
            cs.setTimestamp(2, Timestamp.valueOf(historicoDataFim.getValue().atTime(23,59,59))); // Fim do dia

            ResultSet rsBusca = cs.executeQuery();

            // Itera sobre os resultados da consulta
            while (rsBusca.next()) {

                // Converte o Timestamp do DB para LocalDateTime
                Timestamp ts = rsBusca.getTimestamp("datahora_solicitacao");
                LocalDateTime data = ts.toLocalDateTime();

                // Cria um objeto Historico com os dados
                Historico busca = new Historico(
                        data,
                        rsBusca.getString("cod_os"),
                        rsBusca.getString("cod_operacao"),
                        rsBusca.getString("cod_item"),
                        rsBusca.getInt("qtd_solicitada"),
                        rsBusca.getString("descricao"),
                        rsBusca.getInt("solicitador_por")
                );
                listabuscada.add(busca); // Adiciona o objeto à lista da tabela
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao buscar histórico no banco de dados.").showAndWait();
        }
    }

    /**
     * Busca no banco de dados o histórico de RETIRADAS
     * com base no intervalo de datas e preenche a TableView.
     */
    public void BuscarDB_retiradas() {
        listabuscada.clear(); // Limpa a lista antes de uma nova busca

        // Try-with-resources
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_historico_retirada(?,?) }")) {

            cs.setTimestamp(1, Timestamp.valueOf(historicoDataInicio.getValue().atStartOfDay()));
            cs.setTimestamp(2, Timestamp.valueOf(historicoDataFim.getValue().atTime(23,59,59)));
            ResultSet rsBusca = cs.executeQuery();

            while (rsBusca.next()) {

                // Converte Timestamp para LocalDateTime
                Timestamp ts = rsBusca.getTimestamp("data_retirada");
                LocalDateTime data = ts.toLocalDateTime();

                Historico busca = new Historico(
                        data,
                        rsBusca.getString("cod_os"),
                        rsBusca.getString("cod_operacao"),
                        rsBusca.getString("cod_item"),
                        rsBusca.getInt("qtd_retirada"),
                        rsBusca.getString("descricao"),
                        rsBusca.getInt("entregue_para")
                );
                listabuscada.add(busca); // Adiciona na lista
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao buscar histórico no banco de dados.").showAndWait();
        }
    }

    /**
     * Atualiza o Label (título) da janela com base no modo ("Solicitações" ou "Retiradas").
     * Este método deve ser chamado pelo controller que abre esta janela.
     */
    public void AtualizarTituloPorModo() {
        if (modo != null) {
            switch (modo) {
                case "Solicitações":
                    historicoTitulo.setText("Consultar histórico de solicitações");
                    break;
                case "Retiradas":
                    historicoTitulo.setText("Consultar histórico de retiradas");
                    break;
            }
        }
    }

    /**
     * Classe interna (inner class) que representa o modelo de dados
     * para cada linha da TableView (historicoTableView).
     * Utiliza Properties do JavaFX para permitir o data binding.
     */
    public static class Historico {

        private final ObjectProperty<LocalDateTime> data;
        private final SimpleStringProperty ordem;
        private final SimpleStringProperty operacao;
        private final SimpleStringProperty codItem;
        private final SimpleIntegerProperty qtdItem;
        private final SimpleStringProperty descricaoItem;
        private final SimpleIntegerProperty entregue_por;

        public Historico(LocalDateTime data, String ordem, String operacao, String codItem, int qtdItem, String descricaoItem, int entregue_por) {
            this.data = new SimpleObjectProperty(data);
            this.ordem = new SimpleStringProperty(ordem);
            this.operacao = new SimpleStringProperty(operacao);
            this.codItem = new SimpleStringProperty(codItem);
            this.qtdItem = new SimpleIntegerProperty(qtdItem);
            this.descricaoItem = new SimpleStringProperty(descricaoItem);
            this.entregue_por = new SimpleIntegerProperty(entregue_por);
        }

        // --- Getters ---
        public LocalDateTime getData() {
            return data.get();
        }

        public String getOrdem() {
            return ordem.get();
        }

        public String getOperacao() {
            return operacao.get();
        }

        public String getCodItem() {
            return codItem.get();
        }

        public int getQtdItem() {
            return qtdItem.get();
        }

        public String getDescricaoItem() {
            return descricaoItem.get();
        }

        public int getEntregue_por() {
            return entregue_por.get();
        }

        // --- Setters ---
        public void setData(LocalDateTime data) {
            this.data.set(data);
        }

        public void setOrdem(String value) {
            ordem.set(value);
        }

        public void setOperacao(String value) {
            operacao.set(value);
        }

        public void setCodItem(String value) {
            codItem.set(value);
        }

        public void setQtdItem(int value) {
            qtdItem.set(value);
        }

        public void setDescricaoItem(String value) {
            descricaoItem.set(value);
        }

        public void setEntregue_por(int value) {
            entregue_por.set(value);
        }

        // --- Property methods (usados pelo TableView) ---
        public ObjectProperty<LocalDateTime> dataProperty() {
            return data;
        }

        public SimpleStringProperty ordemProperty() {
            return ordem;
        }

        public SimpleStringProperty operacaoProperty() {
            return operacao;
        }

        public SimpleStringProperty codItemProperty() {
            return codItem;
        }

        public SimpleIntegerProperty qtdItemProperty() {
            return qtdItem;
        }

        public SimpleStringProperty descricaoItemProperty() {
            return descricaoItem;
        }

        public SimpleIntegerProperty entregue_porProperty() {
            return entregue_por;
        }
    }// Fim da classe interna Historico
}
