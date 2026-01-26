package br.ufpa.fazenda.engine;

import br.ufpa.fazenda.controller.PersonagemIA;
import br.ufpa.fazenda.model.FazendaEstado;
import br.ufpa.fazenda.model.Maquina;
import br.ufpa.fazenda.model.Solo;
import br.ufpa.fazenda.model.Vegetal;
import br.ufpa.fazenda.model.Cercado;
import br.ufpa.fazenda.model.Animal;
import br.ufpa.fazenda.util.Constantes;

public class GameLoop extends Thread {
    
    private boolean rodando = true;
    private final FazendaEstado fazenda;
    private GerenciadorEventos ouvinte; // A tela da Enya
    
    // Controle de tempo
    private long ultimaAtualizacao;
    private double acumuladorTempoDia = 0.0;
    
    // Controle de tempo para animais
    private double acumuladorTempoAnimal = 0.0;
    private static final double INTERVALO_ANIMAL = 0.5; // Atualizar animais a cada 0.5s
    
    // Sistema de IA do Personagem
    private PersonagemIA personagemIA;
    private boolean modoIAActivo = false;
    
    public GameLoop(GerenciadorEventos ouvinte) {
        this.fazenda = FazendaEstado.getInstance();
        this.ouvinte = ouvinte;
        this.personagemIA = new PersonagemIA(fazenda);
        this.ultimaAtualizacao = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        while (rodando) {
            long agora = System.currentTimeMillis();
            double deltaSegundos = (agora - ultimaAtualizacao) / 1000.0;
            ultimaAtualizacao = agora;
            
            atualizarJogo(deltaSegundos);
            
            try {
                // Dorme um pouco para não fritar o processador (aprox. 60 FPS)
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void atualizarJogo(double delta) {
        // 1. Atualizar Ciclo do Dia
        acumuladorTempoDia += delta;
        if (acumuladorTempoDia >= Constantes.SEGUNDOS_POR_DIA) {
            acumuladorTempoDia = 0;
            fazenda.avancarDia();
            // Notifica a tela que o dia mudou
            if (ouvinte != null) {
                ouvinte.aoNotificarEvento("Um novo dia começou! (Dia " + fazenda.getDiaAtual() + ")" +
                                          " Custo de manutenção aplicado.");
            }
        }
        
        // 2. Atualizar cada Solo (Crescimento e Automação) - apenas solos desbloqueados
        for (Solo solo : fazenda.getSolos()) {
            // Ignorar solos bloqueados
            if (!solo.isDesbloqueado()) continue;
            
            boolean houveMudanca = false;
            
            // A. Crescimento
            if (solo.isOcupado() && !solo.isPronto()) {
                solo.atualizarTempo(delta);
                houveMudanca = true; // O progresso mudou, precisa redesenhar
            }
            
            // B. Automação (A mágica dos robôs)
            if (processarAutomacao(solo)) {
                houveMudanca = true;
            }
            
            // Se algo mudou, avisa a tela para redesenhar ESTE solo
            if (houveMudanca && ouvinte != null) {
                ouvinte.aoAtualizarSolo(solo);
            }
        }
        
        // 3. Atualizar Animais (com intervalo controlado)
        acumuladorTempoAnimal += delta;
        if (acumuladorTempoAnimal >= INTERVALO_ANIMAL) {
            acumuladorTempoAnimal = 0;
            for (Cercado cercado : fazenda.getCercados()) {
                cercado.atualizarTempo(INTERVALO_ANIMAL);
                if (cercado.isProdutoPronto() && ouvinte != null) {
                    ouvinte.aoNotificarEvento(cercado.getEspecie().getNome() + 
                                            " no cercado " + (cercado.getId() + 1) + 
                                            " produziu " + cercado.getEspecie().getProduto() + "!");
                }
            }
        }
        
        // 4. Atualizar PersonagemIA (se estiver ativo)
        if (modoIAActivo) {
            personagemIA.atualizar(delta);
        }
        
        // 5. Atualizar HUD Geral (sempre bom garantir)
        if (ouvinte != null) {
            ouvinte.aoAtualizarStatusFazenda(
                fazenda.getDinheiro(), 
                fazenda.getDiaAtual(), 
                fazenda.getEstoqueFertilizante()
            );
        }
    }
    
    /**
     * Lógica dos Tratores e Aradores.
     * Retorna TRUE se alguma ação foi feita.
     */
    private boolean processarAutomacao(Solo solo) {
        boolean agiu = false;
        
        // TRATOR: Colhe se estiver pronto
        if (solo.temMaquina(Maquina.TRATOR) && solo.isPronto()) {
            double valorColheita = solo.colher(); // Já inclui todos os bônus
            
            fazenda.ganharDinheiro(valorColheita);
            agiu = true;
            
            if (ouvinte != null) {
                Vegetal vegetalColhido = solo.getVegetal(); // Pega antes de limpar
                if (vegetalColhido != null) {
                    ouvinte.aoNotificarEvento("Trator vendeu " + vegetalColhido.getNome() + 
                                            " por R$" + String.format("%.2f", valorColheita));
                }
            }
        }
        
        // ARADOR: Planta se estiver vazio
        if (solo.temMaquina(Maquina.ARADOR) && !solo.isOcupado()) {
            // Usa o último vegetal plantado na fazenda
            Vegetal paraPlantar = fazenda.getUltimoVegetalPlantado();
            
            // Verifica se tem dinheiro para o vegetal (simplificado)
            // Em um sistema mais complexo, teríamos custo de sementes
            if (paraPlantar != null) {
                // Aplica fertilizante se estiver ativado e houver estoque
                if (solo.isFertilizanteAtivado() && fazenda.getEstoqueFertilizante() > 0) {
                    solo.aplicarFertilizante();
                }
                
                solo.plantar(paraPlantar);
                agiu = true;
                
                if (ouvinte != null) {
                    ouvinte.aoNotificarEvento("Arador plantou " + paraPlantar.getNome() + 
                                            " no Solo " + (solo.getId() + 1));
                }
            }
        }
        
        return agiu;
    }
    
    /**
     * Método para coletar produtos dos animais manualmente (chamado pela interface)
     */
    public double coletarProdutosAnimais(int cercadoId) {
        if (cercadoId < 0 || cercadoId >= fazenda.getCercados().size()) {
            return 0.0;
        }
        
        Cercado cercado = fazenda.getCercados().get(cercadoId);
        double valor = cercado.coletarProdutos();
        
        if (valor > 0) {
            fazenda.ganharDinheiro(valor);
            if (ouvinte != null) {
                ouvinte.aoNotificarEvento("Coletou " + cercado.getEspecie().getProduto() + 
                                        " do cercado " + (cercadoId + 1) + 
                                        " por R$" + String.format("%.2f", valor));
            }
        }
        
        return valor;
    }
    
    /**
     * Método para adicionar animal manualmente (chamado pela interface)
     * NOTA: Este método é usado apenas para adição gratuita (inicialização).
     * Para compra, use comprarAnimal().
     */
    public boolean adicionarAnimal(int cercadoId, Animal animal) {
        if (cercadoId < 0 || cercadoId >= fazenda.getCercados().size()) {
            return false;
        }
        
        Cercado cercado = fazenda.getCercados().get(cercadoId);
        boolean sucesso = cercado.adicionarAnimal(animal);
        
        if (sucesso && ouvinte != null) {
            ouvinte.aoNotificarEvento("Adicionou " + animal.getNome() + 
                                    " ao cercado " + (cercadoId + 1));
        }
        
        return sucesso;
    }
    
    /**
     * Método para comprar um animal (com custo)
     */
    public boolean comprarAnimal(int cercadoId) {
        if (cercadoId < 0 || cercadoId >= fazenda.getCercados().size()) {
            return false;
        }
        
        boolean sucesso = fazenda.comprarAnimal(cercadoId);
        
        if (sucesso && ouvinte != null) {
            Cercado cercado = fazenda.getCercados().get(cercadoId);
            double preco = cercado.getEspecie().getPrecoCompra();
            int quantidade = cercado.getQuantidade();
            
            ouvinte.aoNotificarEvento("Comprou " + cercado.getEspecie().getNome() + 
                                    " para cercado " + (cercadoId + 1) + 
                                    " por R$" + String.format("%.2f", preco) + 
                                    " (Total: " + quantidade + "/3)");
        }
        
        return sucesso;
    }
    
    /**
     * Método para desbloquear um solo
     */
    public boolean desbloquearSolo(int soloId) {
        if (soloId < 0 || soloId >= fazenda.getSolos().size()) {
            return false;
        }
        
        boolean sucesso = fazenda.desbloquearSolo(soloId);
        
        if (sucesso && ouvinte != null) {
            ouvinte.aoNotificarEvento("Solo " + (soloId + 1) + " desbloqueado por R$300!");
        }
        
        return sucesso;
    }
    
    /**
     * Método para ativar a IA do Personagem
     */
    public void ativarIA() {
        modoIAActivo = true;
        personagemIA.ativar();
        if (ouvinte != null) {
            ouvinte.aoNotificarEvento("Modo IA do Personagem ATIVADO");
        }
    }
    
    /**
     * Método para desativar a IA do Personagem
     */
    public void desativarIA() {
        modoIAActivo = false;
        personagemIA.desativar();
        if (ouvinte != null) {
            ouvinte.aoNotificarEvento("Modo IA do Personagem DESATIVADO");
        }
    }
    
    /**
     * Verifica se a IA está ativa
     */
    public boolean isIAActivo() {
        return modoIAActivo;
    }
    
    /**
     * Retorna a instância da PersonagemIA
     */
    public PersonagemIA getPersonagemIA() {
        return personagemIA;
    }
    
    public void parar() {
        this.rodando = false;
    }
}