/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientk;

/**
 *
 * @author zeysu
 */
/**
 * Sunucudan gelen oyun olaylarını dinlemek için arayüz
 */
/**
 * Sunucudan gelen oyun olaylarını dinlemek için arayüz
 */
public interface NetworkGameListener {
    
    /**
     * Oyun başladığında çağrılır
     * @param player1Id Birinci oyuncu ID
     * @param player2Id İkinci oyuncu ID
     * @param isWhitePlayer Bu oyuncu beyaz mı?
     */
    void onGameStart(int player1Id, int player2Id, boolean isWhitePlayer);
    
    /**
     * Zar atıldığında çağrılır
     * @param dice1 Birinci zar
     * @param dice2 İkinci zar
     */
    void onDiceRoll(int dice1, int dice2);
    
    /**
     * Taş hareketi yapıldığında çağrılır
     * @param playerId Hareketi yapan oyuncu ID
     * @param fromTriangle Başlangıç üçgeni
     * @param toTriangle Hedef üçgen
     */
    void onPieceMove(int playerId, int fromTriangle, int toTriangle);
    
    /**
     * Sıra değiştiğinde çağrılır
     * @param currentPlayerId Şu anki oyuncu ID
     * @param isMyTurn Bu oyuncunun sırası mı?
     */
    void onTurnChange(int currentPlayerId, boolean isMyTurn);
    
    /**
     * Oyun bittiğinde çağrılır
     * @param reason Oyun bitiş sebebi
     */
    void onGameEnd(String reason);
    
    /**
     * Sunucu bağlantısı kesildiğinde çağrılır
     */
    void onDisconnected();
    
    /**
     * Hata oluştuğunda çağrılır
     * @param error Hata mesajı
     */
    void onError(String error);
}