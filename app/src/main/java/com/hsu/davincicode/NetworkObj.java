package com.hsu.davincicode;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class NetworkObj implements Serializable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkObj() {}

    public void init(Socket socket, ObjectInputStream ois, ObjectOutputStream oos) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
    }

    private static class LazyHolder {
        public static final NetworkObj uniqueInstance = new NetworkObj();
    }

    public static NetworkObj getInstance() {
        return NetworkObj.LazyHolder.uniqueInstance;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }
}
