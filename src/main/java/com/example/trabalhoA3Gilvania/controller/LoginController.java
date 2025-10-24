package com.example.trabalhoA3Gilvania.controller;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.Sessao;

// Importações de classes do JavaFX
import javafx.application.Platform;
import javafx.concurrent.Task; // Usado para tarefas em background (não travar a UI)
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane; // Usado para o GIF de loading
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;
import org.mindrot.jbcrypt.BCrypt; // Importa a biblioteca para criptografia (verificar PIN)
import java.sql.*;
import java.util.ResourceBundle;


/**
 * Controlador JavaFX para a tela "login.fxml".
 * Gerencia a autenticação do usuário, validando matrícula e PIN
 * com o banco de dados.
 */
public class LoginController implements Initializable {

    // --- Injeção de Componentes FXML ---
    @FXML private Button loginButton;
    @FXML private ImageView brand;
    @FXML private ImageView login1;
    @FXML private ImageView login2;
    @FXML private ImageView login3;
    @FXML private Stage janelaInicio;
    @FXML private TextField enterUserNameField; // Campo da Matrícula
    @FXML private TextField enterPasswordField; // Campo do PIN (Senha)
    @FXML private Button loginButtonFechar;
    @FXML private ImageView loginImagemFechar;
    @FXML private AnchorPane rootPane; // Painel raiz (usado para o GIF de loading)

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    // Variáveis para permitir arrastar a janela (sem borda)
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Método de inicialização, chamado automaticamente pelo JavaFX
     * após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- Carregamento das Imagens da Interface ---

        // Carrega a imagem de fundo
        URL imagemPrincipalURL = getClass().getResource("/imagens/login1.jpg");
        Image imagemPrincipal = new Image(imagemPrincipalURL.toExternalForm());
        login1.setImage(imagemPrincipal);

        // Carrega o ícone do campo de usuário
        URL imagemUsuarioURL = getClass().getResource("/imagens/login2.png");
        Image imagemUsuario = new Image(imagemUsuarioURL.toExternalForm());
        login2.setImage(imagemUsuario);

        // Carrega o ícone do campo de PIN
        URL imagemPinURL = getClass().getResource("/imagens/login3.png");
        Image imagemPin = new Image(imagemPinURL.toExternalForm());
        login3.setImage(imagemPin);

        // Carrega a logo da marca
        URL imagemBrandURL = getClass().getResource("/imagens/brand.png");
        Image imagemBrand = new Image(imagemBrandURL.toExternalForm());
        brand.setImage(imagemBrand);

        // Carrega o ícone "X" do botão de fechar
        URL loginImagemFecharURL = getClass().getResource("/imagens/close.png");
        Image loginImagemFecharImagem = new Image(loginImagemFecharURL.toExternalForm());
        loginImagemFechar.setImage(loginImagemFecharImagem);

