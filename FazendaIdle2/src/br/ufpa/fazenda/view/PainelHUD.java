package br.ufpa.fazenda.view;

import javax.swing.*;
import java.awt.*;

/**
 * Painel do HUD (Heads-Up Display) que mostra informações do jogo:
 * - Dinheiro
 * - Dia atual
 * - Estoque de fertilizante
 * - Botões de controle
 */
public class PainelHUD extends JPanel {
    
    private JLabel labelDinheiro;
    private JLabel labelDia;
    private JLabel labelFertilizante;
    private JButton botaoIA;
    
    public PainelHUD() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        setBackground(new Color(240, 240, 200));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        
        // Componentes
        labelDinheiro = new JLabel("R$ 500,00");
        labelDinheiro.setFont(new Font("Arial", Font.BOLD, 16));
        labelDinheiro.setForeground(new Color(0, 100, 0));
        
        labelDia = new JLabel("Dia: 1");
        labelDia.setFont(new Font("Arial", Font.PLAIN, 14));
        
        labelFertilizante = new JLabel("Fertilizante: 0");
        labelFertilizante.setFont(new Font("Arial", Font.PLAIN, 14));
        
        botaoIA = new JButton("IA: DESLIGADO");
        botaoIA.setBackground(Color.RED);
        botaoIA.setForeground(Color.WHITE);
        
        // Adiciona componentes
        add(new JLabel("💰"));
        add(labelDinheiro);
        add(Box.createHorizontalStrut(20));
        add(new JLabel("📅"));
        add(labelDia);
        add(Box.createHorizontalStrut(20));
        add(new JLabel("🌱"));
        add(labelFertilizante);
        add(Box.createHorizontalStrut(20));
        add(botaoIA);
    }
    
    public void atualizar(double dinheiro, int dia, int estoqueFert) {
        labelDinheiro.setText(String.format("R$ %.2f", dinheiro));
        labelDia.setText("Dia: " + dia);
        labelFertilizante.setText("Fertilizante: " + estoqueFert);
    }
    
    public JButton getBotaoIA() {
        return botaoIA;
    }
    
    public void setEstadoIA(boolean ativa) {
        if (ativa) {
            botaoIA.setText("IA: LIGADO");
            botaoIA.setBackground(new Color(0, 150, 0));
        } else {
            botaoIA.setText("IA: DESLIGADO");
            botaoIA.setBackground(Color.RED);
        }
    }
}