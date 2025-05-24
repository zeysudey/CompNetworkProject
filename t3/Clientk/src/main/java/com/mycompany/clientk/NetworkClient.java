/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientk;

/**
 *
 * @author zeysu
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sunucuyla iletişimi yöneten sınıf
 */
public class NetworkClient extends Thread {
private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String serverAddress;
    private int serverPort;
    private boolean connected;
    private NetworkGameListener gameListener;
    private int playerId = -1; // ✅ Varsayılan değer -1
    private boolean isWhitePlayer;
    
    public NetworkClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connected = false;
    }
    
    /**
     * Sunucuya bağlanır
     */
    public boolean connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            connected = true;
            this.start(); // Mesaj dinleme thread'ini başlat
            
            System.out.println("Sunucuya bağlanıldı: " + serverAddress + ":" + serverPort);
            return true;
            
        } catch (IOException e) {
            System.err.println("Sunucuya bağlanılamadı: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    /**
     * Bağlantıyı keser
     */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            while (connected && !socket.isClosed()) {
                // Mesaj boyutunu oku
                int messageSize = inputStream.read();
                if (messageSize == -1) break;
                
                // Mesajı oku
                byte[] buffer = new byte[messageSize];
                int bytesRead = inputStream.read(buffer);
                
                if (bytesRead > 0) {
                    String message = new String(buffer, 0, bytesRead);
                    handleMessage(message);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Sunucu bağlantısı kesildi: " + e.getMessage());
            }
        } finally {
            connected = false;
            if (gameListener != null) {
                gameListener.onDisconnected();
            }
        }
    }
    
    /**
     * Sunucudan gelen mesajları işler - ✅ TAMAMEN DÜZELTİLDİ
     */
    private void handleMessage(String message) {
        if (gameListener == null) return;
        
        System.out.println("📨 MESAJ ALINDI: " + message);
        
        String[] tokens = message.trim().split("#");
        if (tokens.length < 1) return;
        
        String messageType = tokens[0];
        
        switch (messageType) {
            case "GAME_START":
                if (tokens.length >= 2) {
                    handleGameStart(tokens[1]);
                }
                break;
            case "DICE_ROLL":
                if (tokens.length >= 3) {
                    // Format: "DICE_ROLL#dice1#dice2" 
                    handleDiceRoll(tokens[1] + "," + tokens[2]);
                } else if (tokens.length >= 2) {
                    // Format: "DICE_ROLL#dice1,dice2"
                    handleDiceRoll(tokens[1]);
                }
                break;
            case "PIECE_MOVE":
                if (tokens.length >= 2) {
                    handlePieceMove(tokens[1]);
                }
                break;
            case "TURN":
                if (tokens.length >= 2) {
                    System.out.println("🔄 TURN mesajı işleniyor: " + tokens[1]);
                    handleTurnChange(tokens[1]);
                }
                break;
            case "TURN_CHANGE":
                if (tokens.length >= 2) {
                    System.out.println("🔄 TURN_CHANGE mesajı işleniyor: " + tokens[1]);
                    handleTurnChange(tokens[1]);
                }
                break;
            case "GAME_END":
                if (tokens.length >= 2) {
                    handleGameEnd(tokens[1]);
                }
                break;
            case "ERROR":
                if (tokens.length >= 2 && gameListener != null) {
                    System.out.println("❌ HATA: " + tokens[1]);
                    gameListener.onError(tokens[1]);
                }
                break;
              case "PLAYER_REPLACED":
    if (tokens.length >= 3) {
        handlePlayerReplaced(tokens[1], tokens[2]);
    }
    break;
              case "COLOR_CONFIRM":
    if (tokens.length >= 2) {
        handleColorConfirmation(tokens[1]);
    }
    break;
case "WAITING_FOR_PLAYER":
    if (tokens.length >= 2) {
        handleWaitingForPlayer(tokens[1]);
    } else {
        handleWaitingForPlayer("Yeni oyuncu bekleniyor...");
    }
    break;
case "GAME_STATUS":
    if (tokens.length >= 2) {
        handleGameStatus(tokens[1]);
    }
    break;
case "SERVER_SHUTDOWN":
    if (tokens.length >= 2) {
        handleServerShutdown(tokens[1]);
    }
    break;  
            default:
                System.out.println("❓ Bilinmeyen mesaj tipi: " + messageType);
        }
    }
   private void handleColorConfirmation(String color) {
    System.out.println("🎨 Renk doğrulandı: " + color);
    
    // Renk bilgisini client'ta da güncelle
    boolean isWhite = color.equals("BEYAZ");
    this.isWhitePlayer = isWhite;
    
    if (gameListener != null) {
        try {
            // Eğer bu metod varsa çağır
            gameListener.onColorConfirmed(color, isWhite);
        } catch (Exception e) {
            // Yoksa basit error mesajı ile bilgilendir
            gameListener.onError("Renginiz: " + color);
        }
    }
    
    System.out.println("✅ Client renk bilgisi güncellendi: " + 
                      (this.isWhitePlayer ? "BEYAZ" : "SİYAH"));
}
 public String getPlayerColorString() {
    return isWhitePlayer ? "BEYAZ" : "SİYAH";
}

private void handlePlayerReplaced(String newPlayerName, String playerColor) {
    System.out.println("🔄 Yeni oyuncu: " + newPlayerName + " (" + playerColor + ")");
    
    if (gameListener != null) {
        // Kısa ve net mesaj
        gameListener.onError("Yeni rakip: " + newPlayerName + " (" + playerColor + ")");
    }
}

/**
 * Oyun durumu mesajını işler
 */
private void handleGameStatus(String status) {
    System.out.println("📊 Oyun durumu: " + status);
    
    if (gameListener != null) {
        switch (status) {
            case "STARTED":
                gameListener.onError("Oyun başladı");
                break;
            case "WAITING":
                gameListener.onError("Oyun bekleniyor");
                break;
            case "WAITING_TO_START":
                gameListener.onError("Yeni oyuncu geldi - Oyun başlatılacak");
                break;
            case "RESUMED":
                gameListener.onError("Yeni oyuncu geldi - Oyun devam ediyor");
                break;
            case "READY_NEEDED":
                gameListener.onError("Hazır olun");
                break;
            case "RESET":
                gameListener.onError("Oyun sıfırlandı - Yeni oyuncu bekleniyor");
                break;
            default:
                gameListener.onError("Durum: " + status);
                break;
        }
    }
}
/**
 * Sunucu kapanma mesajını işler
 */
private void handleServerShutdown(String reason) {
    System.out.println("🛑 Sunucu kapanıyor: " + reason);
    
    if (gameListener != null) {
        gameListener.onServerShutdown(reason);
    }
    
    // Bağlantıyı kapat
    disconnect();
} 
/**
     * Oyun başlama mesajını işler - ✅ GELİŞTİRİLDİ
     */
    private void handleGameStart(String data) {
        System.out.println("handleGameStart ÇAĞRILDI! data: " + data);
        System.out.println("🎮 GAME_START verisi işleniyor: " + data);
        
        String[] gameData = data.split(",");
        if (gameData.length >= 3) {
            int receivedId = Integer.parseInt(gameData[0]);
            int opponentId = Integer.parseInt(gameData[1]);
            boolean isWhite = Boolean.parseBoolean(gameData[2]);
            
            // ✅ OYUNCU ID'SİNİ KAYDET
            this.playerId = receivedId;
            this.isWhitePlayer = isWhite;
            
            System.out.println("✅ Oyuncu bilgileri kaydedildi - ID: " + this.playerId + 
                             " | Rakip: " + opponentId + " | Beyaz: " + isWhite);
            
            gameListener.onGameStart(receivedId, opponentId, isWhite);
        } else {
            System.out.println("❌ GAME_START verisi eksik: " + data);
        }
    }
    
    /**
     * Zar atma mesajını işler
     */
    private void handleDiceRoll(String data) {
        System.out.println("🎲 DICE_ROLL verisi işleniyor: " + data);
        
        String[] diceData = data.split(",");
        if (diceData.length >= 2) {
            int dice1 = Integer.parseInt(diceData[0]);
            int dice2 = Integer.parseInt(diceData[1]);
            gameListener.onDiceRoll(dice1, dice2);
        } else {
            System.out.println("❌ DICE_ROLL verisi eksik: " + data);
        }
    }
    
    /**
     * Taş hareketi mesajını işler
     */
    private void handlePieceMove(String data) {
        System.out.println("♟️ PIECE_MOVE verisi işleniyor: " + data);
        
        String[] moveData = data.split(",");
        if (moveData.length >= 3) {
            int playerId = Integer.parseInt(moveData[0]);
            int fromTriangle = Integer.parseInt(moveData[1]);
            int toTriangle = Integer.parseInt(moveData[2]);
            gameListener.onPieceMove(playerId, fromTriangle, toTriangle);
        } else {
            System.out.println("❌ PIECE_MOVE verisi eksik: " + data);
        }
    }
    
    /**
     * Sıra değişimi mesajını işler - ✅ GELİŞTİRİLDİ
     */
    private void handleTurnChange(String data) {
        System.out.println("handleTurnChange çağrıldı! data: " + data);
        try {
            int currentPlayerId = Integer.parseInt(data.trim());
            boolean isMyTurn = (currentPlayerId == this.playerId);
            System.out.println("TURN mesajı geldi. currentPlayerId: " + currentPlayerId + ", myPlayerId: " + this.playerId + ", isMyTurn: " + isMyTurn);
            gameListener.onTurnChange(currentPlayerId, isMyTurn);
        } catch (NumberFormatException e) {
            System.out.println("❌ TURN_CHANGE parse hatası: " + data);
        }
    }
    
    /**
     * Oyun bitişi mesajını işler
     */
    private void handleGameEnd(String data) {
        System.out.println("🏁 GAME_END verisi işleniyor: " + data);
        gameListener.onGameEnd(data);
    }
    
    /**
     * Sunucuya mesaj gönderir
     */
    public void sendMessage(String message) {
        if (!connected || socket.isClosed()) {
            System.err.println("Bağlantı yok, mesaj gönderilemedi: " + message);
            return;
        }
        
        try {
            byte[] messageBytes = message.getBytes();
            outputStream.write(messageBytes.length);
            outputStream.write(messageBytes);
            outputStream.flush();
            System.out.println("📤 Mesaj gönderildi: " + message);
        } catch (IOException e) {
            System.err.println("Mesaj gönderilemedi: " + e.getMessage());
            connected = false;
        }
    }
    
    /**
     * Zar atma isteği gönderir
     */
    public void requestDiceRoll() {
        sendMessage("DICE_ROLL#");
    }
    
    /**
     * Taş hareketi gönderir
     */
    public void sendPieceMove(int fromTriangle, int toTriangle) {
        sendMessage("PIECE_MOVE#" + fromTriangle + "," + toTriangle);
    }
    
    /**
     * Taş hareketi gönderir (sendPieceMove için alternatif ad)
     */
    public void sendMove(int fromTriangle, int toTriangle) {
        sendPieceMove(fromTriangle, toTriangle);
    }
    
    /**
     * ✅ Manuel sıra geçme isteği gönderir - DÜZELTİLDİ
     */
    public void requestPassTurn() {
        if (this.playerId == -1) {
            System.out.println("❌ Oyuncu ID bilinmiyor, sıra geçme iptal edildi");
            return;
        }
        
        sendMessage("PASS_TURN#" + playerId);
        System.out.println("🔄 Manuel sıra geçme isteği gönderildi - Oyuncu ID: " + playerId);
    }
 private void handleWaitingForPlayer(String message) {
    System.out.println("⏳ " + message);
    
    if (gameListener != null) {
        gameListener.onError(message);
    }
}

    /**
     * Oyuncu hazır mesajı gönderir
     */
    public void sendPlayerReady() {
        sendMessage("PLAYER_READY#");
    }
    
    /**
     * Oyuncu ismini gönderir
     */
    public void sendPlayerName(String name) {
        sendMessage("PLAYER_NAME#" + name);
    }
    
    // Getter ve Setter metotları
    public boolean isConnected() {
        return connected;
    }
    
    public void setGameListener(NetworkGameListener listener) {
        this.gameListener = listener;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public boolean isWhitePlayer() {
        return isWhitePlayer;
    }
    
    public void setWhitePlayer(boolean whitePlayer) {
        this.isWhitePlayer = whitePlayer;
    }
 
}