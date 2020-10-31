package de.bittim.bitwarn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    ImageView backBtn;
    DatabaseHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbh = new DatabaseHelper(getApplicationContext());

        initBtn();
        populateList(dbh.getDataAsArray());

        IntentFilter filter = new IntentFilter();
        filter.addAction("de.bittim.DATABASE_UPDATE");
        this.registerReceiver(databaseUpdateReceiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        try { unregisterReceiver(databaseUpdateReceiver); }
        catch (final Exception e) { }
    }

    public void initBtn()
    {
        backBtn = (ImageView) findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) { finish(); }
        });
    }

    private void populateList(ArrayList<ArrayList<String>> data)
    {
        View v = findViewById(R.id.listContainer);

        for(int i = 0; i < data.get(0).size(); i++)
        {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_entry_template, (ViewGroup) v, false);

            GradientTextView name = (GradientTextView) layout.findViewById(R.id.name);
            GradientTextView time = (GradientTextView) layout.findViewById(R.id.time);

            String nameString = data.get(1).get(i);
            if(nameString == null || nameString == "") nameString = data.get(0).get(i);
            name.setText(nameString);

            time.setText(DateUtils.formatDateTime(this, (long) Double.parseDouble(data.get(2).get(i)), 0));

            ((ViewGroup) v).addView(layout);
        }
    }

    private final BroadcastReceiver databaseUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.d("HistoryActivity", "onReceive: Received " + action + " broadcast");

            View v = findViewById(R.id.listContainer);
            ((ViewGroup) v).removeAllViews();

            populateList(dbh.getDataAsArray());
        }
    };
}