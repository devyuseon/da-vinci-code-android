package com.hsu.davincicode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hsu.davincicode.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    // network
    final String ip_addr = "10.0.2.2"; // Emulator PC의 127.0.0.1
    final int port_no = 30000;
    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }
}