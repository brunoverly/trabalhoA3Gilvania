package com.example.trabalhoA3Gilvania.excelHandling;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main (String[] args) throws IOException {


        //Teste da busca de itens por OS
        //GerenciadorItens itensdaOs = new GerenciadorItens();
        //List<Item> listaItensdaOs = itensdaOs.criar();
        //itensdaOs.Imprimir(listaItensdaOs);

        //Teste da busca de Operacoes por OS
        GerenciadorOperacao operacoesdaOs = new GerenciadorOperacao();
        File filePath;
        filePath = operacoesdaOs.selecionarArquivo();
        operacoesdaOs.criar("6500640702", filePath);

    }
}
