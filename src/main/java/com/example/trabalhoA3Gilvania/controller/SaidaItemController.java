package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Utils.Sessao;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

// Importações padrão do Java
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import com.example.trabalhoA3Gilvania.Utils.PdfRetiradaItens;
import com.example.trabalhoA3Gilvania.Utils.PdfRetiradaItens.Item;
import java.util.ArrayList;
import java.util.List;


/**
 * Controlador JavaFX para a tela "saidaItem.fxml".
 * Esta tela funciona como um pop-up para registrar a "saída" (retirada)
 * de um item do estoque para ser entregue a um mecânico na oficina.
 */
public class SaidaItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button retirarCancelButton;
    @FXML private TextField retiraraCodOs;
    @FXML private TextField retirarCodOperacao;
    @FXML private TextField retirarCodItem;
    @FXML private TextField retiradaQtdRetirada;
    @FXML private TextField retirarDescricaoItem;
    @FXML private TextField retirarQtdItemOs; // Quantidade do pedido original
    @FXML private TextField retirarQtdItemRecebida; // Quantidade já recebida no estoque
    @FXML private TextField retirarStatusItem;
    @FXML private TextField retirarLocalItem;
    @FXML private TextField retiradaQtdJaRetirada;
    @FXML private TextField solicitarQtdSolicitadaAnteriormente;
    @FXML private TextField retirarMatriculaMecanico; // Campo para o usuário preencher
    @FXML private ImageView retiradaVoltarButtonImage;

    // --- Campos Privados ---
    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private String qtdPedido;
    private String localizacao;
    private String status;
    private int qtdRecebida;
    private int idOperacao;
    private int qtdSolcitada;
    private int matriculaSolicitador;

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

    FormsUtil alerta = new FormsUtil();
    private OnFecharJanela listener;

    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    public void setCodItem(String codItem) { this.codItem = codItem; }
    public void setCodOperacao(String codOperacao) { this.codOperacao = codOperacao; }
    public void setCodOs(String codOs) { this.codOs = codOs; }
    public void setDescricaoItem(String descricaoItem) { this.descricaoItem = descricaoItem; }
    public void setQtdPedido(int qtdPedido) { this.qtdPedido = String.valueOf(qtdPedido); }
    public void setIdItem(int idItem) { this.idItem = idItem; }
    public void setLocalizacao(String localizacao){ this.localizacao = localizacao; }
    public void setStatus(String status){ this.status = status; }
    public void setQtdRecebida(int qtdRecebida){ this.qtdRecebida = qtdRecebida; }
    public void setIdOperacao(int idOperacao){ this.idOperacao = idOperacao; }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL retiradaVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image retiradaVoltarButtonImageImagem = new Image(retiradaVoltarButtonImageURL.toExternalForm());
        retiradaVoltarButtonImage.setImage(retiradaVoltarButtonImageImagem);

        Platform.runLater(() -> {
            Stage stage = (Stage) retiraraCodOs.getScene().getWindow();
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela();
                }
            });
        });

        ImageView fecharImagem = (ImageView) retirarCancelButton.getGraphic();
        retirarCancelButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            retirarCancelButton.setCursor(Cursor.HAND);
        });
        retirarCancelButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            retirarCancelButton.setCursor(Cursor.DEFAULT);
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });


    }

    public void carregaDados(){
        retiraraCodOs.setText(codOs);
        retirarCodOperacao.setText(codOperacao);
        retirarCodItem.setText(codItem);
        retirarDescricaoItem.setText(descricaoItem);
        retirarQtdItemOs.setText(qtdPedido);
        retirarQtdItemRecebida.setText(String.valueOf(qtdRecebida));
        retirarStatusItem.setText(status);
        retirarLocalItem.setText(localizacao);
    }

    public void retirarCancelButtonOnAction(ActionEvent event){
        Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
        stage.close();
    }

    public boolean validarQtdRetirada(){
        try{
            int converNumero = Integer.parseInt(retiradaQtdRetirada.getText().trim());
        }
        catch (Exception e){
            return false;
        }
        if(Integer.parseInt(retiradaQtdRetirada.getText().trim()) <= 0){
            return false;
        }
        else if(Integer.parseInt(retiradaQtdRetirada.getText().trim()) > qtdRecebida){
            return false;
        }
        return true;
    }

    public void buscarQtdRetida(){
        int qtdJaRetirada = 0;
        String sql = "{CALL projeto_java_a3.somar_retiradas_item(?)}";

        try (Connection conn = new DataBaseConection().getConection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, idItem);
            boolean temResultado = cs.execute();

            // Primeiro result set: total_retirado
            if (temResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        qtdJaRetirada = rs.getInt("total_retirado");
                        retiradaQtdJaRetirada.setText(String.valueOf(qtdJaRetirada));
                    }
                }
            }

            // Próximo result set: qtd_solicitada e solicitado_por
            if (cs.getMoreResults()) {
                try (ResultSet rs2 = cs.getResultSet()) {
                    if (rs2.next()) {
                        qtdSolcitada = rs2.getInt("qtd_solicitada");
                        matriculaSolicitador = rs2.getInt("solicitador_por");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao carregar quantidade já retirada").showAndWait();
        }
    }


    public void retirarConfirmarButtonOnAction() {
        if (retirarMatriculaMecanico.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Informe a matrícula a quem foi entregue").showAndWait();
            return;
        }

        if (!validarQtdRetirada()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Quantidade de retirada inválida ou maior que a quantidade disponível").showAndWait();
            return;
        }

        try {
            Integer.parseInt(retirarMatriculaMecanico.getText().trim());
        } catch (NumberFormatException e) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Informe uma matrícula válida").showAndWait();
            return;
        }

        try (Connection conn = new DataBaseConection().getConection()) {
            int qtdRetirada = Integer.parseInt(retiradaQtdRetirada.getText().trim());
            int qtdJaRetirada = retiradaQtdJaRetirada.getText().isBlank() ? 0 : Integer.parseInt(retiradaQtdJaRetirada.getText().trim());
            int qtdPedidoInt = Integer.parseInt(qtdPedido);

            String statusItem;
            String statusOperacao;
            int totalRetirado = qtdJaRetirada + qtdRetirada;

            if (totalRetirado >= qtdPedidoInt) {
                statusItem = statusItem7; // Entregue (integral)
                statusOperacao = statusOperacao4; // Itens entregues (Integral)
            } else {
                statusItem = statusItem6; // Entregue (parcial)
                statusOperacao = statusOperacao3; // Itens entregues (Parcial)
            }

            String descricaoLog = statusOperacao;

            String sql = "{CALL projeto_java_a3.atualizar_item_saida(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            int numeroRegistroPdf = 0; // ID do registro_pdf
            int idRetirada = 0;        // ID da retirada

            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, idItem);
                cs.setString(2, codOperacao);
                cs.setString(3, "Item");
                cs.setString(4, codOs);
                cs.setInt(5, Integer.parseInt(retirarMatriculaMecanico.getText())); // emitido_para
                cs.setInt(6, Sessao.getMatricula()); // emitido_por
                cs.setString(7, descricaoLog);
                cs.setInt(8, Sessao.getMatricula());
                cs.setString(9, codItem);
                cs.setInt(10, qtdRetirada);
                cs.setString(11, statusItem);

                boolean hasResult = cs.execute();
                if (hasResult) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (rs.next()) {
                            idRetirada = rs.getInt("id_retirada");
                            numeroRegistroPdf = rs.getInt("id_pdf"); // pega o novo ID da tabela registro_pdf
                        }
                    }
                }
            }

            // Alerta de sucesso na retirada
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Retirada cadastrada com sucesso").showAndWait();

            // Montar a lista de itens para o PDF
            List<Item> listaItensPdf = new ArrayList<>();
            Item itemPdf = new Item(
                    codOs,
                    codOperacao,
                    codItem,
                    descricaoItem,
                    String.valueOf(qtdSolcitada),
                    String.valueOf(qtdRetirada),
                    String.valueOf(matriculaSolicitador)
            );
            listaItensPdf.add(itemPdf);

            // Preparar pasta Desktop/Retiradas
            String userDesktop = System.getProperty("user.home") + "/Desktop/Retiradas";
            File pastaRetiradas = new File(userDesktop);
            if (!pastaRetiradas.exists()) {
                pastaRetiradas.mkdirs();
            }

            // Nome do arquivo
            String nomeArquivo = "Retirada_" + retirarMatriculaMecanico.getText() + "_" + numeroRegistroPdf + ".pdf";
            String caminhoPdf = pastaRetiradas.getAbsolutePath() + "/" + nomeArquivo;

            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
            String dataPorExtenso = hoje.format(formatter);

            // Gerar PDF
            PdfRetiradaItens.gerarPdf(
                    caminhoPdf,
                    String.valueOf(numeroRegistroPdf),
                    dataPorExtenso,
                    Sessao.getNome(),
                    "Almoxarife",
                    listaItensPdf,
                    Sessao.getNome(),
                    "Matr: " + retirarMatriculaMecanico.getText().trim()
            );

            // Abrir PDF em nova thread para não travar a interface
            new Thread(() -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(caminhoPdf));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Fechar janela automaticamente
            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
