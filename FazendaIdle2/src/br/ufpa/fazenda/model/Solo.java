package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;
import java.util.HashSet;
import java.util.Set;

public class Solo {
    
    private int id; // Para identificar o solo (0, 1, 2...)
    private int nivel; // Nível do solo (1-10 conforme documentação)
    private boolean desbloqueado; // Indica se o solo está desbloqueado
    private Vegetal vegetalPlantado;
    private double progressoCrescimento; // 0.0 a 1.0 (1.0 = pronto)
    
    // Configurações
    private Set<Maquina> maquinasInstaladas;
    private boolean fertilizanteAtivado; // O interruptor do usuário
    private boolean estaComFertilizanteAplicado; // Se a planta atual recebeu o efeito
    
    public Solo(int id, boolean desbloqueadoInicialmente) {
        this.id = id;
        this.nivel = 1; // Nível inicial
        this.desbloqueado = desbloqueadoInicialmente;
        this.maquinasInstaladas = new HashSet<>();
        this.fertilizanteAtivado = false;
        limparSolo();
    }
    
    /**
     * Tenta plantar um vegetal.
     * Retorna true se conseguiu, false se já estava ocupado.
     */
    public boolean plantar(Vegetal vegetal) {
        if (vegetalPlantado != null || !desbloqueado) return false; // Solo ocupado ou bloqueado
        
        this.vegetalPlantado = vegetal;
        this.progressoCrescimento = 0.0;
        
        // Lógica do Fertilizante:
        // Se o botão estiver ligado E a fazenda tiver estoque (verificaremos estoque depois na integração)
        if (fertilizanteAtivado) {
            // Aqui futuramente chamaremos FazendaEstado.getInstance().consumirFertilizante()
            this.estaComFertilizanteAplicado = true; 
        } else {
            this.estaComFertilizanteAplicado = false;
        }
        
        return true;
    }
    
    /**
     * Força a plantação de um vegetal, substituindo o atual se houver.
     * Retorna true se conseguiu plantar.
     */
    public boolean plantarSubstituindo(Vegetal vegetal) {
        if (!desbloqueado) return false;
        
        // Se já estiver ocupado, arranca a planta atual
        if (vegetalPlantado != null) {
            arrancar();
        }
        
        return plantar(vegetal);
    }
    
    /**
     * Calcula o valor de venda com todos os bônus aplicados.
     * Inclui: bônus por nível do solo, irrigador e fertilizante.
     */
    public double calcularValorVenda() {
        if (vegetalPlantado == null) return 0.0;
        
        double valorBase = vegetalPlantado.getValorVenda();
        double multiplicador = 1.0;
        
        // 1. Bônus por nível do solo (+20% por nível)
        multiplicador += (nivel - 1) * Constantes.BONUS_SOLO_NV_VALOR;
        
        // 2. Bônus do Irrigador (+25% valor)
        if (maquinasInstaladas.contains(Maquina.IRRIGADOR)) {
            multiplicador += Constantes.BONUS_IRRIGADOR_VALOR;
        }
        
        // 3. Bônus do Fertilizante (+50% valor)
        if (estaComFertilizanteAplicado) {
            multiplicador += Constantes.BONUS_FERTILIZANTE_VALOR;
        }
        
        return valorBase * multiplicador;
    }
    
    /**
     * Método chamado pelo GameLoop a cada X milissegundos.
     * @param deltaTempoSegundos Quanto tempo passou desde o último update
     */
    public void atualizarTempo(double deltaTempoSegundos) {
        if (vegetalPlantado == null) return;
        if (progressoCrescimento >= 1.0) return; // Já está maduro
        
        // 1. Calcula o tempo base em segundos (Ex: Alface 2 dias * 15s = 30s)
        double tempoTotalNecessario = vegetalPlantado.getTempoEmSegundos();
        
        // 2. Aplica REDUTORES de tempo (Irrigador, Fertilizante, Nível do Solo)
        double fatorReducao = 0.0;
        
        if (maquinasInstaladas.contains(Maquina.IRRIGADOR)) {
            fatorReducao += Constantes.BONUS_IRRIGADOR_TEMPO;
        }
        
        if (estaComFertilizanteAplicado) {
            fatorReducao += Constantes.BONUS_FERTILIZANTE_TEMPO;
        }
        
        // Bônus de nível (Nível 1 não dá bônus, Nível 2 dá 10%...)
        fatorReducao += (nivel - 1) * Constantes.BONUS_SOLO_NV_CRESCIMENTO;
        
        // Limite de segurança: nunca reduzir mais que 90% do tempo
        if (fatorReducao > 0.9) fatorReducao = 0.9;
        
        double tempoFinal = tempoTotalNecessario * (1.0 - fatorReducao);
        
        // 3. Incrementa o progresso
        // Se tempoFinal é 10s e passou 1s, aumentamos 0.1 (10%)
        this.progressoCrescimento += (deltaTempoSegundos / tempoFinal);
        
        if (this.progressoCrescimento > 1.0) this.progressoCrescimento = 1.0;
    }
    
