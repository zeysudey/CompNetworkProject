/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverk;

/**
 *
 * @author zeysu
 */
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;



/**
 * İki oyuncu arasındaki oyun odasını yönetir
 */
public class GameRoom {
    private int roomId;
    private GameClient player1; // Beyaz oyuncu
    private GameClient player2; // Siyah oyuncu
    private GameClient currentPlayer;
    private GameServer server;
    private boolean gameStarted;
    private boolean gameEnded;
    private int[] lastDiceRoll = new int[2];
    private boolean diceRolled;
    private int readyPlayerCount = 0;
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private List<Integer> remainingDice = new ArrayList<>(); // O turda kullanılacak zarlar
    private int movesLeft = 0; // O turda kalan hamle sayısı
    private List<String> usedPieceDiceCombos = new ArrayList<>(); // Oynanan taş-zar kombinasyonları (her tur için)
    
    public GameRoom(int roomId, GameClient player1, GameClient player2, GameServer server) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        
        // Oyuncuları bu odaya ata
        player1.setGameRoom(this);
        player2.setGameRoom(this);
        
        // Rastgele renk atama (garantili ve loglu)
        boolean player1White = Math.random() < 0.5;
        player1.setWhitePlayer(player1White);
        player2.setWhitePlayer(!player1White);
        System.out.println("[ROOM " + roomId + "] Oda oluşturuldu: " + player1.getPlayerName() + " (ID: " + player1.getPlayerId() + ") ve " + player2.getPlayerName() + " (ID: " + player2.getPlayerId() + ")");
        System.out.println("[ROOM " + roomId + "] Renkler: " + player1.getPlayerName() + "=" + (player1White ? "BEYAZ" : "SİYAH") + ", " + player2.getPlayerName() + "=" + (!player1White ? "BEYAZ" : "SİYAH"));
        
        // Beyaz oyuncu başlar
        this.currentPlayer = player1.isWhitePlayer() ? player1 : player2;
        this.gameStarted = false;
        this.gameEnded = false;
        this.diceRolled = false;
        this.readyPlayerCount = 0;
        this.player1Ready = false;
        this.player2Ready = false;
        
        System.out.println("🏠 GameRoom oluşturuldu - ID: " + roomId + 
                          " | Player1: " + player1.getPlayerName() + " (" + (player1.isWhitePlayer() ? "BEYAZ" : "SİYAH") + ")" +
                          " | Player2: " + player2.getPlayerName() + " (" + (player2.isWhitePlayer() ? "BEYAZ" : "SİYAH") + ")" +
                          " | İlk sıra: " + currentPlayer.getPlayerName());
        
