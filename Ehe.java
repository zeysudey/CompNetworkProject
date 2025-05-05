/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ehe;

/**
 *
 * @author zeysu
 */
public class Ehe {
     public static void main(String[] args) {
    try {
        Client client = new Client("localhost", 6000);
        NewJFrame gui = new NewJFrame(client);
        client.attachGUI(gui);

        java.awt.EventQueue.invokeLater(() -> {
            gui.setVisible(true);  // sadece bir tane göster
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
