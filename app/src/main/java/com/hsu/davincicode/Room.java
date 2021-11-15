package com.hsu.davincicode;

class Room {
    private int maxCount;
    private int curCount;
    private String roomName;


    public Room(String roomName, int curCount, int maxCount) {
        this.curCount = curCount;
        this.maxCount = maxCount;
        this.roomName = roomName;
        }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getCurCount() {
        return curCount;
    }

    public void setCurCount(int curCount) {
        this.curCount = curCount;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
