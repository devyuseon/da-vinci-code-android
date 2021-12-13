package com.hsu.davincicode;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private String owner;
    private int cardNum; // 0~11
    private String cardColor;
    private Boolean isJocker;
    private Boolean isOpened;
    private Boolean isNewOpened;
    private Boolean isMyCard;

    public Card(String color, int num, Boolean isMyCard) {
        this.owner = "RoomId";
        this.cardColor = color;
        this.cardNum = num;
        this.isMyCard = isMyCard;
        if (cardNum == -1) this.isJocker = true;
        else this.isJocker = false;
        isOpened = false;
        isNewOpened = false;

    }

    @Override
    public String toString() {
        return "("+ cardColor + " " + cardNum + ")";
    }
}