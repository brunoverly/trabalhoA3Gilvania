package com.example.trabalhoA3Gilvania.excelHandling;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorOperacao {

    FormsUtil alerta = new FormsUtil();

    public File selecionarArquivo(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Planilhas Excel (*.xlsx, *.xls)", "*.xlsx", "*.xls")
        );

        File fileSelected = fileChooser.showOpenDialog(stage);

        if (fileSelected == null) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Nenhum arquivo foi selecionado")
                    .showAndWait();
        }

        return fileSelected;
    }

    public void criar(String numeroOs, File fileSelected) throws IOException {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        List<String> operacoes = new ArrayList<>();

        // Acessando o arquivo
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0); // primeira aba

        boolean osExistente = false;
        boolean osCadastrada = false;

        try {
            connetDB.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 🔹 Primeiro, verifica ou insere a OS usando procedure
        try {
            String procVerificarOuInserirOS = "{CALL verificar_ou_inserir_os(?, ?, ?, ?)}";
            try (CallableStatement cs = connetDB.prepareCall(procVerificarOuInserirOS)) {
                cs.setString(1, numeroOs);
                cs.setInt(2, Sessao.getMatricula());
                cs.registerOutParameter(3, java.sql.Types.BOOLEAN); // p_os_existente
                cs.registerOutParameter(4, java.sql.Types.BOOLEAN); // p_os_inserida

                cs.execute();

                osExistente = cs.getBoolean(3);
                osCadastrada = cs.getBoolean(4);

                if (osExistente) {
                    alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Ordem de serviço já cadastrada")
                            .showAndWait();
                    return; // interrompe execução
                }

                // **Não mostra alerta aqui, vamos aguardar todas inserções**

                connetDB.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return;
        }

        // 🔹 Processa as linhas da planilha para inserir operações e itens
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // cabeçalho

            String osString = row.getCell(1).getStringCellValue();
            if (!osString.equals(numeroOs)) continue; // ignora outras OS

            String operacaoString = row.getCell(2).getStringCellValue();
            int idOperacao = 0;

            try {
                // 🔹 Inserir operação ou buscar idOperacao existente
                if (!operacoes.contains(operacaoString)) {
                    String sqlOperacao = "{CALL inserir_operacao(?, ?, ?)}";
                    try (CallableStatement csOp = connetDB.prepareCall(sqlOperacao)) {
                        csOp.setString(1, operacaoString);
                        csOp.setString(2, osString);
                        csOp.registerOutParameter(3, java.sql.Types.INTEGER);
                        csOp.execute();

                        idOperacao = csOp.getInt(3);
                    }
                    operacoes.add(operacaoString);
                } else {
                    String sqlBuscaOperacao = "{CALL buscar_operacao_id(?, ?, ?)}";
                    try (CallableStatement csBusca = connetDB.prepareCall(sqlBuscaOperacao)) {
                        csBusca.setString(1, operacaoString);
                        csBusca.setString(2, osString);
                        csBusca.registerOutParameter(3, java.sql.Types.INTEGER);
                        csBusca.execute();

                        idOperacao = csBusca.getInt(3);
                    }
                }

                // 🔹 Inserir item, sempre que houver quantidade > 0
                int qtdPedido = (int) row.getCell(6).getNumericCellValue();
                if (idOperacao != 0 && qtdPedido != 0) {
                    String sqlItem = "{CALL inserir_item(?, ?, ?, ?)}";
                    try (CallableStatement csItem = connetDB.prepareCall(sqlItem)) {
                        csItem.setInt(1, idOperacao);
                        csItem.setString(2, row.getCell(4).getStringCellValue()); // cod_item
                        csItem.setString(3, row.getCell(5).getStringCellValue()); // descricao
                        csItem.setInt(4, qtdPedido);
                        csItem.execute();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                throw new RuntimeException(e);
            }
        }

        // 🔹 Commit final após inserir todas operações e itens
        try {
            connetDB.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try { connetDB.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return;
        }

        // 🔹 Mostra alerta apenas se tudo deu certo
        alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço cadastrada com sucesso")
                .showAndWait();
    }
}
