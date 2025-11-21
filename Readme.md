## ⚙️ Sistema de Gerenciamento de Ordens de Serviço (OficinaPro)

![Java](https://img.shields.io/badge/Java-17+-%23007396?logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-%23007396?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Local-%234479A1?logo=mysql&logoColor=white)
![Apache POI](https://img.shields.io/badge/Excel%20Import-Apache%20POI-darkgreen?logo=apache&logoColor=white)
![iTextPDF](https://img.shields.io/badge/PDF%20Generation-iTextPDF-red?logo=adobe-acrobat-reader&logoColor=white)
![jBCrypt](https://img.shields.io/badge/Security-BCrypt-orange)

---
### 📖 Descrição

Este é um projeto de conclusão da Avaliação A3 para o curso de Modelagem de Software, baseado em um estudo de caso real.
O OficinaPro é uma aplicação desktop (JavaFX) desenvolvida para gerenciar o fluxo de ordens de serviço (OS) e controlar a entrada e movimentação de peças. O sistema rastreia os itens desde sua entrada na empresa (importação e recebimento no estoque) até a chegada na oficina (solicitação e retirada pelo mecânico).

### ✨ Funcionalidades Principais

O sistema é dividido em três perfis de usuário, cada um com permissões específicas:

👨‍💼 **Administrador**

Acesso total a todas as funcionalidades.
Gestão de Usuários: cadastrar/remover usuários (Admin, Aprovisionador, Mecânico).
Todas as permissões do Aprovisionador.

👷 **Aprovisionador (Almoxarifado)**

Importação de OS: cadastrar novas ordens através de arquivos .xlsx (Apache POI).

Gestão de Estoque:

Lançar Entrada: registrar recebimento de materiais, atualizar status e localização.
Lançar Retirada (Baixa): atender solicitações dos mecânicos e gerar PDF de comprovação (iText).
Gestão de OS: encerrar ordens concluídas.
Relatórios: gerar PDFs e consultar históricos por período.
Consulta: visualizar status de qualquer OS e seus itens.

🔧 **Mecânico**

Consulta: visualizar ordens e status dos itens (estoque ou pendentes).
Solicitar Item: criar solicitação formal para itens recebidos no estoque.

### ⚡ Outras Funcionalidades

Autenticação Segura: login com matrícula e PIN, usando jBCrypt.
Dashboard Central: estatísticas de OS (Abertas, Em Andamento, Encerradas) e log em tempo real das atividades.
Rastreabilidade: todo o fluxo do item é registrado no banco de dados.

### 🛠️ Tecnologias Utilizadas

| Categoria              | Tecnologia                                      | Descrição / Uso                                      |
|------------------------|-------------------------------------------------|-------------------------------------------------------|
| Linguagem              | **Java 17+**                                    | Linguagem principal do projeto                        |
| Interface Gráfica      | **JavaFX**                                      | Interface desktop rica e moderna                      |
| Banco de Dados         | **MySQL**                                       | Banco relacional + Stored Procedures para lógica de negócio |
| Driver JDBC            | `mysql-connector-java`                          | Conexão Java ↔ MySQL                                  |
| Leitura Excel          | `org.apache.poi:poi-ooxml`                      | Importação de ordens de serviço (.xlsx)               |
| Geração de PDF         | `com.itextpdf:itext7-core`                      | Comprovantes de retirada em PDF                       |
| Hashing de senhas/PIN  | `org.mindrot:jbcrypt`                           | Segurança no armazenamento de PINs                    |
| Redução de boilerplate | `org.projectlombok:lombok`                      | Anotações como `@Cleanup`, `@Getter`, etc.            |


### 🚀 Como Executar o Projeto

### 1️⃣ Clonar Repositório
git clone https://github.com/BrunoVerly/trabalhoA3Gilvania.git

### 2️⃣ Configuração do Banco de Dados
Abra DataBaseConection.java em 
```
src/
└── main/
    └── java/
        └── com/example/trabalhoA3Gilvania/
            ├── DataBaseConection.java
```

Você deve editar as credenciais de acesso ao seu banco de dados local.

Local do arquivo: src/main/java/com/example/trabalhoA3Gilvania/DataBaseConection.java

Edite as seguintes variáveis, substituindo pelos seus dados de acesso:
```
// [...]
private final String databaseUser = "USUARIO";        // seu usuário
private final String databasePassword = "SENHA";   // sua senha
private final String url = "jdbc:mysql://localhost:3306/"SCHEMA"?useSSL=false&serverTimezone=UTC"; // seu schema no banco
// [...] 
```
### 4️⃣ Executar via IDE

Abra o projeto como Maven/Gradle no IntelliJ ou Eclipse.
Aguarde download das dependências.
Execute a classe Main.java em
```
src/
└── main/
    └── java/
        └── com/example/trabalhoA3Gilvania/
           ├── Main.java
```

### 👥 Usuários de Teste
Usuários de teste disponíveis no banco cadastrado para testes:
```
| Nome                  | Perfil        | Matrícula |
|-----------------------|---------------|-----------|
| Bruno Verly Santos    | Administrador | 47219     |
| Carla Mendes Oliveira | Aprovisionador| 58302     |
| Lucas Silva Ferreira  | Aprovisionador| 69047     |
| Rafael Souza Lima     | Mecânico      | 25138     |
| Mariana Costa Alves   | Mecânico      | 83714     |
| Thiago Lima Rocha     | Mecânico      | 41625     |

PIN para todos: 123456
```
### ⚙️ Fluxo de Funcionamento da Aplicação

Para o funcionamento correto da aplicação, o fluxo da ordem de serviço (OS) e movimentação de itens deve ser respeitado conforme abaixo:
1. Importar – Carregar os dados dos itens e operação para dentro do sistema.
2. Lançar Entrada – Registrar os itens que foram recebidos no estoque.
3. Solicitar – Realizar solicitações de itens de entrega do estoque a oficina.
4. Lançar Retirada – Efetuar a retirada e entrega a oficina dos itens solicitados e gerar comprovante em PDF.
5. Fechar OS – Encerrar a ordem de serviço quando todos os itens forem entregues.


### 📝 Testando a Importação de OS

Logue como Administrador ou Aprovisionador.
Vá em Importar OS no menu e use o arquivo IWBK GERAL.xlsx disponível em:
```
src/
└── main/
    └── com/example/trabalhoA3Gilvania/
        └── resources/
            └── IWBK GERAL.xlsx
```

### 🗂️ Estrutura do Projeto
```
com.example.trabalhoA3Gilvania/
├── Main.java          # Classe main do projeto
├── controller/        # Controladores JavaFX
│   ├── LoginController.java
│   ├── InicioController.java       # Dashboard
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
├── Utils/             # Classes utilitárias
│   ├── DataBaseConection.java
│   ├── FormsUtil.java
│   ├── Sessao.java
│   ├── OnFecharJanela.java
│   └── PdfRetiradaItens.java
├── excelHandling/     # Leitura de Excel
│   └── LeitorExcel.java
└── resources/
    ├── com/example/trabalhoA3Gilvania/   # FXML das telas
    ├── css/                               # Estilo
    ├── imagens/                           # Ícones
    ├── fonts/                             # Fontes personalizadas
    ├── SQL/                               # Scripts: tables.sql, procedures.sql
    └── IWBK GERAL.xlsx                     # Modelo para teste de importação
