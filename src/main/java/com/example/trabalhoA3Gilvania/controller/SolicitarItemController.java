package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto

import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Utils.Sessao;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "solicitarItem.fxml".
 * Esta tela funciona como um pop-up para o Mecânico registrar a
 * solicitação de um item que está no estoque.
 */
public class SolicitarItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button entradaItemCancelar;
    @FXML private TextField entradaOrdemServico;
    @FXML private TextField entradaQtdRecebidaParcial; // Campo visual: Qtd Disponível no Estoque
    @FXML private TextField entradadCodItem;
    @FXML private TextField entrdadaQtdPedido; // Campo visual: Qtd Total do Pedido
    @FXML private TextField solicitarItemQtd; // Campo de entrada: Qtd a Solicitar
    @FXML private TextField entradaCodOperacao;
    @FXML private TextField entradaItemDescricao;
    @FXML private TextField entradaQtdRecebida; // (Este campo parece não ser usado nesta tela)
    @FXML private TextField solicitarQtdSolicitadaAnteriormente; // Campo visual: Qtd já Solicitada
    @FXML private ImageView entradaItemVoltarButtonImage;

    // --- Campos Privados (Injetados) ---
    // Estas variáveis armazenam os dados que são "injetados"
    // pelo controller que abriu esta janela (ex: ConsultarItemController).
    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private int qtdPedido;
    private int idOperacao;
    private int qtdRecebida; // Quantidade total recebida no estoque
    private int qtdDisponivel; // Quantidade disponível (recebida - já retirada)
    private int qtdJaSolicitada; // Quantidade já solicitada anteriormente

    // Interface usada como "callback" para notificar a tela anterior quando esta fechar.
    private OnFecharJanela listener;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

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


    /**
     * Define o "ouvinte" (listener/callback) que será acionado quando esta janela for fechada.
     * @param listener A implementação da interface (geralmente vinda da tela anterior).
     */
    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }


    // --- Setters para Injeção de Dados ---
    // Estes métodos são chamados pelo controller anterior para passar os dados
    // do item que será solicitado.
    public void setQtdRecebida(int qtdRecebida) {
        this.qtdRecebida = qtdRecebida;
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
        this.qtdPedido = qtdPedido;
    }
    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }
    public void setIdOperacao(int idOperacao){
        this.idOperacao = idOperacao;
    }

    // Setters vazios (necessários pela interface de abertura, mas não usados nesta tela)
    public void setLocalizacao(String localizacao) { }
    public void setStatus(String status) { }


    /**
     * Pega os dados armazenados nas variáveis privadas (definidas pelos setters)
     * e os exibe nos campos de texto (TextFields) da interface.
     * Este método é chamado pelo controller anterior logo após "injetar" os dados.
     */
    public void carregaDados(){
        entradaOrdemServico.setText(codOs);
        entradaCodOperacao.setText(codOperacao);
        entradadCodItem.setText(codItem);
        entradaItemDescricao.setText(descricaoItem);
        entrdadaQtdPedido.setText(String.valueOf(qtdPedido));
        // O campo 'entradaQtdRecebidaParcial' é usado aqui para mostrar a Qtd *Disponível*
        entradaQtdRecebidaParcial.setText(String.valueOf(qtdRecebida));
    }

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega a imagem "close.png" para o botão de voltar/cancelar
        URL entradaItemVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image entradaItemVoltarButtonImageImage = new Image(entradaItemVoltarButtonImageURL.toExternalForm());
        entradaItemVoltarButtonImage.setImage(entradaItemVoltarButtonImageImage);

        // --- Configuração do Callback de Fechamento ---
        Platform.runLater(() -> {
            Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
            // Adiciona um listener para QUANDO a janela for FECHADA
            stage.setOnHidden(event -> {
                if (listener != null) {
                    // Chama o método da interface (o "callback")
                    listener.aoFecharJanela(); // Avisa a tela anterior para se atualizar
                }
            });
        });

        // --- Efeitos de Hover (mouse) no botão Cancelar ---
        ImageView fecharImagem = (ImageView) entradaItemCancelar.getGraphic();
        entradaItemCancelar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            entradaItemCancelar.setCursor(Cursor.HAND);
        });
        entradaItemCancelar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            entradaItemCancelar.setCursor(Cursor.DEFAULT);
        });

        // Define o Stage principal na classe utilitária
        Platform.runLater(() -> {
            Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    } // Fim do initialize()


    /**
     * Ação do botão "Cancelar".
     * Fecha a janela (Stage) atual.
     */
    public void entradaItemCancelarOnAction(ActionEvent event){
        Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
        stage.close(); // Ao fechar, o 'stage.setOnHidden' (do initialize) será acionado
    }

    /**
     * Ação do botão "Confirmar Solicitação".
     * Valida os dados e, se corretos, chama a procedure de solicitação no banco.
     */
    public void entradaItemConfirmarOnAction() {
        int qtdSolicitada;
        // 1. Valida se a quantidade digitada é um número
        try {
            qtdSolicitada = Integer.parseInt(solicitarItemQtd.getText().trim());
        } catch (NumberFormatException e) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Valor informado é inválido").showAndWait();
            return;
        }

        // 2. Verifica se a quantidade solicitada é válida (maior que 0 e menor/igual ao disponível)
        if (qtdSolicitada <= 0 || qtdSolicitada > qtdDisponivel) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                            "Quantidade inválida ou maior que a disponível para retirada: (" + qtdDisponivel + ")")
                    .showAndWait();
            return;
        }

        // 3. Define o status do item (Parcial ou Integral)
        String statusItem = (qtdSolicitada < qtdPedido) ? statusItem4: statusItem5; // Solicitado (parcial) ou (integral)

        // 4. Define o comentário para o log de atualizações
        String comentarioAtualizacao = (qtdSolicitada < qtdPedido)
                ? "Item solicitado (Parcial - QTD: " + qtdSolicitada + ")"
                : "Item solicitado (Integral)";

        String statusOperacao = statusOperacao2; // "Item(s) solicitados"

        // 5. Chama a procedure 'solicitar_item' para registrar a solicitação
        String sqlSolicitar = "CALL projeto_java_a3.solicitar_item(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement csSolicitar = connectDB.prepareCall(sqlSolicitar)) {

            csSolicitar.setInt(1, idItem);                     // p_id
            csSolicitar.setInt(2, idOperacao);                 // p_id_operacao
            csSolicitar.setString(3, codOs);                   // p_cod_os
            csSolicitar.setInt(4, Sessao.getMatricula());      // p_solicitado_por
            csSolicitar.setInt(5, Sessao.getMatricula());      // p_matricula (para log)
            csSolicitar.setString(6, statusItem);             // p_status_item
            csSolicitar.setString(7, statusOperacao);         // p_status_operacao
            csSolicitar.setString(8, codItem);                // p_cod_item
            csSolicitar.setInt(9, qtdSolicitada);             // p_qtd_solicitada
            csSolicitar.setString(10, "Operação");            // p_tipo (log)
            csSolicitar.setString(11, comentarioAtualizacao); // p_descricao (log)
            csSolicitar.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado ao solicitar item").showAndWait();
            return; // Interrompe em caso de erro
        }

        // 6. Sucesso
        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                "Item solicitado com sucesso").showAndWait();

        // 7. Fecha a janela
        Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Busca no banco de dados a quantidade real disponível para solicitação
     * (o que foi recebido menos o que já foi retirado) e o que já foi solicitado.
     * Chamado pelo controller anterior.
     */
    public void verificarDisponibilidadeItem() {
        try (Connection connectDB = new DataBaseConection().getConection()) {

            qtdDisponivel = 0;
            qtdJaSolicitada = 0;

            String sqlVerificacao = "{CALL projeto_java_a3.verificar_disponibilidade_item(?)}";

            try (CallableStatement csVerificar = connectDB.prepareCall(sqlVerificacao)) {
                csVerificar.setInt(1, idItem);

                try (ResultSet rs = csVerificar.executeQuery()) {
                    if (rs.next()) {
                        qtdDisponivel = rs.getInt("qtd_disponivel");
                        qtdJaSolicitada = rs.getInt("qtd_solicitada"); // Pega a quantidade já solicitada
                    }
                }
            }

            // Atualiza os campos visuais
            solicitarQtdSolicitadaAnteriormente.setText(String.valueOf(qtdJaSolicitada));
            // O campo 'entradaQtdRecebidaParcial' é usado para mostrar a Qtd *Disponível* real
            entradaQtdRecebidaParcial.setText(String.valueOf(qtdDisponivel));


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método auxiliar para validar o campo "Quantidade Recebida" (Quantidade Solicitada).
     * @return true se o valor for válido, false caso contrário.
     */
    public boolean verificarValorDigitado(){
        // Verificação 1: Tenta converter a quantidade a solicitar para um número inteiro.
        try{
            Integer.parseInt(solicitarItemQtd.getText().trim());
        }
        catch(Exception e){
            // Se falhar (ex: "abc"), o valor é inválido.
            return false;
        }

        // Verificação 2: Compara a quantidade disponível com a quantidade solicitada.
        // A quantidade solicitada não pode ser maior que a disponível.
        // ✅ .trim() ADICIONADO aqui
        if(Integer.parseInt(entradaQtdRecebidaParcial.getText().trim()) < Integer.parseInt(solicitarItemQtd.getText().trim())){
            return false;
        }

        // Se passou nas duas verificações, o valor é válido
        return true;
    }
}
