/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.kendininesandn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 *
 * @author zeysu
 */
public class ehe extends javax.swing.JFrame {
          private Triangle[] triangles = new Triangle[24];
    private JButton zarAtButton, newGameButton;
    private JLabel zar1Label, zar2Label;
    private JLabel turnLabel;
    private JLabel whiteScoreLabel, blackScoreLabel;
    private Random random;
    private boolean whiteTurn = true;
    private BoardPanel board;
    private boolean diceRolled = false;
    
    public ehe() {
 setTitle("Tavla Oyunu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(1200, 700);

        // Ana içerik paneli
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Tavla tahtası
        board = new BoardPanel();
        board.setParentFrame(this); // BoardPanel'e bu frame'i tanıt
        mainPanel.add(board, BorderLayout.CENTER);
        
        // Kontrol paneli (sağ taraf)
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(150, 600));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(139, 69, 19));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        // Sıra göstergesi
        turnLabel = new JLabel("Sıra: BEYAZ");
        turnLabel.setPreferredSize(new Dimension(150, 40));
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setMaximumSize(new Dimension(150, 40));
        turnLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Skor göstergeleri
        whiteScoreLabel = new JLabel("Beyaz: 0/15");
        whiteScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        whiteScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        whiteScoreLabel.setForeground(Color.WHITE);
        whiteScoreLabel.setMaximumSize(new Dimension(150, 30));
        whiteScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        blackScoreLabel = new JLabel("Siyah: 0/15");
        blackScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        blackScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        blackScoreLabel.setForeground(Color.BLACK);
        blackScoreLabel.setMaximumSize(new Dimension(150, 30));
        blackScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Zar atma butonu
        zarAtButton = new JButton("Zar At");
        zarAtButton.setFont(new Font("Arial", Font.BOLD, 14));
        zarAtButton.setFocusable(false);
        zarAtButton.setMaximumSize(new Dimension(120, 40));
        zarAtButton.setAlignmentX(CENTER_ALIGNMENT);
        
        // Yeni oyun butonu
        newGameButton = new JButton("Yeni Oyun");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setFocusable(false);
        newGameButton.setMaximumSize(new Dimension(120, 40));
        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        
        // Zar göstergeleri paneli
        JPanel dicePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        dicePanel.setOpaque(false);
        dicePanel.setMaximumSize(new Dimension(120, 60));
        
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
        
        // Random nesnesi oluştur
        random = new Random();
        
        // Zar atma düğmesi için action listener
        zarAtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!diceRolled) {
                    int zar1 = random.nextInt(6) + 1;
                    int zar2 = random.nextInt(6) + 1;
                    
                    zar1Label.setText(String.valueOf(zar1));
                    zar2Label.setText(String.valueOf(zar2));
                    
                    if (board != null) {
                        board.setDiceValues(zar1, zar2);
                        board.setDiceRolled(true);
                    }
                    
                    diceRolled = true;
                    zarAtButton.setEnabled(false);
                    
                    // Çift zar durumunda buton metnini güncelle
                    if (zar1 == zar2) {
                        zarAtButton.setText("Çift " + zar1);
                    }
                }
            }
        });
        
        // Yeni oyun düğmesi için action listener
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        
        // Kontrol paneline elemanları ekle
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(turnLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(whiteScoreLabel);
        controlPanel.add(blackScoreLabel);
        controlPanel.add(Box.createVerticalStrut(30));
        controlPanel.add(zarAtButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(dicePanel);
        controlPanel.add(Box.createVerticalStrut(40));
        controlPanel.add(newGameButton);
        
        // Ana panele kontrol panelini ekle
        mainPanel.add(controlPanel, BorderLayout.EAST);
        
        // Frame'e ana paneli ekle
        setContentPane(mainPanel);
        
        // Merkeze hizala
        setLocationRelativeTo(null);
        
        // Form gösterilmeden önce BoardPanel'e başlangıç sırasını bildir
        if (board != null) {
            board.setWhiteTurn(whiteTurn);
        }
        
        // Periyodik güncelleme için timer (skorları günceller)
        javax.swing.Timer timer = new javax.swing.Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScores();
                updateTurnStatus();
            }
        });
        timer.start();
        
        setVisible(true);
    }
    
    // Oyuncu sırasını değiştiren metod
    public void changeTurn() {
        whiteTurn = !whiteTurn;
        updateTurnLabel();
        
        // Yeni tur için hazırlık
        diceRolled = false;
        zarAtButton.setEnabled(true);
        zarAtButton.setText("Zar At");
        zar1Label.setText("0");
        zar2Label.setText("0");
        
        if (board != null) {
            board.setWhiteTurn(whiteTurn);
        }
    }

    // Sıra etiketini güncelle
    private void updateTurnLabel() {
        if (whiteTurn) {
            turnLabel.setText("Sıra: BEYAZ");
            turnLabel.setForeground(Color.WHITE);
        } else {
            turnLabel.setText("Sıra: SİYAH");
            turnLabel.setForeground(Color.BLACK);
        }
    }
    
    // Skorları güncelle
    private void updateScores() {
        if (board != null) {
            whiteScoreLabel.setText("Beyaz: " + board.whiteHome.size() + "/15");
            blackScoreLabel.setText("Siyah: " + board.blackHome.size() + "/15");
        }
    }
    
    // Sıra durumunu güncelle
    private void updateTurnStatus() {
        // BoardPanel'deki gerçek sıra durumunu kontrol et
        if (board != null && board.isWhiteTurn() != whiteTurn) {
            whiteTurn = board.isWhiteTurn();
            updateTurnLabel();
        }
        
        // Zar durumunu kontrol et ve güncelle
        if (board != null) {
            diceRolled = board.isDiceRolled();
            zarAtButton.setEnabled(!diceRolled);
        }
    }
    
    // Oyunu sıfırla
    private void resetGame() {
        whiteTurn = true; // Başlangıç sırası beyaza ver
        diceRolled = false;
        updateTurnLabel();
        
        zarAtButton.setEnabled(true);
        zarAtButton.setText("Zar At");
        zar1Label.setText("0");
        zar2Label.setText("0");
        
        // BoardPanel'i sıfırla
        if (board != null) {
            board.resetGame();
        }
    }
    
    private void setupTriangles() {
        int width = 60;
        int upY = 0;
        int downY = 600;

        for (int i = 0; i < 12; i++) {
            triangles[i] = new Triangle(i * width + 20, upY, true);
        }
        for (int i = 0; i < 12; i++) {
            triangles[i + 12] = new Triangle((11 - i) * width + 20, downY, false);
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 3072, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 896, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ehe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ehe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ehe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ehe.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
