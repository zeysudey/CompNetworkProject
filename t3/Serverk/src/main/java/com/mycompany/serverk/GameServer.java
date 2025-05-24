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
    private List<GameRoom> waitingForReplacementRooms; // Oyuncu değişimi bekleyen odalar
    private int roomCounter;
    private Random random;
    private boolean isRunning;
    
    public GameServer(int port) throws IOException {
        this.clientId = 1;
        this.serverSocket = new ServerSocket(port);
        this.waitingClients = new ArrayList<>();
        this.gameRooms = new HashMap<>();
        this.waitingForReplacementRooms = new ArrayList<>();
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
                
                System.out.println("👤 Yeni oyuncu bağlandı - ID: " + newClient.getPlayerId() + 
                                 " (" + newClient.getPlayerName() + ")");
                
                // Önce oyuncu değişimi gereken oda var mı kontrol et
                if (tryReplacePlayerInRoom(newClient)) {
                    System.out.println("🔄 Oyuncu mevcut odaya yerleştirildi");
                } else {
                    // Bekleyen listesine ekle
                    synchronized (waitingClients) {
                        waitingClients.add(newClient);
                    }
                    System.out.println("📊 Bekleyen oyuncu sayısı: " + waitingClients.size());
                    
                    // İki oyuncu olduğunda oyun odasına al
                    tryCreateGameRoom();
                }
            }
        } catch (IOException ex) {
            if (isRunning) {
                LOGGER.log(Level.SEVERE, "Server çalışma hatası", ex);
                System.err.println("❌ Sunucu hatası: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Yeni oyuncuyu mevcut bir odaya yerleştirmeye çalışır
     */
 private synchronized boolean tryReplacePlayerInRoom(GameClient newClient) {
    if (waitingForReplacementRooms.isEmpty()) {
        return false;
    }
    
    GameRoom room = waitingForReplacementRooms.remove(0);
    
    try {
        System.out.println("🔄 Oyuncu değiştirme deneniyor - Oda: " + room.getRoomId() + 
                          " | Yeni oyuncu: " + newClient.getPlayerName());
        
        // ÖNCE: Kalan oyuncuyu bul (değişimden önce)
        GameClient remainingPlayer = null;
        if (room.getPlayer1() != null && room.getPlayer1().isConnected()) {
            remainingPlayer = room.getPlayer1();
        } else if (room.getPlayer2() != null && room.getPlayer2().isConnected()) {
            remainingPlayer = room.getPlayer2();
        }
        
        // Odaya oyuncuyu yerleştir
        boolean replaced = room.replaceDisconnectedPlayer(newClient);
        
        if (replaced) {
            newClient.setGameRoom(room);
            System.out.println("✅ Oyuncu değişimi başarılı - Oda: " + room.getRoomId() + 
                             " | Yeni oyuncu: " + newClient.getPlayerName());
            
            // SONRA: Kalan oyuncuya ek mesaj gönder (eğer gerekirse)
            if (remainingPlayer != null && remainingPlayer.isConnected()) {
                try {
                    // Artık "bekleme" durumu değil, normal oyun durumu
                    System.out.println("📤 Kalan oyuncuya final durum mesajı gönderildi: " + remainingPlayer.getPlayerName());
                } catch (Exception e) {
                    System.err.println("❌ Final mesaj gönderilemedi: " + e.getMessage());
                }
            }
            
            return true;
        } else {
            // Değiştirilemedi, odayı kapat
            System.out.println("❌ Oyuncu değişimi başarısız - Oda kapatılıyor: " + room.getRoomId());
            try {
                room.endGame("Oyuncu değişimi başarısız");
            } catch (IOException e) {
                System.err.println("❌ Oda kapatma hatası: " + e.getMessage());
            }
            gameRooms.remove(room.getRoomId());
            return false;
        }
        
    } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Oyuncu değiştirme hatası", e);
        System.err.println("❌ Oyuncu değiştirme hatası: " + e.getMessage());
        return false;
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
     * Oyuncu bağlantısı kesildiğinde çağrılır - GELİŞTİRİLDİ
     */
public synchronized void clientDisconnected(GameClient client) {
    System.out.println("🔌 Oyuncu bağlantısı kesildi: " + client.getPlayerName() + " (ID: " + client.getPlayerId() + ")");
    
    // Bekleyen listesinden çıkar
    synchronized (waitingClients) {
        if (waitingClients.remove(client)) {
            System.out.println("📤 Oyuncu bekleyen listesinden çıkarıldı");
            return;
        }
    }
    
    // Eğer oyuncu bir odadaysa
    if (client.getGameRoom() != null) {
        GameRoom room = client.getGameRoom();
        
        System.out.println("🔍 Oda durumu kontrolü - Oda: " + room.getRoomId() + 
                          " | Bağlı oyuncu: " + room.getConnectedPlayerCount() + "/2");
        
        // Oyuncu değişimi yapılabilir mi kontrol et
        if (canRoomWaitForReplacement(room)) {
            System.out.println("🔄 Oda oyuncu değişimi için bekliyor - Oda: " + room.getRoomId());
            
            // Kalan oyuncuya bekle mesajı gönder
            GameClient remainingPlayer = null;
            if (room.getPlayer1() == client) {
                remainingPlayer = room.getPlayer2();
            } else if (room.getPlayer2() == client) {
                remainingPlayer = room.getPlayer1();
            }
            
            if (remainingPlayer != null && remainingPlayer.isConnected()) {
                try {
                    remainingPlayer.sendMessage("WAITING_FOR_PLAYER#Rakip çıktı, yeni oyuncu bekleniyor...");
                    System.out.println("📤 Bekle mesajı gönderildi: " + remainingPlayer.getPlayerName());
                } catch (IOException e) {
                    System.err.println("❌ Bekle mesajı gönderilemedi: " + e.getMessage());
                }
            }
            
            // Oyuncuyu odadan çıkar
            room.removePlayer(client);
            
            // Odayı bekleyen listeye ekle
            if (!waitingForReplacementRooms.contains(room)) {
                waitingForReplacementRooms.add(room);
                System.out.println("📋 Oda bekleyen listeye eklendi - Toplam bekleyen: " + waitingForReplacementRooms.size());
            }
            
        } else {
            // Oyun aktifse odayı sonlandır
            try {
                System.out.println("🏁 Oyun sonlandırılıyor - Oda: " + room.getRoomId() + " (Oyun aktifti)");
                room.endGame("Oyuncu bağlantısı kesildi: " + client.getPlayerName());
                gameRooms.remove(room.getRoomId());
                
                // Bekleyen listesinden de çıkar
                waitingForReplacementRooms.remove(room);
                
                System.out.println("📊 Aktif oyun odası sayısı: " + gameRooms.size());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Oyun sonlandırma hatası", e);
                System.err.println("❌ Oyun sonlandırma hatası: " + e.getMessage());
            }
        }
    }
    
    System.out.println("🧹 Oyuncu temizlendi: " + client.getPlayerName() + " (ID: " + client.getPlayerId() + ")");
}
    /**
     * Odanın oyuncu değişimi için bekleyip bekleyemeyeceğini kontrol eder
     */
 private boolean canRoomWaitForReplacement(GameRoom room) {
    System.out.println("🔍 Oda değişim kontrolü - Oda: " + room.getRoomId() + 
                      " | Oyun başladı: " + room.isGameStarted() + 
                      " | Zar atıldı: " + room.isDiceRolled() + 
                      " | Oyun bitti: " + room.isGameEnded());
    
    // Oda geçersizse false
    if (room.isGameEnded()) {
        System.out.println("❌ Oda geçersiz - Oyun bitti");
        return false;
    }
    
    // Oyun başlamışsa VE zar atılmışsa değişim yapma
    if (room.isGameStarted() && room.isDiceRolled()) {
        System.out.println("❌ Oyun aktif (başladı ve zar atıldı), oyuncu değişimi yapılamaz");
        return false;
    }
    
    // ÖNEMLI: Oyun başlamışsa ama zar atılmamışsa da değişim yapılabilir
    // Çünkü henüz gerçek oyun hamlesi yapılmadı
    if (room.isGameStarted() && !room.isDiceRolled()) {
        System.out.println("✅ Oyun başladı ama zar atılmadı, oyuncu değişimi yapılabilir");
        return true;
    }
    
    // Oyun henüz başlamamışsa kesinlikle değişim yapılabilir
    System.out.println("✅ Oyun henüz başlamadı, oyuncu değişimi yapılabilir");
    return true;
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
     * Sunucu durumunu yazdır - GELİŞTİRİLDİ
     */
    public void printServerStatus() {
        System.out.println("=== SUNUCU DURUMU ===");
        System.out.println("Çalışıyor: " + (isRunning ? "EVET" : "HAYIR"));
        System.out.println("Bekleyen oyuncu: " + waitingClients.size());
        System.out.println("Aktif oyun odası: " + gameRooms.size());
        System.out.println("Oyuncu değişimi bekleyen oda: " + waitingForReplacementRooms.size());
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
        
        if (!waitingForReplacementRooms.isEmpty()) {
            System.out.println("Oyuncu değişimi bekleyen odalar:");
            for (GameRoom room : waitingForReplacementRooms) {
                System.out.println("  - Oda " + room.getRoomId() + " (Kalan oyuncu sayısı: " + room.getConnectedPlayerCount() + ")");
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
    public synchronized void cleanupWaitingRooms() {
    System.out.println("🧹 Bekleyen odalar temizleniyor...");
    
    List<GameRoom> toRemove = new ArrayList<>();
    
    for (GameRoom room : waitingForReplacementRooms) {
        System.out.println("🔍 Oda kontrol ediliyor: " + room.getRoomId() + 
                          " | Oyun bitti: " + room.isGameEnded() + 
                          " | Bağlı oyuncu: " + room.getConnectedPlayerCount());
        
        // Oda artık geçersizse veya oyuncu sayısı 0 ise
        if (room.isGameEnded() || room.getConnectedPlayerCount() == 0) {
            toRemove.add(room);
            System.out.println("❌ Geçersiz oda bulundu: " + room.getRoomId());
        }
    }
    
    // Geçersiz odaları temizle
    for (GameRoom room : toRemove) {
        waitingForReplacementRooms.remove(room);
        gameRooms.remove(room.getRoomId());
        
        try {
            room.endGame("Oda temizlendi - Zaman aşımı");
            System.out.println("🗑️ Geçersiz oda temizlendi: " + room.getRoomId());
        } catch (IOException e) {
            System.err.println("❌ Oda temizlenirken hata: " + room.getRoomId() + " - " + e.getMessage());
        }
    }
    
    System.out.println("✅ Temizlik tamamlandı - " + toRemove.size() + " oda temizlendi");
    System.out.println("📊 Kalan bekleyen oda sayısı: " + waitingForReplacementRooms.size());
    System.out.println("📊 Aktif oyun odası sayısı: " + gameRooms.size());
}
    
    public static void main(String[] args) {
        GameServer server = null;
        try {
            server = new GameServer(8080);
            server.startAcceptance();
            
            System.out.println("🎮 Tavla Sunucusu Çalışıyor!");
            System.out.println("📝 Komutlar:");
            System.out.println("   'status' - Sunucu durumunu göster");
            System.out.println("   'clean' - Bekleyen odaları temizle");
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
                        
                    case "clean":
                        server.cleanupWaitingRooms();
                        System.out.println("🧹 Bekleyen odalar temizlendi");
                        break;
                        
                    case "help":
                        System.out.println("📝 Komutlar: status, clean, exit, help");
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