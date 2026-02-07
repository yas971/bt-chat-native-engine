package com.native.chat;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;
    private TextView logView;
    private EditText inputMsg;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        logView = new TextView(this);
        logView.setHeight(600);
        layout.addView(logView);

        Button scanBtn = new Button(this);
        scanBtn.setText("SCANNER & CONNECTER");
        scanBtn.setOnClickListener(v -> scanDevices());
        layout.addView(scanBtn);

        inputMsg = new EditText(this);
        layout.addView(inputMsg);

        sendBtn = new Button(this);
        sendBtn.setText("ENVOYER");
        sendBtn.setOnClickListener(v -> sendMessage());
        layout.addView(sendBtn);

        setContentView(layout);
        adapter = BluetoothAdapter.getDefaultAdapter();
        
        requestPermissions(new String[]{
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.ACCESS_FINE_LOCATION"
        }, 1);
        
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket server = adapter.listenUsingInsecureRfcommWithServiceRecord("Chat", MY_UUID);
                socket = server.accept();
                manageConnection();
            } catch (Exception e) {}
        }).start();
    }

    private void scanDevices() {
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        for (BluetoothDevice d : paired) {
            new Thread(() -> {
                try {
                    socket = d.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();
                    manageConnection();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void manageConnection() {
        runOnUiThread(() -> {
            logView.append("\nCONNECTÉ !");
        });
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int bytes = input.read(buffer);
                String msg = new String(buffer, 0, bytes);
                runOnUiThread(() -> logView.append("\nReçu: " + msg));
            }
        } catch (Exception e) {}
    }

    private void sendMessage() {
        try {
            String msg = inputMsg.getText().toString();
            output.write(msg.getBytes());
            logView.append("\nMoi: " + msg);
            inputMsg.setText("");
        } catch (Exception e) {}
    }
}