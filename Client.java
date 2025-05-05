/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ehe;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author zeysu
 */
public class Client {
    private OutputStream out;
    private InputStream in;
    private NewJFrame gui;

    public Client(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        out = socket.getOutputStream();
        in = socket.getInputStream();

        // Dinleyici thread
        new Thread(() -> {
            try {
                while (true) {
                    int size = in.read();
                    if (size == -1) break;
                    byte[] buffer = new byte[size];
                    in.read(buffer);
                    String msg = new String(buffer);
                    if (gui != null) gui.showIncomingMessage(msg);
                }
            } catch (Exception e) {
                System.out.println("Bağlantı koptu.");
            }
        }).start();
    }

    public void attachGUI(NewJFrame gui) {
        this.gui = gui;
    }

    public void sendMessage(String msg) {
        try {
            byte[] bmsg = Message.GenerateMsg(Message.Type.TOCLIENT, "0," + msg).getBytes();
            out.write(bmsg.length);
            out.write(bmsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
