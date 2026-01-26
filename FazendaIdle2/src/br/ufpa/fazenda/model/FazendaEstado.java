package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton que gerencia o estado global do jogo.
 */
public class FazendaEstado {
    
    private static FazendaEstado instance;
    
    // Dados Globais
    private double dinheiro;
    private int diaAtual;
    private int estoqueFertilizante;
    
    // Lista de todos os solos da fazenda
    private List<Solo> solos;
    
    // Lista de todos os cercados da fazenda
    private List<Cercado> cercados;
    
    // Inventário de máquinas (quantidade disponível para instalar)
    private Map<Maquina, Integer> inventarioMaquinas;
    
    // Mapeamento de máquinas instaladas por solo (soloId -> Set<Maquina>)
    private Map<Integer, List<Maquina>> maquinasInstaladas;
    
    // Último vegetal plantado (para o Arador lembrar)
    private Vegetal ultimoVegetalPlantado;
    
    // Construtor privado (padrão Singleton)
    private FazendaEstado() {
        this.dinheiro = 500.0; // Dinheiro inicial
        this.diaAtual = 1;
        this.estoqueFertilizante = 0;
        this.solos = new ArrayList<>();
        this.cercados = new ArrayList<>();
        this.inventarioMaquinas = new HashMap<>();
        this.maquinasInstaladas = new HashMap<>();
        this.ultimoVegetalPlantado = Vegetal.ALFACE; // Default
        
        // Inicializa 6 solos: 3 desbloqueados, 3 bloqueados
        for (int i = 0; i < 6; i++) {
            boolean desbloqueado = (i < 3); // Primeiros 3 desbloqueados
            solos.add(new Solo(i, desbloqueado));
            maquinasInstaladas.put(i, new ArrayList<>());
        }
        
        // Inicializa 3 cercados com 1 animal cada da espécie específica
        for (int i = 0; i < 3; i++) {
            Cercado cercado = new Cercado(i);
            Animal especie = null;
            switch (i) {
                case 0: especie = Animal.GALINHA; break;
                case 1: especie = Animal.OVELHA; break;
                case 2: especie = Animal.VACA; break;
            }
            
            if (especie != null) {
                // Adiciona 1 animal inicial (GRÁTIS no início)
                cercado.adicionarAnimal(especie);
            }
            cercados.add(cercado);
        }
        
        // Inicializa inventário de máquinas vazio
        for (Maquina maquina : Maquina.values()) {
            inventarioMaquinas.put(maquina, 0);
        }
    }
    
    // Acesso global
    public static synchronized FazendaEstado getInstance() {
        if (instance == null) {
            instance = new FazendaEstado();
        }
        return instance;
    }
    
    // --- Métodos de Negócio ---
    
    public boolean gastarDinheiro(double valor) {
        if (dinheiro >= valor) {
            dinheiro -= valor;
            return true;
        }
        return false;
    }
    
    public void ganharDinheiro(double valor) {
        this.dinheiro += valor;
    }
    
    public void avancarDia() {
        this.diaAtual++;
        // Aplicar custos de manutenção diária dos animais
        aplicarCustosManutencaoAnimais();
        // Aplicar custos de manutenção semanal das máquinas (a cada 7 dias)
        if (diaAtual % 7 == 0) {
            aplicarCustosManutencaoMaquinas();
        }
    }
    
    private void aplicarCustosManutencaoAnimais() {
        double custoTotal = 0;
        for (Cercado cercado : cercados) {
            custoTotal += cercado.calcularCustoManutencao();
        }
        if (custoTotal > 0) {
            dinheiro -= custoTotal;
        }
    }
    
    private void aplicarCustosManutencaoMaquinas() {
        double custoTotal = 0;
        int totalMaquinas = 0;
        
        // Conta todas as máquinas instaladas
        for (List<Maquina> maqs : maquinasInstaladas.values()) {
            totalMaquinas += maqs.size();
        }
        
        // Custo: R$ 10 por máquina por semana
        custoTotal = totalMaquinas * 10.0;
        
        if (custoTotal > 0 && dinheiro >= custoTotal) {
            dinheiro -= custoTotal;
        }
    }
    
    public boolean consumirFertilizanteDoEstoque() {
        if (estoqueFertilizante > 0) {
            estoqueFertilizante--;
            return true;
        }
        return false;
    }
    
    public void comprarFertilizante() {
        if (gastarDinheiro(Constantes.CUSTO_FERTILIZANTE_LOTE)) {
            estoqueFertilizante += Constantes.QTD_FERTILIZANTE_LOTE;
        }
    }
    
    // --- Métodos de Desbloqueio de Solos ---
    
    /**
     * Desbloqueia um solo específico.
     * @param soloId ID do solo (0-5)
     * @return true se conseguiu desbloquear
     */
    public boolean desbloquearSolo(int soloId) {
        if (soloId < 0 || soloId >= solos.size()) return false;
        
        Solo solo = solos.get(soloId);
        return solo.desbloquear();
    }
    
