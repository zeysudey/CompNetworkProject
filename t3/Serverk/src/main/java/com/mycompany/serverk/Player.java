package com.mycompany.serverk;

/**
 * Backgammon oyunu için oyuncu arabirimi
 * @author zeysu
 */
public interface Player {
    /**
     * Oyuncu ID'sini döndürür
     * @return oyuncu ID'si
     */
    int getPlayerId();
    
    /**
     * Oyuncunun beyaz taşları kontrol edip etmediğini belirtir
     * @return beyaz ise true, siyah ise false
     */
    boolean isWhitePlayer();
    
    /**
     * Oyuncu adını döndürür
     * @return oyuncu adı
     */
    String getPlayerName();
    
} 