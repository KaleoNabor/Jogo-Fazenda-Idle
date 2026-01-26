package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;

public class Cercado {
    
    private int id;
    private Animal especie;
    private int quantidadeAnimais;
    private static final int CAPACIDADE_MAXIMA = 3;
    
    // Controle de Produção
    private double progressoProducao; // 0.0 a 1.0
    private boolean produtoPronto;
    
    public Cercado(int id) {
        this.id = id;
        this.quantidadeAnimais = 0;
        this.progressoProducao = 0.0;
        this.produtoPronto = false;
    }
    
    /**
     * Tenta adicionar um animal ao cercado.
     * Só aceita se for da mesma espécie (ou se estiver vazio) e se houver espaço.
     */
    public boolean adicionarAnimal(Animal animal) {
        // Se está vazio, define a espécie
        if (quantidadeAnimais == 0) {
            this.especie = animal;
        }
        
        // Validações
        if (this.especie != animal) return false; // Espécie errada
        if (quantidadeAnimais >= CAPACIDADE_MAXIMA) return false; // Lotado
        
        this.quantidadeAnimais++;
        return true;
    }
    
    /**
     * Atualiza o tempo de produção dos animais.
     * @param deltaTempoSegundos Tempo passado no loop
     */
    public void atualizarTempo(double deltaTempoSegundos) {
        if (quantidadeAnimais == 0 || produtoPronto) return;
        
        // Usa o tempo específico da espécie do animal
        double tempoNecessario = especie.getTempoProducaoSegundos();
        
        // Incrementa progresso
        this.progressoProducao += (deltaTempoSegundos / tempoNecessario);
        
        if (this.progressoProducao >= 1.0) {
            this.progressoProducao = 1.0;
            this.produtoPronto = true;
        }
    }
    
    public double coletarProdutos() {
        if (!produtoPronto || quantidadeAnimais == 0) return 0.0;
        
        // Valor = Valor do produto * quantidade de animais
        double valorTotal = especie.getProdutoValor() * quantidadeAnimais;
        
        // Reseta o ciclo
        this.produtoPronto = false;
        this.progressoProducao = 0.0;
        
        return valorTotal;
    }
    
    public double calcularCustoManutencao() {
        if (quantidadeAnimais == 0) return 0.0;
        return especie.getCustoManutencaoDiaria() * quantidadeAnimais;
    }

    // --- Getters ---
    public int getId() { return id; }
    public int getQuantidade() { return quantidadeAnimais; }
    public Animal getEspecie() { return especie; }
    public boolean isProdutoPronto() { return produtoPronto; }
    public double getProgresso() { return progressoProducao; }
    public boolean isVazio() { return quantidadeAnimais == 0; }
    
    // Para a interface gráfica saber quantos slots estão ocupados
    public int getCapacidadeMaxima() { return CAPACIDADE_MAXIMA; }
}