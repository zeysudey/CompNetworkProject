/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientk;

/**
 *
 * @author zeysu
 */
import java.awt.BasicStroke;
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
 * Ağ tabanlı oyun için taş temsili
 */
public class NetworkTaslabel extends JLabel {
    
 private boolean showTas = false;
    private Color tasRenk = Color.BLACK;
    private boolean secili = false;
    private Point dragPoint;
    private int originalX, originalY;
    private NetworkBoardPanel boardPanel;
    private int triangleIndex = -1;
    private boolean hover = false;
    private static final int WHITE_HOME_INDEX = 25;
    private static final int BLACK_HOME_INDEX = 26;

    public NetworkTaslabel() {
        // Default constructor
    }

    /**
     * Taş görüntüsünü ayarlar
     */
    public void setTasGoster(boolean goster, Color renk) {
        this.showTas = goster;
        this.tasRenk = renk;
        repaint();
    }

    /**
     * Taşın beyaz olup olmadığını kontrol eder
     */
    public boolean isWhite() {
        return tasRenk == Color.WHITE;
    }

    /**
     * BoardPanel'i ayarlar ve mouse listener'ları ekler
     */
    public void setBoardPanel(NetworkBoardPanel panel, int triangleIndex) {
        this.boardPanel = panel;
        this.triangleIndex = triangleIndex;
        setupMouseListeners();
    }
    
    /**
     * Üçgen indeksini döndürür
     */
    public int getTriangleIndex() {
        return this.triangleIndex;
    }
    
    /**
     * Üçgen indeksini ayarlar
     */
    public void setTriangleIndex(int index) {
        this.triangleIndex = index;
    }
    
    /**
     * Taş rengini döndürür
     */
    public Color getTasRenk() {
        return this.tasRenk;
    }
    
    /**
     * Taşın seçili durumunu ayarlar
     */
    public void setSelected(boolean selected) {
        this.secili = selected;
        repaint();
    }
    
