package com.imihov.tamssql;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DBController extends SQLiteOpenHelper {

    private static DBController dbInstance = null;
    private Context mCxt;

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DBController(Context ctx) {
        super(ctx, Variables.DATABASE_NAME, null, Variables.DATABASE_VERSION);
        this.mCxt = ctx;
    }

    /**
     * Get the singleton
     * use the application context.
     * this will ensure that you dont accidentally leak an Activity context
     *
     * @param ctx
     * @return
     */
    public static DBController getInstance(Context ctx) {
        if (dbInstance == null) {
            dbInstance = new DBController(ctx.getApplicationContext());
        }
        return dbInstance;
    }

    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE " +Variables._TABLE+ " ( " +
                Variables._COLUMN_ASSETID   + " INTEGER PRIMARY KEY, " +
                Variables._COLUMN_ASSETNAME + " TEXT, " +
                Variables._COLUMN_TIMESTAMP + " INTEGER, " +
                Variables._COLUMN_NEEDSSYNC + " INTEGER, " +
                Variables._COLUMN_DELETED   + " INTEGER DEFAULT '0', " +
                Variables._COLUMN_ISNEW + " INTEGER DEFAULT '0')";
        database.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS " +Variables._TABLE;
        database.execSQL(query);
        onCreate(database);
    }

    /**
     * Inserts Asset into SQLite DB
     * @param queryValues
     */
    protected void insertAsset(HashMap<String, String> queryValues) {
        String time_stamp = toString().valueOf(System.currentTimeMillis() / 1000L);
        String assetId = time_stamp;
        String deleted = "0";
        String needsSync = "1";
        String isNew = "1";

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //if the insert is from the server
        if (queryValues.get(Variables._COLUMN_DELETED) != null ) { deleted = queryValues.get(Variables._COLUMN_DELETED);}
        if (queryValues.get(Variables._COLUMN_TIMESTAMP) != null ) { time_stamp = queryValues.get(Variables._COLUMN_TIMESTAMP);}
        if (queryValues.get(Variables._COLUMN_ASSETID) != null ) { assetId = queryValues.get(Variables._COLUMN_ASSETID);}
        if (queryValues.get(Variables._COLUMN_ISNEW) != null ) { isNew = queryValues.get(Variables._COLUMN_ISNEW);}
        if (queryValues.get(Variables._COLUMN_NEEDSSYNC) != null ) { needsSync = queryValues.get(Variables._COLUMN_NEEDSSYNC);}

        values.put(Variables._COLUMN_ASSETID, assetId);
        values.put(Variables._COLUMN_TIMESTAMP, time_stamp);
        values.put(Variables._COLUMN_NEEDSSYNC, needsSync);
        values.put(Variables._COLUMN_DELETED, deleted);
        values.put(Variables._COLUMN_ISNEW, isNew);
        values.put(Variables._COLUMN_ASSETNAME, queryValues.get(Variables._COLUMN_ASSETNAME));

        database.insert(Variables._TABLE, null, values);
        database.close();
    }

    /**
     * Updates User into SQLite DB
     * @param queryValues
     */
    protected void updateAsset(HashMap<String, String> queryValues) {
        String time_stamp = toString().valueOf(System.currentTimeMillis() / 1000L);
        String deleted = "0";
        String needsSync = "1";
        String isNew = "0";

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (queryValues.get(Variables._COLUMN_DELETED) != null ) { deleted = queryValues.get(Variables._COLUMN_DELETED);}
        if (queryValues.get(Variables._COLUMN_TIMESTAMP) != null ) { time_stamp = queryValues.get(Variables._COLUMN_TIMESTAMP);}
        if (queryValues.get(Variables._COLUMN_ISNEW) != null ) { isNew = queryValues.get(Variables._COLUMN_ISNEW);}
        if (queryValues.get(Variables._COLUMN_NEEDSSYNC) != null ) { needsSync = queryValues.get(Variables._COLUMN_NEEDSSYNC);}
        values.put(Variables._COLUMN_TIMESTAMP, time_stamp);
        values.put(Variables._COLUMN_DELETED, deleted);
        values.put(Variables._COLUMN_NEEDSSYNC, needsSync);
        values.put(Variables._COLUMN_ISNEW, isNew);

        if (queryValues.get(Variables._COLUMN_ASSETNAME) != null)
            values.put(Variables._COLUMN_ASSETNAME, queryValues.get(Variables._COLUMN_ASSETNAME));

        database.update(Variables._TABLE, values, Variables._COLUMN_ASSETID + "=" + queryValues.get(Variables._COLUMN_ASSETID), null);
        database.close();
    }

    /**
     * Marks Asset as deleted
     * @param assetId
     */
    protected void deleteAsset(String assetId) {
        String time_stamp = toString().valueOf(System.currentTimeMillis() / 1000L);
        String deleted = "1";
        String needsSync = "1";

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Variables._COLUMN_DELETED, deleted);
        values.put(Variables._COLUMN_NEEDSSYNC, needsSync);
        values.put(Variables._COLUMN_TIMESTAMP, time_stamp);
        database.update(Variables._TABLE, values, Variables._COLUMN_ASSETID + "=" + assetId, null);
        database.close();
        //System.out.println("Asset Deleted: " + assetId);
    }

    /**
     * Purge Asset from SQLlite
     * @param assetId
     */
    protected void purgeAsset(String assetId) {
        if(hasAsset(assetId)) {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(Variables._TABLE, Variables._COLUMN_ASSETID + "=" + assetId, null);
            database.close();
        }
    }

    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    protected ArrayList<HashMap<String, String>> getAllAssets() {
        ArrayList<HashMap<String, String>> assetsList;
        assetsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " +Variables._TABLE + " WHERE " +Variables._COLUMN_DELETED + " =0";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                for(int i=0; i<cursor.getColumnCount();i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }
                assetsList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        //System.out.println("ALL:");
        //System.out.println(assetsList);
        return assetsList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    protected String assetsToJSON(boolean needSyncOnly){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<>();
        String selectQuery = "";
        if (needSyncOnly) {
            selectQuery = "SELECT * FROM " + Variables._TABLE + " WHERE " + Variables._COLUMN_NEEDSSYNC + " = '1'";
        } else {
            selectQuery = "SELECT " + Variables._COLUMN_ASSETID + " FROM " + Variables._TABLE;
        }
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                for(int i=0; i<cursor.getColumnCount();i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get Sync status of SQLite
     * @return
     */
    protected String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }

    /**
     * Get SQLite records that need syncing
     * @return
     */
    protected int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT * FROM assets where needsSync = '1'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param assetId
     * @param needsSync
     */
    protected void updateSyncStatus(String assetId, String needsSync){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Variables._COLUMN_NEEDSSYNC, needsSync);
        database.update(Variables._TABLE, values, Variables._COLUMN_ASSETID + "=" + assetId, null);
        database.close();
        //System.out.println("Asset Sync Status Updated: " + assetId);
    }

    protected boolean isAssetDeleted(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + Variables._TABLE + " WHERE " + Variables._COLUMN_ASSETID + " =? AND "+ Variables._COLUMN_DELETED + "=1";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{id});

        boolean isAssetDeleted = false;
        if(cursor.moveToFirst()){
            isAssetDeleted = true;
        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return isAssetDeleted;
    }

    protected boolean hasAsset(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + Variables._TABLE + " WHERE " + Variables._COLUMN_ASSETID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{id});

        boolean hasAsset = false;
        if(cursor.moveToFirst()){
            hasAsset = true;
        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasAsset;
    }

    protected int getAssetTimestamp(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT " + Variables._COLUMN_TIMESTAMP + " FROM " + Variables._TABLE + " WHERE " + Variables._COLUMN_ASSETID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{id});

        int lastTimeStamp = 0;
        if(cursor.moveToFirst()){

            lastTimeStamp = cursor.getInt(0);
        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return lastTimeStamp;
    }
}