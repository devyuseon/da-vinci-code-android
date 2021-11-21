package com.hsu.davincicode;

import java.util.ArrayList;

public class RoomList {
    private ArrayList<Room> roomList;

    public RoomList() {}

    private static class LazyHolder {
        public static final RoomList uniqueInstance = new RoomList();
    }

    public static RoomList getInstance() {
        return RoomList.LazyHolder.uniqueInstance;
    }

    public ArrayList<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(ArrayList<Room> roomList) {
        this.roomList = roomList;
    }
}
