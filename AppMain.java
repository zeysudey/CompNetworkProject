/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ehe;

/**
 *
 * @author zeysu
 */
import java.io.IOException;
import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        try {
            Server server = new Server(6000);
            server.StartAcceptance();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Sunucudan mesaj gönderin: ");
                String msg = scanner.nextLine();
                server.SendBroadcastMsg(msg.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
