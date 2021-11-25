package com.hsu.davincicode;

class Room {
    private String roomName;
    private String roomId;
    private int maxCount;
    private int curCount;

    public Room(String roomName, String roomId, int maxCount, int curCount) {
        this.roomName = roomName;
        this.roomId = roomId;
        this.maxCount = maxCount;
        this.curCount = curCount;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getCurCount() {
        return curCount;
    }

    public void setCurCount(int curCount) {
        this.curCount = curCount;
    }
}
