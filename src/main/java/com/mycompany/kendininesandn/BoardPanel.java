/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.kendininesandn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import com.mycompany.kendininesandn.Taslabel;

/**
 *
 * @author zeysu
 */
public class BoardPanel extends javax.swing.JPanel {
 private Triangle[] triangles = new Triangle[24];
    private Image backgroundImage;
    private Taslabel selectedPiece = null;
    private Map<Integer, List<Taslabel>> pieces = new HashMap<>(); // Üçgenlerdeki taşları takip et
    
    /**
     * Creates new form BoardPanel
     */
    public BoardPanel() {
       initComponents();
        setLayout(null);
        setPreferredSize(new Dimension(1000, 600));

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/tav.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Her üçgen için boş liste oluştur
        for (int i = 0; i < 24; i++) {
            pieces.put(i, new ArrayList<>());
        }

        setupTriangles();
        setupInitialStones();
    }
    
    // Seçili taşı ayarla
    public void setSelectedPiece(Taslabel piece) {
        this.selectedPiece = piece;
    }
    
    // Üçgenlerin kurulumu
    private void setupTriangles() {
        // Arka plan görüntünüze göre ayarlanması gereken değerler
        int triangleWidth = 72;  // üçgen genişliği
        
        // Her üçgenin tam ortasını hesapla
        // Üçgen 12'nin x pozisyonu görüntüden belirlendi
        int x12 = 105; // 12 numaralı üçgenin x koordinatı
        int triangleSpacing = triangleWidth; // Üçgenler arası boşluk
        
        // Üst sıra - Sol taraf
        triangles[12] = new Triangle(x12, 50, true);
        triangles[13] = new Triangle(x12 + triangleSpacing, 50, true);
        triangles[14] = new Triangle(x12 + 2 * triangleSpacing, 50, true);
        triangles[15] = new Triangle(x12 + 3 * triangleSpacing, 50, true);
        triangles[16] = new Triangle(x12 + 4 * triangleSpacing, 50, true);
        triangles[17] = new Triangle(x12 + 5 * triangleSpacing, 50, true);
        
        // Üst sıra - Sağ taraf
        int x18 = 580; // 18 numaralı üçgenin x koordinatı
        triangles[18] = new Triangle(x18, 50, true);
        triangles[19] = new Triangle(x18 + triangleSpacing, 50, true);
        triangles[20] = new Triangle(x18 + 2 * triangleSpacing, 50, true);
        triangles[21] = new Triangle(x18 + 3 * triangleSpacing, 50, true);
        triangles[22] = new Triangle(x18 + 4 * triangleSpacing, 50, true);
        triangles[23] = new Triangle(x18 + 5 * triangleSpacing, 50, true);
        
        // Alt sıra - Sağ taraf
        int x0 = x18 + 5 * triangleSpacing; // 0 numaralı üçgenin x koordinatı
        triangles[0] = new Triangle(x0, 550, false);
        triangles[1] = new Triangle(x0 - triangleSpacing, 550, false);
        triangles[2] = new Triangle(x0 - 2 * triangleSpacing, 550, false);
        triangles[3] = new Triangle(x0 - 3 * triangleSpacing, 550, false);
        triangles[4] = new Triangle(x0 - 4 * triangleSpacing, 550, false);
        triangles[5] = new Triangle(x0 - 5 * triangleSpacing, 550, false);
        
        // Alt sıra - Sol taraf
        int x6 = x12 + 5 * triangleSpacing; // 6 numaralı üçgenin x koordinatı
        triangles[6] = new Triangle(x6, 550, false);
        triangles[7] = new Triangle(x6 - triangleSpacing, 550, false);
        triangles[8] = new Triangle(x6 - 2 * triangleSpacing, 550, false);
        triangles[9] = new Triangle(x6 - 3 * triangleSpacing, 550, false);
        triangles[10] = new Triangle(x6 - 4 * triangleSpacing, 550, false);
        triangles[11] = new Triangle(x6 - 5 * triangleSpacing, 550, false);
    }
    
    // Belirtilen konumda hangi üçgenin olduğunu bul
    public int findTriangleAt(int x, int y) {
        int closestTriangle = -1;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < triangles.length; i++) {
            Triangle t = triangles[i];
            
            // Üçgenin merkez noktasına olan mesafe
            double distance = Math.sqrt(Math.pow(x - t.x, 2) + Math.pow(y - t.y, 2));
            
            // Üst üçgenler için y koordinatı kontrolü
            if (t.isUp && y < 300) {
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            }
            // Alt üçgenler için y koordinatı kontrolü
            else if (!t.isUp && y >= 300) {
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            }
        }
        
        // Maksimum uzaklık kontrolü (çok uzaksa üçgen yok)
        if (minDistance > 100) {
            return -1;
        }
        
