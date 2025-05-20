/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.kendininesandn;

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
 *
 * @author zeysu
 */
public class Taslabel extends JLabel{
  private boolean showTas = false;
    private Color tasRenk = Color.BLACK;
    private boolean secili = false;
    private Point dragPoint;
    private int originalX, originalY;
    private BoardPanel boardPanel;
    private int triangleIndex = -1;
    private boolean hover = false;
    private static final int BAR_INDEX = 24;
    private static final int WHITE_HOME_INDEX = 25;
    private static final int BLACK_HOME_INDEX = 26;
    private boolean isWhite;

    public Taslabel(boolean isWhite, BoardPanel boardPanel) {
    this.isWhite = isWhite;
    this.boardPanel = boardPanel;
}
  // Default constructor - required for the original implementation
    public Taslabel() {
        // Default constructor
    }
    public void setTasGoster(boolean goster, Color renk) {
        this.showTas = goster;
        this.tasRenk = renk;
        repaint();
    }
    public boolean isWhite() {
    return tasRenk == Color.WHITE;
}
    public void moveTo(int x, int y) {
    setLocation(x, y);
    boardPanel.nextTurn();  // whiteTurn burada değiştirilsin
}
    public void setBoardPanel(BoardPanel panel, int triangleIndex) {
        this.boardPanel = panel;
        this.triangleIndex = triangleIndex;
        setupMouseListeners();
    }
    
    public int getTriangleIndex() {
        return this.triangleIndex;
    }
    
    public void setTriangleIndex(int index) {
        this.triangleIndex = index;
    }
    
    public Color getTasRenk() {
        return this.tasRenk;
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!showTas) return;
                
                // Bar'daki veya Home'daki taşlar hareket ettirilmez
                if (triangleIndex == WHITE_HOME_INDEX || triangleIndex == BLACK_HOME_INDEX) {
                    return;
                }
                
                secili = true;
                originalX = getX();
                originalY = getY();
                dragPoint = e.getPoint();
                
                getParent().setComponentZOrder(Taslabel.this, 0);
                boardPanel.setSelectedPiece(Taslabel.this);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!secili) return;
                
                secili = false;
                
                int targetTriangleIndex = boardPanel.findTriangleAt(getX() + getWidth()/2, getY() + getHeight()/2);
                
                if (targetTriangleIndex != -1) {
                    boardPanel.movePiece(Taslabel.this, triangleIndex, targetTriangleIndex);
                } else {
                    setLocation(originalX, originalY);
                }
                
                boardPanel.clearLegalMoves();
                repaint();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!showTas) return;
                
                // Home'daki taşlar için hover efekti yok
                if (triangleIndex == WHITE_HOME_INDEX || triangleIndex == BLACK_HOME_INDEX) {
                    return;
                }
                
                hover = true;
                boardPanel.setHoverPiece(Taslabel.this);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!showTas) return;
                
                hover = false;
                boardPanel.setHoverPiece(null);
                boardPanel.clearLegalMoves();
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!secili) return;
                
                int newX = getX() + e.getX() - dragPoint.x;
                int newY = getY() + e.getY() - dragPoint.y;
                
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
            
            // Taşı çiz
            g2.setColor(tasRenk);
            int diameter = Math.min(getWidth(), getHeight()) - 10;
            g2.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
            
            // Kenar çizgisi
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
            
            // Bar'daki taşlar için özel gösterim
            if (triangleIndex == BAR_INDEX) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval((getWidth() - diameter) / 2 - 2, (getHeight() - diameter) / 2 - 2, diameter + 4, diameter + 4);
            }
            
            // Seçili taşa belirgin kenar çizgisi
            if (secili) {
                if (tasRenk.equals(Color.BLACK)) {
                    g2.setColor(Color.CYAN);
                } else {
                    g2.setColor(Color.RED);
                }
                
                g2.setStroke(new BasicStroke(3.0f));
                int borderDiameter = diameter + 4;
                int borderX = (getWidth() - borderDiameter) / 2;
                int borderY = (getHeight() - borderDiameter) / 2;
                g2.drawOval(borderX, borderY, borderDiameter, borderDiameter);
            }
            
            // Hover efekti
            else if (hover) {
                g2.setColor(new Color(255, 255, 0, 100)); // Yarı saydam sarı
                g2.setStroke(new BasicStroke(2.0f));
                int hoverDiameter = diameter + 6;
                int hoverX = (getWidth() - hoverDiameter) / 2;
                int hoverY = (getHeight() - hoverDiameter) / 2;
                g2.drawOval(hoverX, hoverY, hoverDiameter, hoverDiameter);
            }
        }
    }

 
}