    /**
     * Colhe o vegetal e retorna o valor de venda.
     */
    public double colher() {
        if (progressoCrescimento < 1.0 || vegetalPlantado == null) return 0.0;
        
        double valorVenda = calcularValorVenda();
        limparSolo();
        return valorVenda;
    }
    
    /**
     * Arranca a planta atual sem colher (sem ganhar dinheiro).
     */
    public void arrancar() {
        limparSolo();
    }
    
    /**
     * Aplica fertilizante na planta atual (se houver estoque).
     */
    public boolean aplicarFertilizante() {
        if (vegetalPlantado == null || estaComFertilizanteAplicado) return false;
        
        FazendaEstado fazenda = FazendaEstado.getInstance();
        if (fazenda.consumirFertilizanteDoEstoque()) {
            this.estaComFertilizanteAplicado = true;
            return true;
        }
        return false;
    }
    
    private void limparSolo() {
        this.vegetalPlantado = null;
        this.progressoCrescimento = 0.0;
        this.estaComFertilizanteAplicado = false;
    }

    // --- Instalação de Máquinas ---
    
    public void instalarMaquina(Maquina maquina) {
        if (!desbloqueado) return;
        maquinasInstaladas.add(maquina);
    }
    
    public void removerMaquina(Maquina maquina) {
        maquinasInstaladas.remove(maquina);
    }
    
    public boolean temMaquina(Maquina maquina) {
        return maquinasInstaladas.contains(maquina);
    }
    
    // --- Upgrade do Solo ---
    
    /**
     * Melhora o nível do solo (custo: R$ 100 × nível atual).
     * Retorna true se conseguiu fazer o upgrade.
     */
    public boolean upgrade() {
        if (nivel >= 10 || !desbloqueado) return false; // Nível máximo ou solo bloqueado
        
        double custoUpgrade = 100.0 * nivel;
        FazendaEstado fazenda = FazendaEstado.getInstance();
        
        if (fazenda.gastarDinheiro(custoUpgrade)) {
            nivel++;
            return true;
        }
        return false;
    }
    
    // --- Desbloqueio do Solo ---
    
    /**
     * Desbloqueia o solo (custo: R$ 300).
     * Retorna true se conseguiu desbloquear.
     */
    public boolean desbloquear() {
        if (desbloqueado) return true; // Já está desbloqueado
        
        double custoDesbloqueio = 300.0;
        FazendaEstado fazenda = FazendaEstado.getInstance();
        
        if (fazenda.gastarDinheiro(custoDesbloqueio)) {
            this.desbloqueado = true;
            return true;
        }
        return false;
    }

    // --- Getters e Setters para a Interface Gráfica ---
    
    public int getId() { return id; }
    public int getNivel() { return nivel; }
    public boolean isDesbloqueado() { return desbloqueado; }
    public boolean isOcupado() { return vegetalPlantado != null; }
    public boolean isPronto() { return progressoCrescimento >= 1.0; }
    public double getProgresso() { return progressoCrescimento; }
    public Vegetal getVegetal() { return vegetalPlantado; }
    public Set<Maquina> getMaquinasInstaladas() { return new HashSet<>(maquinasInstaladas); }
    
    public void setFertilizanteAtivado(boolean ativo) { 
        this.fertilizanteAtivado = ativo; 
    }
    
    public boolean isFertilizanteAtivado() { 
        return fertilizanteAtivado; 
    }
    
    public boolean isEstaComFertilizanteAplicado() { 
        return estaComFertilizanteAplicado; 
    }
}