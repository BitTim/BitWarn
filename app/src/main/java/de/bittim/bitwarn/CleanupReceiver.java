package de.bittim.bitwarn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class CleanupReceiver extends BroadcastReceiver {
    final static String TAG = "CleanupReceiver";
    DatabaseHelper dbh;

    @Override
    public void onReceive(Context context, Intent intent) {
        dbh = new DatabaseHelper(context);

        Date cTime = new Date(Calendar.getInstance().getTimeInMillis());
        Cursor c = dbh.getData();

        while(c.moveToNext())
        {
            long millis = c.getLong(3);
            Date timestamp = new Date(millis);
            long diff = cTime.getTime() - timestamp.getTime();
            float days = (float) diff / 1000f / 60f / 60f / 24f;

            if(days > 16) { dbh.removeData(c.getInt(0)); }
        }
    }
}
