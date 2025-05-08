/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kendininesandn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JLabel;

/**
 *
 * @author zeysu
 */
public class Taslabel extends JLabel{
  private boolean showTas = false;
    private Color tasRenk = Color.BLACK;
    private boolean secili = false;
    private Point dragPoint;
    private int originalX, originalY;
    private BoardPanel boardPanel;  // BoardPanel referansı
    private int triangleIndex = -1; // Taşın hangi üçgende olduğunu takip et

    // Taşı görünür yap ve rengini belirle
    public void setTasGoster(boolean goster, Color renk) {
        this.showTas = goster;
        this.tasRenk = renk;
        repaint();  // yeniden çiz
    }
// BoardPanel referansını ayarla
    public void setBoardPanel(BoardPanel panel, int triangleIndex) {
        this.boardPanel = panel;
        this.triangleIndex = triangleIndex;
        setupMouseListeners();
    }
    
 // Taşın üçgen indeksini al
    public int getTriangleIndex() {
        return this.triangleIndex;
    }
    
    // Taşın üçgen indeksini ayarla
    public void setTriangleIndex(int index) {
        this.triangleIndex = index;
    }
    
    // Taşın rengini al
    public Color getTasRenk() {
        return this.tasRenk;
    }
    
    // Sürükle-bırak özelliği için fare dinleyicileri
    private void setupMouseListeners() {
        // Fare basıldığında
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!showTas) return;
                
                secili = true;
                originalX = getX();
                originalY = getY();
                dragPoint = e.getPoint();
                
                // Taşı en üste getir
                getParent().setComponentZOrder(Taslabel.this, 0);
                boardPanel.setSelectedPiece(Taslabel.this);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!secili) return;
                
                secili = false;
                
                // Bırakılan yerdeki üçgeni bul
                int targetTriangleIndex = boardPanel.findTriangleAt(getX() + getWidth()/2, getY() + getHeight()/2);
                
                if (targetTriangleIndex != -1) {
                    // Taşın hareketini BoardPanel'e bildir
                    boardPanel.movePiece(Taslabel.this, triangleIndex, targetTriangleIndex);
                } else {
                    // Taş geçerli bir üçgene bırakılmadıysa orijinal konumuna geri dön
                    setLocation(originalX, originalY);
                }
                
                repaint();
            }
        });

        // Fare sürüklendiğinde
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!secili) return;
                
                // Yeni konum hesapla
                int newX = getX() + e.getX() - dragPoint.x;
                int newY = getY() + e.getY() - dragPoint.y;
                
                // Taşı taşı
                setLocation(newX, newY);
            }
        });
    }
     @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showTas) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Seçili taşa vurgu efekti
            if (secili) {
                g2.setColor(tasRenk.brighter());
                int highlightSize = 4;
                g2.fillOval(-highlightSize/2, -highlightSize/2, getWidth() + highlightSize, getHeight() + highlightSize);
            }
            
            g2.setColor(tasRenk);
            int diameter = Math.min(getWidth(), getHeight()) - 10;  // kenarlardan boşluk
            g2.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
        }
    }
}
