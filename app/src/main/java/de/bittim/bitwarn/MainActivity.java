package de.bittim.bitwarn;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
    public ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private final BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals(bt.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver btScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver btFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);

                Log.d("MainActivity", "onReceive: " + device.getName() + " at " + device.getAddress());
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(btStateReceiver);
        unregisterReceiver(btScanReceiver);
        unregisterReceiver(btFoundReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBT();
        btDiscover();
    }

    public void enableBT()
    {
        if(!bt.isEnabled())
        {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(btStateReceiver, btIntent);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bt.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btScanReceiver, intentFilter);
    }

    public void btDiscover()
    {
        if(bt.isDiscovering()) bt.cancelDiscovery();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0)
            {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }

        bt.startDiscovery();
        IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btFoundReceiver, discoverIntent);
    }
}