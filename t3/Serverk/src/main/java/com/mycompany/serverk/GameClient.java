package com.mycompany.serverk;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author zeysu
 */
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**@param id Oyuncu ID'si
 * Sunucu tarafında oyuncu temsili
 * @author zeysu
 */
public class GameClient  extends Thread{
    private static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());
    
    private int id;
    private Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private GameServer server;
    private GameRoom gameRoom;
    private boolean isWhitePlayer;
    private String playerName;
    private boolean isRunning;
    
    // UI bileşenleri
    private JButton zarButton;
    private JLabel statusLabel;
    private JLabel colorLabel;
    private JLabel diceLabel;
    
    private String thisPlayerName;
    
    public GameClient(Socket socket, GameServer server, int id) throws IOException {
        this.id = id;
        this.clientSocket = socket;
        this.server = server;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        this.playerName = "Oyuncu" + id;
        this.thisPlayerName = this.playerName;
        this.isRunning = true;
        
        LOGGER.info("Yeni GameClient oluşturuldu - ID: " + id);
    }
    
    public int getPlayerId() {
        return id;
    }
    
    public boolean isConnected() {
        return isRunning && clientSocket != null && !clientSocket.isClosed();
    }
    
    public void startListening() {
        this.start();
        LOGGER.info("GameClient " + id + " dinleme başlatıldı");
    }
    
    public void disconnect() {
        isRunning = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Socket kapatılırken hata: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void run() {
        try {
            while (isRunning && !clientSocket.isClosed()) {
                int messageSize = inputStream.read();
                if (messageSize == -1) {
                    LOGGER.info("Client " + id + " bağlantısı kesildi (EOF)");
                    break;
                }
                
                if (messageSize <= 0 || messageSize > 1024) {
                    LOGGER.warning("Geçersiz mesaj boyutu: " + messageSize);
                    continue;
                }
                
                byte[] buffer = new byte[messageSize];
                int bytesRead = inputStream.read(buffer);
                
                if (bytesRead > 0) {
                    String message = new String(buffer, 0, bytesRead);
                    LOGGER.fine("Mesaj alındı - Client " + id + ": " + message);
                    parseMessage(message);
                }
            }
        } catch (IOException ex) {
            if (isRunning) {
                LOGGER.log(Level.INFO, "Oyuncu bağlantısı kesildi: " + id, ex);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            cleanup();
        }
    }
    
    private void cleanup() {
        isRunning = false;
        server.clientDisconnected(this);
        
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cleanup sırasında hata: " + e.getMessage(), e);
        }
        
        LOGGER.info("GameClient " + id + " temizlendi");
    }
    
    private void parseMessage(String message) throws IOException, InterruptedException {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        System.out.println("📨 Mesaj alındı: " + message);
        
        // YENİ: PASS_TURN mesajlarını işle
        if (message.startsWith("PASS_TURN#")) {
            handlePassTurn(message);
            return;
        }
        
        // TURN mesajlarını işle
        if (message.startsWith("TURN#")) {
            handleTurnMessage(message);
            return;
        }
        
        String[] tokens = message.trim().split("#");
        if (tokens.length < 1) {
            return;
        }
        
        String messageType = tokens[0];
        
        switch (messageType) {
            case "GAME_START":
                handleGameStart(tokens);
                break;
            case "DICE_ROLL":
                handleDiceRoll(tokens);
                break;
            case "PIECE_MOVE":
                handlePieceMove(tokens);
                break;
            case "GAME_END":
                handleGameEnd(tokens);
                break;
            case "ERROR":
                handleError(tokens);
                break;
            case "PLAYER_NAME":
                if (tokens.length > 1) {
                    handlePlayerName(tokens[1]);
                }
                break;
            case "PLAYER_READY":
                handlePlayerReady();
                break;
            default:
                System.out.println("Bilinmeyen mesaj: " + messageType);
        }
    }
    
    /**
     * YENİ: Manuel sıra geçme mesajını işler
     */
    private void handlePassTurn(String message) throws IOException {
        String[] parts = message.split("#");
        if (parts.length < 2) return;
        
        int passingPlayerId = Integer.parseInt(parts[1].trim());
        
        System.out.println("🔄 PASS_TURN mesajı alındı - Oyuncu: " + passingPlayerId);
        
        if (gameRoom != null) {
            // GameRoom'da sıra geçme işlemini yönet
            gameRoom.passTurn();
        } else {
            LOGGER.warning("GameRoom yok - Client " + id);
            sendMessage("ERROR#Oyun odasında değilsiniz!");
        }
    }
    
    private void handleGameStart(String[] tokens) {
        System.out.println("🎮 Oyun başladı!");
        
        if (tokens.length >= 4) {
            int player1Id = Integer.parseInt(tokens[1]);
            int player2Id = Integer.parseInt(tokens[2]);
            boolean player1IsWhite = Boolean.parseBoolean(tokens[3]);
            
            if (this.id == player1Id) {
                isWhitePlayer = player1IsWhite;
            } else if (this.id == player2Id) {
                isWhitePlayer = !player1IsWhite;
            }
            
            System.out.println("🎨 Oyuncu rengi: " + (isWhitePlayer ? "BEYAZ" : "SİYAH"));
            
            SwingUtilities.invokeLater(() -> {
                if (colorLabel != null) {
                    colorLabel.setText("Renginiz: " + (isWhitePlayer ? "BEYAZ" : "SİYAH"));
                    colorLabel.setForeground(isWhitePlayer ? Color.WHITE : Color.BLACK);
                }
            });
        }
    }
    
    private void handleTurnMessage(String message) {
        String[] parts = message.split("#");
        if (parts.length < 2) return;
        int turnPlayerId = Integer.parseInt(parts[1].trim());
        boolean myTurn = (turnPlayerId == this.id);
        updateTurnStatus(myTurn);
    }
    
    private void updateTurnStatus(boolean myTurn) {
        System.out.println("[DEBUG] updateTurnStatus çağrıldı. myTurn: " + myTurn + " | Benim ID: " + this.getPlayerId());
        SwingUtilities.invokeLater(() -> {
            if (zarButton != null) {
                zarButton.setEnabled(myTurn);
                zarButton.setVisible(true);
            }
            if (statusLabel != null) {
                if (myTurn) {
                    statusLabel.setText("Sıra: SİZDE - Zar Atın!");
                    statusLabel.setForeground(Color.GREEN);
                } else {
                    statusLabel.setText("Sıra: RAKİPTE - Bekleyiniz");
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        System.out.println("🎯 UI sıra durumu güncellendi: " + (myTurn ? "SENDE" : "RAKİPTE"));
    }
    
    private void handleDiceRoll(String[] tokens) throws IOException {
        System.out.println("🎲 ZAR ATMA İSTEĞİ ALINDI - Client " + id + " (" + playerName + ")");
        
        if (gameRoom != null) {
            gameRoom.rollDice(this);
        } else {
            LOGGER.warning("GameRoom yok - Client " + id);
            sendMessage("ERROR#Oyun odasında değilsiniz!");
        }
    }
    
    private void handlePieceMove(String[] tokens) throws IOException {
        System.out.println("♟️ HAMLE İSTEĞİ ALINDI - Client " + id + " (" + playerName + ")");
        
        if (tokens.length < 2) {
            LOGGER.warning("Eksik hamle verisi - Client " + getPlayerId());
            sendMessage("ERROR#Eksik hamle verisi!");
            return;
        }
        
        String data = tokens[1];
        
        if (gameRoom != null) {
            String[] moveData = data.split(",");
            if (moveData.length >= 2) {
                try {
                    int fromTriangle = Integer.parseInt(moveData[0]);
                    int toTriangle = Integer.parseInt(moveData[1]);
                    
                    gameRoom.makeMove(this, fromTriangle, toTriangle);
                    
                } catch (NumberFormatException e) {
                    LOGGER.warning("Geçersiz hamle verisi - Client " + getPlayerId() + ": " + data);
                    sendMessage("ERROR#Geçersiz hamle verisi!");
                }
            } else {
                LOGGER.warning("Eksik hamle verisi - Client " + getPlayerId() + ": " + data);
                sendMessage("ERROR#Eksik hamle verisi!");
            }
        } else {
            LOGGER.warning("GameRoom yok - Client " + id);
            sendMessage("ERROR#Oyun odasında değilsiniz!");
        }
    }
    
    private void handleGameEnd(String[] tokens) {
        System.out.println("🏁 Oyun bitti mesajı alındı");
        
        if (tokens.length > 1) {
            String reason = tokens[1];
            System.out.println("Oyun bitiş sebebi: " + reason);
            
            SwingUtilities.invokeLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Oyun Bitti: " + reason);
                    statusLabel.setForeground(Color.BLUE);
                }
                
                if (zarButton != null) {
                    zarButton.setEnabled(false);
                }
            });
        }
    }
    
    private void handleError(String[] tokens) {
        if (tokens.length > 1) {
            String errorMsg = tokens[1];
            System.out.println("❌ Hata mesajı: " + errorMsg);
            
            SwingUtilities.invokeLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Hata: " + errorMsg);
                    statusLabel.setForeground(Color.RED);
                }
            });
        }
    }
    
    private void handlePlayerReady() throws IOException, InterruptedException {
        System.out.println("✅ OYUNCU HAZIR - Client " + id + " (" + playerName + ")");
        
        if (gameRoom != null) {
            gameRoom.playerReady(this);
            LOGGER.info("Oyuncu hazır - Client " + id);
        } else {
            LOGGER.warning("GameRoom yok - Client " + id);
            sendMessage("ERROR#Oyun odasında değilsiniz!");
        }
    }
    
    private void handlePlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
            this.thisPlayerName = this.playerName;
            LOGGER.info("Oyuncu ismi güncellendi: " + playerName + " (ID: " + getPlayerId() + ")");
        } else {
            LOGGER.warning("Geçersiz oyuncu ismi - Client " + getPlayerId());
        }
    }
    
    public synchronized void sendMessage(byte[] message) throws IOException {
        if (!isRunning || clientSocket.isClosed()) {
            throw new IOException("Bağlantı kapalı - Client " + id);
        }
        
        if (message == null || message.length == 0) {
            LOGGER.warning("Boş mesaj gönderilmeye çalışıldı - Client " + id);
            return;
        }
        
        try {
            outputStream.write(message.length);
            outputStream.write(message);
            outputStream.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Mesaj gönderilemedi - Client " + id, e);
            disconnect();
            throw e;
        }
    }
    
    public void sendMessage(String message) throws IOException {
        if (message != null) {
            sendMessage(message.getBytes("UTF-8"));
        }
    }
    
    // Getter ve Setter metodları
    public GameRoom getGameRoom() {
        return gameRoom;
    }
    
    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        LOGGER.info("Client " + id + " oyun odasına atandı: " + 
                   (gameRoom != null ? gameRoom.getRoomId() : "null"));
    }
    
    public boolean isWhitePlayer() {
        return isWhitePlayer;
    }
    
    public void setWhitePlayer(boolean whitePlayer) {
        this.isWhitePlayer = whitePlayer;
        LOGGER.info("Client " + id + " rengi: " + (whitePlayer ? "BEYAZ" : "SİYAH"));
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        if (playerName != null && !playerName.trim().isEmpty()) {
            this.playerName = playerName.trim();
            this.thisPlayerName = this.playerName;
        }
    }
    
    public Socket getClientSocket() {
        return clientSocket;
    }
    
    // UI bileşenlerini ayarlama metodları
    public void setZarButton(JButton zarButton) {
        this.zarButton = zarButton;
    }
    
    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }
    
    public void setColorLabel(JLabel colorLabel) {
        this.colorLabel = colorLabel;
    }
    
    public void setDiceLabel(JLabel diceLabel) {
        this.diceLabel = diceLabel;
    }
    
    @Override
    public String toString() {
        return "GameClient{" +
                "playerId=" + getPlayerId() +
                ", playerName='" + playerName + '\'' +
                ", isWhitePlayer=" + isWhitePlayer +
                ", connected=" + isConnected() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameClient that = (GameClient) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}