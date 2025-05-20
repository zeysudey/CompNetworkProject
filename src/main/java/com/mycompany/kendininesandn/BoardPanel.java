/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.kendininesandn;

import java.awt.Color;
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
import com.mycompany.kendininesandn.Taslabel;

/**
 *
 * @author zeysu
 */
public class BoardPanel extends javax.swing.JPanel {
     private ehe parentFrame;
        private Triangle[] triangles = new Triangle[24];
    private Image backgroundImage;
    private Taslabel selectedPiece = null;
    private Map<Integer, List<Taslabel>> pieces = new HashMap<>();
    private int[] diceValues = {0, 0};
    private boolean whiteTurn = true;
    private List<Integer> legalMoves = new ArrayList<>();
    private Taslabel hoverPiece = null;
    private BoardPanel boardPanel;

    // Yeni eklemeler
    private List<Taslabel> whiteBar = new ArrayList<>();
    private List<Taslabel> blackBar = new ArrayList<>();
    List<Taslabel> whiteHome = new ArrayList<>();
    List<Taslabel> blackHome = new ArrayList<>();
    private static final int BAR_INDEX = 24;
    private static final int WHITE_HOME_INDEX = 25;
    private static final int BLACK_HOME_INDEX = 26;
    private List<Integer> usedDice = new ArrayList<>();
    private boolean diceRolled = false;
    private boolean isWhite;

    public BoardPanel() {
        initComponents();
        setLayout(null);
        setPreferredSize(new Dimension(1100, 600));

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/tav.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < 24; i++) {
            pieces.put(i, new ArrayList<>());
        }

        setupTriangles();
        setupInitialStones();
    }
     public boolean isWhite() {
        return isWhite;
    }

public void nextTurn() {
    whiteTurn = !whiteTurn;
}

    

   public void pieceClicked(Taslabel piece) {
    if ((whiteTurn && !piece.isWhite()) || (!whiteTurn && piece.isWhite())) {
        System.out.println("Sıra diğer oyuncuda!");
        return;
    }

    if (selectedPiece == null) {
        selectedPiece = piece;
        selectedPiece.setBackground(Color.YELLOW);
    } else {
        selectedPiece.setBackground(null);
        selectedPiece.moveTo(piece.getX(), piece.getY());
        selectedPiece = null;
        whiteTurn = !whiteTurn; // turn değiştir
    }
}
  public void setDiceValues(int dice1, int dice2) {
    this.diceValues[0] = dice1;
    this.diceValues[1] = dice2;
    usedDice.clear();

    if (selectedPiece != null) {
        updateLegalMoves(selectedPiece);
    }

    repaint();
}


  // 1. En başta, whiteTurn değişkenini güncelleme şeklini düzeltelim
public void setWhiteTurn(boolean whiteTurn) {
    this.whiteTurn = whiteTurn;
    System.out.println("BoardPanel: Sıra " + (whiteTurn ? "BEYAZ" : "SİYAH") + " olarak ayarlandı");
    repaint(); // UI güncellemesi için repaint çağırıyoruz
}

