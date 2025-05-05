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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {
    int clientId;
    ServerSocket ssocket;
    ArrayList<SClient> clients;

    public Server(int port) throws IOException {
        this.clientId = 0;
        this.ssocket = new ServerSocket(port);
        this.clients = new ArrayList<>();
        System.out.println("Server başlatıldı, port: " + port);
    }
       public void SendBroadcastMsg(byte[] bmsg) {
        for (SClient client : clients) {
            try {
                client.SendMessage(bmsg);
            } catch (Exception e) {
                System.out.println("Mesaj gönderilemedi: " + e.getMessage());
            }
        }
    }

    public void StartAcceptance() throws IOException {
        this.start();
    }

  
    @Override
    public void run() {
        try {
            while (!this.ssocket.isClosed()) {
                System.out.println("Yeni istemci bekleniyor...");
                Socket csocket = this.ssocket.accept(); // blocking
                SClient newClient = new SClient(csocket, this);
                newClient.start(); // thread olarak başla
                this.clients.add(newClient);
                System.out.println("Yeni istemci bağlandı.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
