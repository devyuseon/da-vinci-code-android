package com.hsu.davincicode;

// Singleton
public class UserInfo {
    private String userName;
    private NetworkObj networkObj;
    private Room myRoom;

    public UserInfo() {}

    public void init (String userName, NetworkObj networkObj) {
        this.userName = userName;
        this.networkObj = networkObj;
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

    public NetworkObj getNetworkObj() {
        return networkObj;
    }

    public Room getMyRoom() {
        return myRoom;
    }

    public void setMyRoom(Room myRoom) {
        this.myRoom = myRoom;
    }
}
