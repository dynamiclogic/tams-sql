package com.imihov.tamssql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imihov.tamssql.Variables;

import org.json.JSONArray;

public class DBController  extends SQLiteOpenHelper {

    public DBController(Context applicationcontext) {
        super(applicationcontext, "assets.db", null, 1);
    }
    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE " +Variables._TABLE+ " ( assetId INTEGER PRIMARY KEY, name TEXT, last_timestamp INTEGER, needsSync INTEGER)";
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
     * Inserts User into SQLite DB from Mysql
     * @param queryValues
     */
    public void insertRemoteUser(HashMap<String, String> queryValues) {
        //long time_stamp = System.currentTimeMillis() / 1000L;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("assetId", queryValues.get("assetId"));
        values.put("name", queryValues.get("name"));
        values.put("last_timestamp", queryValues.get("last_timestamp"));
        //values.put("last_timestamp", (int)time_stamp);
        database.insert(Variables._TABLE, null, values);
        database.close();
    }

    /**
     * Updates User into SQLite DB from Mysql
     * @param queryValues
     */
    public void updateRemoteUser(HashMap<String, String> queryValues) {
        //long time_stamp = System.currentTimeMillis() / 1000L;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //values.put("assetId", queryValues.get("assetId"));
        values.put("name", queryValues.get("name"));
        values.put("last_timestamp", queryValues.get("last_timestamp"));
        //values.put("last_timestamp", (int)time_stamp);
        database.update(Variables._TABLE, values, Variables._ASSETID + "=" + queryValues.get("assetId"), null);
        database.close();
    }

    /**
     * Inserts User into SQLite DB
     * @param queryValues
     */
    public void insertLocalUser(HashMap<String, String> queryValues) {
        long time_stamp = System.currentTimeMillis() / 1000L;
        System.out.println(time_stamp);
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("assetId", time_stamp);
        values.put("name", queryValues.get("name"));
        values.put("last_timestamp", time_stamp);
        values.put("needsSync", 1);
        //values.put("last_timestamp",(int)time_stamp);
        database.insert("assets", null, values);
        System.out.println(values.toString());
        database.close();
        System.out.println(Arrays.toString(getAllUsers().toArray()));
    }

    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> usersList;
        usersList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " +Variables._TABLE;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("assetId", cursor.getString(0));
                map.put("name", cursor.getString(1));
                map.put("last_timestamp", cursor.getString(2));
                usersList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return usersList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM assets where needsSync = '1'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("assetId", cursor.getString(0));
                map.put("name", cursor.getString(1));
                map.put("last_timestamp", cursor.getString(2));
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
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
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
     * @param id
     * @param needsSync
     */
    public void updateSyncStatus(String id, String needsSync){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update assets set needsSync = '"+ needsSync +"' where assetId="+"'"+ id +"'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    public boolean hasAsset(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + Variables._TABLE + " WHERE " + Variables._ASSETID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{id});

        boolean hasAsset = false;
        if(cursor.moveToFirst()){
            hasAsset = true;

            //region if you had multiple records to check for, use this region.

            //int count = 0;
            //while(cursor.moveToNext()){
            //    count++;
            //}
            //here, count is records found
            //Log.d(TAG, String.format("%d records found", count));

            //endregion

        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasAsset;
    }

    public int getAssetTimestamp(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT " + Variables._TIMESTAMP + " FROM " + Variables._TABLE + " WHERE " + Variables._ASSETID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[] {id});

        int lastTimeStamp = 0;
        if(cursor.moveToFirst()){

            lastTimeStamp = cursor.getInt(0);
        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return lastTimeStamp;
    }

    //todo: also check if asset needs sync or mark the asset as deleted on server
    public boolean remoteAssetDeleted(JSONArray currentAssets) {
        //check if there is a local asset that is not in the currentAssets array and has needsSync = 0
        SQLiteDatabase db = getReadableDatabase();
        String selectString = "SELECT * FROM " + Variables._TABLE;// + " WHERE " + Variables._NEEDSSYNC + " = 1";

        Cursor cursor = db.rawQuery(selectString, null);

        List<String> array = new ArrayList<String>();

        /*while(cursor.moveToNext()) {
            String asset = cursor.getString(cursor.getColumnIndex("assetId"));
            array.add(asset);
        }*/

        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                String name = cursor.getString(cursor
                        .getColumnIndex("needsSync"));

                array.add(name);
                cursor.moveToNext();
            }
        }

        System.out.println("ARRAY:");
        System.out.println(array);

        return false;
    }

}