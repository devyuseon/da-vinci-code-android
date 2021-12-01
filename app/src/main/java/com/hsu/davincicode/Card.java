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

    public Card(String color, int num) {
        this.owner = "RoomId";
        this.cardColor = color;
        this.cardNum = num;
        if (cardNum == -1) this.isJocker = true;
        else this.isJocker = false;
        isOpened = false;
    }

    public Card() { // 상대방 카드 초기화할때
        cardNum = 100; // 모르는 카드
        cardColor = "n"; // 모르는 카드 색은 n
        isJocker = false; // 모르는 카드 조커 기본값은 false
        isOpened = false;
    }

    @Override
    public String toString() {
        return "("+ cardColor + " " + cardNum + ")";
    }
}