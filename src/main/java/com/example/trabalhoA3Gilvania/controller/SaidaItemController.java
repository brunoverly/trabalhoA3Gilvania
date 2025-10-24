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
import com.example.trabalhoA3Gilvania.Utils.PdfRetiradaItens.Item; // Importa a classe Item de dentro do PdfRetiradaItens
import java.util.ArrayList;
import java.util.List;


/**
 * Controlador JavaFX para a tela "saidaItem.fxml".
 * Esta tela funciona como um pop-up para registrar a "saída" (retirada)
 * de um item do estoque para ser entregue a um mecânico na oficina.
 * Também gera um PDF de comprovação da retirada.
 */
public class SaidaItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button retirarCancelButton;
    @FXML private TextField retiraraCodOs;
    @FXML private TextField retirarCodOperacao;
    @FXML private TextField retirarCodItem;
    @FXML private TextField retiradaQtdRetirada; // Campo de entrada: Quantidade a retirar
    @FXML private TextField retirarDescricaoItem;
    @FXML private TextField retirarQtdItemOs; // Quantidade do pedido original
    @FXML private TextField retirarQtdItemRecebida; // Quantidade já recebida no estoque
    @FXML private TextField retirarStatusItem;
    @FXML private TextField retirarLocalItem;
    @FXML private TextField retiradaQtdJaRetirada; // Quantidade já retirada anteriormente
    @FXML private TextField solicitarQtdSolicitadaAnteriormente; // Quantidade total solicitada
    @FXML private TextField retirarMatriculaMecanico; // Campo de entrada: Matrícula de quem retira
    @FXML private ImageView retiradaVoltarButtonImage;

    // --- Campos Privados (Injetados) ---
    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private String qtdPedido; // É String pois é setado a partir de um int
    private String localizacao;
    private String status;
    private int qtdRecebida;
    private int idOperacao;
    private int qtdSolcitada; // Armazena a qtd solicitada (buscada do DB)
    private int matriculaSolicitador; // Armazena a matrícula de quem solicitou (buscada do DB)

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

    FormsUtil alerta = new FormsUtil();
    private OnFecharJanela listener; // Callback para a tela anterior

    /**
     * Define o "ouvinte" (listener/callback) que será acionado quando esta janela for fechada.
     * @param listener A implementação da interface (geralmente vinda da tela anterior).
     */
    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    // --- Setters para Injeção de Dados ---
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

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega a imagem "close.png" para o botão de voltar/cancelar
        URL retiradaVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image retiradaVoltarButtonImageImagem = new Image(retiradaVoltarButtonImageURL.toExternalForm());
        retiradaVoltarButtonImage.setImage(retiradaVoltarButtonImageImagem);

        // Configura o callback de fechamento da janela
        Platform.runLater(() -> {
            Stage stage = (Stage) retiraraCodOs.getScene().getWindow();
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela(); // Notifica a tela anterior para atualizar
                }
            });
        });

        // --- Efeitos de Hover (mouse) no Botão Cancelar ---
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

        // Define o Stage principal na classe utilitária
        Platform.runLater(() -> {
            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    }

    /**
     * Pega os dados injetados (pelos setters) e os exibe nos TextFields da tela.
     * Chamado pelo controller anterior (ConsultarItemController) após a injeção.
     */
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

    /**
     * Ação do botão "Cancelar".
     * Fecha a janela (Stage) atual.
     */
    public void retirarCancelButtonOnAction(ActionEvent event){
        Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
        stage.close(); // Aciona o listener 'setOnHidden' configurado no initialize
    }

    /**
     * Valida se a quantidade digitada para retirada é um número válido,
     * positivo e não excede a quantidade disponível em estoque.
     * @return true se a quantidade for válida, false caso contrário.
     */
    public boolean validarQtdRetirada(){
        try{
            // ✅ .trim() usado para conversão
            int converNumero = Integer.parseInt(retiradaQtdRetirada.getText().trim());

            // Valida se é maior que zero
            if(converNumero <= 0){
                return false;
            }
            // Valida se é maior que a quantidade recebida (disponível em estoque)
            else if(converNumero > qtdRecebida){
                return false;
            }
        }
        catch (Exception e){
            return false; // Não é um número
        }
        return true; // Passou em todas as validações
    }

    /**
     * Busca no banco de dados a soma de retiradas anteriores para este item
     * e também a quantidade total solicitada e quem solicitou (para o PDF).
     * Popula os campos 'retiradaQtdJaRetirada' e as variáveis 'qtdSolcitada' e 'matriculaSolicitador'.
     */
    public void buscarQtdRetida(){
        int qtdJaRetirada = 0;
        String sql = "{CALL projeto_java_a3.somar_retiradas_item(?)}";

        try (Connection conn = new DataBaseConection().getConection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, idItem);
            boolean temResultado = cs.execute();

            // 1. Processa o primeiro ResultSet (Total Retirado)
            if (temResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        qtdJaRetirada = rs.getInt("total_retirado");
                        retiradaQtdJaRetirada.setText(String.valueOf(qtdJaRetirada));
                    }
                }
            }

            // 2. Processa o segundo ResultSet (Dados da Solicitação)
            if (cs.getMoreResults()) {
                try (ResultSet rs2 = cs.getResultSet()) {
                    if (rs2.next()) {
                        qtdSolcitada = rs2.getInt("qtd_solicitada");
                        matriculaSolicitador = rs2.getInt("solicitador_por");
                        // Popula o campo da quantidade solicitada (apenas visualização)
                        solicitarQtdSolicitadaAnteriormente.setText(String.valueOf(qtdSolcitada));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao carregar quantidade já retirada").showAndWait();
        }
    }


    /**
     * Ação do botão "Confirmar Retirada".
     * Valida os campos, atualiza o item no banco de dados (via procedure),
     * gera um PDF de comprovação e abre o PDF.
     */
    public void retirarConfirmarButtonOnAction() {
        // 1. Validação de campos obrigatórios
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

        // Valida se a matrícula é um número
        try {
            // ✅ .trim() usado para validar
            Integer.parseInt(retirarMatriculaMecanico.getText().trim());
        } catch (NumberFormatException e) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Informe uma matrícula válida").showAndWait();
            return;
        }

        try (Connection conn = new DataBaseConection().getConection()) {
            // 2. Coleta e calcula as quantidades
            // ✅ .trim() usado para garantir a conversão
            int qtdRetirada = Integer.parseInt(retiradaQtdRetirada.getText().trim());
            int qtdJaRetirada = retiradaQtdJaRetirada.getText().isBlank() ? 0 : Integer.parseInt(retiradaQtdJaRetirada.getText().trim());
            int qtdPedidoInt = Integer.parseInt(qtdPedido); // qtdPedido é String, mas vem de um int (seguro)

            // 3. Determina o novo status do item
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

            // 4. Prepara a chamada da Stored Procedure
            String sql = "{CALL projeto_java_a3.atualizar_item_saida(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            int numeroRegistroPdf = 0; // ID do registro_pdf (retornado pelo DB)
            int idRetirada = 0;        // ID da retirada (retornado pelo DB)

            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, idItem);
                cs.setString(2, codOperacao);
                cs.setString(3, "Item"); // p_tipo (para log)
                cs.setString(4, codOs); // p_cod_os (para log)
                // ✅ .trim() ADICIONADO aqui
                cs.setInt(5, Integer.parseInt(retirarMatriculaMecanico.getText().trim())); // emitido_para
                cs.setInt(6, Sessao.getMatricula()); // emitido_por
                cs.setString(7, descricaoLog); // p_descricao (para log)
                cs.setInt(8, Sessao.getMatricula()); // p_matricula
                cs.setString(9, codItem);
                cs.setInt(10, qtdRetirada);
                cs.setString(11, statusItem); // p_status_item

                // 5. Executa e obtém os IDs de retorno
                boolean hasResult = cs.execute();
                if (hasResult) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (rs.next()) {
                            idRetirada = rs.getInt("id_retirada");
                            numeroRegistroPdf = rs.getInt("id_pdf"); // Pega o ID do PDF gerado no DB
                        }
                    }
                }
            }

            // Alerta de sucesso na retirada
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Retirada cadastrada com sucesso").showAndWait();

            // 6. Monta a lista de itens para o PDF
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

            // 7. Prepara o caminho do arquivo (Desktop/Retiradas)
            String userDesktop = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Retiradas";
            File pastaRetiradas = new File(userDesktop);
            if (!pastaRetiradas.exists()) {
                pastaRetiradas.mkdirs();
            }

            // 8. Define o nome do arquivo
            // ✅ .trim() ADICIONADO aqui para evitar espaços no nome do arquivo
            String nomeArquivo = "Retirada_" + retirarMatriculaMecanico.getText().trim() + "_" + numeroRegistroPdf + ".pdf";
            String caminhoPdf = pastaRetiradas.getAbsolutePath() + File.separator + nomeArquivo;

            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
            String dataPorExtenso = hoje.format(formatter);

            // 9. Gera o PDF
            PdfRetiradaItens.gerarPdf(
                    caminhoPdf,
                    String.valueOf(numeroRegistroPdf),
                    dataPorExtenso,
                    Sessao.getNome(),
                    "Almoxarife", // (Assumindo que quem dá saída é Almoxarife)
                    listaItensPdf,
                    Sessao.getNome(), // (Assinatura 1)
                    "Matr: " + retirarMatriculaMecanico.getText().trim() // (Assinatura 2)
            );

            // 10. Abre o PDF em uma nova thread (para não travar a UI)
            new Thread(() -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(caminhoPdf));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 11. Fecha a janela automaticamente
            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro de SQL ao confirmar retirada.").showAndWait();
            throw new RuntimeException(e);
        } catch (IOException e) {
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao gerar ou salvar o PDF.").showAndWait();
            throw new RuntimeException(e);
        }
    }
}
