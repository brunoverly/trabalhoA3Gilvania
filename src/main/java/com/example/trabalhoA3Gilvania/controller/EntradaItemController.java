package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

// Importações padrão do Java
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "entradaItem.fxml".
 * Esta tela funciona como um pop-up para registrar o recebimento (entrada)
 * de um item específico no estoque.
 */
public class EntradaItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button entradaItemCancelar;
    @FXML private TextField entradaOrdemServico;
    @FXML private TextField entradadCodItem;
    @FXML private TextField entrdadaQtdPedido;
    @FXML private TextField entradaLocalArmazenado; // Campo para o usuário preencher
    @FXML private TextField entradaCodOperacao;
    @FXML private TextField entradaItemDescricao;
    @FXML private TextField entradaQtdRecebida;    // Campo para o usuário preencher
    @FXML private ImageView entradaItemVoltarButtonImage;

    // --- Campos Privados ---
    // Estas variáveis armazenam os dados que são "injetados"
    // pelo controller que abriu esta janela (ex: ConsultarItemController).
    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private String qtdPedido;
    private int idOperacao;

    // Interface usada como "callback" para notificar a tela anterior quando esta fechar.
    private OnFecharJanela listener;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

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
        this.qtdPedido = String.valueOf(qtdPedido); // Converte int para String para o TextField
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
        entrdadaQtdPedido.setText(qtdPedido);
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
        // 1. Validação: Verifica se os campos de entrada estão em branco
        if ((entradaLocalArmazenado.getText().isBlank()) || (entradaQtdRecebida.getText().isBlank())) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe a quantidade recebida e local armazenado")
                    .showAndWait();

            // 2. Validação: Verifica se a quantidade é um número válido e se não excede o pedido
        } else if (!verificarValorDigitado()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Valor informado é inválida ou maior que a quantidade informada na ordem de serviço")
                    .showAndWait();
        } else {
            // 3. Se passou nas validações, executa a lógica de banco de dados
            int qtdRecebida = Integer.parseInt(entradaQtdRecebida.getText());
            String localizacao = entradaLocalArmazenado.getText();

            // String de chamada da Stored Procedure
            String procedureCall = "{ CALL projeto_java_a3.atualizar_item_entrada(?, ?, ?, ?, ?, ?, ?, ?) }";

            // Try-with-resources para garantir o fechamento da conexão e statement
            try (Connection connectDB = new DataBaseConection().getConection();
                 CallableStatement cs = connectDB.prepareCall(procedureCall)) {

                // Define os 8 parâmetros de entrada (IN) da procedure
                cs.setInt(1, idItem);                // p_id (ID do item a ser atualizado)
                cs.setString(2, "Recebido");         // p_status (Novo status do item)
                cs.setString(3, localizacao);        // p_localizacao (Onde foi guardado)
                cs.setInt(4, qtdRecebida);           // p_qtd_recebida (Quanto foi recebido)
                cs.setString(5, "Item");             // p_tipo (Para o log)
                cs.setString(6, codOs);              // p_cod_os (Para o log)
                cs.setString(7, "Item recebido na base"); // p_descricao (Para o log)
                cs.setInt(8, Sessao.getMatricula()); // p_matricula (Quem fez a operação)

                // Executa a procedure
                cs.execute();

                // Mostra mensagem de sucesso
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Item atualizado com sucesso")
                        .showAndWait();

                // Fecha a janela de "Entrada de Item"
                Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
                stage.close(); // Isso também acionará o callback 'setOnHidden'

            } catch (SQLException e) {
                // Trata erros de banco de dados
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar item")
                        .showAndWait();
            }
        }
    }

    /**
     * Método auxiliar para validar o campo "Quantidade Recebida".
     * @return true se o valor for válido, false caso contrário.
     */
    public boolean verificarValorDigitado(){
        // Verificação 1: Tenta converter a quantidade para um número inteiro.
        try{
            Integer.parseInt(entradaQtdRecebida.getText().trim());
        }
        catch(Exception e){
            // Se falhar (ex: "abc"), o valor é inválido.
            return false;
        }

        // Verificação 2: Compara a quantidade pedida com a quantidade recebida.
        // A quantidade recebida não pode ser maior que a pedida.
        if(Integer.parseInt(entrdadaQtdPedido.getText()) < Integer.parseInt(entradaQtdRecebida.getText().trim())){
            return false;
        }

        // Se passou nas duas verificações, o valor é válido
        return true;
    }

}