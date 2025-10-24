package com.example.trabalhoA3Gilvania.excelHandling;

// Importações de classes do projeto
import com.example.trabalhoA3Gilvania.Utils.DataBaseConection;
import com.example.trabalhoA3Gilvania.Utils.FormsUtil;
import com.example.trabalhoA3Gilvania.Utils.Sessao;

// Importações de classes do JavaFX
import javafx.scene.control.Alert;
import javafx.stage.FileChooser; // Para abrir o seletor de arquivos do sistema
import javafx.stage.Stage;

// Importações de bibliotecas externas e do Java
import lombok.Cleanup; // (do Lombok) Garante que o FileInputStream será fechado
import org.apache.poi.ss.usermodel.*; // (do Apache POI) Para ler Excel
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // (do Apache POI) Para ler .xlsx

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de lógica de negócios para lidar com a importação de Ordens de Serviço.
 * Contém os métodos para selecionar o arquivo Excel e para processar
 * e inserir os dados no banco de dados.
 */
public class LeitorExcel {

    // Instância da classe utilitária para exibir pop-ups de alerta
    FormsUtil alerta = new FormsUtil();

    /**
     * Abre a janela (FileChooser) do sistema operacional para o usuário
     * selecionar um arquivo Excel (.xlsx ou .xls).
     *
     * @param stage A janela (Stage) principal, usada para ancorar o FileChooser.
     * @return O objeto File selecionado, ou null se o usuário cancelar.
     */
    public File selecionarArquivo(Stage stage) {
        // Cria o seletor de arquivos
        FileChooser fileChooser = new FileChooser();
        // Adiciona um filtro para mostrar apenas arquivos Excel
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Planilhas Excel (*.xlsx, *.xls)", "*.xlsx", "*.xls")
        );

        // Mostra a janela de "Abrir Arquivo"
        File fileSelected = fileChooser.showOpenDialog(stage);