public void toggleTurn() {
    whiteTurn = !whiteTurn;
    System.out.println("Sıra değişti: " + (whiteTurn ? "BEYAZ" : "SİYAH"));
    clearLegalMoves();
    usedDice.clear();
    diceValues[0] = 0;
    diceValues[1] = 0;
    diceRolled = false;
    checkForWinner();
    
    // Ebeveyn pencereyi bilgilendir
    if (parentFrame != null) {
        parentFrame.changeTurn();
    }
    
    repaint();
}
    
    public void setSelectedPiece(Taslabel piece) {
        if (piece != null) {
            if ((piece.getTasRenk() == Color.WHITE && !whiteTurn) || 
                (piece.getTasRenk() == Color.BLACK && whiteTurn)) {
                return;
            }
        }
        
        this.selectedPiece = piece;
        
        if (piece != null) {
            updateLegalMoves(piece);
        } else {
            clearLegalMoves();
        }
    }
    
    public void updateLegalMoves(Taslabel piece) {
        legalMoves.clear();
        
        if (piece == null) return;
        
        if ((piece.getTasRenk() == Color.WHITE && !whiteTurn) || 
            (piece.getTasRenk() == Color.BLACK && whiteTurn)) {
            return;
        }
        
        int currentTriangle = piece.getTriangleIndex();
        Color pieceColor = piece.getTasRenk();
        
        if (hasBarPieces(pieceColor)) {
            if (currentTriangle != BAR_INDEX) {
                return;
            }
            calculateBarMoves(piece);
            return;
        }
        
        if (!canBearOff(pieceColor)) {
            calculateNormalMoves(piece, currentTriangle);
        } else {
            calculateBearOffMoves(piece, currentTriangle);
        }
    }
    
    private void calculateBarMoves(Taslabel piece) {
        Color pieceColor = piece.getTasRenk();
        int startPosition = (pieceColor == Color.WHITE) ? -1 : 24;
        
        for (int i = 0; i < diceValues.length; i++) {
            if (diceValues[i] == 0 || usedDice.contains(i)) continue;
            
            int targetTriangle = (pieceColor == Color.WHITE) ? 
                (startPosition + diceValues[i]) : 
                (startPosition - diceValues[i]);
            
            if (targetTriangle >= 0 && targetTriangle < 24) {
                List<Taslabel> targetPieces = pieces.get(targetTriangle);
                
                if (targetPieces.isEmpty() || targetPieces.get(0).getTasRenk() == pieceColor ||
                    (targetPieces.size() == 1 && targetPieces.get(0).getTasRenk() != pieceColor)) {
                    legalMoves.add(targetTriangle);
                }
            }
        }
    }
    
    private void calculateNormalMoves(Taslabel piece, int currentTriangle) {
        Color pieceColor = piece.getTasRenk();
        int direction = (pieceColor == Color.WHITE) ? 1 : -1;
        
        for (int i = 0; i < diceValues.length; i++) {
            if (diceValues[i] == 0 || usedDice.contains(i)) continue;
            
            int targetTriangle = currentTriangle + (diceValues[i] * direction);
            
            if (targetTriangle >= 0 && targetTriangle < 24 && isValidMove(targetTriangle, pieceColor)) {
                legalMoves.add(targetTriangle);
            }
        }
        
        if (diceValues[0] > 0 && diceValues[1] > 0 && !usedDice.contains(0) && !usedDice.contains(1)) {
            int combinedDice = diceValues[0] + diceValues[1];
            int targetTriangle = currentTriangle + (combinedDice * direction);
            
            if (targetTriangle >= 0 && targetTriangle < 24 && isValidMove(targetTriangle, pieceColor)) {
                legalMoves.add(targetTriangle);
            }
        }
        
        repaint();
    }
    
    private void calculateBearOffMoves(Taslabel piece, int currentTriangle) {
        Color pieceColor = piece.getTasRenk();
        int direction = (pieceColor == Color.WHITE) ? 1 : -1;
        
        calculateNormalMoves(piece, currentTriangle);
        
        int homeStart = (pieceColor == Color.WHITE) ? 18 : 5;
        int homeEnd = (pieceColor == Color.WHITE) ? 23 : 0;
        
        if ((pieceColor == Color.WHITE && currentTriangle >= homeStart) ||
            (pieceColor == Color.BLACK && currentTriangle <= homeStart)) {
            
            for (int i = 0; i < diceValues.length; i++) {
                if (diceValues[i] == 0 || usedDice.contains(i)) continue;
                
                int exactDistance = (pieceColor == Color.WHITE) ? 
                    (24 - currentTriangle) : (currentTriangle + 1);
                
                if (diceValues[i] == exactDistance) {
                    legalMoves.add((pieceColor == Color.WHITE) ? WHITE_HOME_INDEX : BLACK_HOME_INDEX);
                } else if (diceValues[i] > exactDistance && !hasFurtherPieces(currentTriangle, pieceColor)) {
                    legalMoves.add((pieceColor == Color.WHITE) ? WHITE_HOME_INDEX : BLACK_HOME_INDEX);
                }
            }
        }
    }
    
    private boolean hasFurtherPieces(int currentTriangle, Color color) {
        if (color == Color.WHITE) {
            for (int i = 0; i < currentTriangle; i++) {
                if (!pieces.get(i).isEmpty() && pieces.get(i).get(0).getTasRenk() == color) {
                    return true;
                }
            }
        } else {
            for (int i = currentTriangle + 1; i < 24; i++) {
                if (!pieces.get(i).isEmpty() && pieces.get(i).get(0).getTasRenk() == color) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean canBearOff(Color color) {
        if (hasBarPieces(color)) return false;
        
        if (color == Color.WHITE) {
            for (int i = 0; i < 18; i++) {
                if (!pieces.get(i).isEmpty() && pieces.get(i).get(0).getTasRenk() == color) {
                    return false;
                }
            }
        } else {
            for (int i = 6; i < 24; i++) {
                if (!pieces.get(i).isEmpty() && pieces.get(i).get(0).getTasRenk() == color) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean hasBarPieces(Color color) {
        return (color == Color.WHITE && !whiteBar.isEmpty()) || 
               (color == Color.BLACK && !blackBar.isEmpty());
    }
    
    private boolean isValidMove(int targetTriangle, Color pieceColor) {
        List<Taslabel> targetPieces = pieces.get(targetTriangle);
        
        if (targetPieces.isEmpty() || targetPieces.get(0).getTasRenk() == pieceColor) {
            return true;
        }
        
        return targetPieces.size() == 1 && targetPieces.get(0).getTasRenk() != pieceColor;
    }
    
    public void movePiece(Taslabel piece, int fromTriangleIndex, int toTriangleIndex) {
        if (!legalMoves.contains(toTriangleIndex)) {
            repositionPieceInTriangle(piece, fromTriangleIndex);
            return;
        }
        
        if (fromTriangleIndex == toTriangleIndex) {
            repositionPieceInTriangle(piece, fromTriangleIndex);
            return;
        }
        
        if ((piece.getTasRenk().equals(Color.WHITE) && !whiteTurn) || 
            (piece.getTasRenk().equals(Color.BLACK) && whiteTurn)) {
            repositionPieceInTriangle(piece, fromTriangleIndex);
            return;
        }
        
        if (toTriangleIndex == WHITE_HOME_INDEX || toTriangleIndex == BLACK_HOME_INDEX) {
            handleBearOff(piece, fromTriangleIndex, toTriangleIndex);
            return;
        }
        
        List<Taslabel> targetPieces = pieces.get(toTriangleIndex);
        if (!targetPieces.isEmpty() && targetPieces.get(0).getTasRenk() != piece.getTasRenk() && targetPieces.size() == 1) {
            handleCapture(targetPieces.get(0), toTriangleIndex);
        }
        
        if (fromTriangleIndex == BAR_INDEX) {
            if (piece.getTasRenk() == Color.WHITE) {
                whiteBar.remove(piece);
            } else {
                blackBar.remove(piece);
            }
        } else if (fromTriangleIndex >= 0) {
            pieces.get(fromTriangleIndex).remove(piece);
        }
        
        pieces.get(toTriangleIndex).add(piece);
        piece.setTriangleIndex(toTriangleIndex);
        
        rearrangePiecesInTriangle(toTriangleIndex);
        
        updateUsedDice(fromTriangleIndex, toTriangleIndex);
        
        clearLegalMoves();
        
        if (areAllDiceUsed()) {
            toggleTurn();
        }
        
        repaint();
    }
    
    private void handleCapture(Taslabel capturedPiece, int triangleIndex) {
        pieces.get(triangleIndex).remove(capturedPiece);
        remove(capturedPiece);
        
        if (capturedPiece.getTasRenk() == Color.WHITE) {
            whiteBar.add(capturedPiece);
            capturedPiece.setTriangleIndex(BAR_INDEX);
        } else {
            blackBar.add(capturedPiece);
            capturedPiece.setTriangleIndex(BAR_INDEX);
        }
        
        positionPieceOnBar(capturedPiece);
        add(capturedPiece);
    }
    
    private void handleBearOff(Taslabel piece, int fromTriangleIndex, int toTriangleIndex) {
        pieces.get(fromTriangleIndex).remove(piece);
        remove(piece);
        
        if (piece.getTasRenk() == Color.WHITE) {
            whiteHome.add(piece);
            piece.setTriangleIndex(WHITE_HOME_INDEX);
        } else {
            blackHome.add(piece);
            piece.setTriangleIndex(BLACK_HOME_INDEX);
        }
        
        positionPieceInHome(piece);
        add(piece);
        
        updateUsedDice(fromTriangleIndex, toTriangleIndex);
        
        clearLegalMoves();
        
        if (areAllDiceUsed()) {
            toggleTurn();
        }
    }
    
    private void positionPieceOnBar(Taslabel piece) {
        int barX = 540;
        int y = (piece.getTasRenk() == Color.WHITE) ? 350 : 200;
        piece.setBounds(barX, y, 42, 42);
    }
    
    private void positionPieceInHome(Taslabel piece) {
        int homeX = (piece.getTasRenk() == Color.WHITE) ? 1040 : 40;
        int baseY = 250;
        
        int count = (piece.getTasRenk() == Color.WHITE) ? whiteHome.size() : blackHome.size();
        
        int row = (count - 1) / 2;
        int col = (count - 1) % 2;
        
        int x = homeX + col * 25;
        int y = baseY + row * 20;
        
        piece.setBounds(x, y, 20, 20);
    }
    // Getter metodu ekleyin
public boolean isWhiteTurn() {
    return whiteTurn;
}


    private void updateUsedDice(int from, int to) {
    boolean zarKullanildi = false;
    int distance = Math.abs(to - from);
    
    if (from == BAR_INDEX) {
        distance = (whiteTurn) ? (to + 1) : (24 - to);
    } else if (to == WHITE_HOME_INDEX || to == BLACK_HOME_INDEX) {
        distance = (whiteTurn) ? (24 - from) : (from + 1);
    }
    
    System.out.println("Hamle mesafesi: " + distance);
    
    for (int i = 0; i < diceValues.length; i++) {
        if (diceValues[i] == distance && !usedDice.contains(i)) {
            usedDice.add(i);
            zarKullanildi = true;
            System.out.println((i+1) + ". zar kullanıldı: " + diceValues[i]);
            break;
        }
    }
    
    if (!zarKullanildi && diceValues[0] + diceValues[1] == distance && 
        !usedDice.contains(0) && !usedDice.contains(1)) {
        usedDice.add(0);
        usedDice.add(1);
        System.out.println("İki zar birleşik kullanıldı: " + diceValues[0] + " + " + diceValues[1]);
    }
}
    private boolean areAllDiceUsed() {
        return usedDice.size() >= diceValues.length;
    }
    
    private void checkForWinner() {
        if (whiteHome.size() == 15) {
            JOptionPane.showMessageDialog(this, "Beyaz Oyuncu Kazandı!", "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        } else if (blackHome.size() == 15) {
            JOptionPane.showMessageDialog(this, "Siyah Oyuncu Kazandı!", "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        }
    }
    
    public void resetGame() {
        removeAll();
        pieces.clear();
        whiteBar.clear();
        blackBar.clear();
        whiteHome.clear();
        blackHome.clear();
        usedDice.clear();
        legalMoves.clear();
        selectedPiece = null;
        hoverPiece = null;
        
        diceValues[0] = 0;
        diceValues[1] = 0;
        
        for (int i = 0; i < 24; i++) {
            pieces.put(i, new ArrayList<>());
        }
        
        setupInitialStones();
        whiteTurn = true;
        diceRolled = false;
        repaint();
    }
    
    public void setDiceRolled(boolean rolled) {
        this.diceRolled = rolled;
    }
    
    public boolean isDiceRolled() {
        return this.diceRolled;
    }
    
    private void setupTriangles() {
        int triangleWidth = 72;

        int x12 = 105;
        int triangleSpacing = triangleWidth;

        triangles[12] = new Triangle(x12, 50, true);
        triangles[13] = new Triangle(x12 + triangleSpacing, 50, true);
        triangles[14] = new Triangle(x12 + 2 * triangleSpacing, 50, true);
        triangles[15] = new Triangle(x12 + 3 * triangleSpacing, 50, true);
        triangles[16] = new Triangle(x12 + 4 * triangleSpacing, 50, true);
        triangles[17] = new Triangle(x12 + 5 * triangleSpacing, 50, true);

        int x18 = 580;
        triangles[18] = new Triangle(x18, 50, true);
        triangles[19] = new Triangle(x18 + triangleSpacing, 50, true);
        triangles[20] = new Triangle(x18 + 2 * triangleSpacing, 50, true);
        triangles[21] = new Triangle(x18 + 3 * triangleSpacing, 50, true);
        triangles[22] = new Triangle(x18 + 4 * triangleSpacing, 50, true);
        triangles[23] = new Triangle(x18 + 5 * triangleSpacing, 50, true);

        int x0 = x18 + 5 * triangleSpacing;
        triangles[0] = new Triangle(x0, 550, false);
        triangles[1] = new Triangle(x0 - triangleSpacing, 550, false);
        triangles[2] = new Triangle(x0 - 2 * triangleSpacing, 550, false);
        triangles[3] = new Triangle(x0 - 3 * triangleSpacing, 550, false);
        triangles[4] = new Triangle(x0 - 4 * triangleSpacing, 550, false);
        triangles[5] = new Triangle(x0 - 5 * triangleSpacing, 550, false);

        int x6 = x12 + 5 * triangleSpacing;
        triangles[6] = new Triangle(x6, 550, false);
        triangles[7] = new Triangle(x6 - triangleSpacing, 550, false);
        triangles[8] = new Triangle(x6 - 2 * triangleSpacing, 550, false);
        triangles[9] = new Triangle(x6 - 3 * triangleSpacing, 550, false);
        triangles[10] = new Triangle(x6 - 4 * triangleSpacing, 550, false);
        triangles[11] = new Triangle(x6 - 5 * triangleSpacing, 550, false);
    }

    public int findTriangleAt(int x, int y) {
        if (x > 1010 && x < 1090 && y > 200 && y < 450) {
            return WHITE_HOME_INDEX;
        } else if (x > 10 && x < 90 && y > 200 && y < 450) {
            return BLACK_HOME_INDEX;
        }
        
        int closestTriangle = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < triangles.length; i++) {
            Triangle t = triangles[i];

            double distance = Math.sqrt(Math.pow(x - t.x, 2) + Math.pow(y - t.y, 2));

            if (t.isUp && y < 300) {
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            } 
            else if (!t.isUp && y >= 300) {
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTriangle = i;
                }
            }
        }

        if (minDistance > 100) {
            return -1;
        }

        return closestTriangle;
    }

    private void repositionPieceInTriangle(Taslabel piece, int triangleIndex) {
        Triangle t = triangles[triangleIndex];
        List<Taslabel> trianglePieces = pieces.get(triangleIndex);
        int index = trianglePieces.indexOf(piece);

        if (index >= 0) {
            int tasBoyutu = 42;
            int x = t.x - tasBoyutu / 2 - 20;

            int y;
            if (t.isUp) {
                y = t.y + index * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - tasBoyutu - index * (tasBoyutu - 12) - 10;
            }

            piece.setLocation(x, y);
        }
    }

    private void rearrangePiecesInTriangle(int triangleIndex) {
        Triangle t = triangles[triangleIndex];
        List<Taslabel> trianglePieces = pieces.get(triangleIndex);

        for (int i = 0; i < trianglePieces.size(); i++) {
            Taslabel piece = trianglePieces.get(i);

            int tasBoyutu = 42;
            int x = t.x - tasBoyutu / 2 - 20;

            int y;
            if (t.isUp) {
                y = t.y + i * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - tasBoyutu - i * (tasBoyutu - 12) - 10;
            }

            piece.setLocation(x, y);
        }
    }

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

    private void addStonesToTriangle(int index, Color renk, int count) {
        Triangle t = triangles[index];
        List<Taslabel> trianglePieces = pieces.get(index);

        for (int i = 0; i < count; i++) {
            Taslabel tas = new Taslabel();
            tas.setTasGoster(true, renk);

            int tasBoyutu = 42;
            int x = t.x - tasBoyutu / 2 - 20;

            int y;
            if (t.isUp) {
                y = t.y + i * (tasBoyutu - 12) + 10;
            } else {
                y = t.y - tasBoyutu - i * (tasBoyutu - 12) - 10;
            }

            tas.setBounds(x, y, tasBoyutu, tasBoyutu);
            tas.setBoardPanel(this, index);

            trianglePieces.add(tas);
            add(tas);
        }
    }
    
    public void clearLegalMoves() {
        legalMoves.clear();
        selectedPiece = null;
        repaint();
    }

public void setParentFrame(ehe frame) {
    this.parentFrame = frame;
}
    public void setHoverPiece(Taslabel piece) {
        this.hoverPiece = piece;
        if (piece != null && selectedPiece == null) {
            updateLegalMoves(piece);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        g.setColor(new Color(139, 69, 19));
        g.fillRect(530, 0, 30, getHeight());
        
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
        
        if (!whiteBar.isEmpty()) {
            g.setColor(Color.WHITE);
            g.fillOval(540, 320, 20, 20);
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(whiteBar.size()), 548, 335);
        }
        
        if (!blackBar.isEmpty()) {
            g.setColor(Color.BLACK);
            g.fillOval(540, 240, 20, 20);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(blackBar.size()), 548, 255);
        }
        
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
    
        g.setColor(whiteTurn ? Color.WHITE : Color.BLACK);
        g.fillOval(10, 10, 20, 20);
        g.setColor(Color.RED);
        g.drawOval(10, 10, 20, 20);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(String.valueOf(whiteHome.size()), 1040, 340);
        
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(blackHome.size()), 40, 340);
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
