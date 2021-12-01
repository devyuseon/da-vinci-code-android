package com.hsu.davincicode;

import java.io.Serializable;

class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private String owner;
    private int cardNum; // 0~11
    private String cardColor;
    private Boolean isJocker;

    public Card(String color, int num) {
        this.owner = "RoomId";
        this.cardColor = color;
        this.cardNum = num;
        this.isJocker = false;
    }

    public Card(String color, Boolean isJocker) {
        this.owner = "RoomId";
        this.cardColor = color;
        this.cardNum = -1;
        this.isJocker = isJocker;
    }
}