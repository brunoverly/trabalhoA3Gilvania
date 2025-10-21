package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt; // Importa a biblioteca para criptografia de senhas
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controlador JavaFX para a tela de cadastro de novos usuários.
 * Implementa a interface Initializable para executar ações na inicialização.
 */
public class CadastrarUsuarioController implements Initializable {

    // --- Injeção de Componentes FXML ---
    // Estes campos são vinculados aos componentes definidos no arquivo .fxml
    @FXML private ImageView cadastrarVoltarButtonImage;
    @FXML private Button cadastrarCancelButton;
    @FXML private TextField cadastroNome;
    @FXML private TextField cadastroMatricula;
    @FXML private TextField cadastroSenha;
    @FXML private TextField cadastroConfirmarSenha;
    @FXML private ComboBox<String> cadastroComboBox;

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    /**
     * Método executado automaticamente quando a tela é carregada.
     * Usado para configurar o estado inicial dos componentes.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Carrega a imagem "close.png" para o botão de voltar/fechar
        URL cadastrarVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image cadastrarVoltarButtonImageImagem = new Image(cadastrarVoltarButtonImageURL.toExternalForm());
        cadastrarVoltarButtonImage.setImage(cadastrarVoltarButtonImageImagem);

        // Adiciona as opções de cargo (roles) ao ComboBox
        cadastroComboBox.getItems().addAll("Administrador", "Aprovisionador", "Mecânico");

        // Obtém o ícone de dentro do botão "Cancelar"
        ImageView fecharImagem = (ImageView) cadastrarCancelButton.getGraphic();

        // --- Efeitos de Hover (passar o mouse) no Botão Cancelar ---

        // Efeito ao entrar com o mouse
        cadastrarCancelButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2); // Aumenta a escala X do ícone
            fecharImagem.setScaleY(1.2); // Aumenta a escala Y do ícone
            cadastrarCancelButton.setCursor(Cursor.HAND); // Muda o cursor para "mãozinha"
        });

        // Efeito ao sair com o mouse
        cadastrarCancelButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0); // Retorna a escala X ao normal
            fecharImagem.setScaleY(1.0); // Retorna a escala Y ao normal
            cadastrarCancelButton.setCursor(Cursor.DEFAULT); // Retorna o cursor ao padrão
        });
    }

    /**
     * Ação executada ao clicar no botão "Cancelar" (cadastrarCancelButton).
     * Fecha a janela (Stage) atual.
     */
    public void cadastrarCancelButtonOnAction(ActionEvent event) {
        // Obtém a referência da janela (Stage) a partir do botão
        Stage stage = (Stage) cadastrarCancelButton.getScene().getWindow();
        // Fecha a janela
        stage.close();
    }

    /**
     * Ação executada ao clicar no botão "Cadastrar" (cadastrarButton).
     * Realiza a validação dos campos antes de chamar o registro.
     */
    public void cadastrarButtonOnAction(ActionEvent event) {
        // 1. Valida se todos os campos estão preenchidos
        if (cadastroNome.getText().isBlank()
                || cadastroMatricula.getText().isBlank()
                || cadastroComboBox.getValue() == null
                || cadastroSenha.getText().isBlank()
                || cadastroConfirmarSenha.getText().isBlank()) {

            // Exibe um alerta de aviso se algum campo estiver vazio
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Preencha todos os campos para prosseguir")
                    .showAndWait();
            return; // Interrompe a execução do método
        }

        // 2. Valida o formato do PIN (senha)
        if (!verificarPIN()) {
            // Exibe alerta se o PIN não for numérico ou não tiver 6 dígitos
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","PIN deve ser númerico e possuir 6 dígitos")
                    .showAndWait();
            return; // Interrompe a execução do método
        }

        // 3. Valida se os campos de PIN e Confirmação de PIN são iguais
        if (!cadastroSenha.getText().equals(cadastroConfirmarSenha.getText())) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","PIN informados não correspondem")
                    .showAndWait();
            return; // Interrompe a execução do método
        }

        // 4. Se todas as validações passarem, chama o método para registrar o usuário
        registerUser();
    }

    /**
     * Método principal de registro.
     * Conecta-se ao banco de dados e chama uma Stored Procedure para cadastrar o usuário.
     */
    public void registerUser() {
        // Coleta os dados dos campos da interface
        String nome = cadastroNome.getText();
        int matricula = Integer.parseInt(cadastroMatricula.getText()); // Converte matrícula para inteiro
        String cargo = cadastroComboBox.getValue();
        int pin = Integer.parseInt(cadastroSenha.getText()); // Converte PIN para inteiro

        // Criptografa o PIN usando BCrypt (gera um "hash")
        String hash = BCrypt.hashpw(String.valueOf(pin), BCrypt.gensalt(12));

        // String de chamada da Stored Procedure do MySQL
        String procedureCall = "{ CALL projeto_java_a3.cadastrar_usuario(?, ?, ?, ?) }";

        // Usa try-with-resources para garantir que a conexão (connectDB) e
        // o CallableStatement (cs) sejam fechados automaticamente
        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall(procedureCall)) {

            // Define os parâmetros de entrada (IN) da procedure
            cs.setInt(1, matricula);
            cs.setString(2, nome);
            cs.setString(3, cargo);
            cs.setString(4, hash); // Salva o PIN criptografado

            // Executa a Stored Procedure
            boolean hasResult = cs.execute();

            // Verifica se a procedure retornou um resultado (um ResultSet)
            if (hasResult) {
                try (ResultSet rs = cs.getResultSet()) {
                    // Move para a primeira linha do resultado
                    if (rs.next()) {
                        // Obtém o valor da coluna "resultado" (definida na procedure)
                        int resultado = rs.getInt("resultado");

                        if (resultado == 1) {
                            // Caso 1: Sucesso
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso","Usuário cadastrado com sucesso")
                                    .showAndWait();
                        } else {
                            // Caso 0 (ou outro): Usuário já existe
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","Já existe um usuário cadastrado com a matrícula informada")
                                    .showAndWait();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            // Captura qualquer erro de SQL (conexão, sintaxe da procedure, etc.)
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro","Falha ao cadastrar usuário")
                    .showAndWait();
        }

        // Limpa os campos após a tentativa de cadastro (seja sucesso ou falha)
        limparCampos();
    }

    /**
     * Método auxiliar para limpar todos os campos de entrada da tela.
     */
    private void limparCampos() {
        cadastroNome.clear();
        cadastroMatricula.clear();
        cadastroSenha.clear();
        cadastroConfirmarSenha.clear();
        cadastroComboBox.getSelectionModel().clearSelection(); // Limpa a seleção do ComboBox
    }

    /**
     * Verifica se o PIN (senha) é válido.
     * Condições: Deve ser numérico E deve ter exatamente 6 dígitos.
     *
     * @return true se o PIN for válido, false caso contrário.
     */
    public boolean verificarPIN() {
        boolean tipoValido = true;
        boolean tamanhoValido = true;

        // Tenta converter o texto do PIN para um Inteiro
        try {
            Integer.parseInt(cadastroSenha.getText());
        } catch (Exception e) {
            // Se der erro (ex: contém letras), o tipo é inválido
            tipoValido = false;
        }

        // Verifica se o comprimento (length) do texto é diferente de 6
        if (cadastroSenha.getText().length() != 6) {
            tamanhoValido = false;
        }

        // Retorna true somente se ambas as condições (tipo E tamanho) forem verdadeiras
        return tipoValido && tamanhoValido;
    }
}