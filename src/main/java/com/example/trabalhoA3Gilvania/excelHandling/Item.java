package com.example.trabalhoA3Gilvania.excelHandling;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private String nome;
    private int qtd;
    private String codigoItem;
    private int operacao;
}