        return closestTriangle;
    }
    
    // Taşı bir üçgenden diğerine taşı
    public void movePiece(Taslabel piece, int fromTriangleIndex, int toTriangleIndex) {
        if (fromTriangleIndex == toTriangleIndex) {
            // Aynı üçgende hareket yok, taşı orijinal konumuna geri koy
            repositionPieceInTriangle(piece, fromTriangleIndex);
            return;
        }
        
        // Kaynak üçgendeki taş listesinden çıkar
        if (fromTriangleIndex >= 0) {
            pieces.get(fromTriangleIndex).remove(piece);
        }
        
        // Hedef üçgendeki taş listesine ekle
        pieces.get(toTriangleIndex).add(piece);
        
        // Taşın üçgen indeksini güncelle
        piece.setTriangleIndex(toTriangleIndex);
        
        // Tüm taşları yeniden düzenle
        rearrangePiecesInTriangle(toTriangleIndex);
    }
    
    // Taşı üçgende yeniden konumlandır
    private void repositionPieceInTriangle(Taslabel piece, int triangleIndex) {
        Triangle t = triangles[triangleIndex];
        List<Taslabel> trianglePieces = pieces.get(triangleIndex);
        int index = trianglePieces.indexOf(piece);
        
        if (index >= 0) {
            int tasBoyutu = 42;
            int x = t.x - tasBoyutu/2 - 20; // Taşı sola kaydırma
            
            int y;
            if (t.isUp) {
                y = t.y + index * (tasBoyutu - 12) + 10; // Aşağı kaydırma
            } else {
                y = t.y - tasBoyutu - index * (tasBoyutu - 12) - 10; // Aşağı kaydırma
            }
            
            piece.setLocation(x, y);
        }
    }
    
    // Üçgendeki tüm taşları yeniden düzenle
    private void rearrangePiecesInTriangle(int triangleIndex) {
        Triangle t = triangles[triangleIndex];
        List<Taslabel> trianglePieces = pieces.get(triangleIndex);
        
        for (int i = 0; i < trianglePieces.size(); i++) {
            Taslabel piece = trianglePieces.get(i);
            
            int tasBoyutu = 42;
            int x = t.x - tasBoyutu/2 - 20; // Taşı sola kaydırma
            
            int y;
            if (t.isUp) {
                y = t.y + i * (tasBoyutu - 12) + 10; // Aşağı kaydırma
            } else {
                y = t.y - tasBoyutu - i * (tasBoyutu - 12) - 10; // Aşağı kaydırma
            }
            
            piece.setLocation(x, y);
        }
    }
    
    // Başlangıç taşlarını yerleştir
    private void setupInitialStones() {
        // Beyaz taşlar (standart dizilim)
        addStonesToTriangle(5, Color.WHITE, 5);  // 6. üçgen (altta sağ bölüm)
        addStonesToTriangle(7, Color.WHITE, 3);  // 8. üçgen (altta sol bölüm)
        addStonesToTriangle(12, Color.WHITE, 5); // 13. üçgen (üstte sol köşe)
        addStonesToTriangle(23, Color.WHITE, 2); // 24. üçgen (üstte sağ köşe)
        
        // Siyah taşlar (standart dizilim)
        addStonesToTriangle(0, Color.BLACK, 2);  // 1. üçgen (altta sağ köşe)
        addStonesToTriangle(11, Color.BLACK, 5); // 12. üçgen (altta sol köşe)
        addStonesToTriangle(16, Color.BLACK, 3); // 17. üçgen (üstte sol bölüm)
        addStonesToTriangle(18, Color.BLACK, 5); // 19. üçgen (üstte sağ bölüm)
    }

    // Üçgene taş ekle
    private void addStonesToTriangle(int index, Color renk, int count) {
        Triangle t = triangles[index];
        List<Taslabel> trianglePieces = pieces.get(index);
        
        for (int i = 0; i < count; i++) {
            Taslabel tas = new Taslabel();
            tas.setTasGoster(true, renk);
            
            // Taş boyutunu ayarla
            int tasBoyutu = 42;
            
            // X konumunu sola kaydır
            int x = t.x - tasBoyutu/2 - 20;
            
            // Yöne göre Y eksenini hesapla
            int y;
            if (t.isUp) {
                y = t.y + i * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - tasBoyutu - i * (tasBoyutu - 12) - 10;
            }
            
            tas.setBounds(x, y, tasBoyutu, tasBoyutu);
            tas.setBoardPanel(this, index); // BoardPanel ve üçgen indeksini ayarla
            
            // Üçgendeki taş listesine ekle
            trianglePieces.add(tas);
            
            add(tas);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        // Debug için üçgenlerin merkezini göster (geliştirme sırasında açabilirsiniz)
        g.setColor(Color.RED);
        for (int i = 0; i < triangles.length; i++) {
            Triangle t = triangles[i];
            g.drawRect(t.x-2, t.y-2, 4, 4);
            g.drawString(Integer.toString(i), t.x-5, t.y);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
