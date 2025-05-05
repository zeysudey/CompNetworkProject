/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ehe;

/**
 *
 * @author zeysu
 */
public class Message {
     public enum Type {
        NONE,
        CLIENTIDS,
        MSGFROMCLIENT,
        MSGFROMSERVER,
        TOCLIENT,
        TAVLA_MOVE,
        GAME_START,
        GAME_END
    }

    public static String GenerateMsg(Type type, String data) {
        return type + "#" + data;
    }
}