        // --- Efeitos de Hover (mouse) no botão de Fechar App ---
        ImageView fecharImagem = (ImageView) loginButtonFechar.getGraphic();
        // Ao entrar com o mouse: aumenta o ícone e muda o cursor
        loginButtonFechar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.1);
            fecharImagem.setScaleY(1.1);
            loginButtonFechar.setCursor(Cursor.HAND); // cursor muda para mão
        });
        // Ao sair com o mouse: retorna ao normal
        loginButtonFechar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            loginButtonFechar.setCursor(Cursor.DEFAULT);
        });

        // Define a janela principal na classe utilitária (para alertas futuros)
        Platform.runLater(() -> {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FormsUtil.setPrimaryStage(stage);
        });

    } // Fim do initialize()

    /**
     * Ação do botão "X" de fechar a aplicação (canto superior direito).
     */
    public void loginButtonFecharOnAction(ActionEvent event){
        // Obtém a referência da janela (Stage) a partir do botão
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close(); // Fecha a aplicação
    }

    /**
     * Ação do botão principal "Login".
     * Inicia a validação dos campos e a autenticação.
     */
    public void LoginButtonOnAction(ActionEvent event) {
        // 1. Validação: Verifica se os campos NÃO estão em branco
        if (!enterUserNameField.getText().isBlank() && !enterPasswordField.getText().isBlank()) {

            // 2. Cria o painel de "loading" (GIF) e o sobrepõe à tela
            StackPane loadingPane = FormsUtil.createGifLoading();
            loadingPane.prefWidthProperty().bind(rootPane.widthProperty()); // Ocupa a tela toda
            loadingPane.prefHeightProperty().bind(rootPane.heightProperty());
            loadingPane.setStyle("-fx-background-color: rgba(0,0,0,0.15);"); // Fundo semitransparente
            rootPane.getChildren().add(loadingPane); // Adiciona o loading à tela

            // 3. Cria uma Task (tarefa em background) para validar o login
            // Isso impede que a UI (interface) trave durante a consulta ao DB.
            Task<Integer> task = new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    return validateLogin(); // Chama o método de validação (pesado)
                }
            };

            // 4. Define o que fazer quando a Task terminar (na UI Thread)
            task.setOnSucceeded(e -> {
                rootPane.getChildren().remove(loadingPane); // Remove o loading
                int resultado = task.getValue(); // Pega o código de status (0, 1, 2, 3)

                // ⚡ Garante que a atualização da UI (alertas, fechar janela)
                //    ocorra na Thread de Aplicação do JavaFX.
                Platform.runLater(() -> {
                    // Trata os diferentes códigos de resultado
                    switch (resultado) {
                        case 0: // Sucesso
                            TelaInicial(); // Abre a tela principal
                            // Fecha a janela de login
                            Stage stage = (Stage) loginButton.getScene().getWindow();
                            stage.close();
                            break;
                        case 1: // Credenciais erradas
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Usuário ou PIN inválidos").showAndWait();
                            break;
                        case 2: // Erro de formato (não numérico)
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Insira valores numéricos para matrícula e PIN").showAndWait();
                            break;
                        case 3: // Erro de SQL
                            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao conectar ao banco de dados").showAndWait();
                            break;
                    }
                });
            });

            // 5. Define o que fazer se a Task falhar (exceção inesperada)
            task.setOnFailed(e -> {
                rootPane.getChildren().remove(loadingPane); // Remove o loading
                Platform.runLater(() -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro inesperado: " + task.getException().getMessage()).showAndWait());
                task.getException().printStackTrace(); // Loga o erro no console
            });

            // 6. Inicia a Task em uma nova Thread
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Garante que a thread morra se a aplicação fechar
            thread.start();

        } else {
            // Se os campos ESTIVEREM em branco
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe usuário e PIN para prosseguir")
                    .showAndWait();
        }
    } // Fim do LoginButtonOnAction()

    /**
     * Método que se conecta ao banco de dados e valida as credenciais.
     * É executado em background pela Task.
     *
     * @return int - Código de status (0=Sucesso, 1=Inválido, 2=Formato, 3=Erro SQL)
     */
    public int validateLogin() {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connectDB = connectNow.getConection();

        // Validação defensiva: se a conexão falhar, retorna erro
        if (connectDB == null) {
            return 3; // Erro de SQL (conexão)
        }

        // String de chamada da Stored Procedure 'login'
        String querySqlUser = "{ CALL projeto_java_a3.login(?) }"; // A procedure só precisa da matrícula

        // Try-with-resources para garantir o fechamento do CallableStatement (cs)
        try (CallableStatement cs = connectDB.prepareCall(querySqlUser)) {

            int matricula;
            try {
                // Tenta converter a Matrícula (usuário) para um número
                // **Verificação .trim()**: Confirmado que .trim() já está sendo usado corretamente.
                matricula = Integer.parseInt(enterUserNameField.getText().trim());
            } catch (NumberFormatException e) {
                return 2; // Retorna 2 se a matrícula não for um número
            }

            cs.setInt(1, matricula); // Define o parâmetro de entrada (IN) da procedure

            try (ResultSet rs = cs.executeQuery()) { // Executa a consulta e garante que o ResultSet feche

                // Verifica se o banco retornou um usuário com essa matrícula
                if (rs.next()) {
                    // Se o usuário existe, salva os dados dele na Sessão estática
                    // para serem usados em outras telas
                    Sessao.setUsuario(
                            rs.getInt("matricula"),
                            rs.getString("nome"),
                            rs.getString("cargo")
                    );

                    // Agora, verifica o PIN (senha)
                    // **Verificação .trim()**: Confirmado que .trim() já está sendo usado corretamente.
                    String senhaDigitada = enterPasswordField.getText().trim();
                    String hashArmazenado = rs.getString("pin"); // Pega o hash (PIN criptografado) do banco

                    // Usa a biblioteca BCrypt para comparar a senha digitada com o hash armazenado
                    boolean senhaCorreta = BCrypt.checkpw(senhaDigitada, hashArmazenado);

                    // Retorna 0 (Sucesso) se a senha bater, 1 (Inválido) se não bater
                    return senhaCorreta ? 0 : 1;
                } else {
                    return 1; // Retorna 1 se a matrícula (usuário) não foi encontrada no DB
                }
            } // ResultSet é fechado aqui

        } catch (SQLException e) {
            e.printStackTrace(); // Loga a exceção SQL no console
            return 3; // Retorna 3 se der um erro de SQL
        } finally {
            // Garante que a conexão seja fechada
            try {
                if (connectDB != null && !connectDB.isClosed()) {
                    connectDB.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    } // Fim do validateLogin()

    /**
     * Método estático para iniciar e exibir a tela principal (Dashboard).
     * Chamado após o login ser bem-sucedido.
     */
    public void TelaInicial() {
        try {
            janelaInicio = new Stage();
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/inicio.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // 3. Carrega fontes personalizadas
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // 6. Configura a cena e o Stage para serem transparentes (sem borda)
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            janelaInicio.initStyle(StageStyle.TRANSPARENT);
            janelaInicio.setScene(scene);

            // 7. Adiciona ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaInicio.getIcons().add(new Image(logoUrl.toExternalForm()));

            // 8. --- Bloco para arrastar a janela transparente ---
            root.setOnMousePressed(event2 -> {
                xOffset = event2.getSceneX();
                yOffset = event2.getSceneY();
            });
            root.setOnMouseDragged(event2 -> {
                janelaInicio.setX(event2.getScreenX() - xOffset);
                janelaInicio.setY(event2.getScreenY() - yOffset);
            });
            // -------------------------------------------------

            // 9. Carrega o CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // 10. Configura e mostra a janela
            janelaInicio.setTitle("Importar ordem de serviço");
            janelaInicio.setResizable(false);

            // 11. Limpa a referência da janela quando ela for fechada
            janelaInicio.setOnHidden(e -> janelaInicio = null);

            janelaInicio.show();

            // CORREÇÃO: Removidas linhas duplicadas que estavam aqui (após o .show())

        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de falha ao abrir a tela principal, mostra um alerta
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro Crítico",
                            "Não foi possível carregar a tela principal. Verifique o log de erros.")
                    .showAndWait();
        }
    }
}
// Fim da classe LoginController
