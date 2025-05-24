/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientk;

/**
 *
 * @author zeysu
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Gerçek tavla oyunu ana penceresi
 */
public class NetworkGameFrame extends javax.swing.JFrame implements NetworkGameListener {
     NetworkClient networkClient;
    private String playerName;
    private boolean isWhitePlayer;
    private boolean isMyTurn = false;
    
    int myPlayerId;
    private int opponentPlayerId;
    
    // UI Bileşenleri
    private JButton zarAtButton, newGameButton, disconnectButton;
    private JButton passTurnButton;
    private JLabel zar1Label, zar2Label;
    private JLabel turnLabel, connectionLabel;
    private JLabel myScoreLabel, opponentScoreLabel;
    private NetworkBoardPanel board;
    private boolean diceRolled = false;
    
    public NetworkGameFrame(NetworkClient client, String playerName) {
        this.networkClient = client;
        this.playerName = playerName;
        
        if (this.networkClient != null) {
            this.networkClient.setGameListener(this);
        }
        
        initializeUI();
        setupEventHandlers();
        // Player ID'yi board'a aktar - ÖNEMLİ!
    if (board != null) {
        board.setMyPlayerId(this.myPlayerId); // Bu henüz -1 olabilir
    }
        setTitle("Tavla Online - " + playerName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        System.out.println("NetworkGameFrame oluşturuldu - Oyuncu: " + playerName);
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        board = new NetworkBoardPanel(this);
        mainPanel.add(board, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(140, 600));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(139, 69, 19, 180));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
        
        // Bağlantı durumu
        connectionLabel = new JLabel("Bağlantı: Aktif");
        connectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        connectionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        connectionLabel.setForeground(Color.GREEN);
        connectionLabel.setMaximumSize(new Dimension(180, 25));
        connectionLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Sıra göstergesi
        turnLabel = new JLabel("Sıra: Bekliyor");
        turnLabel.setPreferredSize(new Dimension(180, 40));
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setMaximumSize(new Dimension(180, 40));
        turnLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Skor göstergeleri
        myScoreLabel = new JLabel(playerName + ": 0/15");
        myScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        myScoreLabel.setForeground(Color.WHITE);
        myScoreLabel.setMaximumSize(new Dimension(180, 30));
        myScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        opponentScoreLabel = new JLabel("Rakip: 0/15");
        opponentScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        opponentScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        opponentScoreLabel.setForeground(Color.BLACK);
        opponentScoreLabel.setMaximumSize(new Dimension(180, 30));
        opponentScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Zar atma butonu
        zarAtButton = new JButton("Zar At");
        zarAtButton.setFont(new Font("Arial", Font.BOLD, 14));
        zarAtButton.setFocusable(false);
        zarAtButton.setMaximumSize(new Dimension(160, 40));
        zarAtButton.setAlignmentX(CENTER_ALIGNMENT);
        zarAtButton.setEnabled(false);
        
        // Manuel sıra geçme butonu
        passTurnButton = new JButton("Sırayı Geç");
        passTurnButton.setFont(new Font("Arial", Font.BOLD, 12));
        passTurnButton.setFocusable(false);
        passTurnButton.setMaximumSize(new Dimension(160, 35));
        passTurnButton.setAlignmentX(CENTER_ALIGNMENT);
        passTurnButton.setEnabled(false);
        passTurnButton.setBackground(new Color(200, 100, 50));
        passTurnButton.setForeground(Color.WHITE);
        
        // Yeni oyun butonu
        newGameButton = new JButton("Yeni Oyun");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setFocusable(false);
        newGameButton.setMaximumSize(new Dimension(160, 40));
        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        newGameButton.setEnabled(false);
        
        // Bağlantı kes butonu
        disconnectButton = new JButton("Bağlantı Kes");
        disconnectButton.setFont(new Font("Arial", Font.BOLD, 12));
        disconnectButton.setFocusable(false);
        disconnectButton.setMaximumSize(new Dimension(160, 35));
        disconnectButton.setAlignmentX(CENTER_ALIGNMENT);
        
        // Zar göstergeleri paneli
        JPanel dicePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        dicePanel.setOpaque(false);
        dicePanel.setMaximumSize(new Dimension(160, 60));
        
        zar1Label = new JLabel("0");
        zar1Label.setHorizontalAlignment(SwingConstants.CENTER);
        zar1Label.setFont(new Font("Arial", Font.BOLD, 28));
        zar1Label.setForeground(Color.BLACK);
        zar1Label.setBackground(Color.WHITE);
        zar1Label.setOpaque(true);
        zar1Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        zar2Label = new JLabel("0");
        zar2Label.setHorizontalAlignment(SwingConstants.CENTER);
        zar2Label.setFont(new Font("Arial", Font.BOLD, 28));
        zar2Label.setForeground(Color.BLACK);
        zar2Label.setBackground(Color.WHITE);
        zar2Label.setOpaque(true);
        zar2Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        dicePanel.add(zar1Label);
        dicePanel.add(zar2Label);
        
        // Kontrol paneline elemanları ekle
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(connectionLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(turnLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(myScoreLabel);
        controlPanel.add(opponentScoreLabel);
        controlPanel.add(Box.createVerticalStrut(30));
        controlPanel.add(zarAtButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(passTurnButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(dicePanel);
        controlPanel.add(Box.createVerticalStrut(30));
        controlPanel.add(newGameButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(disconnectButton);
        
        mainPanel.add(controlPanel, BorderLayout.EAST);
        setContentPane(mainPanel);
    }
    
    private void setupEventHandlers() {
        // Zar atma düğmesi
        zarAtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isMyTurn && !diceRolled && networkClient != null && networkClient.isConnected()) {
                    networkClient.requestDiceRoll();
                    zarAtButton.setEnabled(false);
                    System.out.println("🎲 Zar atma isteği gönderildi");
                }
            }
        });
        
        // Manuel sıra geçme butonu
        passTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isMyTurn && networkClient != null && networkClient.isConnected()) {
                    // Zar atılmış mı kontrol et
                    if (!diceRolled) {
                        JOptionPane.showMessageDialog(
                            NetworkGameFrame.this,
                            "Önce zar atmalısınız!",
                            "Bilgi",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                    
                    // Normal sıra geçişi
                    networkClient.requestPassTurn();
                    System.out.println("🔄 Manuel sıra geçme isteği gönderildi");
                    
                    // UI'ı güncelle
                    isMyTurn = false;
                    diceRolled = false;
                    updateTurnUI(false);
                    clearDiceDisplay();
                }
            }
        });
        
