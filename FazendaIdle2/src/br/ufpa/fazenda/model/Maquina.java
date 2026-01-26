package br.ufpa.fazenda.model;

/**
 * Define os tipos de máquinas e melhorias que podem ser instaladas no solo.
 */
public enum Maquina {
    
    TRATOR("Trator Automático", 300.0, "Realiza a colheita e venda automaticamente"),
    ARADOR("Arador Automático", 250.0, "Planta automaticamente a última semente usada"),
    IRRIGADOR("Sistema de Irrigação", 400.0, "Aumenta valor em 25% e reduz tempo em 15%");

    private final String nome;
    private final double custo;
    private final String descricao;

    Maquina(String nome, double custo, String descricao) {
        this.nome = nome;
        this.custo = custo;
        this.descricao = descricao;
    }

    // --- MÉTODOS DE LÓGICA ---
    
    // Futuro: Útil para o HUD mostrar a descrição quando passar o mouse
    public String getInfoCompleta() {
        return String.format("%s (R$ %.2f) - %s", nome, custo, descricao);
    }

    public String getNome() { return nome; }
    public double getCusto() { return custo; }
    public String getDescricao() { return descricao; }
}