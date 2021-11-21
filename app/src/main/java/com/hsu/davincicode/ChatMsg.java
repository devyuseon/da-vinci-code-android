package com.hsu.davincicode;

import java.util.ArrayList;

// ChatMsg.java 채팅 메시지 ObjectStream 용.
public class ChatMsg {
    public String code;
    public String UserName;
    public String data = "";
    public ArrayList<String> arrayList = new ArrayList<>();

    public ChatMsg(String UserName, String code, String msg) {
        this.code = code;
        this.UserName = UserName;
        this.data = msg;
    }
}
