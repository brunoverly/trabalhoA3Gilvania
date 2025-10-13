package com.example.trabalhoA3Gilvania.excelHandling;

import lombok.Cleanup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorItens {

    public List<Item> criar() throws IOException {
        List<Item> itens = new ArrayList<>();

        // Abre a janela do seletor de arquivo
        JFileChooser fileChooser = new JFileChooser();

        //Ativa filtro para somente arquivos de planilhas
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Planilhas Excel (*.xlsx, *.xls)", "xlsx", "xls"));

        //Checa se algum arquivo foi selecionado
        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo selecionado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return itens;
        }
        File fileSelected = fileChooser.getSelectedFile();
        if (!fileSelected.exists() || !fileSelected.isFile()) {
            JOptionPane.showMessageDialog(null, "Arquivo inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return itens;
        }

        //Verifica se o arquivo escolhido e uma planilha
        String nome = fileSelected.getName().toLowerCase();
        if (!nome.endsWith(".xlsx") && !nome.endsWith(".xls")) {
            JOptionPane.showMessageDialog(null, "Selecione apenas arquivos Excel (.xlsx ou .xls)!", "Erro", JOptionPane.ERROR_MESSAGE);
            return itens;
        }

        //Acessando o arquivo
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file);

        //Seleciona a primeira aba
        Sheet sheet = workbook.getSheetAt(0);


        //Loop para percorrer todas as linhas da planilha e retirar os itens de acordo com o numero da OS informado
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            int numerodaOs = 15901;

                //Se a OS for correspondente, cria um objeto Item e adiciona a lista de itens da OS
                if ((int) row.getCell(0).getNumericCellValue() == numerodaOs) {
                    Item item = Item.builder()
                            .nome(row.getCell(2).getStringCellValue())
                            .qtd((int) row.getCell(4).getNumericCellValue())
                            .codigoItem(row.getCell(3).getStringCellValue())
                            .operacao((int) row.getCell(1).getNumericCellValue())
                            .build();

                    itens.add(item);
                }
            }

        return itens;
    }

    //Imprime os itens da lista no cmd
    public void Imprimir (List<Item> itens){
        itens.forEach(System.out::println);
    }


    //Cadastra


}
