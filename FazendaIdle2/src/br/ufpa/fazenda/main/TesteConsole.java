package br.ufpa.fazenda.main;

import br.ufpa.fazenda.engine.GameLoop;
import br.ufpa.fazenda.engine.GerenciadorEventos;
import br.ufpa.fazenda.model.*;
import java.util.Scanner;

/**
 * Classe para testar a lógica SEM a interface gráfica.
 */
public class TesteConsole implements GerenciadorEventos {

    private GameLoop loop;
    private Scanner scanner;
    private boolean sair = false;
    
    public static void main(String[] args) {
        new TesteConsole().iniciar();
    }
    
    public void iniciar() {
        System.out.println("=== INICIANDO SIMULAÇÃO FAZENDA IDLE ===");
        
        // 1. Pega a fazenda
        FazendaEstado fazenda = FazendaEstado.getInstance();
        
        // Mostra estado inicial
        statusFazenda();
        
        // 2. Planta algo manualmente no Solo 0 para testar (se estiver desbloqueado)
        Solo solo0 = fazenda.getSolos().get(0);
        if (solo0.isDesbloqueado()) {
            System.out.println("\nPlantando Alface no Solo 1 (desbloqueado)...");
            solo0.plantar(Vegetal.ALFACE);
        }
        
        // 3. Inicia o GameLoop
        loop = new GameLoop(this); // 'this' é a própria classe TesteConsole
        loop.start();
        
        // 4. Menu de interação
        scanner = new Scanner(System.in);
        menuPrincipal();
        
        scanner.close();
    }
    
