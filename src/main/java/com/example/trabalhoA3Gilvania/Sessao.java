package com.example.trabalhoA3Gilvania;

public class Sessao {
    private static String nome;
    private static int matricula;
    private static String cargo;

    public static void setUsuario(int matriculaUsuario, String nomeUsuario, String cargoUsuario) {
        Sessao.matricula = matriculaUsuario; // agora atribui corretamente ao atributo estático
        nome = nomeUsuario;
        cargo = cargoUsuario;
    }

    public static String getNome() {
        return nome;
    }

    public static String getCargo() {
        return cargo;
    }

    public static int getMatricula() {
        return matricula;
    }

    public static void clear() {
        nome = null;
        cargo = null;
        matricula = 0;
    }
}