        // Yeni oyun düğmesi
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(NetworkGameFrame.this, 
                    "Yeni oyun özelliği henüz aktif değil.");
            }
        });
        
        // Bağlantı kes düğmesi
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(NetworkGameFrame.this,
                    "Oyundan çıkmak istediğinize emin misiniz?", 
                    "Çıkış", JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    if (networkClient != null) {
                        networkClient.disconnect();
                    }
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * Sıra UI'ını günceller - GELİŞTİRİLDİ
     */
    private void updateTurnUI(boolean myTurn) {
        System.out.println("\n=== 🔄 TURN UI GÜNCELLENİYOR ===");
        System.out.println("updateTurnUI çağrıldı! myTurn=" + myTurn + ", myPlayerId=" + myPlayerId);
        
        SwingUtilities.invokeLater(() -> {
            zarAtButton.setEnabled(myTurn && !diceRolled);
            passTurnButton.setEnabled(myTurn && diceRolled);
            if (myTurn) {
                turnLabel.setText("Sıra: SİZDE");
                turnLabel.setForeground(Color.YELLOW);
                System.out.println("✅ Sıra bende - Zar butonu: " + (!diceRolled ? "AKTİF" : "PASİF"));
            } else {
                turnLabel.setText("Sıra: RAKİPTE");
                turnLabel.setForeground(Color.RED);
                System.out.println("❌ Sıra rakibin - Butonlar PASİF");
            }
            
            if (board != null) {
                board.setMyTurn(myTurn);
                if (!myTurn) {
                    board.clearLegalMoves();
                }
            }
            
            System.out.println("=== ✅ TURN UI GÜNCELLENDİ ===\n");
        });
    }
    
    /**
     * Zar görüntüsünü temizler
     */
    private void clearDiceDisplay() {
        SwingUtilities.invokeLater(() -> {
            zar1Label.setText("0");
            zar2Label.setText("0");
            zarAtButton.setText("Zar At");
            if (board != null) {
                board.clearLegalMoves();
            }
        });
    }
    
    // NetworkGameListener implementasyonu
   @Override
public void onGameStart(int player1Id, int player2Id, boolean isWhitePlayer) {
    if (networkClient != null) {
        networkClient.sendPlayerReady();
        System.out.println("[CLIENT] PLAYER_READY mesajı GAME_START sonrası gönderildi! (myId=" + player1Id + ", rakipId=" + player2Id + ", isWhite=" + isWhitePlayer + ")");
    }
    
    System.out.println("\n=== 🎮 OYUN BAŞLATILIYOR ===");
    System.out.println("onGameStart ÇAĞRILDI! player1Id=" + player1Id + ", player2Id=" + player2Id + ", isWhitePlayer=" + isWhitePlayer);
    
    this.myPlayerId = player1Id;
    this.opponentPlayerId = player2Id;
    this.isWhitePlayer = isWhitePlayer;
    
    // Oyun başında sıra kimseye verilmez, sadece TURN mesajı ile belirlenir
    this.isMyTurn = false;
    this.diceRolled = false;
    
    // BURADA PLAYER ID'YI BOARD'A AKTAR!
    if (board != null) {
        board.setMyPlayerId(this.myPlayerId);  // 🔥 ÖNEMLİ: Player ID'yi aktar
        board.setPlayerColor(isWhitePlayer);
        board.setMyTurn(isMyTurn);
        board.resetGame();
    }
    
    updateTurnUI(isMyTurn);
    clearDiceDisplay();
    
    System.out.println("✅ Oyun başlatıldı:");
    System.out.println("  Benim ID: " + this.myPlayerId);
    System.out.println("  Rakip ID: " + this.opponentPlayerId);
    System.out.println("  Renk: " + (isWhitePlayer ? "BEYAZ" : "SİYAH"));
    System.out.println("  İlk sıra: SUNUCUDAN TURN MESAJI GELİNCE BELİRLENECEK");
    System.out.println("=== ✅ OYUN BAŞLATILDI ===\n");
}
    
    @Override
    public void onDiceRoll(int dice1, int dice2) {
        System.out.println("\n===  ZAR ATILDI ===");
        System.out.println("ZAR SONUCU ALINDI: " + dice1 + ", " + dice2);
        
        SwingUtilities.invokeLater(() -> {
            zar1Label.setText(String.valueOf(dice1));
            zar2Label.setText(String.valueOf(dice2));
            if (board != null) {
                board.setDiceValues(dice1, dice2);
                board.setDiceRolled(true);
                board.setAvailableDice(dice1, dice2);
            }
            diceRolled = true;
            if (dice1 == dice2) {
                zarAtButton.setText("Çift " + dice1);
            }
            updateTurnUI(isMyTurn);
            System.out.println("=== ✅ ZAR UI GÜNCELLENDİ ===\n");
        });
    }
    
    @Override
    public void onPieceMove(int playerId, int fromTriangle, int toTriangle) {
        System.out.println("♟️ HAMLE ALINDI - Oyuncu: " + playerId + " | " + fromTriangle + " → " + toTriangle);
        
        SwingUtilities.invokeLater(() -> {
            // Rakibin hamlesi ise board'a uygula
            if (playerId != myPlayerId && board != null) {
                board.applyNetworkMove(playerId, fromTriangle, toTriangle);
            }
        });
    }
    
    @Override
    public void onTurnChange(int currentPlayerId, boolean isMyTurnParam) {
        System.out.println("\n=== 🔄 SIRA DEĞİŞİYOR ===");
        System.out.println("onTurnChange çağrıldı! currentPlayerId=" + currentPlayerId + ", myPlayerId=" + myPlayerId + ", isMyTurn=" + isMyTurnParam);
        this.isMyTurn = isMyTurnParam;
        this.diceRolled = false;
        if (board != null) {
            board.setMyTurn(isMyTurn);
            board.setDiceRolled(false);
            board.setAvailableDice(0, 0);
        }
        updateTurnUI(this.isMyTurn);
        clearDiceDisplay();
        System.out.println("✅ Sıra değişti:\n  Sıra: " + (isMyTurn ? "BENDE" : "RAKİPTE") + "\n  Zar durumu: SIFIRLANDI\n=== ✅ SIRA DEĞİŞTİ ===\n");
    }
    
    @Override
    public void onGameEnd(String reason) {
        System.out.println("🏁 OYUN BİTTİ: " + reason);
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Oyun bitti: " + reason, 
                "Oyun Sonu", JOptionPane.INFORMATION_MESSAGE);
            
            connectionLabel.setText("Oyun: Bitti");
            connectionLabel.setForeground(Color.RED);
            zarAtButton.setEnabled(false);
            passTurnButton.setEnabled(false);
            newGameButton.setEnabled(true);
        });
    }
    
    @Override
    public void onDisconnected() {
        System.out.println("🔌 BAĞLANTI KESİLDİ");
        
        SwingUtilities.invokeLater(() -> {
            connectionLabel.setText("Bağlantı: Kesildi");
            connectionLabel.setForeground(Color.RED);
            zarAtButton.setEnabled(false);
            passTurnButton.setEnabled(false);
            
            JOptionPane.showMessageDialog(this, "Sunucu bağlantısı kesildi!", 
                "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    @Override
    public void onError(String error) {
        System.out.println("❌ HATA: " + error);
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Hata: " + error, 
                "Oyun Hatası", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    // NetworkBoardPanel için callback metotları
    public void sendPieceMove(int fromTriangle, int toTriangle) {
        if (networkClient != null && networkClient.isConnected() && isMyTurn) {
            networkClient.sendMove(fromTriangle, toTriangle);
            System.out.println("♟️ Hamle gönderildi: " + fromTriangle + " → " + toTriangle);
        }
    }
    
    public boolean isMyTurn() {
        return isMyTurn;
    }
    
    public boolean isMyColor(boolean pieceIsWhite) {
        return pieceIsWhite == isWhitePlayer;
    }
    
    public void updateScores(int myScore, int opponentScore) {
        SwingUtilities.invokeLater(() -> {
            myScoreLabel.setText(playerName + ": " + myScore + "/15");
            opponentScoreLabel.setText("Rakip: " + opponentScore + "/15");
        });
    }
    
    // NetworkBoardPanel için oyuncu ID'si getter
    public int getMyPlayerId() {
        return myPlayerId;
    }
    
    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    private void handleTurnMessage(String[] tokens) {
        if (tokens.length < 2) return;
        String playerId = tokens[1];
        boolean myTurn = playerId.equals(String.valueOf(this.myPlayerId));
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("\n=== 🔄 TURN MESAJI ALINDI ===");
            System.out.println("Gelen playerId: " + playerId);
            System.out.println("Benim playerId: " + this.myPlayerId);
            System.out.println("Sıra bende mi: " + myTurn);
            
            if (myTurn) {
                zarAtButton.setEnabled(true);
                passTurnButton.setEnabled(false);
                System.out.println("✅ Sıra sizde - Zar At butonu AKTİF");
            } else {
                zarAtButton.setEnabled(false);
                passTurnButton.setEnabled(false);
                System.out.println("❌ Sıra rakibin - Butonlar PASİF");
            }
            
            if (board != null) {
                board.setMyTurn(myTurn);
                if (!myTurn) {
                    board.clearLegalMoves();
                    board.clearDiceDisplay();
                }
            }
            
            System.out.println("=== ✅ TURN UI GÜNCELLENDİ ===\n");
        });
    }

    private void handleDiceRollMessage(String[] tokens) {
        if (tokens.length < 3) return;
        
        int die1 = Integer.parseInt(tokens[1]);
        int die2 = Integer.parseInt(tokens[2]);
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("\n=== 🎲 DICE_ROLL MESAJI ALINDI ===");
            System.out.println("Zar1: " + die1 + ", Zar2: " + die2);
            
            if (board != null) {
                board.setDiceValues(die1, die2);
                board.setDiceRolled(true);
            }
            
            zarAtButton.setEnabled(false);
            passTurnButton.setEnabled(true);
            
            System.out.println("=== ✅ DICE_ROLL UI GÜNCELLENDİ ===\n");
        });
    }

    /**
     * Oyunu sıfırlar ve tahtayı temizler
     */
    public void resetGame() {
        if (board != null) {
            board.clearLegalMoves();
            board.clearDiceDisplay();
            // Eğer board'da taşları sıfırlayan bir fonksiyon varsa onu da çağırabilirsiniz:
            // board.resetBoard();
        }
        isMyTurn = false;
        diceRolled = false;
        updateTurnUI(false);
        clearDiceDisplay();
        updateScores(0, 0);
    }
}