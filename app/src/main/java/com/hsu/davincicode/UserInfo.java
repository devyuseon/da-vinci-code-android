package com.hsu.davincicode;

// Singleton
public class UserInfo {
    private String userName;
    private Room myRoom;

    public UserInfo() {}

    public void init (String userName) {
        this.userName = userName;
    }

    private static class LazyHolder {
        public static final UserInfo uniqueInstance = new UserInfo();
    }

    public static UserInfo getInstance() {
        return LazyHolder.uniqueInstance;
    }

    public String getUserName() {
        return userName;
    }

    public Room getMyRoom() {
        return myRoom;
    }

    public void setMyRoom(Room myRoom) {
        this.myRoom = myRoom;
    }
}
