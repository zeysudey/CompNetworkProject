/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientk;

/**
 *
 * @author zeysu
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Sunucuya bağlanmak için giriş ekranı
 */
public class NetworkGiris extends JFrame {
    
    // Sabit sunucu ayarları
    private static final String SERVER_ADDRESS = "13.60.97.46";
    private static final int SERVER_PORT = 8080;
    
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel playerNameLabel;
    private JTextField playerNameField;
    private JButton connectButton;
    private JLabel statusLabel;
    
    public NetworkGiris() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tavla Online");
        setResizable(false);
        
        // Ana panel
        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(139, 69, 19));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Başlık
        titleLabel = new JLabel("TAVLA ONLINE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 20, 40, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Oyuncu adı etiketi
        playerNameLabel = new JLabel("Oyuncu Adınız:");
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playerNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(playerNameLabel, gbc);
        
        // Oyuncu adı text field
        playerNameField = new JTextField("Oyuncu1", 20);
        playerNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        playerNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectButtonActionPerformed(e);
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(playerNameField, gbc);
        
        // Sunucu bilgi etiketi
        JLabel serverInfoLabel = new JLabel("Sunucu: " + SERVER_ADDRESS + ":" + SERVER_PORT);
        serverInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        serverInfoLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(serverInfoLabel, gbc);
        
        // Bağlan butonu
        connectButton = new JButton("OYUNA BAŞLA");
        connectButton.setBackground(new Color(51, 51, 51));
        connectButton.setFont(new Font("Arial", Font.BOLD, 20));
        connectButton.setForeground(Color.WHITE);
        connectButton.setPreferredSize(new Dimension(200, 50));
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(connectButton, gbc);
        
        // Durum etiketi
        statusLabel = new JLabel("Oyuncu adınızı girin ve başlayın!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 30, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(statusLabel, gbc);
        
        // Ana paneli frame'e ekle
        add(mainPanel);
        pack();
        
        // Text field'e focus ver
        playerNameField.requestFocus();
        playerNameField.selectAll();
    }

    private void connectButtonActionPerformed(ActionEvent evt) {
        String playerName = playerNameField.getText().trim();
        
        // Oyuncu adı kontrolü
        if (playerName.isEmpty()) {
            statusLabel.setText("Lütfen oyuncu adınızı girin!");
            statusLabel.setForeground(Color.RED);
            playerNameField.requestFocus();
            return;
        }
        
        if (playerName.length() < 2) {
            statusLabel.setText("Oyuncu adı en az 2 karakter olmalı!");
            statusLabel.setForeground(Color.RED);
            playerNameField.requestFocus();
            return;
        }
        
        if (playerName.length() > 15) {
            statusLabel.setText("Oyuncu adı en fazla 15 karakter olabilir!");
            statusLabel.setForeground(Color.RED);
            playerNameField.requestFocus();
            return;
        }
        
        // Bağlantı butonunu devre dışı bırak
        connectButton.setEnabled(false);
        playerNameField.setEnabled(false);
        statusLabel.setText("Sunucuya bağlanılıyor...");
        statusLabel.setForeground(Color.YELLOW);
        
        // Bağlantıyı ayrı thread'de yap
        SwingWorker<Boolean, Void> connectionWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    System.out.println("Bağlantı deneniyor: " + SERVER_ADDRESS + ":" + SERVER_PORT);
                    NetworkClient client = new NetworkClient(SERVER_ADDRESS, SERVER_PORT);
                    boolean connected = client.connect();
                    
                    if (connected) {
                        System.out.println("Sunucuya bağlandı! Oyuncu: " + playerName);
                        
                        // OYUN FRAME'İNİ HEMEN OLUŞTUR!
                        SwingUtilities.invokeAndWait(() -> {
                            try {
                                NetworkGameFrame gameFrame = new NetworkGameFrame(client, playerName);
                                gameFrame.setVisible(true);
                                NetworkGiris.this.dispose();
                                System.out.println("NetworkGameFrame başarıyla açıldı!");
                            } catch (Exception e) {
                                System.err.println("NetworkGameFrame açılamadı: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                        
                        // Sadece oyuncu adını gönder
                        Thread.sleep(100);
                        client.sendPlayerName(playerName);
                        
                        return true;
                    } else {
                        System.out.println("Sunucuya bağlanılamadı!");
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("Bağlantı hatası: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (!success) {
                        statusLabel.setText("Sunucuya bağlanılamadı! Sunucu çalışıyor mu?");
                        statusLabel.setForeground(Color.RED);
                        connectButton.setEnabled(true);
                        playerNameField.setEnabled(true);
                        playerNameField.requestFocus();
                    }
                } catch (Exception e) {
                    statusLabel.setText("Bağlantı hatası: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    connectButton.setEnabled(true);
                    playerNameField.setEnabled(true);
                    playerNameField.requestFocus();
                }
            }
        };
        
        connectionWorker.execute();
    }
    
    public static void main(String args[]) {
        // Look and feel ayarları - hata yakalamak için try-catch
        try {
            // Nimbus look and feel dene
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Nimbus look and feel bulunamadı, varsayılan kullanılacak");
        } catch (InstantiationException ex) {
            System.out.println("Look and feel başlatılamadı, varsayılan kullanılacak");
        } catch (IllegalAccessException ex) {
            System.out.println("Look and feel erişim hatası, varsayılan kullanılacak");
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println("Look and feel desteklenmiyor, varsayılan kullanılacak");
        }

        // Ana form oluştur ve göster
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new NetworkGiris().setVisible(true);
            }
        });
    }
}