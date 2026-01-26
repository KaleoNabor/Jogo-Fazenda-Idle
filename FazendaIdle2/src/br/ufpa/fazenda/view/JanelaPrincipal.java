package br.ufpa.fazenda.view;

import br.ufpa.fazenda.engine.GerenciadorEventos;
import br.ufpa.fazenda.model.Solo;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;

/**
 * A Tela Principal do jogo.
 * Ela "assina o contrato" (implements) do GerenciadorEventos para receber avisos do motor.
 */
public class JanelaPrincipal extends JFrame implements GerenciadorEventos {

    private JLabel labelInfo; // Só para teste
    
    public JanelaPrincipal() {
        // 1. Configurações Básicas da Janela
        setTitle("Fazenda Idle 2.0 - UFPA");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza na tela
        setLayout(new BorderLayout());
        
        // 2. Elementos de Teste (A Enya vai substituir isso pelos Paineis Reais)
        labelInfo = new JLabel("Carregando fazenda...");
        labelInfo.setHorizontalAlignment(JLabel.CENTER);
        add(labelInfo, BorderLayout.CENTER);
        
        // Aqui ela vai adicionar:
        // add(new PainelHUD(), BorderLayout.NORTH);
        // add(new PainelFazenda(), BorderLayout.CENTER);
    }

    // --- MÉTODOS QUE O SEU MOTOR VAI CHAMAR AUTOMATICAMENTE ---

    @Override
    public void aoAtualizarStatusFazenda(double dinheiro, int dia, int estoqueFert) {
        // Exemplo de atualização visual
        labelInfo.setText(String.format("Dia: %d | R$ %.2f | Fertilizante: %d", dia, dinheiro, estoqueFert));
        repaint(); // Força o redesenho da tela
    }

    @Override
    public void aoAtualizarSolo(Solo solo) {
        // A Enya vai repassar esse aviso para o PainelFazenda
        // painelFazenda.repaint();
    }

    @Override
    public void aoNotificarEvento(String mensagem) {
        System.out.println("GUI Diz: " + mensagem);
    }
}