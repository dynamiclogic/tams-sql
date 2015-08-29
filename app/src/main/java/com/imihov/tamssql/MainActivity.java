package com.imihov.tamssql;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import com.imihov.tamssql.Variables;

public class MainActivity extends ActionBarActivity {
    // DB Class to perform DB related operations
    DBController controller = new DBController(this);
    // Progress Dialog Object
    ProgressDialog prgDialog;
    HashMap<String, String> queryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
        // If users exists in SQLite DB
        /*if (userList.size() != 0) {
            // Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, userList, R.layout.view_user_entry, new String[] {
                    "assetId", "name" }, new int[] { R.id.assetId, R.id.name });
            ListView myList = (ListView) findViewById(android.R.id.list);
            myList.setAdapter(adapter);
        }*/
        if(userList.size()!=0){
            //Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter( MainActivity.this,userList, R.layout.view_user_entry, new String[] { "assetId","name"}, new int[] {R.id.assetId, R.id.name});
            ListView myList=(ListView)findViewById(android.R.id.list);
            myList.setAdapter(adapter);
            //Display Sync status of SQLite DB
            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
        }
        // Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Syncing... Please wait...");
        prgDialog.setCancelable(false);
        // BroadCase Receiver Intent Object
        Intent alarmIntent = new Intent(getApplicationContext(), SampleBC.class);
        // Pending Intent Object
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Alarm Manager Object
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        // Alarm Manager calls BroadCast for every Ten seconds (10 * 1000), BroadCase further calls service to check if new records are inserted in 
        // Remote MySQL DB
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 5000, 10 * 1000, pendingIntent);
    }

    // Options Menu (ActionBar Menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // When Options Menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //When Sync action button is clicked
        if (id == R.id.refresh) {
            //Sync SQLite DB data to remote MySQL DB
            insertSQLitetoMySQL();
            insertMySQLtoSQLite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Add User method getting called on clicking (+) button
    public void addUser(View view) {
        Intent objIntent = new Intent(getApplicationContext(), NewUser.class);
        startActivity(objIntent);
    }

    // Method to Sync MySQL to SQLite DB
    public void insertMySQLtoSQLite() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        // Show ProgressBar
        prgDialog.show();
        // Make Http call to getusers.php
        client.post(Variables._IPADDRESS + "/mysqlsqlitesync/getusers.php", params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                // Hide ProgressBar
                prgDialog.hide();
                // Update SQLite DB with response sent by getusers.php
                updateSQLite(response);
            }

            // When error occured
            @Override
            public void onFailure(int statusCode, Header[] headers, String content, Throwable error) {
                // TODO Auto-generated method stub
                // Hide ProgressBar
                prgDialog.hide();
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    // Method to Sync SQLite to MySQL DB
    public void insertSQLitetoMySQL(){
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
        if(userList.size()!=0){
            if(controller.dbSyncCount() != 0){
                prgDialog.show();
                params.put("usersJSON", controller.composeJSONfromSQLite());
                System.out.println("Params:"+params);
                client.post(Variables._IPADDRESS + "/sqlitemysqlsync/insertuser.php",params ,new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response) {
                        System.out.println(response);
                        System.out.println("TESTINGGGG");
                        prgDialog.hide();
                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                System.out.println(obj.get("id"));
                                System.out.println("ASSET Sync: "+obj.get("needsSync"));
                                controller.updateSyncStatus(obj.get("id").toString(),obj.get("needsSync").toString());
                            }
                            Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String content, Throwable error) {
                        // TODO Auto-generated method stub
                        prgDialog.hide();
                        if(statusCode == 404){
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
        }
    }

    public void updateSQLite(String response){
        //long time_stamp = System.currentTimeMillis() / 1000L;
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            System.out.println(arr.length());
            // If no of array elements is not zero
            if(arr.length() != 0){
                // Loop through each array element, get JSON object which has assetid and username
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                    System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                    System.out.println("Remote timestamp" +Double.parseDouble(obj.get("last_timestamp").toString()));
                    //server return list of assets
                    //ensure that asset is not present on the device or
                    //if it is, the version on the server is newer
                    //TODO: THIS inserts new record if it find that timestamp on server is newer -> change it to update existing record
                    //      Split into two if's one creates new record if doesnt exist, the other updates an existing record
                    //      when downloading data from server set the time stamp that is stored on the server, do not create a new one
                    if (!controller.hasAsset((String)obj.get("assetId"))){
                        // Get JSON object
                        System.out.println(obj.get("assetId"));
                        System.out.println(obj.get("name"));
                        System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                        System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                        System.out.println("Remote timestamp" +Integer.parseInt(obj.get("last_timestamp").toString()));

                        // DB QueryValues Object to insert into SQLite
                        queryValues = new HashMap<String, String>();
                        // Add assetID extracted from Object
                        queryValues.put("assetId", obj.get("assetId").toString());
                        // Add userName extracted from Object
                        queryValues.put("name", obj.get("name").toString());
                        queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                        queryValues.put("needsSync", "0");
                        // Insert User into SQLite DB
                        controller.insertRemoteUser(queryValues);
                        HashMap<String, String> map = new HashMap<String, String>();
                        // Add status for each User in Hashmap
                        map.put("id", obj.get("assetId").toString());
                        //map.put("last_timestamp", String.valueOf(time_stamp));
                        map.put("last_timestamp", obj.get("last_timestamp").toString());
                        map.put("needsSync", "0");
                        usersynclist.add(map);
                    }
                    else if(controller.hasAsset(obj.get("assetId").toString()) &&
                            controller.getAssetTimestamp(obj.get("assetId").toString())<Integer.parseInt(obj.get("last_timestamp").toString())){

                        // DB QueryValues Object to insert into SQLite
                        queryValues = new HashMap<String, String>();
                        // Add assetID extracted from Object
                        queryValues.put("assetId", obj.get("assetId").toString());
                        // Add userName extracted from Object
                        queryValues.put("name", obj.get("name").toString());
                        queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                        queryValues.put("needsSync", "0");
                        // Insert User into SQLite DB
                        controller.updateRemoteUser(queryValues);
                        HashMap<String, String> map = new HashMap<String, String>();
                        // Add status for each User in Hashmap
                        map.put("id", obj.get("assetId").toString());
                        //map.put("last_timestamp", String.valueOf(time_stamp));
                        map.put("last_timestamp", obj.get("last_timestamp").toString());
                        map.put("needsSync", "0");
                        usersynclist.add(map);                    }
                    else if (controller.remoteAssetDeleted(arr)){
                        Toast.makeText(getApplicationContext(), "Nothing to update", Toast.LENGTH_LONG).show();
                    }
                    controller.remoteAssetDeleted(arr);
                }
                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                //updateMySQLSyncSts(gson.toJson(usersynclist));
                // Reload the Main Activity
                reloadActivity();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    // Method to inform remote MySQL DB about completion of Sync activity
    /*public void updateMySQLSyncSts(String json) {
        System.out.println(json);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("last_timestamp", json);
        // Make Http call to updatesyncsts.php with JSON parameter which has Sync statuses of Users
        client.post("http://10.117.243.22:8888/mysqlsqlitesync/updatesyncsts.php", params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Toast.makeText(getApplicationContext(), "MySQL DB has been informed about Sync activity", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String content, Throwable error) {
                Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
            }
        });
    }*/

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(objIntent);
    }
}