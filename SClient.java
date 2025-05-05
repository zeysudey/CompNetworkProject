/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ehe;

/**
 *
 * @author zeysu
 */
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SClient extends Thread {
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    Server ownerServer;

    public SClient(Socket connectedSocket, Server server) throws Exception {
        this.csocket = connectedSocket;
        this.coutput = this.csocket.getOutputStream();
        this.cinput = this.csocket.getInputStream();
        this.ownerServer = server;
    }

    public void run() {
        try {
            while (!this.csocket.isClosed()) {
                int bsize = cinput.read(); // gelen mesajın byte uzunluğu
                if (bsize == -1) break;
                byte[] buffer = new byte[bsize];
                cinput.read(buffer);
                String msg = new String(buffer);
                System.out.println("İstemciden mesaj: " + msg);

                // gelen mesajı diğer tüm client'lara gönder
                ownerServer.SendBroadcastMsg(buffer);
            }
        } catch (Exception e) {
            System.out.println("Bir istemci bağlantısı koptu.");
        }
    }

    public void SendMessage(byte[] msg) throws Exception {
        this.coutput.write(msg.length); // önce uzunluk
        this.coutput.write(msg);        // sonra veri
    }
    }