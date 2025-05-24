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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tavla oyunu sunucusu
 */
public class GameServer extends Thread {
  private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
    
    private int clientId;
    private ServerSocket serverSocket;
    private List<GameClient> waitingClients; // Oyun bekleyen oyuncular
    private Map<Integer, GameRoom> gameRooms;    // Aktif oyun odaları
    private int roomCounter;
    private Random random;
    private boolean isRunning;
    
    public GameServer(int port) throws IOException {
        this.clientId = 1;
        this.serverSocket = new ServerSocket(port);
        this.waitingClients = new ArrayList<>();
        this.gameRooms = new HashMap<>();
        this.roomCounter = 1;
        this.random = new Random();
        this.isRunning = true;
        
        System.out.println("🚀 Tavla oyun sunucusu başlatıldı - Port: " + port);
        LOGGER.info("GameServer başlatıldı - Port: " + port);
    }
    
    public void startAcceptance() {
        this.start();
        System.out.println("✅ Sunucu client kabul etmeye başladı");
    }
    
    @Override
    public void run() {
        try {
            while (isRunning && !serverSocket.isClosed()) {
                System.out.println("⏳ Yeni oyuncu bekleniyor...");
                Socket clientSocket = serverSocket.accept();
                
                // Yeni client oluştur
                GameClient newClient = new GameClient(clientSocket, this, clientId++);
                newClient.startListening();
                
                // Bekleyen listesine ekle
                synchronized (waitingClients) {
                    waitingClients.add(newClient);
                }
                
                System.out.println("👤 Yeni oyuncu bağlandı - ID: " + newClient.getPlayerId() + 
                                 " (" + newClient.getPlayerName() + ")");
                System.out.println("📊 Bekleyen oyuncu sayısı: " + waitingClients.size());
                
                // İki oyuncu olduğunda oyun odasına al
                tryCreateGameRoom();
                
            }
        } catch (IOException ex) {
            if (isRunning) {
                LOGGER.log(Level.SEVERE, "Server çalışma hatası", ex);
                System.err.println("❌ Sunucu hatası: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Eğer yeterli oyuncu varsa yeni oyun odası oluşturur
     */
    private synchronized void tryCreateGameRoom() {
        System.out.println("🔍 Oyun odası oluşturma kontrolü - Bekleyen: " + waitingClients.size());
        
        if (waitingClients.size() >= 2) {
            try {
                createGameRoom();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Oyun odası oluşturma hatası", e);
                System.err.println("❌ Oyun odası oluşturulamadı: " + e.getMessage());
            }
        }
    }
    
    /**
     * Yeni oyun odası oluşturur
     */
    private void createGameRoom() throws IOException {
        synchronized (waitingClients) {
            if (waitingClients.size() < 2) {
                System.out.println("⚠️ Oyun odası oluşturulamadı - Yetersiz oyuncu: " + waitingClients.size());
                return;
            }
            
            // İlk iki oyuncuyu al
            GameClient player1 = waitingClients.remove(0);
            GameClient player2 = waitingClients.remove(0);
            
            System.out.println("🎮 Oyun odası oluşturuluyor...");
            System.out.println("   Player1: " + player1.getPlayerName() + " (ID: " + player1.getPlayerId() + ")");
            System.out.println("   Player2: " + player2.getPlayerName() + " (ID: " + player2.getPlayerId() + ")");
            
            // Yeni oyun odası oluştur
            GameRoom room = new GameRoom(roomCounter++, player1, player2, this);
            gameRooms.put(room.getRoomId(), room);
            
            // Oyuncuları odaya ata
            player1.setGameRoom(room);
            player2.setGameRoom(room);
            
            System.out.println("✅ Yeni oyun odası oluşturuldu - Oda ID: " + room.getRoomId());
            System.out.println("📊 Aktif oyun odası sayısı: " + gameRooms.size());
            System.out.println("📊 Bekleyen oyuncu sayısı: " + waitingClients.size());
        }
    }
    
    /**
     * Oyuncu bağlantısı kesildiğinde çağrılır
     */
    public synchronized void clientDisconnected(GameClient client) {
        System.out.println("🔌 Oyuncu bağlantısı kesildi: " + client.getPlayerName() + " (ID: " + client.getPlayerId() + ")");
        
        // Bekleyen listesinden çıkar
        synchronized (waitingClients) {
            if (waitingClients.remove(client)) {
                System.out.println("📤 Oyuncu bekleyen listesinden çıkarıldı");
            }
        }
        
        // Eğer oyuncu bir odadaysa, odayı sonlandır
        if (client.getGameRoom() != null) {
            GameRoom room = client.getGameRoom();
            try {
                System.out.println("🏁 Oyun sonlandırılıyor - Oda: " + room.getRoomId() + " (Oyuncu çıktı)");
                room.endGame("Oyuncu bağlantısı kesildi: " + client.getPlayerName());
                gameRooms.remove(room.getRoomId());
                System.out.println("📊 Aktif oyun odası sayısı: " + gameRooms.size());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Oyun sonlandırma hatası", e);
                System.err.println("❌ Oyun sonlandırma hatası: " + e.getMessage());
            }
        }
        
        System.out.println("🧹 Oyuncu temizlendi: " + client.getPlayerName() + " (ID: " + client.getPlayerId() + ")");
    }
    
    /**
     * Belirli bir odaya mesaj gönderir
     */
    public void sendToRoom(int roomId, String message) throws IOException {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            room.broadcastMessage(message);
            System.out.println("📤 Oda mesajı gönderildi - Oda: " + roomId + " | Mesaj: " + message);
        } else {
            System.out.println("❌ Oda bulunamadı: " + roomId);
        }
    }
    
    /**
     * Tüm oyunculara mesaj gönderir
     */
    public void broadcastToAll(String message) throws IOException {
        System.out.println("📢 Global broadcast: " + message);
        
        // Bekleyen oyuncular
        synchronized (waitingClients) {
            for (GameClient client : waitingClients) {
                try {
                    client.sendMessage(message.getBytes());
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Bekleyen oyuncuya mesaj gönderilemedi: " + client.getPlayerId(), e);
                }
            }
        }
        
        // Oyun odalarındaki oyuncular
        for (GameRoom room : gameRooms.values()) {
            try {
                room.broadcastMessage(message);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Oda mesajı gönderilemedi: " + room.getRoomId(), e);
            }
        }
    }
    
    /**
     * Zar atma işlemi
     */
    public int[] rollDice() {
        int dice1 = random.nextInt(6) + 1;
        int dice2 = random.nextInt(6) + 1;
        System.out.println("🎲 Zar atıldı: " + dice1 + ", " + dice2);
        return new int[]{dice1, dice2};
    }
    
    /**
     * Sunucu durumunu yazdır
     */
    public void printServerStatus() {
        System.out.println("=== SUNUCU DURUMU ===");
        System.out.println("Çalışıyor: " + (isRunning ? "EVET" : "HAYIR"));
        System.out.println("Bekleyen oyuncu: " + waitingClients.size());
        System.out.println("Aktif oyun odası: " + gameRooms.size());
        System.out.println("Toplam client ID: " + (clientId - 1));
        
        if (!waitingClients.isEmpty()) {
            System.out.println("Bekleyen oyuncular:");
            for (GameClient client : waitingClients) {
                System.out.println("  - " + client.getPlayerName() + " (ID: " + client.getPlayerId() + ")");
            }
        }
        
        if (!gameRooms.isEmpty()) {
            System.out.println("Aktif oyun odaları:");
            for (GameRoom room : gameRooms.values()) {
                System.out.println("  - Oda " + room.getRoomId() + ": " + 
                                 room.getPlayer1().getPlayerName() + " vs " + 
                                 room.getPlayer2().getPlayerName() + 
                                 " (Sıra: " + (room.getCurrentPlayer() != null ? room.getCurrentPlayer().getPlayerName() : "YOK") + ")");
            }
        }
        System.out.println("====================");
    }
    
    /**
     * Sunucuyu güvenli şekilde kapatır
     */
    public void shutdown() {
        System.out.println("🛑 Sunucu kapatılıyor...");
        isRunning = false;
        
        try {
            // Tüm oyunları sonlandır
            for (GameRoom room : gameRooms.values()) {
                try {
                    room.endGame("Sunucu kapanıyor");
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Oyun sonlandırma hatası", e);
                }
            }
            
            // Bekleyen oyuncuları bilgilendir
            synchronized (waitingClients) {
                for (GameClient client : waitingClients) {
                    try {
                        client.sendMessage("SERVER_SHUTDOWN#Sunucu kapanıyor");
                        client.disconnect();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Oyuncu bilgilendirme hatası", e);
                    }
                }
            }
            
            // Server socket'i kapat
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Sunucu kapatma hatası", e);
        }
        
        System.out.println("✅ Sunucu kapatıldı");
    }
    
    public static void main(String[] args) {
        GameServer server = null;
        try {
            server = new GameServer(8080);
            server.startAcceptance();
            
            System.out.println("🎮 Tavla Sunucusu Çalışıyor!");
            System.out.println("📝 Komutlar:");
            System.out.println("   'status' - Sunucu durumunu göster");
            System.out.println("   'exit' - Sunucuyu kapat");
            System.out.println("=====================================");
            
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                
                switch (input) {
                    case "exit":
                    case "quit":
                        System.out.println("👋 Çıkış komutu alındı");
                        return;
                        
                    case "status":
                        server.printServerStatus();
                        break;
                        
                    case "help":
                        System.out.println("📝 Komutlar: status, exit, help");
                        break;
                        
                    default:
                        if (!input.isEmpty()) {
                            System.out.println("❓ Bilinmeyen komut: " + input + " (Yardım için 'help')");
                        }
                        break;
                }
            }
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Sunucu başlatma hatası", ex);
            System.err.println("❌ Sunucu başlatılamadı: " + ex.getMessage());
        } finally {
            if (server != null) {
                server.shutdown();
            }
        }
    }
}
