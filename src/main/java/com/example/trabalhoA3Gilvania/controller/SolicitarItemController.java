package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
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
 * Controlador JavaFX para a tela "entradaItem.fxml".
 * Esta tela funciona como um pop-up para registrar o recebimento (entrada)
 * de um item específico no estoque.
 */
public class SolicitarItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button entradaItemCancelar;
    @FXML private TextField entradaOrdemServico;
    @FXML private TextField entradaQtdRecebidaParcial;
    @FXML private TextField entradadCodItem;
    @FXML private TextField entrdadaQtdPedido;
    @FXML private TextField solicitarItemQtd; // Campo para o usuário preencher
    @FXML private TextField entradaCodOperacao;
    @FXML private TextField entradaItemDescricao;
    @FXML private TextField entradaQtdRecebida;
    @FXML private TextField solicitarQtdSolicitadaAnteriormente;// Campo para o usuário preencher
    @FXML private ImageView entradaItemVoltarButtonImage;

    // --- Campos Privados ---
    // Estas variáveis armazenam os dados que são "injetados"
    // pelo controller que abriu esta janela (ex: ConsultarItemController).
    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private int qtdPedido;
    private int idOperacao;
    private int qtdRecebida;
    private int qtdDisponivel;
    private int qtdJaSolicitada;

    // Interface usada como "callback" para notificar a tela anterior quando esta fechar.
    private OnFecharJanela listener;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

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
    // do item que será recebido.
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
        this.qtdPedido = qtdPedido; // Converte int para String para o TextField
    }
    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }
    public void setIdOperacao(int idOperacao){
        this.idOperacao = idOperacao;
    }


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
        entradaQtdRecebidaParcial.setText(String.valueOf((qtdRecebida)));
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
        // Usa Platform.runLater para garantir que a cena (scene) e a janela (stage)
        // já existam antes de tentar acessá-las.
        Platform.runLater(() -> {
            Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();

            // Adiciona um listener para QUANDO a janela for FECHADA
            // (seja pelo "X" do sistema ou pelo stage.close())
            stage.setOnHidden(event -> {
                if (listener != null) {
                    // 🔔 Chama o método da interface (o "callback")
                    listener.aoFecharJanela(); // Isso avisa a tela anterior para se atualizar
                }
            });
        });

        // --- Efeitos de Hover (mouse) no botão Cancelar ---
        ImageView fecharImagem = (ImageView) entradaItemCancelar.getGraphic();

        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        entradaItemCancelar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            entradaItemCancelar.setCursor(Cursor.HAND);
        });

        // Ao sair com o mouse: retorna ao normal
        entradaItemCancelar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            entradaItemCancelar.setCursor(Cursor.DEFAULT);
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
     * Ação do botão "Confirmar".
     * Valida os dados e, se corretos, chama a procedure de atualização no banco.
     */
    public void entradaItemConfirmarOnAction() {
        int qtdSolicitada;
        try {
            qtdSolicitada = Integer.parseInt(solicitarItemQtd.getText().trim());
        } catch (NumberFormatException e) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                    "Valor informado é inválido").showAndWait();
            return;
        }

            // 2️⃣ Verifica se a quantidade solicitada é válida
            if (qtdSolicitada <= 0 || qtdSolicitada > qtdDisponivel) {
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso",
                                "Quantidade inválida ou maior que a disponível para retirada: (" + qtdDisponivel + ")")
                        .showAndWait();
                return;
            }

            // 3️⃣ Define status padronizado do item
            String statusItem = (qtdSolicitada < qtdPedido) ? statusItem4: statusItem5;

            // 4️⃣ Comentário para a tabela de atualizações
            String comentarioAtualizacao = (qtdSolicitada < qtdPedido)
                    ? "Item solicitado (Parcial - QTD: " + qtdSolicitada + ")"
                    : "Item solicitado (Integral)";

            String statusOperacao = statusOperacao2;

            // 5️⃣ Chama a procedure 'solicitar_item' para registrar a solicitação
            String sqlSolicitar = "CALL projeto_java_a3.solicitar_item(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connectDB = new DataBaseConection().getConection()){
                 CallableStatement csSolicitar = connectDB.prepareCall(sqlSolicitar);

                csSolicitar.setInt(1, idItem);                     // p_id
                csSolicitar.setInt(2, idOperacao);                 // p_id_operacao
                csSolicitar.setString(3, codOs);                   // p_cod_os
                csSolicitar.setInt(4, Sessao.getMatricula());      // p_solicitado_por
                csSolicitar.setInt(5, Sessao.getMatricula());      // p_matricula (para log)
                csSolicitar.setString(6, statusItem);             // p_status_item
                csSolicitar.setString(7, statusOperacao);         // p_status_operacao
                csSolicitar.setString(8, codItem);                // p_cod_item
                csSolicitar.setInt(9, qtdSolicitada);             // p_qtd_solicitada
                csSolicitar.setString(10, "Operação");            // p_tipo (comentário)
                csSolicitar.setString(11, comentarioAtualizacao); // p_descricao da atualizacao
                csSolicitar.execute();

            } catch (SQLException e) {
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado").showAndWait();
            }

        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Item atualizado com sucesso").showAndWait();

            // Fecha a janela
            Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
            stage.close();
    }

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
                        qtdJaSolicitada = rs.getInt("qtd_solicitada"); // ✅ pega a quantidade já solicitada
                    }
                }
            }

            solicitarQtdSolicitadaAnteriormente.setText(String.valueOf(qtdJaSolicitada));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Método auxiliar para validar o campo "Quantidade Recebida".
     * @return true se o valor for válido, false caso contrário.
     */
    public boolean verificarValorDigitado(){
        // Verificação 1: Tenta converter a quantidade para um número inteiro.
        try{
            Integer.parseInt(solicitarItemQtd.getText().trim());
        }
        catch(Exception e){
            // Se falhar (ex: "abc"), o valor é inválido.
            return false;
        }

        // Verificação 2: Compara a quantidade pedida com a quantidade recebida.
        // A quantidade recebida não pode ser maior que a pedida.
        if(Integer.parseInt(entradaQtdRecebidaParcial.getText()) < Integer.parseInt(solicitarItemQtd.getText().trim())){
            return false;
        }

        // Se passou nas duas verificações, o valor é válido
        return true;
    }

    public void setLocalizacao(String localizacao) {
    }

    public void setStatus(String status) {
    }
}