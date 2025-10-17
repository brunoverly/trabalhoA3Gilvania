package com.example.trabalhoA3Gilvania.excelHandling;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorOperacao {


    public File selecionarArquivo(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Planilhas Excel (*.xlsx, *.xls)", "*.xlsx", "*.xls")
        );

        File fileSelected = fileChooser.showOpenDialog(stage);

        if (fileSelected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Nenhum arquivo selecionado");
            alert.showAndWait();
        }

        return fileSelected;
    }

    public void criar(String numeroOs, File fileSelected) throws IOException {
        DataFormatter formatter = new DataFormatter();
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        List<Operacao> operacoes = new ArrayList<>();


        //Acessando o arquivo
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file);

        //Seleciona a primeira aba
        Sheet sheet = workbook.getSheetAt(0);

        boolean osExistOnExcel = false;
        boolean osExistente = false;
        boolean osCadastrada = false;

        try {
            connetDB.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String osString = formatter.formatCellValue(row.getCell(1));
            String operacaoString = formatter.formatCellValue(row.getCell(2));
            int idOperacao;

            if (osString.equals(numeroOs)) {
                osExistOnExcel = true;

                String verifcarCadastroBanco = "SELECT COUNT(*) FROM ordem_servico WHERE cod_os = ?";
                try (PreparedStatement statement1 = connetDB.prepareStatement(verifcarCadastroBanco)) {
                    statement1.setString(1, osString);
                    ResultSet resultadoBuscaOs = statement1.executeQuery();

                if (resultadoBuscaOs.next()) {
                    int count = resultadoBuscaOs.getInt(1);
                    if (count > 0) {
                        osExistente = true;
                    } else {
                        osExistOnExcel = true;
                        LocalDateTime agora = LocalDateTime.now();
                        Timestamp ts = Timestamp.valueOf(agora);
                        try {
                            String sqlOrdem = "INSERT INTO ordem_servico (datahora_abertura, cod_os) VALUES (?, ?)";
                            try (PreparedStatement ps = connetDB.prepareStatement(sqlOrdem)) {
                                ps.setTimestamp(1, ts); // data/hora no formato MySQL
                                ps.setString(2, osString); // numero_os
                                ps.executeUpdate();
                                osCadastrada = true;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getCause();
                        }
                    }
                }
                    try {
                        connetDB.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (osCadastrada) {
                        try {
                            // 🔹 Inserir operação e recuperar idOperacao corretamente
                            String sqlOperacao = "INSERT INTO operacao (cod_operacao, cod_os, cod_item) VALUES (?, ?, ?)";
                            try (PreparedStatement ps = connetDB.prepareStatement(sqlOperacao, Statement.RETURN_GENERATED_KEYS)) {
                                ps.setString(1, operacaoString);
                                ps.setString(2, osString);
                                ps.setString(3, row.getCell(4).getStringCellValue());
                                ps.executeUpdate();

                                // Recupera o ID gerado automaticamente apenas da operação
                                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        idOperacao = generatedKeys.getInt(1); // ID correto da operação
                                    } else {
                                        throw new SQLException("Falha ao obter id da operação");
                                    }
                                }
                            }

                            // 🔹 Inserir item vinculado ao idOperacao correto
                            String sqlItem = "INSERT INTO item (id_operacao, cod_item, descricao, qtd_pedido) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement psItem = connetDB.prepareStatement(sqlItem)) {
                                psItem.setInt(1, idOperacao); // usa o id correto da operação
                                psItem.setString(2, row.getCell(4).getStringCellValue());
                                psItem.setString(3, row.getCell(5).getStringCellValue());
                                psItem.setInt(4, (int) row.getCell(6).getNumericCellValue());
                                psItem.executeUpdate();

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getCause();
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    e.getCause();
                }
            }
        }

        try {
            connetDB.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }

        if(!osExistOnExcel){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("O número da ordem de serviço informada não foi localizado na planilha");
            alert.showAndWait();
        }
        else if (osCadastrada) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Ordem cadastrada com sucesso");
            alert.showAndWait();
        }
        else if (osExistente) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Ordem de serviço já cadastrada");
            alert.showAndWait();
        }
    }
}