    // --- Métodos de Máquinas ---
    
    /**
     * Compra uma máquina e adiciona ao inventário.
     */
    public boolean comprarMaquina(Maquina maquina) {
        if (gastarDinheiro(maquina.getCusto())) {
            int quantidadeAtual = inventarioMaquinas.get(maquina);
            inventarioMaquinas.put(maquina, quantidadeAtual + 1);
            return true;
        }
        return false;
    }
    
    /**
     * Instala uma máquina em um solo específico.
     */
    public boolean instalarMaquina(int soloId, Maquina maquina) {
        // Verifica se tem a máquina no inventário
        int quantidadeDisponivel = inventarioMaquinas.get(maquina);
        if (quantidadeDisponivel <= 0) return false;
        
        // Verifica se o solo existe e está desbloqueado
        if (soloId < 0 || soloId >= solos.size()) return false;
        Solo solo = solos.get(soloId);
        if (!solo.isDesbloqueado()) return false;
        
        // Instala no solo
        solo.instalarMaquina(maquina);
        
        // Adiciona ao mapeamento
        maquinasInstaladas.get(soloId).add(maquina);
        
        // Remove do inventário
        inventarioMaquinas.put(maquina, quantidadeDisponivel - 1);
        
        return true;
    }
    
    /**
     * Remove uma máquina de um solo específico.
     */
    public boolean removerMaquina(int soloId, Maquina maquina) {
        // Verifica se o solo existe
        if (soloId < 0 || soloId >= solos.size()) return false;
        
        // Remove do solo
        solos.get(soloId).removerMaquina(maquina);
        
        // Remove do mapeamento
        boolean removido = maquinasInstaladas.get(soloId).remove(maquina);
        
        // Adiciona de volta ao inventário
        if (removido) {
            int quantidadeAtual = inventarioMaquinas.get(maquina);
            inventarioMaquinas.put(maquina, quantidadeAtual + 1);
        }
        
        return removido;
    }
    
    // --- Métodos de Compra de Animais ---
    
    /**
     * Compra um animal para um cercado específico.
     * O cercado deve ter espaço e a espécie deve corresponder.
     * @param cercadoId ID do cercado (0-2)
     * @return true se conseguiu comprar e adicionar o animal
     */
    public boolean comprarAnimal(int cercadoId) {
        if (cercadoId < 0 || cercadoId >= cercados.size()) {
            return false;
        }
        
        Cercado cercado = cercados.get(cercadoId);
        
        // Verifica se o cercado está cheio
        if (cercado.getQuantidade() >= cercado.getCapacidadeMaxima()) {
            return false;
        }
        
        // Obtém a espécie do cercado (se estiver vazio, não pode comprar)
        Animal especie = cercado.getEspecie();
        if (especie == null) {
            // Cercado vazio - não pode comprar sem definir a espécie primeiro
            return false;
        }
        
        // Verifica se tem dinheiro para comprar o animal
        double precoAnimal = especie.getPrecoCompra();
        if (!gastarDinheiro(precoAnimal)) {
            return false;
        }
        
        // Tenta adicionar o animal (mesma espécie)
        boolean sucesso = cercado.adicionarAnimal(especie);
        
        // Se falhar, devolve o dinheiro
        if (!sucesso) {
            ganharDinheiro(precoAnimal);
        }
        
        return sucesso;
    }
    
    /**
     * Adiciona um animal específico a um cercado (usado apenas na inicialização).
     * Este método é gratuito e não verifica custos.
     */
    public boolean adicionarAnimalInicial(int cercadoId, Animal animal) {
        if (cercadoId < 0 || cercadoId >= cercados.size()) {
            return false;
        }
        
        Cercado cercado = cercados.get(cercadoId);
        return cercado.adicionarAnimal(animal);
    }
    
    // --- Getters ---
    public double getDinheiro() { return dinheiro; }
    public int getDiaAtual() { return diaAtual; }
    public int getEstoqueFertilizante() { return estoqueFertilizante; }
    public List<Solo> getSolos() { return solos; }
    public List<Cercado> getCercados() { return cercados; }
    
    public Map<Maquina, Integer> getInventarioMaquinas() { 
        return new HashMap<>(inventarioMaquinas); 
    }
    
    public List<Maquina> getMaquinasInstaladas(int soloId) {
        if (soloId < 0 || soloId >= solos.size()) return new ArrayList<>();
        return new ArrayList<>(maquinasInstaladas.get(soloId));
    }
    
    public Vegetal getUltimoVegetalPlantado() { 
        return ultimoVegetalPlantado; 
    }
    
    public void setUltimoVegetalPlantado(Vegetal vegetal) { 
        this.ultimoVegetalPlantado = vegetal; 
    }
}