    /**
     * Mouse listener'ları kurar - İYİLEŞTİRİLMİŞ DRAG & DROP
     */
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!showTas || boardPanel == null) return;
                
                // Home'daki taşlar hareket ettirilmez
                if (triangleIndex == WHITE_HOME_INDEX || triangleIndex == BLACK_HOME_INDEX) {
                    System.out.println("🏠 Home'daki taşlar hareket ettirilemez!");
                    return;
                }
                
                // Sadece kendi sırasında ve kendi taşlarını hareket ettirebilir
                if (!boardPanel.isMyTurn()) {
                    System.out.println("⏳ Sıra sizde değil!");
                    return;
                }
                
                // Renk kontrolü
                boolean pieceIsWhite = (tasRenk == Color.WHITE);
                if (pieceIsWhite != boardPanel.isWhitePlayer()) {
                    System.out.println("🚫 Bu taş size ait değil!");
                    return;
                }
                
                // Taş için geçerli hamleleri kontrol et
                boardPanel.updateLegalMoves(NetworkTaslabel.this);
                if (boardPanel.getLegalMoves().isEmpty()) {
                    System.out.println("❌ Bu taş için geçerli hamle yok!");
                    boardPanel.clearLegalMoves();
                    return;
                }
                
                // Geçerli hamle var, taşı seç ve drag başlat
                secili = true;
                originalX = getX();
                originalY = getY();
                dragPoint = e.getPoint();
                
                // Taşı en üste getir (z-order)
                getParent().setComponentZOrder(NetworkTaslabel.this, 0);
                boardPanel.setSelectedPiece(NetworkTaslabel.this);
                
                System.out.println("✅ Taş seçildi: " + triangleIndex + " (Renk: " + (pieceIsWhite ? "BEYAZ" : "SİYAH") + ")");
                System.out.println("🎯 Geçerli hedefler: " + boardPanel.getLegalMoves());
                
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!secili || boardPanel == null) return;
                
                // Seçimi iptal et
                secili = false;
                
                // Drop konumunu hesapla (taşın merkez noktasına göre)
                int dropX = getX() + getWidth() / 2;
                int dropY = getY() + getHeight() / 2;
                
                // Hedef üçgeni bul
                int targetTriangleIndex = boardPanel.findTriangleAt(dropX, dropY);
                
                System.out.println("🎯 Drop pozisyonu: (" + dropX + ", " + dropY + ")");
                System.out.println("🔍 Hedef üçgen: " + targetTriangleIndex);
                
                // Geçerli hamleler içinde mi kontrol et
                boolean isValidDrop = (targetTriangleIndex != -1) && 
                                    boardPanel.getLegalMoves().contains(targetTriangleIndex);
                
                if (isValidDrop) {
                    // Geçerli hamle - sunucuya gönder
                    System.out.println("✅ Geçerli hamle! Sunucuya gönderiliyor: " + triangleIndex + " → " + targetTriangleIndex);
                    boardPanel.onPlayerMove(NetworkTaslabel.this, triangleIndex, targetTriangleIndex);
                } else {
                    // Geçersiz hamle - taşı orijinal konumuna geri döndür
                    System.out.println("❌ Geçersiz hamle! Taş orijinal konumuna dönüyor.");
                    animateReturnToOriginalPosition();
                }
                
                // Geçerli hamleleri temizle
                boardPanel.clearLegalMoves();
                repaint();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!showTas || boardPanel == null) return;
                
                // Home'daki taşlar için hover efekti yok
                if (triangleIndex == WHITE_HOME_INDEX || triangleIndex == BLACK_HOME_INDEX) {
                    return;
                }
                
                // Sadece kendi taşları ve kendi sırası için hover
                boolean pieceIsWhite = (tasRenk == Color.WHITE);
                if (boardPanel.isMyTurn() && pieceIsWhite == boardPanel.isWhitePlayer()) {
                    hover = true;
                    boardPanel.setHoverPiece(NetworkTaslabel.this);
                    
                    // Hover sırasında geçerli hamleleri göster
                    if (boardPanel.getSelectedPiece() == null) {  // Sadece seçili taş yoksa
                        boardPanel.updateLegalMoves(NetworkTaslabel.this);
                    }
                    
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!showTas || boardPanel == null) return;
                
                hover = false;
                boardPanel.setHoverPiece(null);
                
                // Hover sırasında gösterilen hamleleri temizle (eğer taş seçili değilse)
                if (boardPanel.getSelectedPiece() == null) {
                    boardPanel.clearLegalMoves();
                }
                
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!secili) return;
                
                // Smooth drag hareketi
                int newX = getX() + e.getX() - dragPoint.x;
                int newY = getY() + e.getY() - dragPoint.y;
                
                // Tahtanın sınırları içinde tut (isteğe bağlı)
                newX = Math.max(0, Math.min(newX, getParent().getWidth() - getWidth()));
                newY = Math.max(0, Math.min(newY, getParent().getHeight() - getHeight()));
                
                setLocation(newX, newY);
                
                // Real-time hedef gösterimi (isteğe bağlı)
                int currentDropTarget = boardPanel.findTriangleAt(newX + getWidth()/2, newY + getHeight()/2);
                if (currentDropTarget != -1 && boardPanel.getLegalMoves().contains(currentDropTarget)) {
                    // Geçerli hedef üzerinde - görsel feedback verilebilir
                    setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
                }
            }
        });
    }
    
    /**
     * Taşı orijinal konumuna yumuşak bir şekilde döndürür
     */
    private void animateReturnToOriginalPosition() {
        // Basit animasyon - direkt geri döndürme
        // İleride smooth animation eklenebilir
        setLocation(originalX, originalY);
        
        // Cursor'ı normale döndür
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showTas) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Taş gölgesi (drag sırasında)
            if (secili) {
                g2.setColor(new Color(0, 0, 0, 50));
                int diameter = Math.min(getWidth(), getHeight()) - 8;
                g2.fillOval((getWidth() - diameter) / 2 + 2, (getHeight() - diameter) / 2 + 2, diameter, diameter);
            }
            
            // Ana taş
            g2.setColor(tasRenk);
            int diameter = Math.min(getWidth(), getHeight()) - 10;
            g2.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
            
            // Kenar çizgisi
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
            
            // Seçili taş vurgulama
            if (secili) {
                Color highlightColor = tasRenk.equals(Color.BLACK) ? Color.CYAN : Color.RED;
                g2.setColor(highlightColor);
                g2.setStroke(new BasicStroke(3.0f));
                int borderDiameter = diameter + 4;
                int borderX = (getWidth() - borderDiameter) / 2;
                int borderY = (getHeight() - borderDiameter) / 2;
                g2.drawOval(borderX, borderY, borderDiameter, borderDiameter);
            }
            // Hover efekti
            else if (hover) {
                g2.setColor(new Color(255, 255, 0, 120)); // Yarı saydam altın sarısı
                g2.setStroke(new BasicStroke(2.0f));
                int hoverDiameter = diameter + 6;
                int hoverX = (getWidth() - hoverDiameter) / 2;
                int hoverY = (getHeight() - hoverDiameter) / 2;
                g2.drawOval(hoverX, hoverY, hoverDiameter, hoverDiameter);
            }
            
            // Drag cursor göstergesi
            if (secili) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillOval((getWidth() - 8) / 2, (getHeight() - 8) / 2, 8, 8);
            }
        }
    }
}