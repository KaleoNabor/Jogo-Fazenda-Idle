package br.ufpa.fazenda.main;

/**
 * Ponto de entrada principal do jogo.
 * Pode iniciar em modo console para testes ou modo gráfico quando a interface estiver pronta.
 */
public class Main {
    public static void main(String[] args) {
        // Por enquanto, iniciamos o modo console para testes
        System.out.println("=== FAZENDA IDLE 2.0 - UFPA ===");
        System.out.println("Iniciando em modo console de teste...");
        System.out.println("Para iniciar com interface gráfica, use JanelaPrincipal.");
        System.out.println();
        
        // Inicia o teste console
        TesteConsole.main(args);
    }
}