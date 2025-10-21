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
 * Controlador JavaFX para a tela "saidaItem.fxml".
 * Esta tela funciona como um pop-up para registrar a "saída" (retirada)
 * de um item do estoque para ser entregue a um mecânico na oficina.
 */
public class SaidaItemController implements Initializable {

    // --- Injeção de Componentes FXML ---
    // Estes campos são vinculados aos componentes definidos no arquivo .fxml
    @FXML private Button retirarCancelButton;
    @FXML private TextField retiraraCodOs;
    @FXML private TextField retirarCodOperacao;
    @FXML private TextField retirarCodItem;
    @FXML private TextField retirarDescricaoItem;
    @FXML private TextField retirarQtdItemOs; // Quantidade do pedido original
    @FXML private TextField retirarQtdItemRecebida; // Quantidade já recebida no estoque
    @FXML private TextField retirarStatusItem;
    @FXML private TextField retirarLocalItem;
    @FXML private TextField retirarMatriculaMecanico; // Campo para o usuário preencher
    @FXML private ImageView retiradaVoltarButtonImage;

    // --- Campos Privados ---
    // Estas variáveis armazenam os dados que são "injetados"
    // pelo controller que abriu esta janela (ex: ConsultarItemController).
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


    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();
    // Interface usada como "callback" para notificar a tela anterior quando esta fechar.
    private OnFecharJanela listener;

    /**
     * Define o "ouvinte" (listener/callback) que será acionado quando esta janela for fechada.
     * @param listener A implementação da interface (geralmente vinda da tela anterior).
     */
    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    // --- Setters para Injeção de Dados ---
    // Estes métodos são chamados pelo controller anterior para passar os dados
    // do item que será retirado.
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
    public void setLocalizacao(String localizacao){this.localizacao = localizacao;}
    public void setStatus(String status){this.status = status;}
    public void setQtdRecebida(int qtdRecebida){this.qtdRecebida = qtdRecebida;}
    public void setIdOperacao(int idOperacao){this.idOperacao = idOperacao;}

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega a imagem "close.png" para o botão de voltar/cancelar
        URL retiradaVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image retiradaVoltarButtonImageImagem = new Image(retiradaVoltarButtonImageURL.toExternalForm());
        retiradaVoltarButtonImage.setImage(retiradaVoltarButtonImageImagem);

        // --- Configuração do Callback de Fechamento ---
        // Usa Platform.runLater para garantir que a cena (scene) e a janela (stage)
        // já existam antes de tentar acessá-las.
        Platform.runLater(() -> {
            Stage stage = (Stage) retiraraCodOs.getScene().getWindow();

            // Adiciona um listener para QUANDO a janela for FECHADA
            // (seja pelo "X" do sistema ou pelo stage.close())
            stage.setOnHidden(event -> {
                if (listener != null) {
                    // 🔔 Chama o método da interface (o "callback")
                    listener.aoFecharJanela(); // Isso avisa a tela anterior para se atualizar
                }
            });
        });

        // --- Efeitos de Hover (mouse) no Botão Cancelar ---
        ImageView fecharImagem = (ImageView) retirarCancelButton.getGraphic();

        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        retirarCancelButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            retirarCancelButton.setCursor(Cursor.HAND);
        });

        // Ao sair com o mouse: retorna ao normal
        retirarCancelButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            retirarCancelButton.setCursor(Cursor.DEFAULT);
        });
    } // Fim do initialize()

    /**
     * Pega os dados armazenados nas variáveis privadas (definidas pelos setters)
     * e os exibe nos campos de texto (TextFields) da interface.
     * Este método é chamado pelo controller anterior logo após "injetar" os dados.
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
        stage.close(); // Ao fechar, o 'stage.setOnHidden' (do initialize) será acionado
    }

    /**
     * Ação do botão "Confirmar".
     * Valida a matrícula do mecânico e chama a procedure de atualização no banco.
     */
    public void retirarConfirmarButtonOnAction(){
        // 1. Validação: Verifica se a matrícula do mecânico está em branco
        if((retirarMatriculaMecanico.getText().isBlank())){
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso","Informe a matrícula a quem foi entregue")
                    .showAndWait();
            return; // Interrompe a execução
        }

        // 2. Validação: Verifica se a matrícula é um número válido
        try{
            // Apenas tenta converter, não usa o valor
            int converNumero = Integer.parseInt(retirarMatriculaMecanico.getText().trim());
        }
        catch (Exception e){
            // Se falhar (ex: "abc"), mostra alerta e interrompe
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Informe a matrícula uma matrícula válida")
                    .showAndWait();
            return;
        }

        // 3. Lógica de Banco de Dados
        // Try-with-resources para garantir o fechamento da conexão (conn) e statement (stmt)
        try (Connection conn = new DataBaseConection().getConection()) {
            // String de chamada da Stored Procedure
            String sql = "CALL projeto_java_a3.atualizar_item_saida(?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Define os 8 parâmetros de entrada (IN) da procedure
                stmt.setInt(1, idItem);        // p_id_item
                stmt.setString(2, codOperacao);  // p_cod_operacao
                stmt.setString(3, "Item");       // p_tipo (Para o log)
                stmt.setString(4, codOs);        // p_cod_os (Para o log)
                stmt.setInt(5, Integer.parseInt(retirarMatriculaMecanico.getText())); // p_entregue_a (Matrícula do Mecânico)
                stmt.setInt(6, Sessao.getMatricula()); // p_entregue_por (Matrícula do Aprovisionador/Admin)
                stmt.setString(7, "Item entregue na oficina"); // p_descricao (Para o log)
                stmt.setInt(8, Sessao.getMatricula()); // p_matricula (Quem executou a ação)

                stmt.execute(); // Executa a procedure

                // Mostra alerta de sucesso
                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Registro atualizado com sucesso").showAndWait();
            }
        } catch (SQLException e) {
            // Trata erros de SQL (conexão, procedure, etc.)
            throw new RuntimeException(e);
        }

        // 4. Fechamento da Janela
        Stage stage = (Stage) retirarCancelButton.getScene().getWindow();

        // 🔔 (Esta parte foi removida no seu código original, mas mantida no initialize)
        // (O callback 'setOnHidden' do initialize() será responsável por
        //  notificar a tela anterior quando a janela fechar)
        // if (listener != null) {
        //     listener.aoFecharJanela();
        // }

        // Fecha a janela
        stage.close();
    } // Fim do retirarConfirmarButtonOnAction()
} // Fim da classe