        // Se o usuário fechar a janela sem selecionar, fileSelected será nulo
        if (fileSelected == null) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Nenhum arquivo foi selecionado")
                    .showAndWait();
        }

        return fileSelected; // Retorna o arquivo (ou null)
    }

    /**
     * Processa o arquivo Excel e cadastra a Ordem de Serviço, Operações e Itens
     * no banco de dados dentro de uma transação.
     *
     * @param numeroOs O número da OS que o usuário selecionou na tela de preview.
     * @param fileSelected O arquivo .xlsx a ser processado.
     * @return int - Um código de status:
     * 0 = Erro ao verificar/inserir OS (ex: falha de conexão inicial)
     * 1 = OS já cadastrada
     * 2 = Sucesso (OS, Operações e Itens cadastrados)
     * 3 = Erro durante a inserção de itens/operações ou no commit final
     * @throws IOException Se houver erro ao ler o arquivo.
     */
    public int criar(String numeroOs, File fileSelected) throws IOException {
        // (Nota: mantido o nome original 'connetDB' com o typo)
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        // Lista temporária para evitar inserir a mesma operação (do Excel) várias vezes
        // no banco de dados *durante esta mesma importação*.
        List<String> operacoes = new ArrayList<>();

        // --- Leitura do Arquivo Excel ---
        // @Cleanup (do Lombok) garante que o 'file.close()' será chamado no final,
        // mesmo se ocorrer um erro (similar a um try-with-resources).
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file); // Abre o arquivo .xlsx
        Sheet sheet = workbook.getSheetAt(0); // Pega a primeira aba

        boolean osExistente = false;
        boolean osCadastrada = false;

        // --- Início da Transação com o Banco de Dados ---
        try {
            // Desabilita o AutoCommit. Nenhuma alteração será salva no banco
            // até que 'connetDB.commit()' seja chamado.
            connetDB.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 1. Verifica ou Insere a Ordem de Serviço (OS)
        try {
            // Chama a procedure que verifica se a OS existe ou a insere
            String procVerificarOuInserirOS = "{CALL excel_verificar(?, ?, ?, ?)}";
            try (CallableStatement cs = connetDB.prepareCall(procVerificarOuInserirOS)) {
                cs.setString(1, numeroOs); // p_cod_os
                cs.setInt(2, Sessao.getMatricula()); // p_matricula (quem está importando)
                cs.registerOutParameter(3, java.sql.Types.BOOLEAN); // p_os_existente (OUT)
                cs.registerOutParameter(4, java.sql.Types.BOOLEAN); // p_os_inserida (OUT)

                cs.execute();

                // Pega os valores retornados (OUT) da procedure
                osExistente = cs.getBoolean(3);
                osCadastrada = cs.getBoolean(4);

                if (osExistente) {
                    return 1; // Retorna o código 1 (OS já existe)
                }

                // Se a OS foi inserida (osCadastrada = true), confirma (commit)
                // apenas essa parte da transação.
                connetDB.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Desfaz a transação
            return 0; // Retorna o código 0 (Erro na verificação/inserção da OS)
        }

        // 2. Processa as linhas (Operações e Itens)
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Pula a linha 0 (cabeçalho)

            // Lê os valores das células (assumindo que são String)
            String osString = row.getCell(1).getStringCellValue();
            // Ignora linhas que não sejam da OS selecionada pelo usuário
            if (!osString.equals(numeroOs)) continue;

            String operacaoString = row.getCell(2).getStringCellValue();
            int idOperacao = 0; // Armazena o ID da operação (nova ou existente)

            try {
                // 2a. Inserir ou Buscar a Operação
                // Verifica se esta operação já foi tratada *nesta importação* (cache local)
                if (!operacoes.contains(operacaoString)) {
                    // Se não foi tratada, chama a procedure para inserir a operação
                    String sqlOperacao = "{CALL inserir_operacao(?, ?, ?)}";
                    try (CallableStatement csOp = connetDB.prepareCall(sqlOperacao)) {
                        csOp.setString(1, operacaoString); // p_cod_operacao
                        csOp.setString(2, osString);       // p_cod_os
                        csOp.registerOutParameter(3, java.sql.Types.INTEGER); // p_id_operacao (OUT)
                        csOp.execute();

                        idOperacao = csOp.getInt(3); // Pega o ID da nova operação
                    }
                    operacoes.add(operacaoString); // Adiciona na cache local
                } else {
                    // Se já foi tratada, apenas busca o ID que já existe no banco
                    String sqlBuscaOperacao = "{CALL buscar_operacao_id(?, ?, ?)}";
                    try (CallableStatement csBusca = connetDB.prepareCall(sqlBuscaOperacao)) {
                        csBusca.setString(1, operacaoString); // p_cod_operacao
                        csBusca.setString(2, osString);       // p_cod_os
                        csBusca.registerOutParameter(3, java.sql.Types.INTEGER); // p_id_operacao (OUT)
                        csBusca.execute();

                        idOperacao = csBusca.getInt(3); // Pega o ID existente
                    }
                }

                // 2b. Inserir o Item
                // Lê a quantidade (célula numérica)
                int qtdPedido = (int) row.getCell(6).getNumericCellValue();

                // Só insere o item se a operação foi encontrada/criada (id != 0)
                // e se a quantidade pedida for maior que 0.
                if (idOperacao != 0 && qtdPedido != 0) {
                    String sqlItem = "{CALL inserir_item(?, ?, ?, ?)}";
                    try (CallableStatement csItem = connetDB.prepareCall(sqlItem)) {
                        csItem.setInt(1, idOperacao); // p_id_operacao (chave estrangeira)
                        csItem.setString(2, row.getCell(4).getStringCellValue()); // cod_item
                        csItem.setString(3, row.getCell(5).getStringCellValue()); // descricao
                        csItem.setInt(4, qtdPedido); // qtd_pedido
                        csItem.execute();
                    }
                }

            } catch (SQLException e) {
                // Se der qualquer erro no loop (ex: item duplicado, erro de tipo),
                // desfaz a transação inteira (rollback).
                e.printStackTrace();
                try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                // Lança o erro para o Controller (que trata a Task)
                throw new RuntimeException("Erro ao processar linha do Excel: " + e.getMessage(), e);
            }
        } // Fim do loop 'for (Row row : sheet)'

        // 3. Commit Final
        // Se o loop terminou sem erros, confirma (commit) todas as
        // inserções de operações e itens no banco de dados.
        try {
            connetDB.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return 3; // Retorna código 3 (Erro no commit final)
        }

        return 2; // Retorna código 2 (Sucesso total)
    } // Fim do método criar()
} // Fim da classe
