package com.example.trabalhoA3Gilvania.excelHandling;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import javafx.scene.control.Alert;
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


    public File selecionarArquivo() {
        // Abre a janela do seletor de arquivo
        JFileChooser fileChooser = new JFileChooser();

        //Ativa filtro para somente arquivos de planilhas
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Planilhas Excel (*.xlsx, *.xls)", "xlsx", "xls"));

        //Checa se algum arquivo foi selecionado
        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo selecionado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        File fileSelected = fileChooser.getSelectedFile();
        if (fileSelected == null || !fileSelected.exists() || !fileSelected.isFile()) {
            JOptionPane.showMessageDialog(null, "Arquivo inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
        if (!fileSelected.exists() || !fileSelected.isFile()) {
            JOptionPane.showMessageDialog(null, "Arquivo inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }

        //Verifica se o arquivo escolhido e uma planilha
        String nome = fileSelected.getName().toLowerCase();
        if (!nome.endsWith(".xlsx") && !nome.endsWith(".xls")) {
            JOptionPane.showMessageDialog(null, "Selecione apenas arquivos Excel (.xlsx ou .xls)!", "Erro", JOptionPane.ERROR_MESSAGE);
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
                            String sqlOperacao = "INSERT INTO operacao (cod_operacao, cod_os, cod_item) VALUES (?, ?, ?)";
                            try (PreparedStatement ps = connetDB.prepareStatement(sqlOperacao, Statement.RETURN_GENERATED_KEYS)) {
                                ps.setString(1, operacaoString);
                                ps.setString(2, osString);
                                ps.setString(3, row.getCell(4).getStringCellValue());
                                ps.executeUpdate();

                                // Recupera o ID gerado automaticamente
                                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        idOperacao = generatedKeys.getInt(1); // aqui temos o id correto da operação
                                    } else {
                                        throw new SQLException("Falha ao obter id da operação.");
                                    }
                                }
                            }

                            // Agora insere o item vinculado ao idOperacao correto
                            String sqlItem = "INSERT INTO item (id_operacao, cod_item, descricao, qtd_pedido) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement psItem = connetDB.prepareStatement(sqlItem)) {
                                psItem.setInt(1, idOperacao);
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
            alert.setContentText("Ordem cadastrada com sucesso!");
            alert.showAndWait();
        }
        else if (osExistente) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Ordem de servico ja esta cadastrada!");
            alert.showAndWait();
        }
    }
}



