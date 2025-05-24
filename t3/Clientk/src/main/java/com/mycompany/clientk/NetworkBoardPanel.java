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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.util.stream.Collectors.toList;

/**
 * Ağ tabanlı oyun için tahta paneli - Tam fonksiyonel versiyon
 */
public class NetworkBoardPanel extends javax.swing.JPanel {

   /**
     * Üçgen sınıfı - Board geometrisi için
     */
    static class Triangle {

        int x, y;
        boolean isUp;

        Triangle(int x, int y, boolean isUp) {
            this.x = x;
            this.y = y;
            this.isUp = isUp;
        }
    }

    private NetworkGameFrame parentFrame;
    private Triangle[] triangles = new Triangle[24];
    private Image backgroundImage;
    private NetworkTaslabel selectedPiece = null;
    private Map<Integer, List<NetworkTaslabel>> pieces = new HashMap<>();
    private int[] diceValues = {0, 0};
    private boolean isMyTurn = false;
    private List<Integer> legalMoves = new ArrayList<>();
    private NetworkTaslabel hoverPiece = null;
    private boolean isWhitePlayer = true; // Bu oyuncunun rengi
    private int currentPos = -1; // Seçili taşın mevcut konumu

    // Home alanları (Bar kaldırıldı)
    private List<NetworkTaslabel> whiteHome = new ArrayList<>();
    private List<NetworkTaslabel> blackHome = new ArrayList<>();
    private static final int WHITE_HOME_INDEX = 25;
    private static final int BLACK_HOME_INDEX = 26;
    private boolean diceRolled = false;

    // --- TAVLA KURALLAR İÇİN ---
    private List<Integer> availableDice = new ArrayList<>(); // Kullanılabilir zar değerleri
    private boolean isDoubleRoll = false; // Çift zar kontrolü
    private int doubleRollCount = 0; // Çift zarda kaç hamle kaldı

    // ============ YENİ EKLENEN ÖZELLİKLER - ÇİFT HAMLE SORUNU İÇİN ============
    private int myPlayerId = -1; // Kendi oyuncu ID'nizi buraya kaydedin
    private boolean isApplyingNetworkMove = false; // Network hamle durumu takibi

    public NetworkBoardPanel(NetworkGameFrame parentFrame) {
        this.parentFrame = parentFrame;
        initComponents();
        setLayout(null);
        setPreferredSize(new Dimension(1100, 600));

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/tav.jpg"));
        } catch (IOException e) {
            System.err.println("Arkaplan resmi yüklenemedi: " + e.getMessage());
        }

        for (int i = 0; i < 24; i++) {
            pieces.put(i, new ArrayList<>());
        }

        setupTriangles();
        setupInitialStones();

        // Mouse listener ekle
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // Network hamle uygulanıyorsa tıklamaları engelle
                if (isApplyingNetworkMove) {
                    return;
                }

                // Sıra sende değilse veya zar atılmamışsa tıklama işlemeyecek
                if (!isMyTurn || !diceRolled) {
                    System.out.println("Sıra sizde değil veya zar atılmamış!");
                    return;
                }

                int clickedIndex = findTriangleAt(e.getX(), e.getY());
                if (clickedIndex == -1) {
                    System.out.println("Geçersiz tıklama konumu!");
                    return;
                }

