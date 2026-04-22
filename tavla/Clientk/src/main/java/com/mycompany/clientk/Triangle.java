package com.mycompany.clientk;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author zeysu
 */
/**
 * Tavla tahtasındaki üçgen noktaları temsil eden sınıf
 * @author zeysu
 */
public class Triangle {
    int x, y;        // Üçgenin koordinatları
    boolean isUp;    // Üçgenin yukarı mı aşağı mı baktığı

    /**
     * Triangle constructor
     * @param x X koordinatı
     * @param y Y koordinatı  
     * @param isUp Üçgenin yukarı bakıp bakmadığı (true=yukarı, false=aşağı)
     */
    public Triangle(int x, int y, boolean isUp) {
        this.x = x;
        this.y = y;
        this.isUp = isUp;
    }
    
    /**
     * X koordinatını döndürür
     * @return x koordinatı
     */
    public int getX() {
        return x;
    }
    
    /**
     * Y koordinatını döndürür
     * @return y koordinatı
     */
    public int getY() {
        return y;
    }
    
    /**
     * Üçgenin yönünü döndürür
     * @return true ise yukarı, false ise aşağı
     */
    public boolean isUp() {
        return isUp;
    }
    
    /**
     * X koordinatını ayarlar
     * @param x yeni x koordinatı
     */
    public void setX(int x) {
        this.x = x;
    }
    
    /**
     * Y koordinatını ayarlar
     * @param y yeni y koordinatı
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Üçgenin yönünü ayarlar
     * @param isUp yeni yön (true=yukarı, false=aşağı)
     */
    public void setUp(boolean isUp) {
        this.isUp = isUp;
    }
    
    /**
     * Üçgenin string temsilini döndürür
     * @return string temsili
     */
    @Override
    public String toString() {
        return "Triangle{" +
                "x=" + x +
                ", y=" + y +
                ", isUp=" + isUp +
                '}';
    }
    
    /**
     * İki üçgenin eşit olup olmadığını kontrol eder
     * @param obj karşılaştırılacak obje
     * @return eşitse true, değilse false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Triangle triangle = (Triangle) obj;
        return x == triangle.x && y == triangle.y && isUp == triangle.isUp;
    }
    
    /**
     * Hash code döndürür
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + (isUp ? 1 : 0);
        return result;
    }
}