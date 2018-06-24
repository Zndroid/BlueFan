package com.hakz.www.bluefan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private ListView lv;
    private ArrayList<BluetoothDevice> list;
    private DeviceListAdapter listAdapter;
    private ImageButton btn_scan;

    private boolean mScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "您的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();//结束该界面
        }
        // 如果没有打开蓝牙的话，就自动打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        lv = (ListView) findViewById(R.id.lv);
        btn_scan = (ImageButton) findViewById(R.id.btn_scan);
        list = new ArrayList<>();

        listAdapter = new DeviceListAdapter();
        lv.setAdapter(listAdapter);

        initScan();

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning) {
                    endScan();
                } else {
                    startScan();
                }
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = listAdapter.getItem(position);
                if (device == null)
                    return;

                final Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra(DeviceActivity.DEVICE, device);
                if (mScanning) {
                    endScan();
                }
                mBluetoothAdapter.cancelDiscovery();
                startActivity(intent);
            }
        });
    }

    private void startScan() {
        list.clear();
        listAdapter.notifyDataSetChanged();
        mBluetoothAdapter.startDiscovery();
        btn_scan.post(new Runnable() {
            @Override
            public void run() {
                btn_scan.setImageResource(R.mipmap.s_off);
            }
        });
        mScanning = true;
    }

    private void endScan() {
        mBluetoothAdapter.cancelDiscovery();
        btn_scan.post(new Runnable() {
            @Override
            public void run() {
                btn_scan.setImageResource(R.mipmap.s_on);
            }
        });
        mScanning = false;
    }

    private void initScan() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    if (!list.contains(device)) {
                        list.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    endScan();
                }
            }
        };

        //注册蓝牙广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(receiver, filter);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭蓝牙
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }

        // 注销广播
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }


    class DeviceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(MainActivity.this, R.layout.device_item, null);
            TextView device_name = (TextView) view.findViewById(R.id.device_name);
            TextView device_address = (TextView) view.findViewById(R.id.device_address);
            BluetoothDevice device = getItem(position);
            device_name.setText(TextUtils.isEmpty(device.getName()) ? "未知设备" : device.getName());
            device_address.setText(device.getAddress());
            return view;
        }
    }
}
