package de.bittim.bitwarn;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "devices";
    private static final String COL1 = "ID";
    private static final String COL2 = "MAC";
    private static final String COL3 = "name";
    private static final String COL4 = "timestamp";
    private static final String COL5 = "alert";

    public DatabaseHelper(@Nullable Context context)
    {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TINYTEXT, " + COL3 + " TINYTEXT, " + COL4 + " LONG," + COL5 + " BOOL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String mac, String name)
    {
        Calendar cal = Calendar.getInstance();
        long cTime = cal.getTimeInMillis();
        int update = -1;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = getData();
        while(c.moveToNext())
        {
            Calendar timestamp = Calendar.getInstance();
            timestamp.setTimeInMillis(c.getLong(3));

            if((cal.get(Calendar.DAY_OF_MONTH) == timestamp.get(Calendar.DAY_OF_MONTH) &&
                    cal.get(Calendar.MONTH) == timestamp.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR)))
            {
                if(mac.equals(c.getString(1)))
                {
                    Log.d(TAG, "addData: Updated device entry: " + name + " at " + mac);
                    update = c.getInt(0);
                }
            }
            else { break; }
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2, mac);
        contentValues.put(COL3, name);
        contentValues.put(COL4, cTime);
        contentValues.put(COL5, false);

        long result = -1;
        if(update > -1)
        {
            result = db.update(TABLE_NAME, contentValues, COL1 + "=" + update, null);
            Log.d(TAG, "addData: Updated " + mac + " " + name + " in " + TABLE_NAME);
        }
        else
        {
            result = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "addData: Added " + mac + " " + name + " to " + TABLE_NAME);
        }

        return result != -1;
    }

    protected boolean removeData(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL1 + "=" + id, null) > 0;
    }

    public Cursor getData()
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public ArrayList<ArrayList<String>> getDataAsArray()
    {
        Cursor c = getData();
        ArrayList<ArrayList<String>> listData = new ArrayList<>();
        ArrayList<String> addresses = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> timestamps = new ArrayList<>();

        c.moveToLast();
        while(c.moveToPrevious())
        {
            addresses.add(c.getString(1));
            names.add(c.getString(2));
            timestamps.add(c.getString(3));
        }

        listData.add(addresses);
        listData.add(names);
        listData.add(timestamps);

        return listData;
    }
}
