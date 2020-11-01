package de.bittim.bitwarn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    ImageView status, menuBtn, infoBtn, reportBtn, historyBtn;
    DatabaseHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbh = new DatabaseHelper(getApplicationContext());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        initGUI();
        initButtons();
        scheduleAlarm();

        enableBT();
    }

    public void initGUI()
    {
        status = (ImageView) findViewById(R.id.status);
    }

    public void initButtons()
    {
        menuBtn = (ImageView) findViewById(R.id.menuBtn);
        infoBtn = (ImageView) findViewById(R.id.infoBtn);
        reportBtn = (ImageView) findViewById(R.id.reportBtn);
        historyBtn = (ImageView) findViewById(R.id.historyBtn);

        menuBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { onMenuBtn(); }
        });

        reportBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                status.setImageResource(R.drawable.statusflaglight);
            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { onHistoryBtn(); }
        });
    }

    private void onMenuBtn()
    {
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
    }

    private void onHistoryBtn()
    {
        Intent i = new Intent(this, HistoryActivity.class);

        ArrayList<ArrayList<String>> data = dbh.getDataAsArray();
        i.putExtra("db_addresses", data.get(0));
        i.putExtra("db_names", data.get(1));
        i.putExtra("db_timestamps", data.get(2));

        startActivity(i);
    }

    private void scheduleAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, CleanupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC, new Date().getTime(), 86400000, pendingIntent);
    }

    //================================
    // Bluetooth Stuff
    //================================

    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

    private final BroadcastReceiver btFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("MainActivity", "onReceive: " + device.getName() + " at " + device.getAddress());
                Toast.makeText(context, "Found device " + (device.getName() == "" || device.getName() == null ? device.getAddress() : device.getName()), Toast.LENGTH_LONG).show();
                if(dbh.addData(device.getAddress(), device.getName()))
                {
                    Log.d("MainActivity", "onReceive: Sent DATABASE_UPDATE broadcast");
                    Intent i = new Intent();
                    i.setAction("de.bittim.DATABASE_UPDATE");
                    i.setPackage("de.bittim.bitwarn");
                    sendBroadcast(i);
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
                        Log.d("MainActivity", "onReceive: Changed Scan mode to CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("MainActivity", "onReceive: Changed Scan mode to CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("MainActivity", "onReceive: Changed Scan mode to NONE");
                        btDiscover();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("MainActivity", "onReceive: Changed Scan mode to CONNECTING");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("MainActivity", "onReceive: Changed Scan mode to CONNECTED");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        try { unregisterReceiver(btScanReceiver); }
        catch (final Exception e) { }

        try { unregisterReceiver(btFoundReceiver); }
        catch (final Exception e) { }
    }

    public void enableBT()
    {
        if(!bt.isEnabled())
        {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);
        
        IntentFilter intentFilter = new IntentFilter(bt.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btScanReceiver, intentFilter);
        
        btDiscover();
    }

    public void btDiscover()
    {
        Log.d("MainActivity", "btDiscover: Started Discovery");
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