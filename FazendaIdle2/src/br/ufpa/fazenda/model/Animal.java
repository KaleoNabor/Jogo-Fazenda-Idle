package br.ufpa.fazenda.model;

/**
 * Define as espécies de animais e seus dados econômicos.
 */
public enum Animal {
    
    GALINHA("Galinha", "Ovos", 5.0, 8.0, 30.0, 100.0),    // Custo: 5, Valor: 8, Tempo: 30s, Preço: 100
    OVELHA("Ovelha", "Lã", 10.0, 25.0, 45.0, 250.0),      // Custo: 10, Valor: 25, Tempo: 45s, Preço: 250
    VACA("Vaca", "Leite", 20.0, 50.0, 60.0, 500.0);       // Custo: 20, Valor: 50, Tempo: 60s, Preço: 500

    private final String nome;
    private final String produto;
    private final double custoManutencaoDiaria;
    private final double produtoValor;
    private final double tempoProducaoSegundos; // Tempo para produzir 1 lote
    private final double precoCompra; // Preço para comprar um animal

    Animal(String nome, String produto, double custoManutencaoDiaria, double produtoValor, 
           double tempoProducaoSegundos, double precoCompra) {
        this.nome = nome;
        this.produto = produto;
        this.custoManutencaoDiaria = custoManutencaoDiaria;
        this.produtoValor = produtoValor;
        this.tempoProducaoSegundos = tempoProducaoSegundos;
        this.precoCompra = precoCompra;
    }

    // --- LÓGICA PARA O SISTEMA DE TEMPO ---
    
    // Calcula quanto vai descontar do saldo por ciclo (dia)
    public double calcularCustoPorDia(int quantidadeAnimais) {
        return this.custoManutencaoDiaria * quantidadeAnimais;
    }

    // Getter para o tempo de produção
    public double getTempoProducaoSegundos() {
        return tempoProducaoSegundos;
    }

    // Getter para valor do produto
    public double getProdutoValor() {
        return produtoValor;
    }

    public String getNome() { 
        return nome; 
    }
    
    public String getProduto() { 
        return produto; 
    }
    
    public double getCustoManutencaoDiaria() { 
        return custoManutencaoDiaria; 
    }
    
    public double getPrecoCompra() {
        return precoCompra;
    }
}