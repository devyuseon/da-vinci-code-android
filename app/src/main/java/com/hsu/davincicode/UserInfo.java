package com.hsu.davincicode;

import lombok.Getter;
import lombok.Setter;

// Singleton
@Getter
@Setter
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

}
