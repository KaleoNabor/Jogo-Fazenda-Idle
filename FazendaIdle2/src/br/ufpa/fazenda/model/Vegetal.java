package br.ufpa.fazenda.model;

/**
 * Define o catálogo de plantas disponíveis no jogo.
 * Funciona como um banco de dados estático.
 */
public enum Vegetal {
    
    // Definição: Nome, NivelNecessario, DiasParaCrescer, ValorVenda
    ALFACE("Alface", 1, 2, 15.0),
    CENOURA("Cenoura", 2, 4, 40.0),
    ABOBORA("Abóbora", 5, 10, 150.0);

    private final String nome;
    private final int nivelMinimo;
    private final int diasParaCrescer; // Tempo em "dias do jogo"
    private final double valorVenda;

    // Construtor do Enum
    Vegetal(String nome, int nivelMinimo, int diasParaCrescer, double valorVenda) {
        this.nome = nome;
        this.nivelMinimo = nivelMinimo;
        this.diasParaCrescer = diasParaCrescer;
        this.valorVenda = valorVenda;
    }

    // --- MÉTODOS AUXILIARES (Os Fios) ---

    // Ajuda a calcular quanto tempo real vai levar (Regra: 1 dia = 15s)
    public long getTempoEmSegundos() {
        return this.diasParaCrescer * 15; 
    }

    // Getters padrão
    public String getNome() { return nome; }
    public int getNivelMinimo() { return nivelMinimo; }
    public int getDiasParaCrescer() { return diasParaCrescer; }
    public double getValorVenda() { return valorVenda; }
}