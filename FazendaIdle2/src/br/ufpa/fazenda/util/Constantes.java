package br.ufpa.fazenda.util;

/**
 * Armazena valores fixos para facilitar o balanceamento do jogo.
 */
public class Constantes {
    
    // Tempo
    public static final int SEGUNDOS_POR_DIA = 15;
    
    // Economia
    public static final double CUSTO_FERTILIZANTE_LOTE = 150.0;
    public static final int QTD_FERTILIZANTE_LOTE = 10;
    
    // Bônus de Máquinas e Itens
    public static final double BONUS_IRRIGADOR_TEMPO = 0.15; // Reduz 15% do tempo
    public static final double BONUS_IRRIGADOR_VALOR = 0.25; // Aumenta 25% o valor
    
    public static final double BONUS_FERTILIZANTE_TEMPO = 0.40; // Reduz 40% do tempo
    public static final double BONUS_FERTILIZANTE_VALOR = 0.50; // Aumenta 50% o valor
    
    public static final double BONUS_SOLO_NV_VALOR = 0.20; // +20% valor por nível do solo
    public static final double BONUS_SOLO_NV_CRESCIMENTO = 0.10; // +10% velocidade por nível
}