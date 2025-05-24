/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author zeysu
 */
public class PlayerImpl implements Player{
   private String name;
    private int id;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = true;
    
    // Oyun alanları
    private boolean isTurn = false;
    private boolean isWhite = false;
    private int piecesHome = 0;
    private int piecesInPlay = 15;
    
    // Constructor
    public PlayerImpl(String name, int id, Socket socket) throws IOException {
        this.name = name;
        this.id = id;
        this.socket = socket;
        
        // Stream'leri initialize et
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        System.out.println("PlayerImpl oluşturuldu: " + name + " (ID: " + id + ")");
    }
    
    // Interface metodları - Mevcut
    @Override
    public int getPlayerId() {
        return id;
    }
    
    @Override
    public boolean isWhitePlayer() {
        return isWhite; // isWhite ile aynı
    }
    
    @Override
    public String getPlayerName() {
        return name;
    }
    
    // Interface metodları - Yeni eklenenler
    public boolean isTurn() {
        return isTurn;
    }
    
    public void setTurn(boolean isTurn) {
        this.isTurn = isTurn;
        System.out.println("🎯 " + name + " sırası: " + (isTurn ? "AÇIK ✅" : "KAPALI ❌"));
        
        // Oyuncuya durumu bildir
        sendMessage("TURN_STATUS#" + isTurn);
    }
    
    public boolean isWhite() {
        return isWhite;
    }
    
    
    public void setWhite(boolean isWhite) {
        this.isWhite = isWhite;
        System.out.println("🎨 " + name + " rengi: " + (isWhite ? "BEYAZ" : "SİYAH"));
    }
    
    public int getPiecesHome() {
        return piecesHome;
    }
    
    public void incrementPiecesHome() {
        piecesHome++;
        piecesInPlay--;
        System.out.println("🏠 " + name + " eve taş getirdi: " + piecesHome + "/15");
    }
    
    public int getPiecesInPlay() {
        return piecesInPlay;
    }
    
    public String getScoreString() {
        return name + ": " + piecesHome + "/15";
    }
    
    public void resetForNewGame() {
        isTurn = false;
        piecesHome = 0;
        piecesInPlay = 15;
        System.out.println("🔄 " + name + " oyunu sıfırlandı");
    }
    
    
    public void printStatus() {
        System.out.println("=== " + name + " DURUMU ===");
        System.out.println("ID: " + id);
        System.out.println("Renk: " + (isWhite ? "BEYAZ" : "SİYAH"));
        System.out.println("Sıra: " + (isTurn ? "VAR" : "YOK"));
        System.out.println("Skor: " + piecesHome + "/15");
        System.out.println("Bağlantı: " + (isConnected ? "AKTİF" : "KESİK"));
        System.out.println("========================");
    }
    
    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
            System.out.println("➤ " + name + "'e mesaj: " + message);
        }
    }
    
    public String receiveMessage() throws IOException {
        if (in != null && isConnected) {
            return in.readLine();
        }
        return null;
    }
    
    public void disconnect() throws IOException {
        isConnected = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("Oyuncu bağlantısı kesildi: " + name);
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
    
   
    
    @Override
    public String toString() {
        return "PlayerImpl{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", isWhite=" + isWhite +
                ", isTurn=" + isTurn +
                ", score=" + piecesHome + "/15" +
                '}';
    }  
}