                // Taş seçme aşaması
                if (selectedPiece == null) {
                    List<NetworkTaslabel> trianglePieces = pieces.get(clickedIndex);
                    if (trianglePieces == null || trianglePieces.isEmpty()) {
                        System.out.println("Seçilen üçgende taş yok!");
                        return;
                    }

                    NetworkTaslabel topPiece = trianglePieces.get(trianglePieces.size() - 1);
                    Color renk = topPiece.getTasRenk();

                    // Kendi taşın mı kontrol et
                    if ((renk == Color.WHITE && isWhitePlayer) || (renk == Color.BLACK && !isWhitePlayer)) {
                        selectedPiece = topPiece;
                        selectedPiece.setSelected(true);
                        currentPos = clickedIndex;
                        updateLegalMoves(selectedPiece);
                        System.out.println("✅ Taş seçildi: Üçgen " + clickedIndex);
                        repaint();
                    } else {
                        System.out.println("❌ Sadece kendi taşlarınızı seçebilirsiniz!");
                    }
                } 
                // Taş hareket ettirme aşaması
              else {
    if (!legalMoves.contains(clickedIndex)) {
        System.out.println("❌ Geçersiz hamle! Hedef: " + clickedIndex);
        // Seçimi iptal et
        if (selectedPiece != null) {
            selectedPiece.setSelected(false);
        }
        selectedPiece = null;
        currentPos = -1;
        legalMoves.clear();
        repaint();
        return;
    }

    System.out.println("🎯 Hamle yapılıyor - Player: " + myPlayerId + 
                       ", From: " + currentPos + ", To: " + clickedIndex);

    // ÖNEMLİ: Hamleyi hemen GUI'de uygula
    applyLocalMove(currentPos, clickedIndex);

    // Zar kullanımını hesapla ve güncelle
    int diceValue = calculateRequiredDice(currentPos, clickedIndex, selectedPiece.getTasRenk());
    if (diceValue > 0) {
        consumeDice(diceValue);
    }

    // Sonra sunucuya bildir
    onPlayerMove(selectedPiece, currentPos, clickedIndex);

    // Seçimi temizle
    if (selectedPiece != null) {
        selectedPiece.setSelected(false);
    }
    selectedPiece = null;
    currentPos = -1;
    legalMoves.clear();

    // Skor ve kazanma durumu kontrolü
    updateScores();
    checkForWinner();

    repaint();
}
            }
        });
    }

    // ============ YENİ METODLAR - ÇİFT HAMLE SORUNU ÇÖZÜMÜ ============

    /**
     * Yerel hamle uygulama metodu (sadece kendi hamlelerimiz için)
     */
   private void applyLocalMove(int fromTriangle, int toTriangle) {
    System.out.println("🎯 Yerel hamle uygulanıyor: " + fromTriangle + " → " + toTriangle);
    
    if (selectedPiece == null) {
        System.err.println("❌ selectedPiece null!");
        return;
    }
    
    // 1. Taşı önceki üçgenden çıkar
    List<NetworkTaslabel> fromList = pieces.get(fromTriangle);
    if (fromList.contains(selectedPiece)) {
        fromList.remove(selectedPiece);
        System.out.println("✅ Taş listeden çıkarıldı. Kalan: " + fromList.size());
    }

    // 2. Component'i GUI'den MUTLAKA kaldır
    if (selectedPiece.getParent() != null) {
        Container parent = selectedPiece.getParent();
        parent.remove(selectedPiece);
        System.out.println("✅ Taş GUI'den kaldırıldı");
    } else {
        System.out.println("⚠️ Taşın parent'ı null!");
    }

    // 3. Eski üçgeni temizle ve yeniden düzenle
    rearrangePiecesInTriangle(fromTriangle);

    // 4. Yeni konuma ekle
    List<NetworkTaslabel> toList = getTargetList(toTriangle);
    toList.add(selectedPiece);
    selectedPiece.setTriangleIndex(toTriangle);
    System.out.println("✅ Taş yeni listeye eklendi. Toplam: " + toList.size());

    // 5. GUI'ye yeniden ekle
    add(selectedPiece);
    setComponentZOrder(selectedPiece, 0);
    System.out.println("✅ Taş GUI'ye yeniden eklendi");

    // 6. Pozisyonla
    positionPiece(selectedPiece, toTriangle);

    // 7. GUI'yi zorla güncelle
    revalidate();
    repaint();
    
    System.out.println("🔄 applyLocalMove tamamlandı");
}

    /**
     * Helper metot - Hedef listeyi döndürür
     */
    private List<NetworkTaslabel> getTargetList(int targetIndex) {
        if (targetIndex == WHITE_HOME_INDEX) {
            return whiteHome;
        } else if (targetIndex == BLACK_HOME_INDEX) {
            return blackHome;
        } else {
            return pieces.get(targetIndex);
        }
    }

    /**
     * Helper metot - Taşı pozisyonlar
     */
    private void positionPiece(NetworkTaslabel piece, int targetIndex) {
        if (targetIndex == WHITE_HOME_INDEX || targetIndex == BLACK_HOME_INDEX) {
            positionPieceInHome(piece);
        } else {
            repositionPieceInTriangle(piece, targetIndex);
            rearrangePiecesInTriangle(targetIndex);
        }
    }

    /**
     * Player ID'yi ayarlama metodu
     */
    public void setMyPlayerId(int playerId) {
        this.myPlayerId = playerId;
        System.out.println("🆔 Oyuncu ID ayarlandı: " + playerId);
    }

    /**
     * Debug metodu
     */
    private void debugPieceState(String action, int triangleIndex) {
        List<NetworkTaslabel> list = pieces.get(triangleIndex);
        System.out.println("🔍 " + action + " - Üçgen " + triangleIndex + " taş sayısı: " + 
                          (list != null ? list.size() : "null"));
        
        // GUI'deki component sayısını da kontrol et
        System.out.println("🖥️ Panel'deki toplam component: " + getComponentCount());
    }

    /**
     * Oyuncunun rengini ayarlar
     */
    public void setPlayerColor(boolean isWhite) {
        this.isWhitePlayer = isWhite;
        repaint();
    }

    /**
     * Sırayı ayarlar
     */
    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        if (!isMyTurn) {
            clearLegalMoves();
            clearDiceDisplay();
        }
        repaint();
    }

    /**
     * Zar değerlerini ayarlar - DÜZELTİLDİ
     */
    public void setDiceValues(int die1, int die2) {
        this.diceValues[0] = die1;
        this.diceValues[1] = die2;
        this.diceRolled = true;
        if (selectedPiece != null) {
            updateLegalMoves(selectedPiece);
        }
        repaint();
    }

    public void setDiceRolled(boolean rolled) {
        this.diceRolled = rolled;
        if (!rolled) {
            clearDiceDisplay();
        }
        repaint();
    }

    public void clearDiceDisplay() {
        this.diceValues[0] = 0;
        this.diceValues[1] = 0;
        this.diceRolled = false;
        repaint();
    }

    /**
     * Hamle yapıldığında zar kullanımını günceller - DÜZELTİLDİ
     */
    private void consumeDice(int diceValue) {
        System.out.println("🎲 Zar kullanmaya çalışıyor: " + diceValue + ", Mevcut zarlar: " + availableDice);

        if (availableDice.contains(diceValue)) {
            // Sadece bir tane zar değerini kaldır
            availableDice.remove(Integer.valueOf(diceValue));

            if (isDoubleRoll) {
                doubleRollCount--;
                System.out.println("🎲 Çift zardan " + diceValue + " kullanıldı. Kalan hamle: " + doubleRollCount + ", Zarlar: " + availableDice);
            } else {
                System.out.println("🎲 Zar " + diceValue + " kullanıldı. Kalan zarlar: " + availableDice);
            }

            // Seçili taş varsa hamleleri yeniden hesapla
            if (selectedPiece != null) {
                updateLegalMoves(selectedPiece);
            }

            // Otomatik sıra geçişi kontrolü
            checkAutoPassTurn();

        } else {
            System.err.println("❌ Zar bulunamadı: " + diceValue + " (Mevcut: " + availableDice + ")");
        }
    }

    /**
     * Hangi zarla hamle yapılacağını hesaplar - DÜZELTİLDİ
     */
    private int calculateRequiredDice(int fromTriangle, int toTriangle, Color pieceColor) {
        // ÇIKARMA HAMLESİ
        if (toTriangle == WHITE_HOME_INDEX || toTriangle == BLACK_HOME_INDEX) {
            int exactDistance;
            if (pieceColor == Color.WHITE) {
                exactDistance = 24 - fromTriangle; // 23→1, 22→2, ... 18→6
            } else {
                exactDistance = fromTriangle + 1;  // 0→1, 1→2, ... 5→6
            }

            // Tam mesafe var mı?
            if (availableDice.contains(exactDistance)) {
                return exactDistance;
            }

            // Büyük zarla çıkarma (basit kural)
            for (int dice : availableDice) {
                if (dice > exactDistance) {
                    return dice;
                }
            }
        } // NORMAL HAMLE
        else {
            int requiredDice = Math.abs(toTriangle - fromTriangle);

            // Hareket yönü kontrolü
            boolean correctDirection = false;
            if (pieceColor == Color.WHITE) {
                // Beyaz: 0→23 yönünde (artan indeks)
                correctDirection = (toTriangle > fromTriangle);
            } else {
                // Siyah: 23→0 yönünde (azalan indeks)  
                correctDirection = (toTriangle < fromTriangle);
            }

            if (correctDirection && availableDice.contains(requiredDice)) {
                return requiredDice;
            }
        }

        return -1; // Geçersiz hamle
    }

    private int getPieceDirection(NetworkTaslabel piece) {
        if (piece.getTasRenk() == Color.WHITE) {
            return 1; // Beyazlar saat yönü tersine
        } else {
            return -1; // Siyahlar saat yönünde
        }
    }

    /**
     * Taşın rengine göre hareket yönünü döndürür Beyaz: -1 (azalan index),
     * Siyah: +1 (artan index)
     */
    private boolean isMoveDirectionValid(NetworkTaslabel piece, int from, int to) {
        int direction = getPieceDirection(piece);
        return (to - from) * direction > 0;
    }

    /**
     * Geçerli hamleleri günceller - DÜZELTİLDİ
     */
    public void updateLegalMoves(NetworkTaslabel piece) {
        legalMoves.clear();
        if (piece == null || !isMyTurn || availableDice.isEmpty()) {
            return;
        }

        // Renk kontrolü
        if ((piece.getTasRenk() == Color.WHITE && !isWhitePlayer)
                || (piece.getTasRenk() == Color.BLACK && isWhitePlayer)) {
            return;
        }

        int currentTriangle = piece.getTriangleIndex();
        Color pieceColor = piece.getTasRenk();
        int direction = getPieceDirection(piece);

        System.out.println("🔍 Hamle hesaplıyor: Taş=" + pieceColor + ", Konum=" + currentTriangle + ", Zarlar=" + availableDice);

        for (int diceValue : availableDice) {
            int targetTriangle = currentTriangle + (diceValue * direction);
            System.out.println("  🎯 Zar " + diceValue + " için hedef: " + targetTriangle + " (yön: " + direction + ")");
            // Normal hamle kontrolü
            if (targetTriangle >= 0 && targetTriangle < 24) {
                boolean validDir = isMoveDirectionValid(piece, currentTriangle, targetTriangle);
                boolean validMove = isValidMove(targetTriangle, pieceColor);
                if (validDir && validMove) {
                    legalMoves.add(targetTriangle);
                    System.out.println("    ✅ Geçerli hamle: " + currentTriangle + " → " + targetTriangle);
                } else {
                    if (!validDir) {
                        System.out.println("    ❌ Geçersiz yön: " + currentTriangle + " → " + targetTriangle);
                    }
                    if (!validMove) {
                        List<NetworkTaslabel> targetPieces = pieces.get(targetTriangle);
                        if (!targetPieces.isEmpty() && targetPieces.get(0).getTasRenk() != pieceColor) {
                            System.out.println("    ❌ Hedef üçgende rakipten " + targetPieces.size() + " taş var. Hamle engellendi.");
                        } else {
                            System.out.println("    ❌ Diğer geçersiz hamle nedeni.");
                        }
                    }
                }
            } // Çıkış (bear off) kontrolü
            else if (pieceColor == Color.WHITE && targetTriangle >= 24 && allPiecesInHome(pieceColor)) {
                legalMoves.add(WHITE_HOME_INDEX);
                System.out.println("    ✅ Beyaz çıkarma: " + currentTriangle + " → HOME");
            } else if (pieceColor == Color.BLACK && targetTriangle < 0 && allPiecesInHome(pieceColor)) {
                legalMoves.add(BLACK_HOME_INDEX);
                System.out.println("    ✅ Siyah çıkarma: " + currentTriangle + " → HOME");
            } else {
                System.out.println("    ❌ Çıkarma koşulları sağlanmıyor");
            }
        }

        System.out.println("🎯 Toplam geçerli hamle: " + legalMoves.size() + " → " + legalMoves);
        repaint();
    }

    /**
     * Çıkarma yapılabilir mi kontrol eder - BASİTLEŞTİRİLDİ
     */
    private boolean canBearOffWithDice(Color color, int currentTriangle, int diceValue) {
        // Önce home bölgesinde mi kontrol et
        boolean inHomeArea;
        if (color == Color.WHITE) {
            inHomeArea = (currentTriangle >= 18 && currentTriangle <= 23); // Beyaz home: 18-23
        } else {
            inHomeArea = (currentTriangle >= 0 && currentTriangle <= 5);   // Siyah home: 0-5
        }

        if (!inHomeArea) {
            System.out.println("    🏠 Çıkarma için home bölgesinde değil: " + currentTriangle);
            return false;
        }

        // Çıkarmak için gereken zar değerini hesapla
        int requiredDice;
        if (color == Color.WHITE) {
            requiredDice = 24 - currentTriangle; // 23→1, 22→2, 21→3, 20→4, 19→5, 18→6
        } else {
            requiredDice = currentTriangle + 1;  // 0→1, 1→2, 2→3, 3→4, 4→5, 5→6
        }

        System.out.println("    🎲 Çıkarma için gereken zar: " + requiredDice + ", Mevcut zar: " + diceValue);

        // BASİT KURAL: Tam zar değeri ile çıkış veya daha büyük zar ile çıkış
        return diceValue >= requiredDice;
    }

    /**
     * Hamlenin geçerli olup olmadığını kontrol eder - BASİTLEŞTİRİLDİ
     */
    private boolean isValidMove(int targetTriangle, Color pieceColor) {
        if (targetTriangle < 0 || targetTriangle >= 24) {
            return false;
        }

        List<NetworkTaslabel> targetPieces = pieces.get(targetTriangle);

        // Hedef üçgen boşsa hamle geçerli
        if (targetPieces.isEmpty()) {
            return true;
        }

        // Hedef üçgende aynı renk taşlar varsa hamle geçerli
        if (targetPieces.get(0).getTasRenk() == pieceColor) {
            return true;
        }

        // Hedef üçgende rakip taşı var
        // Rakipten 1 taş varsa alınabilir (hit), 2 veya daha fazla varsa alamaz
        return targetPieces.size() == 1;
    }

    /**
     * OTOMATIK SIRA GEÇİŞİ KONTROLÜ - DÜZELTİLDİ
     */
    private void checkAutoPassTurn() {
        if (!isMyTurn) {
            return;
        }

        // Tüm zarlar bittiyse
        if (availableDice.isEmpty()) {
            System.out.println("🔄 Tüm zarlar kullanıldı - Sıra otomatik geçiyor");
            if (parentFrame != null && parentFrame.networkClient != null) {
                parentFrame.networkClient.requestPassTurn();
            }
            return;
        }

        // Hamle yapılabilir mi kontrol et
        Color myColor = isWhitePlayer ? Color.WHITE : Color.BLACK;
        if (!hasValidMoves(myColor)) {
            System.out.println("🚫 Hamle yapılamıyor - Sıra otomatik geçiyor");
            if (parentFrame != null && parentFrame.networkClient != null) {
                parentFrame.networkClient.requestPassTurn();
            }
        }
    }

    /**
     * Seçili taşı al - NetworkTaslabel için
     */
    public NetworkTaslabel getSelectedPiece() {
        return selectedPiece;
    }

    /**
     * Taşı orijinal konumuna yerleştir - İYİLEŞTİRİLMİŞ
     */
    private void repositionPieceInTriangle(NetworkTaslabel piece, int triangleIndex) {
        if (triangleIndex < 0 || triangleIndex >= 24) {
            return;
        }

        Triangle t = triangles[triangleIndex];
        List<NetworkTaslabel> trianglePieces = pieces.get(triangleIndex);
        int index = trianglePieces.indexOf(piece);

        if (index >= 0) {
            int tasBoyutu = 42;
            int x = t.x - tasBoyutu / 2 - 20;

            int y;
            if (t.isUp) {
                y = t.y + index * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - index * (tasBoyutu - 12) - 10;
            }

            // Smooth positioning
            piece.setLocation(x, y);
            System.out.println("📍 Taş pozisyonlandı: Üçgen " + triangleIndex + " → (" + x + ", " + y + ")");
        }
    }

    /**
     * Üçgendeki taşları yeniden düzenler - İYİLEŞTİRİLMİŞ
     */
    private void rearrangePiecesInTriangle(int triangleIndex) {
    if (triangleIndex < 0 || triangleIndex >= 24) {
        return;
    }

    Triangle t = triangles[triangleIndex];
    List<NetworkTaslabel> trianglePieces = pieces.get(triangleIndex);

    System.out.println("🔄 Üçgen " + triangleIndex + " tamamen temizleniyor ve yeniden düzenleniyor. Liste sayısı: " + trianglePieces.size());

    // ========== ÖNEMLİ: Bu üçgenle ilgili TÜM GUI component'lerini kaldır ==========
    java.awt.Component[] allComponents = getComponents();
    java.util.List<java.awt.Component> toRemove = new java.util.ArrayList<>();
    
    for (java.awt.Component comp : allComponents) {
        if (comp instanceof NetworkTaslabel) {
            NetworkTaslabel piece = (NetworkTaslabel) comp;
            if (piece.getTriangleIndex() == triangleIndex) {
                toRemove.add(comp);
            }
        }
    }
    
    // Bulunan tüm component'leri kaldır
    for (java.awt.Component comp : toRemove) {
        remove(comp);
        System.out.println("🗑️ GUI'den kaldırıldı: üçgen " + triangleIndex);
    }

    // ========== Sonra sadece listede olanları yeniden ekle ==========
    for (int i = 0; i < trianglePieces.size(); i++) {
        NetworkTaslabel piece = trianglePieces.get(i);

        int tasBoyutu = 42;
        int x = t.x - tasBoyutu / 2 - 20;

        int y;
        if (t.isUp) {
            y = t.y + i * (tasBoyutu - 12) + 10;
        } else {
            y = t.y - tasBoyutu - i * (tasBoyutu - 12) - 10;
        }

        // Taşı ekle ve pozisyonla
        add(piece);
        piece.setBounds(x, y, tasBoyutu, tasBoyutu);
        piece.setTriangleIndex(triangleIndex); // Index'i yeniden ayarla
        setComponentZOrder(piece, 0);
        
        System.out.println("✅ Yeniden eklendi: üçgen " + triangleIndex + ", taş " + i);
    }

    // Panel güncelle
    revalidate();
    repaint();
    
    System.out.println("✅ Üçgen " + triangleIndex + " tamamlandı. Listede: " + trianglePieces.size() + ", GUI'de ekli: " + trianglePieces.size());
}

    /**
     * Koordinat kontrolü ile üçgen bulma - İYİLEŞTİRİLMİŞ
     *
     * @return
     */
    public int findTriangleAt(int x, int y) {
        // Home alanları kontrolü - Öncelikli
        if (x > 1010 && x < 1090 && y > 200 && y < 450) {
            System.out.println("🏠 Beyaz HOME alanı tespit edildi");
            return WHITE_HOME_INDEX;
        } else if (x > 10 && x < 90 && y > 200 && y < 450) {
            System.out.println("🏠 Siyah HOME alanı tespit edildi");
            return BLACK_HOME_INDEX;
        }

        // Üçgen arama - Geliştirilmiş algoritma
        int closestTriangle = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < triangles.length; i++) {
            Triangle t = triangles[i];
            double distance = Math.sqrt(Math.pow(x - t.x, 2) + Math.pow(y - t.y, 2));

            // Üst üçgenler (12-23)
            if (t.isUp && y < 350) {  // Y sınırını genişlettik
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            } // Alt üçgenler (0-11)
            else if (!t.isUp && y >= 250) {  // Y sınırını genişlettik
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            }
        }

        // Mesafe kontrolü - Daha toleranslı
        if (minDistance > 120) {
            System.out.println("❌ Geçersiz drop pozisyonu - En yakın üçgen çok uzak: " + minDistance);
            return -1;
        }

        if (closestTriangle != -1) {
            System.out.println("✅ Üçgen " + closestTriangle + " tespit edildi (mesafe: " + String.format("%.1f", minDistance) + ")");
        }

        return closestTriangle;
    }

    /**
     * Görsel feedback için hover efektleri - YENİ
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Arkaplan
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Orta çubuk
        g.setColor(new Color(139, 69, 19));
        g.fillRect(530, 0, 30, getHeight());

        // Home alanları
        g.setColor(new Color(255, 248, 220));
        g.fillRect(1010, 200, 80, 250);
        g.setColor(Color.BLACK);
        g.drawRect(1010, 200, 80, 250);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("BEYAZ", 1025, 195);
        g.drawString("HOME", 1025, 475);

        g.setColor(new Color(64, 64, 64));
        g.fillRect(10, 200, 80, 250);
        g.setColor(Color.WHITE);
        g.drawRect(10, 200, 80, 250);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("SİYAH", 25, 195);
        g.drawString("HOME", 25, 475);

        // Geçerli hamleleri göster
        if (!legalMoves.isEmpty()) {
            g.setColor(new Color(0, 255, 0, 80));

            for (int triangleIndex : legalMoves) {
                if (triangleIndex == WHITE_HOME_INDEX) {
                    g.fillRect(1010, 200, 80, 250);
                } else if (triangleIndex == BLACK_HOME_INDEX) {
                    g.fillRect(10, 200, 80, 250);
                } else if (triangleIndex < 24) {
                    Triangle t = triangles[triangleIndex];

                    int[] xPoints = new int[3];
                    int[] yPoints = new int[3];

                    if (t.isUp) {
                        xPoints[0] = t.x - 30;
                        yPoints[0] = t.y + 100;
                        xPoints[1] = t.x;
                        yPoints[1] = t.y;
                        xPoints[2] = t.x + 30;
                        yPoints[2] = t.y + 100;
                    } else {
                        xPoints[0] = t.x - 30;
                        yPoints[0] = t.y - 100;
                        xPoints[1] = t.x;
                        yPoints[1] = t.y;
                        xPoints[2] = t.x + 30;
                        yPoints[2] = t.y - 100;
                    }

                    g.fillPolygon(xPoints, yPoints, 3);
                }
            }
        }

        // Sıra göstergesi
        g.setColor(isMyTurn ? Color.GREEN : Color.RED);
        g.fillOval(10, 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(10, 10, 20, 20);

        // Home skorları
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(String.valueOf(whiteHome.size()), 1040, 340);

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(blackHome.size()), 40, 340);

        // ZAR DURUMU GÖSTERGESİ
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));

        String diceStatus = "Kalan zarlar: ";
        if (isDoubleRoll) {
            diceStatus += diceValues[0] + " x" + availableDice.size() + " (çift)";
        } else {
            diceStatus += availableDice.toString();
        }

        g.drawString(diceStatus, 200, 30);

        // DETAYLI BİLGİ
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("Zar değerleri: " + diceValues[0] + ", " + diceValues[1], 200, 45);

        // HAMLE HAKKI GÖSTERGESİ
        if (isMyTurn && !availableDice.isEmpty()) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Hamle yapabilirsiniz! (" + availableDice.size() + " hamle kaldı)", 200, 65);
        } else if (isMyTurn && availableDice.isEmpty()) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Hamle hakkınız bitti - Sıra geçecek!", 200, 65);
        }
    }

    private void initComponents() {
        // NetBeans generated kod placeholder
    }

    // Getter metotları
    public boolean isMyTurn() {
        return isMyTurn;
    }

    public boolean isWhitePlayer() {
        return isWhitePlayer;
    }

    public List<Integer> getLegalMoves() {
        return legalMoves;
    }

    public void setSelectedPiece(NetworkTaslabel piece) {
        this.selectedPiece = piece;
        if (piece != null) {
            updateLegalMoves(piece);
        }
    }

    public void setHoverPiece(NetworkTaslabel piece) {
        this.hoverPiece = piece;
        repaint();
    }

    public List<Integer> getAvailableDice() {
        return availableDice;
    }

    public boolean isDoubleRoll() {
        return isDoubleRoll;
    }

    /**
     * Oyuncu taş hareket ettirdiğinde çağrılır
     */
    public void onPlayerMove(NetworkTaslabel piece, int fromTriangle, int toTriangle) {
        if (isMyTurn && parentFrame != null) {
            // Sunucuya hareketi gönder
            parentFrame.sendPieceMove(fromTriangle, toTriangle);
            System.out.println("📤 Hamle sunucuya gönderildi: " + fromTriangle + " → " + toTriangle);
        }
    }

    /**
     * Geçerli hamleleri temizler
     */
    public void clearLegalMoves() {
        legalMoves.clear();
        selectedPiece = null;
        repaint();
    }

    /**
     * Üçgenleri kurar
     */
    private void setupTriangles() {
        int triangleWidth = 72;
        int x12 = 105;
        int triangleSpacing = triangleWidth;

        // Alt sıra üçgenler (0-11) - SOLDAN SAĞA ARTAN INDEX
        int x0 = x12;
        for (int i = 0; i < 12; i++) {
            triangles[i] = new Triangle(x0 + i * triangleSpacing, 550, false);
        }

        // Üst sıra üçgenler (12-23) - SAĞDAN SOLA AZALAN INDEX
        int x23 = x12 + 11 * triangleSpacing;
        for (int i = 0; i < 12; i++) {
            triangles[12 + i] = new Triangle(x23 - i * triangleSpacing, 50, true);
        }
    }

    /**
     * Başlangıç taşlarını kurar
     */
    private void setupInitialStones() {
        addStonesToTriangle(5, Color.WHITE, 5);
        addStonesToTriangle(7, Color.WHITE, 3);
        addStonesToTriangle(12, Color.WHITE, 5);
        addStonesToTriangle(23, Color.WHITE, 2);

        addStonesToTriangle(0, Color.BLACK, 2);
        addStonesToTriangle(11, Color.BLACK, 5);
        addStonesToTriangle(16, Color.BLACK, 3);
        addStonesToTriangle(18, Color.BLACK, 5);
    }

    /**
     * Belirtilen üçgene taş ekler
     */
    private void addStonesToTriangle(int index, Color renk, int count) {
        Triangle t = triangles[index];
        List<NetworkTaslabel> trianglePieces = pieces.get(index);

        for (int i = 0; i < count; i++) {
            NetworkTaslabel tas = new NetworkTaslabel();
            tas.setTasGoster(true, renk);

            int tasBoyutu = 42;
            int x = t.x - tasBoyutu / 2 - 20;

            int y;
            if (t.isUp) {
                y = t.y + i * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - i * (tasBoyutu - 12) - 10;
            }

            tas.setBounds(x, y, tasBoyutu, tasBoyutu);
            tas.setBoardPanel(this, index);

            trianglePieces.add(tas);
            add(tas);
        }
    }

    /**
     * ============ ÇİFT HAMLE SORUNU ÇÖZÜMÜ - SUNUCUDAN GELEN HAMLE ============
     * Sunucudan gelen hamleyi uygular (SADECE RAKİP HAMLELERİ İÇİN)
     */
 public void applyNetworkMove(int playerId, int fromTriangle, int toTriangle) {
    // Kendi hamlemi tekrar uygulama
    if (playerId == myPlayerId) {
        System.out.println("🚫 Kendi hamlemi tekrar uygulamıyorum: " + fromTriangle + " → " + toTriangle);
        return;
    }
    
    System.out.println("🔄 Rakip hamlesini uyguluyorum: " + fromTriangle + " → " + toTriangle);
    
    isApplyingNetworkMove = true;
    
    try {
        List<NetworkTaslabel> fromList = pieces.get(fromTriangle);
        if (fromList == null || fromList.isEmpty()) {
            System.err.println("❌ Kaynak üçgen boş: " + fromTriangle);
            return;
        }
        
        NetworkTaslabel movingPiece = fromList.get(fromList.size() - 1);
        
        // 1. Listeden çıkar
        fromList.remove(movingPiece);
        System.out.println("✅ Rakip taşı listeden çıkarıldı. Kalan: " + fromList.size());
        
        // 2. GUI'den kaldır
        if (movingPiece.getParent() != null) {
            movingPiece.getParent().remove(movingPiece);
            System.out.println("✅ Rakip taşı GUI'den kaldırıldı");
        }
        
        // 3. Eski üçgeni düzenle
        rearrangePiecesInTriangle(fromTriangle);
        
        // 4. Yeni listeye ekle
        List<NetworkTaslabel> toList = getTargetList(toTriangle);
        toList.add(movingPiece);
        movingPiece.setTriangleIndex(toTriangle);
        System.out.println("✅ Rakip taşı yeni listeye eklendi. Toplam: " + toList.size());
        
        // 5. GUI'ye geri ekle
        add(movingPiece);
        setComponentZOrder(movingPiece, 0);
        System.out.println("✅ Rakip taşı GUI'ye eklendi");
        
        // 6. Pozisyonla
        positionPiece(movingPiece, toTriangle);
        
        // 7. Her iki üçgeni de düzenle
        rearrangePiecesInTriangle(toTriangle);
        
        // 8. GUI'yi güncelle
        revalidate();
        repaint();
        
        System.out.println("✅ Rakip hamlesi tamamlandı");
        
    } finally {
        isApplyingNetworkMove = false;
    }
}

    /**
     * Home'daki taşı konumlandırır
     */
    private void positionPieceInHome(NetworkTaslabel piece) {
        int homeX = (piece.getTasRenk() == Color.WHITE) ? 1040 : 40;
        int baseY = 250;

        int count = (piece.getTasRenk() == Color.WHITE) ? whiteHome.size() : blackHome.size();

        int row = (count - 1) / 2;
        int col = (count - 1) % 2;

        int x = homeX + col * 25;
        int y = baseY + row * 20;

        piece.setBounds(x, y, 20, 20);
    }

    /**
     * Skorları günceller
     */
    private void updateScores() {
        if (parentFrame != null) {
            int myScore = isWhitePlayer ? whiteHome.size() : blackHome.size();
            int opponentScore = isWhitePlayer ? blackHome.size() : whiteHome.size();
            parentFrame.updateScores(myScore, opponentScore);
        }
    }

    /**
     * Kazananı kontrol eder
     */
    private void checkForWinner() {
        if (whiteHome.size() == 15) {
            String winner = isWhitePlayer ? "Siz kazandınız!" : "Rakibiniz kazandı!";
            JOptionPane.showMessageDialog(this, winner, "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
        } else if (blackHome.size() == 15) {
            String winner = !isWhitePlayer ? "Siz kazandınız!" : "Rakibiniz kazandı!";
            JOptionPane.showMessageDialog(this, winner, "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Belirtilen renkte oyuncunun oynayabileceği en az bir hamle var mı?
     */
    private boolean hasValidMoves(Color pieceColor) {
        for (int i = 0; i < 24; i++) {
            List<NetworkTaslabel> trianglePieces = pieces.get(i);
            if (trianglePieces.isEmpty()) {
                continue;
            }
            if (trianglePieces.get(0).getTasRenk() != pieceColor) {
                continue;
            }
            int direction = (pieceColor == Color.WHITE) ? 1 : -1;
            for (int diceValue : availableDice) {
                int targetTriangle = i + (diceValue * direction);
                if (targetTriangle >= 0 && targetTriangle < 24) {
                    if (isValidMove(targetTriangle, pieceColor)) {
                        return true;
                    }
                } else if (canBearOffWithDice(pieceColor, i, diceValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tahtayı ve taşları sıfırlar
     */
    public void resetGame() {
        clearLegalMoves();
        clearDiceDisplay();
        whiteHome.clear();
        blackHome.clear();
        for (List<NetworkTaslabel> list : pieces.values()) {
            list.clear();
        }
        setupInitialStones();
        repaint();
    }

    /**
     * Zar atıldığında kullanılabilir zarları ayarlar
     */
    public void setAvailableDice(int die1, int die2) {
        availableDice.clear();
        if (die1 == die2) {
            for (int i = 0; i < 4; i++) {
                availableDice.add(die1);
            }
            isDoubleRoll = true;
            doubleRollCount = 4;
        } else {
            availableDice.add(die1);
            availableDice.add(die2);
            isDoubleRoll = false;
            doubleRollCount = 2;
        }
        repaint();
    }

    /**
     * Belirtilen renkteki tüm taşlar home bölgesinde mi?
     */
    private boolean allPiecesInHome(Color color) {
        if (color == Color.WHITE) {
            for (int i = 0; i < 18; i++) {
                List<NetworkTaslabel> trianglePieces = pieces.get(i);
                for (NetworkTaslabel piece : trianglePieces) {
                    if (piece.getTasRenk() == Color.WHITE) {
                        return false;
                    }
                }
            }
        } else {
            for (int i = 6; i < 24; i++) {
                List<NetworkTaslabel> trianglePieces = pieces.get(i);
                for (NetworkTaslabel piece : trianglePieces) {
                    if (piece.getTasRenk() == Color.BLACK) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}