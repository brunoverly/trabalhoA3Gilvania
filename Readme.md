Sistema de Gerenciamento de Ordens de Serviço (OficinaPro)

📖 Descrição

Este é um projeto de conclusão da Avaliação A3 para o curso de Modelagem de Software, baseado em um estudo de caso real.
O S-GOS (Sistema de Gerenciamento de Ordens de Serviço) é uma aplicação desktop (JavaFX) desenvolvida para gerenciar o fluxo de ordens de serviço (OS) e controlar a entrada e movimentação de peças. O sistema rastreia os itens desde sua entrada na empresa (importação e recebimento no estoque) até a chegada na oficina (solicitação e retirada pelo mecânico).

✨ Funcionalidades Principais

O sistema é dividido em três perfis de usuário, cada um com permissões específicas:

👨‍💼 Administrador

Acesso total a todas as funcionalidades.
Gestão de Usuários: cadastrar/remover usuários (Admin, Aprovisionador, Mecânico).
Todas as permissões do Aprovisionador.

👷 Aprovisionador (Almoxarifado)

Importação de OS: cadastrar novas ordens através de arquivos .xlsx (Apache POI).

Gestão de Estoque:

Lançar Entrada: registrar recebimento de materiais, atualizar status e localização.
Lançar Retirada (Baixa): atender solicitações dos mecânicos e gerar PDF de comprovação (iText).
Gestão de OS: encerrar ordens concluídas.
Relatórios: gerar PDFs e consultar históricos por período.
Consulta: visualizar status de qualquer OS e seus itens.

🔧 Mecânico

Consulta: visualizar ordens e status dos itens (estoque ou pendentes).
Solicitar Item: criar solicitação formal para itens recebidos no estoque.

⚡ Outras Funcionalidades

Autenticação Segura: login com matrícula e PIN, usando jBCrypt.
Dashboard Central: estatísticas de OS (Abertas, Em Andamento, Encerradas) e log em tempo real das atividades.
Rastreabilidade: todo o fluxo do item é registrado no banco de dados.

🛠️ Tecnologias Utilizadas

Linguagem: Java 17+
Interface Gráfica: JavaFX
Banco de Dados: MySQL (com uso intensivo de Stored Procedures para a lógica de negócios)

Bibliotecas:

mysql-connector-java – Driver JDBC
org.apache.poi – leitura de Excel (.xlsx)
com.itextpdf:itext7-core – geração de PDFs
org.mindrot:jbcrypt – hashing de senhas
lombok – anotações pontuais (@Cleanup)

🚀 Como Executar o Projeto

1️⃣ Clonar Repositório
git clone [URL_DO_SEU_REPOSITORIO_AQUI]
cd [NOME_DA_PASTA_DO_PROJETO]

2️⃣ Configuração do Banco de Dados

Teste na Nuvem:

Um banco MySQL no Azure ficará disponível por 7 dias (a partir de 24/10/2025).
Permite rodar o software sem configuração local.
Configuração Local (após período de teste):
Inicie um servidor MySQL local (XAMPP/WAMP/Docker).
Crie o banco: projeto_java_a3.
Importe os scripts SQL da pasta src/main/resources/SQL/:
Primeiro tables.sql (estrutura de tabelas)
Depois procedures.sql (Stored Procedures e usuários de teste)

3️⃣ Configuração da Conexão

Abra src/main/java/com/example/trabalhoA3Gilvania/Utils/DataBaseConection.java.
Ajuste databaseUser, databasePassword e a URL (jdbc:mysql://localhost/projeto_java_a3).

4️⃣ Executar via IDE

Abra o projeto como Maven/Gradle no IntelliJ ou Eclipse.
Aguarde download das dependências.
Execute a classe Main.java ou LoginApplication.java.

👥 Usuários de Teste

Nome	Perfil	Matrícula
Bruno Verly Santos	Administrador	47219
Carla Mendes Oliveira	Aprovisionador	58302
Lucas Silva Ferreira	Aprovisionador	69047
Rafael Souza Lima	Mecânico	25138
Mariana Costa Alves	Mecânico	83714
Thiago Lima Rocha	Mecânico	41625

PIN para todos: 123456

📝 Testando a Importação de OS

Logue como Administrador ou Aprovisionador.
Vá em Importar OS no menu e use o arquivo IWBK GERAL.xlsx (src/main/resources/).

🗂️ Estrutura do Projeto
```
com.example.trabalhoA3Gilvania/
│
├── controller/        # Controladores JavaFX
│   ├── LoginController.java
│   ├── InicioController.java       (Dashboard)
│   ├── CadastrarUsuarioController.java
│   ├── ImportarOsController.java
│   ├── ConsultarOsController.java
│   ├── ConsultarItemController.java
│   ├── EntradaItemController.java
│   ├── SaidaItemController.java
│   ├── SolicitarItemController.java
│   ├── FecharOsController.java
│   ├── GerarPdfController.java
│   ├── ConsultarHistoricoController.java
│   └── RemoverUsuarioController.java
│
├── Utils/             # Classes utilitárias
│   ├── DataBaseConection.java  # Gerencia conexão JDBC
│   ├── FormsUtil.java          # Helpers (Alertas, GIF de loading)
│   ├── Sessao.java             # Dados do usuário logado
│   ├── OnFecharJanela.java     # Interface callback
│   └── PdfRetiradaItens.java   # Geração de PDF
│
├── excelHandling/     # Leitura de Excel
│   └── LeitorExcel.java
│
└── resources/
├── com/example/trabalhoA3Gilvania/ # FXML das telas
├── css/           # Estilo
├── imagens/       # Ícones
├── fonts/         # Fontes personalizadas
├── SQL/           # Scripts: tables.sql, procedures.sql
└── IWBK GERAL.xlsx # Modelo para teste de importação