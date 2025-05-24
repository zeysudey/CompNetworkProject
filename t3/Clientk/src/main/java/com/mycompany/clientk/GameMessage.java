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
 * Oyun mesajları için protokol sınıfı
 */
public class GameMessage {
    public enum Type {
        NONE,
        PLAYER_JOINED,      // Oyuncu katıldı
        GAME_START,         // Oyun başladı
        DICE_ROLL,          // Zar atıldı
        PIECE_MOVE,         // Taş hareketi
        TURN_CHANGE,        // Sıra değişti
        GAME_END,           // Oyun bitti
        PLAYER_LEFT,        // Oyuncu ayrıldı
        BOARD_STATE,        // Oyun tahtası durumu
        ERROR_MSG           // Hata mesajı
    }
    
    /**
     * Mesaj oluşturur
     * @param type Mesaj tipi
     * @param data Mesaj verisi
     * @return Formatlanmış mesaj
     */
    public static String GenerateMsg(GameMessage.Type type, String data) {
        return " " + type + "#" + data;
    }
    
    /**
     * Zar atma mesajı oluşturur
     * @param dice1 Birinci zar
     * @param dice2 İkinci zar
     * @param playerId Oyuncu ID
     * @return Zar mesajı
     */
    public static String CreateDiceMessage(int dice1, int dice2, int playerId) {
        return GenerateMsg(Type.DICE_ROLL, playerId + "," + dice1 + "," + dice2);
    }
    
    /**
     * Taş hareketi mesajı oluşturur
     * @param playerId Oyuncu ID
     * @param fromTriangle Başlangıç üçgeni
     * @param toTriangle Hedef üçgen
     * @return Hareket mesajı
     */
    public static String CreateMoveMessage(int playerId, int fromTriangle, int toTriangle) {
        return GenerateMsg(Type.PIECE_MOVE, playerId + "," + fromTriangle + "," + toTriangle);
    }
    
    /**
     * Sıra değişim mesajı oluşturur
     * @param currentPlayerId Şu anki oyuncu ID
     * @return Sıra mesajı
     */
    public static String CreateTurnMessage(int currentPlayerId) {
        return GenerateMsg(Type.TURN_CHANGE, String.valueOf(currentPlayerId));
    }
    
    /**
     * Oyun başlama mesajı oluşturur
     * @param player1Id Beyaz oyuncu ID
     * @param player2Id Siyah oyuncu ID
     * @return Oyun başlama mesajı
     */
    public static String CreateGameStartMessage(int player1Id, int player2Id) {
        return GenerateMsg(Type.GAME_START, player1Id + "," + player2Id);
    }
    
    /**
     * Oyun bitişi mesajı oluşturur
     * @param winnerId Kazanan oyuncu ID
     * @return Oyun bitişi mesajı
     */
    public static String CreateGameEndMessage(int winnerId) {
        return GenerateMsg(Type.GAME_END, String.valueOf(winnerId));
    }
}
