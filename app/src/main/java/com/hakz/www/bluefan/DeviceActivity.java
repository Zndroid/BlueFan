package com.hakz.www.bluefan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class DeviceActivity extends AppCompatActivity {

    public static final String DEVICE = "DEVICE";
    private static final UUID UUID_KEY_DATA = UUID.fromString
            ("0000ffe1-0000-1000-8000-00805f9b34fb");
    Handler mHandler = null;
    private BluetoothDevice device;
    private BluetoothSocket clientSocket;
    private OutputStream outputStream;

    private SeekBar sb_r,sb_g,sb_b;
    private RelativeLayout rl_connect, rl_control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        device = getIntent().getParcelableExtra(DEVICE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        rl_connect.setVisibility(View.GONE);
                        rl_control.setVisibility(View.VISIBLE);
                        Toast.makeText(DeviceActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        try {
                            outputStream.write((""+Integer.toHexString( sb_r.getProgress())+
                                    Integer.toHexString( sb_g.getProgress())+
                                    Integer.toHexString( sb_b.getProgress())).getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };

        connectBlue();
        initControl();
    }

    private void initControl() {
        rl_connect = (RelativeLayout) findViewById(R.id.rl_connect);
        rl_control = (RelativeLayout) findViewById(R.id.rl_control);
        sb_r = (SeekBar) findViewById(R.id.sb_r);
        sb_g = (SeekBar) findViewById(R.id.sb_g);
        sb_b = (SeekBar) findViewById(R.id.sb_b);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    mHandler.sendEmptyMessage(1);
            }
        });


    }


    private void connectBlue() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = device.createRfcommSocketToServiceRecord(UUID_KEY_DATA);
                } catch (IOException e) {
                    Looper.prepare();
                    Toast.makeText(DeviceActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    e.printStackTrace();
                    finish();
                }

                if (!clientSocket.isConnected()) {
                    try {
                        clientSocket.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            //通过反射来获取端口号
                            clientSocket = (BluetoothSocket) device.getClass().getMethod
                                    ("createRfcommSocket", new Class[]{int.class}).invoke(device,
                                    1);
                            clientSocket.connect();
                        } catch (Exception e1) {
                            Looper.prepare();
                            Toast.makeText(DeviceActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            e1.printStackTrace();
                            finish();
                        }
                    }
                }
                try {
                    outputStream = clientSocket.getOutputStream();
                    mHandler.sendEmptyMessage(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