    private void menuPrincipal() {
        while (!sair) {
            System.out.println("\n=== MENU DE CONTROLE ===");
            System.out.println("1. Ativar/Desativar IA do Personagem");
            System.out.println("2. Status da IA");
            System.out.println("3. Status da Fazenda");
            System.out.println("4. Plantar manualmente (ou substituir)");
            System.out.println("5. Comprar fertilizante");
            System.out.println("6. Desbloquear solo");
            System.out.println("7. Comprar animal");
            System.out.println("8. Evoluir solo");
            System.out.println("9. Comprar e instalar máquina");
            System.out.println("10. Sair do menu (jogo continua rodando)");
            System.out.print("Escolha: ");
            
            try {
                int opcao = Integer.parseInt(scanner.nextLine());
                
                switch (opcao) {
                    case 1:
                        alternarIA();
                        break;
                    case 2:
                        statusIA();
                        break;
                    case 3:
                        statusFazenda();
                        break;
                    case 4:
                        plantarManualmente();
                        break;
                    case 5:
                        comprarFertilizante();
                        break;
                    case 6:
                        desbloquearSolo();
                        break;
                    case 7:
                        comprarAnimal();
                        break;
                    case 8:
                        evoluirSolo();
                        break;
                    case 9:
                        comprarEInstalarMaquina();
                        break;
                    case 10:
                        sair = true;
                        System.out.println("Saindo do menu. O jogo continua rodando...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido!");
            }
        }
    }
    
    private void alternarIA() {
        if (loop.isIAActivo()) {
            loop.desativarIA();
            System.out.println("IA DESATIVADA");
        } else {
            loop.ativarIA();
            System.out.println("IA ATIVADA");
        }
    }
    
    private void statusIA() {
        System.out.println("\n=== STATUS DA IA ===");
        System.out.println("Ativa: " + (loop.isIAActivo() ? "SIM" : "NÃO"));
        System.out.println("Status: " + loop.getPersonagemIA().getStatus());
        System.out.println("Tarefas pendentes: " + loop.getPersonagemIA().getTarefasPendentes());
    }
    
    private void statusFazenda() {
        FazendaEstado fazenda = FazendaEstado.getInstance();
        System.out.println("\n=== STATUS DA FAZENDA ===");
        System.out.printf("Dinheiro: R$ %.2f\n", fazenda.getDinheiro());
        System.out.println("Dia: " + fazenda.getDiaAtual());
        System.out.println("Estoque Fertilizante: " + fazenda.getEstoqueFertilizante());
        
        System.out.println("\n=== SOLOS ===");
        for (Solo solo : fazenda.getSolos()) {
            String status = solo.isDesbloqueado() ? "✅ DESBLOQUEADO" : "🔒 BLOQUEADO (R$300)";
            String maquinas = "";
            if (!solo.getMaquinasInstaladas().isEmpty()) {
                maquinas = " [Máquinas: ";
                for (Maquina maquina : solo.getMaquinasInstaladas()) {
                    maquinas += maquina.getNome().substring(0, 3) + " ";
                }
                maquinas += "]";
            }
            
            if (solo.isOcupado()) {
                System.out.printf("Solo %d: %s - %s (%.1f%%) Nível %d %s %s\n", 
                    solo.getId() + 1,
                    status,
                    solo.getVegetal().getNome(),
                    solo.getProgresso() * 100,
                    solo.getNivel(),
                    solo.isPronto() ? "[PRONTO]" : "",
                    maquinas);
            } else {
                System.out.printf("Solo %d: %s - Vazio (Nível %d) %s\n", 
                    solo.getId() + 1, status, solo.getNivel(), maquinas);
            }
        }
        
        System.out.println("\n=== CERCADOS ===");
        for (int i = 0; i < fazenda.getCercados().size(); i++) {
            var cercado = fazenda.getCercados().get(i);
            String especieNome = cercado.isVazio() ? "Vazio" : cercado.getEspecie().getNome();
            String produtoNome = cercado.isVazio() ? "" : cercado.getEspecie().getProduto();
            double preco = cercado.isVazio() ? 0 : cercado.getEspecie().getPrecoCompra();
            
            System.out.printf("Cercado %d: %s (%d/3) - Preço: R$%.2f %s %s\n",
                i + 1,
                especieNome,
                cercado.getQuantidade(),
                preco,
                cercado.isProdutoPronto() ? "[PRONTO]" : "",
                produtoNome
            );
        }
        
        System.out.println("\n=== INVENTÁRIO DE MÁQUINAS ===");
        var inventario = fazenda.getInventarioMaquinas();
        boolean temMaquinas = false;
        for (Maquina maquina : Maquina.values()) {
            int quantidade = inventario.get(maquina);
            if (quantidade > 0) {
                System.out.printf("- %s: %d unidade(s)\n", maquina.getNome(), quantidade);
                temMaquinas = true;
            }
        }
        if (!temMaquinas) {
            System.out.println("Nenhuma máquina no inventário.");
        }
    }
    
    private void plantarManualmente() {
        System.out.print("Número do solo (1-6): ");
        try {
            int soloId = Integer.parseInt(scanner.nextLine()) - 1;
            if (soloId < 0 || soloId >= 6) {
                System.out.println("Solo inválido!");
                return;
            }
            
            FazendaEstado fazenda = FazendaEstado.getInstance();
            Solo solo = fazenda.getSolos().get(soloId);
            
            if (!solo.isDesbloqueado()) {
                System.out.println("Este solo está bloqueado! Desbloqueie-o primeiro (opção 6).");
                return;
            }
            
            // Verifica se o solo está ocupado
            boolean soloOcupado = solo.isOcupado();
            if (soloOcupado) {
                System.out.printf("\nEste solo já está ocupado com %s (Progresso: %.1f%%).\n", 
                    solo.getVegetal().getNome(), solo.getProgresso() * 100);
                
                if (solo.isPronto()) {
                    System.out.println("A planta está PRONTA para colheita!");
                    System.out.print("Deseja colher antes de plantar novo vegetal? (S/N): ");
                    String resposta = scanner.nextLine().toUpperCase();
                    
                    if (resposta.equals("S") || resposta.equals("SIM")) {
                        double valorColheita = solo.colher();
                        fazenda.ganharDinheiro(valorColheita);
                        System.out.printf("Colhido! Ganhou R$%.2f.\n", valorColheita);
                        soloOcupado = false; // Agora está vazio
                    }
                }
                
                // Se ainda estiver ocupado, perguntar se quer substituir
                if (soloOcupado) {
                    System.out.print("Deseja arrancar a planta atual para plantar outra? (S/N): ");
                    String substituir = scanner.nextLine().toUpperCase();
                    
                    if (substituir.equals("S") || substituir.equals("SIM")) {
                        solo.arrancar();
                        System.out.println("Planta arrancada. Solo agora está vazio.");
                        soloOcupado = false;
                    } else {
                        System.out.println("Operação cancelada.");
                        return;
                    }
                }
            }
            
            // Agora plantamos (solo pode estar vazio ou foi esvaziado acima)
            
            // Mostra apenas vegetais disponíveis para o nível do solo
            System.out.println("\n=== VEGETAIS DISPONÍVEIS ===");
            System.out.println("(Baseado no nível do solo: " + solo.getNivel() + ")");
            int opcao = 1;
            
            for (Vegetal vegetal : Vegetal.values()) {
                if (vegetal.getNivelMinimo() <= solo.getNivel()) {
                    String disponivel = vegetal.getNivelMinimo() <= solo.getNivel() ? "✅" : "❌";
                    System.out.printf("%d. %s %s (Nível %d) - %d dias - Vende por: R$%.2f\n",
                        opcao++,
                        disponivel,
                        vegetal.getNome(),
                        vegetal.getNivelMinimo(),
                        vegetal.getDiasParaCrescer(),
                        vegetal.getValorVenda());
                }
            }
            
            if (opcao == 1) {
                System.out.println("Nenhum vegetal disponível para este nível de solo!");
                return;
            }
            
            System.out.print("\nEscolha o vegetal: ");
            int vegetalEscolha = Integer.parseInt(scanner.nextLine());
            
            // Mapear a escolha para o vegetal correto (considerando apenas os disponíveis)
            Vegetal vegetalSelecionado = null;
            int contador = 1;
            for (Vegetal vegetal : Vegetal.values()) {
                if (vegetal.getNivelMinimo() <= solo.getNivel()) {
                    if (contador == vegetalEscolha) {
                        vegetalSelecionado = vegetal;
                        break;
                    }
                    contador++;
                }
            }
            
            if (vegetalSelecionado == null) {
                System.out.println("Vegetal inválido!");
                return;
            }
            
            // Tenta plantar (agora o solo deve estar vazio)
            boolean plantou = solo.plantar(vegetalSelecionado);
            if (plantou) {
                fazenda.setUltimoVegetalPlantado(vegetalSelecionado);
                System.out.println("\n✅ " + vegetalSelecionado.getNome() + 
                                 " plantado no Solo " + (soloId + 1) + 
                                 " (Nível " + solo.getNivel() + ")");
                System.out.println("Tempo estimado: " + vegetalSelecionado.getDiasParaCrescer() + " dias do jogo.");
                
                // Perguntar se quer ativar fertilizante
                if (fazenda.getEstoqueFertilizante() > 0) {
                    System.out.print("Ativar fertilizante para este solo? (S/N): ");
                    String ativarFert = scanner.nextLine().toUpperCase();
                    if (ativarFert.equals("S") || ativarFert.equals("SIM")) {
                        solo.setFertilizanteAtivado(true);
                        System.out.println("Fertilizante ativado para este solo!");
                    }
                }
            } else {
                System.out.println("Falha ao plantar. O solo pode estar ocupado.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Digite um número válido!");
        }
    }
    
    private void comprarFertilizante() {
        FazendaEstado fazenda = FazendaEstado.getInstance();
        double custo = 150.0;
        int quantidade = 10;
        
        System.out.printf("\n=== COMPRAR FERTILIZANTE ===\n");
        System.out.printf("Preço: R$%.2f por lote de %d aplicações\n", custo, quantidade);
        System.out.printf("Seu dinheiro: R$%.2f\n", fazenda.getDinheiro());
        
        if (fazenda.getDinheiro() >= custo) {
            System.out.print("Confirmar compra? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();
            
            if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                fazenda.comprarFertilizante();
                System.out.println("✅ Fertilizante comprado! Estoque: " + 
                                 fazenda.getEstoqueFertilizante() + " aplicações");
            } else {
                System.out.println("Compra cancelada.");
            }
        } else {
            System.out.println("❌ Dinheiro insuficiente! Necessário: R$" + custo);
        }
    }
    
    private void desbloquearSolo() {
        System.out.print("Número do solo para desbloquear (4-6): ");
        try {
            int soloId = Integer.parseInt(scanner.nextLine()) - 1;
            
            // Só pode desbloquear solos 3, 4, 5 (que correspondem aos IDs 3, 4, 5)
            if (soloId < 3 || soloId >= 6) {
                System.out.println("Só é possível desbloquear solos 4, 5 ou 6!");
                return;
            }
            
            FazendaEstado fazenda = FazendaEstado.getInstance();
            Solo solo = fazenda.getSolos().get(soloId);
            
            if (solo.isDesbloqueado()) {
                System.out.println("Este solo já está desbloqueado!");
                return;
            }
            
            double custo = 300.0;
            System.out.printf("\n=== DESBLOQUEAR SOLO %d ===\n", soloId + 1);
            System.out.printf("Custo: R$%.2f\n", custo);
            System.out.printf("Seu dinheiro: R$%.2f\n", fazenda.getDinheiro());
            System.out.print("Confirmar desbloqueio? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();
            
            if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                boolean sucesso = loop.desbloquearSolo(soloId);
                if (sucesso) {
                    System.out.println("✅ Solo " + (soloId + 1) + " desbloqueado com sucesso!");
                } else {
                    System.out.println("❌ Falha ao desbloquear solo. Verifique se tem dinheiro suficiente.");
                }
            } else {
                System.out.println("Desbloqueio cancelado.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Digite um número válido!");
        }
    }
    
    private void comprarAnimal() {
        System.out.print("Número do cercado (1-3): ");
        try {
            int cercadoId = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (cercadoId < 0 || cercadoId >= 3) {
                System.out.println("Cercado inválido! Escolha entre 1 e 3.");
                return;
            }
            
            FazendaEstado fazenda = FazendaEstado.getInstance();
            var cercado = fazenda.getCercados().get(cercadoId);
            
            if (cercado.getQuantidade() >= 3) {
                System.out.println("❌ Este cercado já está cheio! (3/3 animais)");
                return;
            }
            
            Animal especie = cercado.getEspecie();
            double preco = especie.getPrecoCompra();
            
            System.out.printf("\n=== COMPRAR ANIMAL ===\n");
            System.out.printf("Espécie: %s\n", especie.getNome());
            System.out.printf("Preço: R$%.2f\n", preco);
            System.out.printf("Capacidade atual: %d/3\n", cercado.getQuantidade());
            System.out.printf("Seu dinheiro: R$%.2f\n", fazenda.getDinheiro());
            System.out.print("Confirmar compra? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();
            
            if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                boolean sucesso = loop.comprarAnimal(cercadoId);
                if (sucesso) {
                    System.out.println("✅ Animal comprado com sucesso!");
                    System.out.printf("Cercado %d agora tem %d/3 %s\n", 
                        cercadoId + 1, cercado.getQuantidade(), especie.getNome());
                } else {
                    System.out.println("❌ Falha ao comprar animal. Verifique o dinheiro.");
                }
            } else {
                System.out.println("Compra cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Digite um número válido!");
        }
    }
    
    private void evoluirSolo() {
        System.out.print("Número do solo para evoluir (1-6): ");
        try {
            int soloId = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (soloId < 0 || soloId >= 6) {
                System.out.println("Solo inválido! Escolha entre 1 e 6.");
                return;
            }
            
            FazendaEstado fazenda = FazendaEstado.getInstance();
            Solo solo = fazenda.getSolos().get(soloId);
            
            if (!solo.isDesbloqueado()) {
                System.out.println("Este solo está bloqueado! Desbloqueie-o primeiro.");
                return;
            }
            
            if (solo.getNivel() >= 10) {
                System.out.println("Este solo já está no nível máximo (10)!");
                return;
            }
            
            double custoUpgrade = 100.0 * solo.getNivel();
            int novoNivel = solo.getNivel() + 1;
            
            System.out.printf("\n=== EVOLUIR SOLO %d ===\n", soloId + 1);
            System.out.printf("Nível atual: %d\n", solo.getNivel());
            System.out.printf("Novo nível: %d\n", novoNivel);
            System.out.printf("Custo: R$%.2f\n", custoUpgrade);
            System.out.printf("Seu dinheiro: R$%.2f\n", fazenda.getDinheiro());
            System.out.println("\nBenefícios do nível " + novoNivel + ":");
            System.out.printf("- +%.0f%% valor de venda\n", (novoNivel - 1) * 20.0);
            System.out.printf("- +%.0f%% velocidade de crescimento\n", (novoNivel - 1) * 10.0);
            
            // Mostra vegetais que serão desbloqueados
            System.out.println("\nVegetais que serão desbloqueados:");
            for (Vegetal vegetal : Vegetal.values()) {
                if (vegetal.getNivelMinimo() == novoNivel) {
                    System.out.printf("- %s (Vende por: R$%.2f, %d dias)\n",
                        vegetal.getNome(), vegetal.getValorVenda(), vegetal.getDiasParaCrescer());
                }
            }
            
            System.out.print("\nConfirmar evolução? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();
            
            if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                boolean sucesso = solo.upgrade();
                if (sucesso) {
                    System.out.printf("✅ Solo %d evoluído para nível %d!\n", soloId + 1, solo.getNivel());
                    
                    // Mostra vegetais agora disponíveis
                    System.out.println("\nVegetais agora disponíveis neste solo:");
                    for (Vegetal vegetal : Vegetal.values()) {
                        if (vegetal.getNivelMinimo() <= solo.getNivel()) {
                            System.out.printf("- %s (Nível %d)\n", vegetal.getNome(), vegetal.getNivelMinimo());
                        }
                    }
                } else {
                    System.out.printf("❌ Falha ao evoluir solo. Verifique se tem dinheiro suficiente (R$%.2f).\n", custoUpgrade);
                }
            } else {
                System.out.println("Evolução cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Digite um número válido!");
        }
    }
    
    private void comprarEInstalarMaquina() {
        try {
            // 1. Mostrar máquinas disponíveis para compra
            System.out.println("\n=== MÁQUINAS DISPONÍVEIS ===");
            int opcao = 1;
            for (Maquina maquina : Maquina.values()) {
                System.out.printf("%d. %s - R$%.2f\n", 
                    opcao++, 
                    maquina.getNome(), 
                    maquina.getCusto());
                System.out.println("   " + maquina.getDescricao());
                System.out.println();
            }
            
            System.out.print("Escolha a máquina: ");
            int maquinaEscolha = Integer.parseInt(scanner.nextLine());
            
            Maquina maquinaSelecionada = null;
            switch (maquinaEscolha) {
                case 1: maquinaSelecionada = Maquina.TRATOR; break;
                case 2: maquinaSelecionada = Maquina.ARADOR; break;
                case 3: maquinaSelecionada = Maquina.IRRIGADOR; break;
                default:
                    System.out.println("Máquina inválida!");
                    return;
            }
            
            // 2. Escolher solo para instalar
            System.out.println("\n=== SOLOS DISPONÍVEIS ===");
            FazendaEstado fazenda = FazendaEstado.getInstance();
            for (int i = 0; i < 6; i++) {
                Solo solo = fazenda.getSolos().get(i);
                String status = solo.isDesbloqueado() ? "✅" : "🔒";
                String ocupado = solo.isOcupado() ? "(Ocupado)" : "(Vazio)";
                System.out.printf("%d. Solo %d %s Nível %d %s\n",
                    i + 1, i + 1, status, solo.getNivel(), ocupado);
            }
            
            System.out.print("\nNúmero do solo para instalar a máquina (1-6): ");
            int soloId = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (soloId < 0 || soloId >= 6) {
                System.out.println("Solo inválido!");
                return;
            }
            
            Solo solo = fazenda.getSolos().get(soloId);
            
            if (!solo.isDesbloqueado()) {
                System.out.println("Este solo está bloqueado! Desbloqueie-o primeiro.");
                return;
            }
            
            // Verificar se já tem essa máquina instalada
            if (solo.temMaquina(maquinaSelecionada)) {
                System.out.println("Este solo já tem essa máquina instalada!");
                return;
            }
            
            // Mostrar custo e confirmar
            System.out.printf("\n=== CONFIRMAR COMPRA ===\n");
            System.out.printf("Máquina: %s\n", maquinaSelecionada.getNome());
            System.out.printf("Solo: %d (Nível %d)\n", soloId + 1, solo.getNivel());
            System.out.printf("Custo total: R$%.2f\n", maquinaSelecionada.getCusto());
            System.out.printf("Seu dinheiro: R$%.2f\n", fazenda.getDinheiro());
            System.out.print("\nConfirmar compra e instalação? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();
            
            if (confirmacao.equals("S") || confirmacao.equals("SIM")) {
                // Primeiro compra a máquina
                boolean compraSucesso = fazenda.comprarMaquina(maquinaSelecionada);
                
                if (!compraSucesso) {
                    System.out.printf("❌ Falha ao comprar máquina. Dinheiro insuficiente (R$%.2f).\n", 
                                     maquinaSelecionada.getCusto());
                    return;
                }
                
                // Depois instala no solo
                boolean instalacaoSucesso = fazenda.instalarMaquina(soloId, maquinaSelecionada);
                
                if (instalacaoSucesso) {
                    System.out.printf("\n✅ %s instalada com sucesso no Solo %d!\n", 
                                     maquinaSelecionada.getNome(), soloId + 1);
                    
                    // Mostrar efeitos da máquina
                    System.out.println("\nEFEITOS ATIVADOS:");
                    switch (maquinaSelecionada) {
                        case TRATOR:
                            System.out.println("- Colhe automaticamente quando a planta está pronta");
                            System.out.println("- Vende automaticamente pelo valor máximo");
                            break;
                        case ARADOR:
                            System.out.println("- Planta automaticamente quando o solo está vazio");
                            System.out.println("- Usa o último vegetal plantado na fazenda");
                            break;
                        case IRRIGADOR:
                            System.out.println("- +25% no valor de venda das plantas");
                            System.out.println("- -15% no tempo de crescimento");
                            break;
                    }
                    
                    // Mostrar inventário atualizado
                    System.out.println("\nInventário de máquinas atualizado:");
                    var inventario = fazenda.getInventarioMaquinas();
                    for (Maquina m : Maquina.values()) {
                        int qtd = inventario.get(m);
                        if (qtd > 0) {
                            System.out.printf("- %s: %d\n", m.getNome(), qtd);
                        }
                    }
                } else {
                    System.out.println("❌ Falha ao instalar máquina.");
                }
            } else {
                System.out.println("Compra cancelada.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Digite um número válido!");
        }
    }

    // --- MÉTODOS DO GERENCIADOR DE EVENTOS ---

    @Override
    public void aoAtualizarStatusFazenda(double dinheiro, int dia, int estoqueFert) {
        // Para não poluir o console
    }

    @Override
    public void aoAtualizarSolo(Solo solo) {
        if (solo.isOcupado() && solo.getProgresso() >= 1.0) {
            System.out.printf("[SISTEMA] Solo %d: %s PRONTO para colheita!\n", 
                solo.getId() + 1,
                solo.getVegetal().getNome());
        }
    }

    @Override
    public void aoNotificarEvento(String mensagem) {
        System.out.println("[EVENTO]: " + mensagem);
    }
}