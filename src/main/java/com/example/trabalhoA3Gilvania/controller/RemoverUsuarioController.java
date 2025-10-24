package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Importações padrão do Java
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela "removerUsuario.fxml".
 * Esta tela permite que um administrador busque um usuário por matrícula
 * e, em seguida, confirme sua remoção do banco de dados.
 */
public class RemoverUsuarioController implements Initializable {

    // --- Injeção de Componentes FXML ---
    // Estes campos são vinculados aos componentes definidos no arquivo .fxml
    @FXML private ImageView removerUserVoltarButtonImage;
    @FXML private ImageView remover2; // Imagem estática do usuário
    @FXML private Button removeCancelarButton;
    @FXML private TextField removeMatricula; // Campo para buscar a matrícula
    @FXML private TextField removeDadosNome; // Campo (desabilitado) para exibir o nome
    @FXML private TextField removeDadosCargo; // Campo (desabilitado) para exibir o cargo
    @FXML private TextField removeDadosMatricula; // Campo (desabilitado) para exibir a matrícula

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega a imagem "close.png" para o botão de voltar/cancelar
        URL removerUserVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image removerUserVoltarButtonImageImagem = new Image(removerUserVoltarButtonImageURL.toExternalForm());
        removerUserVoltarButtonImage.setImage(removerUserVoltarButtonImageImagem);

        // Carrega a imagem estática de usuário (avatar)
        URL remover2ImageURL = getClass().getResource("/imagens/user5.png");
        Image remover2Image = new Image(remover2ImageURL.toExternalForm());
        remover2.setImage(remover2Image);

        // Pega o ícone de dentro do botão "Cancelar" para aplicar o efeito
        ImageView fecharImagem = (ImageView) removeCancelarButton.getGraphic();

        // --- Efeitos de Hover (passar o mouse) no Botão Cancelar ---

        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        removeCancelarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            removeCancelarButton.setCursor(Cursor.HAND);
        });

        // Ao sair com o mouse: retorna ao normal
        removeCancelarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            removeCancelarButton.setCursor(Cursor.DEFAULT);
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) removeCancelarButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });
    }

    /**
     * Ação do botão "Cancelar" ou "Voltar" (X).
     * Fecha a janela (Stage) atual.
     */
    public void removeCancelarButtonOnAction(ActionEvent event) {
        // Obtém a referência da janela (Stage) a partir do botão
        Stage stage = (Stage) removeCancelarButton.getScene().getWindow();
        // Fecha a janela
        stage.close();
    }

    /**
     * Ação do botão "Buscar" (lupa).
     * Busca os dados do usuário no banco com base na matrícula digitada.
     */
    public void removeBuscarMatriculaOnAction(ActionEvent event) {
        // 1. Valida se a matrícula é um número válido
        if (!matriculaValida()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe uma matrícula válida para prosseguir")
                    .showAndWait();
            return; // Interrompe a execução
        }

        // 2. Prepara a chamada da Stored Procedure
        int matricula = Integer.parseInt(removeMatricula.getText());
        String procedureCall = "{ CALL projeto_java_a3.remover_usuario_dados(?) }";

        // 3. Conecta ao banco e executa a procedure
        // Try-with-resources garante que a conexão (connectDB) e o statement (cs) sejam fechados
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall(procedureCall)) {

            cs.setInt(1, matricula); // Define o parâmetro de entrada (IN)

            boolean hasResults = cs.execute(); // Executa a procedure

            // 4. Processa o resultado
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) { // Move para a primeira linha de resultado
                        // Pega o código de resultado (1 = Encontrou, 0 = Não encontrou)
                        int resultado = rs.getInt("resultado");

                        if (resultado == 1) {
                            // Se encontrou, popula os campos de texto com os dados do usuário
                            removeDadosNome.setText(rs.getString("nome"));
                            removeDadosCargo.setText(rs.getString("cargo"));
                            removeDadosMatricula.setText(String.valueOf(matricula));
                        } else {
                            // Se não encontrou (resultado == 0), limpa os campos
                            removeDadosNome.setText("");
                            removeDadosCargo.setText("");
                            removeDadosMatricula.setText("");

                            // Exibe alerta de usuário não localizado
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Matrícula informada não localizada")
                                    .showAndWait();
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            // Este catch é para o caso de a matrícula ser válida (passar no matriculaValida)
            // mas dar erro na segunda conversão (improvável)
            e.printStackTrace();
        } catch (SQLException e) {
            // Captura erros de conexão ou da procedure
            e.printStackTrace();
        }
    } // Fim do removeBuscarMatriculaOnAction()


    /**
     * Método auxiliar privado para validar o campo da matrícula.
     * @return true se a matrícula não está em branco E é um número.
     */
    private boolean matriculaValida() {
        // Verifica se o campo está vazio
        if (removeMatricula.getText().isBlank()) {
            return false;
        } else {
            // Tenta converter o texto para um Inteiro
            try {
                int matricula = Integer.parseInt(removeMatricula.getText());
                return true; // Sucesso
            } catch (Exception e) {
                return false; // Falha (ex: contém letras)
            }
        }
    }

    /**
     * Ação do botão "Confirmar Remoção".
     * Pede confirmação e, se positivo, chama a procedure para deletar o usuário.
     */
    public void removeConfirmarButtonOnAction (ActionEvent event){
        // 1. Pede confirmação ao usuário antes de prosseguir
        boolean confirmar = alerta.criarAlertaConfirmacao("Confirmar", "Tem certeza que deseja remover este usuário?");

        if (confirmar) {
            // Se o usuário clicou "OK":
            int matricula = Integer.parseInt(removeMatricula.getText());

            // 2. Prepara a chamada da Stored Procedure de remoção
            String procedureCall = "{ CALL projeto_java_a3.remover_usuario(?) }";

            // 3. Conecta ao banco e executa
            try (Connection connectDB = new DataBaseConection().getConection();
                 CallableStatement cs = connectDB.prepareCall(procedureCall)) {

                cs.setInt(1, matricula); // Define o parâmetro (matrícula a ser removida)

                boolean hasResult = cs.execute(); // Executa

                // 4. Processa o resultado
                if (hasResult) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (rs.next()) {
                            int resultado = rs.getInt("resultado");

                            // (Nota: O código original cria o Alerta manualmente aqui)
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Aviso");
                            alert.setHeaderText(null);

                            if (resultado == 1) {
                                // Se resultado = 1 (Sucesso)
                                alert.setContentText("Usuário removido com sucesso");
                                // Limpa todos os campos da tela
                                removeMatricula.setText("");
                                removeDadosNome.setText("");
                                removeDadosCargo.setText("");
                                removeDadosMatricula.setText("");
                            } else {
                                // Se resultado = 0 (Não encontrado, embora devesse estar após a busca)
                                alert.setContentText("Usuário não encontrado");
                            }
                            alert.showAndWait(); // Exibe o alerta de feedback
                        }
                    }
                }

            } catch (SQLException e) {
                // Captura erros de banco (ex: falha na remoção por restrição de chave)
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao remover usuário")
                        .showAndWait();
            }
        }
        // Se 'confirmar' for false, nada acontece.
    } // Fim do removeConfirmarButtonOnAction()
} // Fim da classe