        try {
            Thread.sleep(500); // Thread'ler başlasın diye bekle
            String player1Message = "GAME_START#" + player1.getPlayerId() + "," + player2.getPlayerId() + "," + player1.isWhitePlayer();
            String player2Message = "GAME_START#" + player2.getPlayerId() + "," + player1.getPlayerId() + "," + player2.isWhitePlayer();
            player1.sendMessage(player1Message);
            player2.sendMessage(player2Message);
            System.out.println("[ROOM " + roomId + "] GAME_START gönderildi: " + player1.getPlayerName() + " -> " + player1Message);
            System.out.println("[ROOM " + roomId + "] GAME_START gönderildi: " + player2.getPlayerName() + " -> " + player2Message);
        } catch (Exception e) {
            System.err.println("[ROOM " + roomId + "] GAME_START gönderilemedi: " + e.getMessage());
        }
    }
    
    /**
     * Oyunu başlatır -  MESAJ FORMATI DÜZELTİLDİ
     */
    public void startGame() throws IOException {
        if (gameStarted) {
            System.out.println(" Oyun zaten başlamış");
            return;
        }
        gameStarted = true;
        this.currentPlayer = player1.isWhitePlayer() ? player1 : player2;
        this.diceRolled = false;
        System.out.println("\n=== 🎮 OYUN BAŞLATILIYOR ===");
        System.out.println("Player1: " + player1.getPlayerName() + " (ID: " + player1.getPlayerId() + ", Renk: " + (player1.isWhitePlayer() ? "BEYAZ" : "SİYAH") + ")");
        System.out.println("Player2: " + player2.getPlayerName() + " (ID: " + player2.getPlayerId() + ", Renk: " + (player2.isWhitePlayer() ? "BEYAZ" : "SİYAH") + ")");
        System.out.println("İlk sıra: " + currentPlayer.getPlayerName() + " (ID: " + currentPlayer.getPlayerId() + ")");
        // Sadece ilk TURN mesajı gönder
        String turnMsg = "TURN#" + currentPlayer.getPlayerId();
        broadcastMessage(turnMsg);
        System.out.println("İlk TURN mesajı gönderildi: " + turnMsg);
        System.out.println("=== OYUN BAŞLATILDI ===\n");
    }
    
    /**
     * Sıra mesajını tüm oyunculara gönder
     */
    private void broadcastTurnMessage() throws IOException {
        String turnMsg = "TURN#" + currentPlayer.getPlayerId();
        System.out.println("broadcastTurnMessage: currentPlayerId=" + currentPlayer.getPlayerId());
        broadcastMessage(turnMsg);
    }
    
    /**
     * Zar atma işlemi - Sıra kontrolü ile - GELİŞTİRİLDİ
     */
    public synchronized void rollDice(GameClient player) throws IOException {
        if (gameEnded) {
            System.out.println(" Zar atma reddedildi - Oyun bitti");
            return;
        }
        if (!gameStarted) {
            System.out.println(" Zar atma reddedildi - Oyun başlamamış");
            return;
        }
        if (!isPlayerTurn(player)) {
            System.out.println(" Zar atma reddedildi - Sıra yok: " + player.getPlayerName());
            return;
        }
        if (diceRolled) {
            System.out.println(" Zar atma reddedildi - Zar zaten atılmış: " + player.getPlayerName());
            return;
        }
        int[] dice = server.rollDice();
        lastDiceRoll[0] = dice[0];
        lastDiceRoll[1] = dice[1];
        diceRolled = true;
        // Zar ve hamle hakkı ayarla
        remainingDice.clear();
        if (dice[0] == dice[1]) {
            // Çift gelirse 4 hamle, 4 zar
            for (int i = 0; i < 4; i++) remainingDice.add(dice[0]);
            movesLeft = 4;
        } else {
            remainingDice.add(dice[0]);
            remainingDice.add(dice[1]);
            movesLeft = 2;
        }
        String diceMessage = "DICE_ROLL#" + dice[0] + "#" + dice[1];
        broadcastMessage(diceMessage);
        System.out.println(" Zar atıldı ve gönderildi: " + dice[0] + "," + dice[1] + " - Oyuncu: " + player.getPlayerName());
    }
    
    /**
     * Hamle işlemi - Manuel sıra kontrolü ile
     */
    public synchronized void makeMove(GameClient player, int from, int to) throws IOException {
        if (gameEnded) return;
        if (!gameStarted) return;
        if (!isPlayerTurn(player)) {
            System.out.println(" Hamle reddedildi - Sıra yok: " + player.getPlayerName());
            return;
        }
        if (!diceRolled) {
            System.out.println(" Hamle reddedildi - Zar atılmamış: " + player.getPlayerName());
            return;
        }
        if (!isValidMove(player, from, to)) {
            System.out.println(" Hamle reddedildi - Geçersiz hamle");
            return;
        }
        // Zar ile uyumlu hamle kontrolü
        int moveDistance = Math.abs(to - from);
        boolean valid = false;
        int usedDie = -1;
        for (int i = 0; i < remainingDice.size(); i++) {
            if (remainingDice.get(i) == moveDistance) {
                valid = true;
                usedDie = i;
                break;
            }
        }
        if (!valid) {
            System.out.println(" Hamle reddedildi - Zar ile uyumsuz hamle: " + moveDistance);
            if (player != null) player.sendMessage("ERROR#Hamle zar ile uyumlu değil! (" + moveDistance + ")");
            return;
        }
        // Aynı taş-zar kombinasyonu daha önce kullanıldı mı?
        String comboKey = from + ":" + moveDistance;
        if (usedPieceDiceCombos.contains(comboKey)) {
            System.out.println(" Hamle reddedildi - Bu taş ve zar kombinasyonu zaten kullanıldı: " + comboKey);
            if (player != null) player.sendMessage("ERROR#Bu taş ve zar kombinasyonu zaten kullanıldı!");
            return;
        }
        usedPieceDiceCombos.add(comboKey);
        // Zar kullanıldı, listeden çıkar
        remainingDice.remove(usedDie);
        movesLeft--;
        String moveMessage = "PIECE_MOVE#" + player.getPlayerId() + "," + from + "," + to;
        broadcastMessage(moveMessage);
        System.out.println(" Hamle yapıldı: " + player.getPlayerName() + " (" + from + " → " + to + ") | Kalan hamle: " + movesLeft);
        // NOT: Sıra manuel olarak geçilecek, otomatik değişmeyecek
    }
    
    /**
     * YENİ: Manuel sıra geçme fonksiyonu - GELİŞTİRİLDİ
     */
    public synchronized void passTurn() throws IOException {
        if (gameEnded) return;
        if (!gameStarted) return;
        if (!diceRolled) {
            System.out.println(" Sıra geçilemedi - Zar atılmamış");
            return;
        }
        // Sırayı diğer oyuncuya geçir
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        diceRolled = false;
        remainingDice.clear();
        movesLeft = 0;
        usedPieceDiceCombos.clear(); // Sıra değişince temizle
        broadcastTurnMessage();
        System.out.println(" Sıra geçildi: " + currentPlayer.getPlayerName());
    }
    
    /**
     * Oyuncunun sırası olup olmadığını kontrol eder
     */
    public boolean isPlayerTurn(GameClient player) {
        if (player == null || currentPlayer == null || !gameStarted || gameEnded) {
            System.out.println(" Sıra kontrolü BAŞARISIZ - Null kontrol veya oyun durumu");
            return false;
        }
        
        boolean isTurn = player.getPlayerId() == currentPlayer.getPlayerId();
        
        System.out.println(" Sıra kontrolü - Oyuncu: " + player.getPlayerName() + " (ID: " + player.getPlayerId() + ")" +
                          " | Mevcut sıra: " + currentPlayer.getPlayerName() + " (ID: " + currentPlayer.getPlayerId() + ")" + 
                          " | Sonuç: " + (isTurn ? " VAR" : " YOK"));
        
        return isTurn;
    }
    
    /**
     * Hamlenin geçerli olup olmadığını kontrol eder
     */
    public boolean isValidMove(GameClient player, int fromTriangle, int toTriangle) {
        // Sıra kontrolü
        if (!isPlayerTurn(player)) {
            System.out.println(" Geçersiz hamle - Sıra yok: " + player.getPlayerName());
            return false;
        }
        // Zar kontrolü
        if (!diceRolled) {
            System.out.println(" Geçersiz hamle - Zar atılmamış: " + player.getPlayerName());
            return false;
        }
        // Koordinat kontrolü
        if (fromTriangle < -1 || fromTriangle > 25 || toTriangle < -1 || toTriangle > 25) {
            System.out.println(" Geçersiz hamle - Koordinat hatası: " + fromTriangle + " → " + toTriangle);
            return false;
        }
        // --- YÖN KONTROLÜ EKLENDİ ---
        int direction = getPieceDirection(player.isWhitePlayer());
        if ((toTriangle - fromTriangle) * direction <= 0) {
            System.out.println(" Geçersiz hamle - Yanlış yön: " + fromTriangle + " → " + toTriangle);
            return false;
        }
        System.out.println(" Hamle geçerli: " + player.getPlayerName() + " (" + fromTriangle + " → " + toTriangle + ")");
        return true;
    }
    
    /**
     * Mesaj yayınla
     */
    public void broadcastMessage(String message) throws IOException {
        System.out.println(" BROADCAST: " + message);
        
        if (player1 != null && player1.isConnected()) {
            try {
                player1.sendMessage(message);
                System.out.println("   " + player1.getPlayerName() + " (ID: " + player1.getPlayerId() + ") - gönderildi");
            } catch (IOException e) {
                System.out.println("   " + player1.getPlayerName() + " - mesaj gönderilemedi: " + e.getMessage());
            }
        } else {
            System.out.println("   " + (player1 != null ? player1.getPlayerName() : "Player1") + " - bağlantısız");
        }
        
        if (player2 != null && player2.isConnected()) {
            try {
                player2.sendMessage(message);
                System.out.println("   " + player2.getPlayerName() + " (ID: " + player2.getPlayerId() + ") - gönderildi");
            } catch (IOException e) {
                System.out.println("   " + player2.getPlayerName() + " - mesaj gönderilemedi: " + e.getMessage());
            }
        } else {
            System.out.println("   " + (player2 != null ? player2.getPlayerName() : "Player2") + " - bağlantısız");
        }
    }
    
    /**
     * Oyunu sonlandırır
     */
    public void endGame(String reason) throws IOException {
        if (!gameEnded) {
            gameEnded = true;
            String endMsg = "GAME_END#" + reason;
            broadcastMessage(endMsg);
            
            System.out.println(" Oyun bitti - Oda: " + roomId + " | Sebep: " + reason);
        }
    }
    
    /**
     * Oyuncu hazır olduğunu bildirir
     */
    public synchronized void playerReady(GameClient player) throws IOException, InterruptedException {
        System.out.println("[ROOM " + roomId + "] playerReady çağrıldı: " + player.getPlayerName());
        if (player == player1 && !player1Ready) {
            player1Ready = true;
            readyPlayerCount++;
            System.out.println("[ROOM " + roomId + "] Player1 hazır: " + player1.getPlayerName() + " (" + readyPlayerCount + "/2)");
        } else if (player == player2 && !player2Ready) {
            player2Ready = true;
            readyPlayerCount++;
            System.out.println("[ROOM " + roomId + "] Player2 hazır: " + player2.getPlayerName() + " (" + readyPlayerCount + "/2)");
        } else {
            System.out.println("[ROOM " + roomId + "] Bu oyuncu zaten hazır: " + player.getPlayerName());
            return;
        }
        if (readyPlayerCount >= 2 && !gameStarted) {
            System.out.println("[ROOM " + roomId + "] İki oyuncu da hazır - Oyun başlatılıyor!");
            Thread.sleep(1000);
            startGame();
        } else if (gameStarted) {
            System.out.println("[ROOM " + roomId + "] Oyun zaten başlamış - " + player.getPlayerName());
        } else {
            System.out.println("[ROOM " + roomId + "] " + player.getPlayerName() + " hazır - " + (2 - readyPlayerCount) + " oyuncu daha bekleniyor");
        }
    }
    
    /**
     * Debug için oda durumunu yazdır
     */
    public void printRoomStatus() {
        System.out.println("=== ODA DURUMU: " + roomId + " ===");
        System.out.println("Oyun Başladı: " + (gameStarted ? "EVET" : "HAYIR"));
        System.out.println("Oyun Bitti: " + (gameEnded ? "EVET" : "HAYIR"));
        System.out.println("Hazır Oyuncu: " + readyPlayerCount + "/2");
        System.out.println("Zar Atıldı: " + (diceRolled ? "EVET (" + lastDiceRoll[0] + "," + lastDiceRoll[1] + ")" : "HAYIR"));
        
        if (currentPlayer != null) {
            System.out.println("Şu Anki Sıra: " + currentPlayer.getPlayerName() + 
                             " (ID: " + currentPlayer.getPlayerId() + ", " + (currentPlayer.isWhitePlayer() ? "BEYAZ" : "SİYAH") + ")");
        } else {
            System.out.println("Şu Anki Sıra: YOK");
        }
        
        if (player1 != null) {
            System.out.println("  Player1: " + player1.getPlayerName() + 
                             " (ID: " + player1.getPlayerId() + ", " + (player1.isWhitePlayer() ? "BEYAZ" : "SİYAH") + 
                             ", Bağlantı: " + (player1.isConnected() ? "AKTİF" : "KESİK") + ")");
        }
        
        if (player2 != null) {
            System.out.println("  Player2: " + player2.getPlayerName() + 
                             " (ID: " + player2.getPlayerId() + ", " + (player2.isWhitePlayer() ? "BEYAZ" : "SİYAH") + 
                             ", Bağlantı: " + (player2.isConnected() ? "AKTİF" : "KESİK") + ")");
        }
        System.out.println("==========================");
    }
    
    // Getter metotları
    public int getRoomId() { return roomId; }
    public GameClient getPlayer1() { return player1; }
    public GameClient getPlayer2() { return player2; }
    public GameClient getCurrentPlayer() { return currentPlayer; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameEnded() { return gameEnded; }
    public int[] getLastDiceRoll() { return lastDiceRoll.clone(); }
    public boolean isDiceRolled() { return diceRolled; }
    public void setLastDiceRoll(int[] diceRoll) { 
        this.lastDiceRoll = diceRoll.clone(); 
        this.diceRolled = true; 
    }
    
    // --- YÖN FONKSİYONU EKLENDİ ---
    public int getPieceDirection(boolean isWhite) {
        return isWhite ? 1 : -1;
    }
    
}
