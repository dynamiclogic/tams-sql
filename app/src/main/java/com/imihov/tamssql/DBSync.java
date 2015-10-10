package com.imihov.tamssql;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

/**
 * Created by imihov on 10/1/15.
 */
public class DBSync extends Observable {
    //ProgressDialog prgDialog; //crashes
    Context applicationContext;
    HashMap<String, String> queryValues;
    private DBController controller;

    public DBSync(Context applicationContext) {
        this.applicationContext = applicationContext;
        controller = DBController.getInstance(applicationContext);

        //prgDialog = new ProgressDialog(applicationContext); //crashes
    }

    protected void sync() {
        boolean push;
        push = push();
        if (push) { //if push is done, call pull
            pull();
        }
    }

    protected boolean push() {
        boolean finished = true;
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        // Show ProgressBar
        //prgDialog.show(); //crashes

        String updatedAssets = controller.assetsToJSON(true);

        if(updatedAssets != null) {
            params.put("assetsJSON", updatedAssets);
            params.put("apiAuth", Variables._API_PASSWORD);
            client.post(Variables._IPADDRESS + Variables._PUSH_URL, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                    //prgDialog.hide(); //crashes
                    if(statusCode == 404){
                        Toast.makeText(applicationContext.getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }else if(statusCode == 500){
                        Toast.makeText(applicationContext.getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(applicationContext.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    String isNew, needsSync, purgeAsset, assetId = "";
                    HashMap<String, String> queryValues = new HashMap<String, String>();

                    try {
                        JSONArray arr = new JSONArray(response);
                        //System.out.println(arr.length());
                        for(int i=0; i<arr.length();i++){
                            JSONObject obj = (JSONObject)arr.get(i);
                            System.out.println("RESPONSE:");
                            System.out.println(obj);

                            //get the returned values
                            assetId = obj.get("assetId").toString().trim();
                            isNew = obj.get("isNew").toString().trim();
                            needsSync = obj.get("needsSync").toString().trim();

                            //if asset was successfully deleted on server, purge it from sqllite
                            purgeAsset = obj.get("purgeAsset").toString().trim();
                            if(purgeAsset.equals("1")) {
                                controller.purgeAsset(assetId);
                            } else {
                                queryValues.put(Variables._COLUMN_ASSETID, assetId);
                                queryValues.put(Variables._COLUMN_ISNEW, isNew);
                                queryValues.put(Variables._COLUMN_NEEDSSYNC, needsSync);
                                controller.updateAsset(queryValues);
                            }
                            //System.out.println("ASSET Sync: "+obj.get("needsSync"));
                            //controller.updateSyncStatus(obj.get("id").toString(),obj.get("needsSync").toString());
                            Toast.makeText(applicationContext.getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(applicationContext.getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }
        return finished;
    }

    protected boolean pull() {
        boolean finished = true; //return true of method finished
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        String allAssets = controller.assetsToJSON(false);
        // Show ProgressBar
        //prgDialog.show(); //crashes

        //params.put("assetsJSON", allAssets);
        params.put("apiAuth", Variables._API_PASSWORD);

        // Make Http call to getusers.php
        client.post(Variables._IPADDRESS + Variables._PULL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                //prgDialog.hide(); //crashes
                if(statusCode == 404){
                    Toast.makeText(applicationContext.getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(applicationContext.getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(applicationContext.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                String isNew, needsSync, purgeAsset, assetId = "";
                ArrayList<HashMap<String, String>> assetsynclist;
                assetsynclist = new ArrayList<>();
                // Create GSON object
                Gson gson = new GsonBuilder().create();

                try {
                    // Extract JSON array from the response
                    JSONArray arr = new JSONArray(response);
                    //System.out.println(arr.length());
                    //store the ids of the current assets in mysql
                    //List<String> remoteAssetIds = new ArrayList<>();
                    // If no of array elements is not zero
                    if(arr.length() != 0){
                        // Loop through each array element, get JSON object which has assetId and username
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            System.out.println("RESPONSE:");
                            System.out.println(obj);
                            //remoteAssetIds.add(obj.get("assetId").toString());
                            //System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                            //System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                            //System.out.println("Remote timestamp" +Double.parseDouble(obj.get("last_timestamp").toString()));
                            //server return list of assets
                            //ensure that asset is not present on the device or
                            //if it is, the version on the server is newer
                            //TODO: THIS inserts new record if it find that timestamp on server is newer -> change it to update existing record
                            //      Split into two if's one creates new record if doesnt exist, the other updates an existing record
                            //      when downloading data from server set the time stamp that is stored on the server, do not create a new one
                            if (!controller.hasAsset((String)obj.get("assetId"))){
                                // Get JSON object
                                //System.out.println(obj.get("assetId"));
                                //System.out.println(obj.get("name"));
                                //System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                                //System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                                //System.out.println("Remote timestamp" +Integer.parseInt(obj.get("last_timestamp").toString()));

                                // DB QueryValues Object to insert into SQLite
                                queryValues = new HashMap<String, String>();
                                // Add assetID extracted from Object
                                queryValues.put("assetId", obj.get("assetId").toString());
                                // Add userName extracted from Object
                                queryValues.put("name", obj.get("name").toString());
                                queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                                queryValues.put("needsSync", obj.get("needsSync").toString());
                                queryValues.put("deleted", obj.get("deleted").toString());
                                queryValues.put("isNew", obj.get("isNew").toString());
                                // Insert User into SQLite DB
                                controller.insertAsset(queryValues);
                                HashMap<String, String> map = new HashMap<String, String>();
                                // Add status for each User in Hashmap
                                map.put("assetId", obj.get("assetId").toString());
                                //map.put("last_timestamp", String.valueOf(time_stamp));
                                map.put("last_timestamp", obj.get("last_timestamp").toString());
                                map.put("needsSync", obj.get("needsSync").toString());
                                map.put("deleted", obj.get("deleted").toString());
                                map.put("isNew", obj.get("isNew").toString());
                                assetsynclist.add(map);
                            }
                            else if((controller.hasAsset(obj.get("assetId").toString()) &&
                                    controller.getAssetTimestamp(obj.get("assetId").toString())<Integer.parseInt(obj.get("last_timestamp").toString()))
                                    || controller.isAssetDeleted(obj.get("assetId").toString())){

                                // DB QueryValues Object to insert into SQLite
                                queryValues = new HashMap<String, String>();
                                // Add assetID extracted from Object
                                queryValues.put("assetId", obj.get("assetId").toString());
                                // Add userName extracted from Object
                                queryValues.put("name", obj.get("name").toString());
                                queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                                queryValues.put("needsSync", obj.get("needsSync").toString());
                                queryValues.put("deleted", obj.get("deleted").toString());
                                queryValues.put("isNew", obj.get("isNew").toString());
                                // Insert User into SQLite DB
                                controller.updateAsset(queryValues);
                                HashMap<String, String> map = new HashMap<String, String>();
                                // Add status for each User in Hashmap
                                map.put("assetId", obj.get("assetId").toString());
                                //map.put("last_timestamp", String.valueOf(time_stamp));
                                map.put("last_timestamp", obj.get("last_timestamp").toString());
                                map.put("needsSync", obj.get("needsSync").toString());
                                map.put("deleted", obj.get("deleted").toString());
                                map.put("isNew", obj.get("isNew").toString());

                                map.put("name", obj.get("name").toString());
                                assetsynclist.add(map);
                            }
                            //else if (controller.remoteAssetDeleted(arr)){
                            //    Toast.makeText(getApplicationContext(), "Nothing to update", Toast.LENGTH_LONG).show();
                            //}
                        }
                        //controller.remoteAssetDeleted(remoteAssetIds);
                        //notify the listeners
                        setChanged();
                        notifyObservers();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(applicationContext.getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        });
        return finished;
    }

    // Method to Sync SQLite to MySQL DB
    private void insertSQLitetoMySQL(){
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  controller.getAllAssets();
        if(userList.size()!=0){
            if(controller.dbSyncCount() != 0){
                //prgDialog.show(); //crashes
                params.put("usersJSON", controller.assetsToJSON(true));
                //System.out.println("Params:"+params);
                client.post(Variables._IPADDRESS + Variables._PUSH_URL,params ,new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response) {
                        //System.out.println(response);
                        //System.out.println("TESTINGGGG");
                        //prgDialog.hide(); //crashes
                        try {
                            JSONArray arr = new JSONArray(response);
                            //System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                //System.out.println(obj.get("id"));
                                //System.out.println("ASSET Sync: "+obj.get("needsSync"));
                                controller.updateSyncStatus(obj.get("id").toString(),obj.get("needsSync").toString());
                            }
                            Toast.makeText(applicationContext.getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(applicationContext.getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String content, Throwable error) {
                        // TODO Auto-generated method stub
                        //prgDialog.hide(); //crashes
                        if(statusCode == 404){
                            Toast.makeText(applicationContext.getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(applicationContext.getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(applicationContext.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(applicationContext.getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(applicationContext.getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
        }
    }

    // Method to Sync MySQL to SQLite DB
    private void insertMySQLtoSQLite() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        // Show ProgressBar
        //prgDialog.show(); //crashes
        // Make Http call to getusers.php
        client.post(Variables._IPADDRESS + Variables._PULL_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                // Hide ProgressBar
                //prgDialog.hide(); //crashes
                // Update SQLite DB with response sent by getusers.php
                updateSQLite(response);
            }

            // When error occured
            @Override
            public void onFailure(int statusCode, Header[] headers, String content, Throwable error) {
                // TODO Auto-generated method stub
                // Hide ProgressBar
                //prgDialog.hide(); //crashes
                if (statusCode == 404) {
                    Toast.makeText(applicationContext.getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(applicationContext.getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationContext.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateSQLite(String response){
        //long time_stamp = System.currentTimeMillis() / 1000L;
        ArrayList<HashMap<String, String>> assetsynclist;
        assetsynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            //System.out.println(arr.length());
            //store the ids of the current assets in mysql
            List<String> remoteAssetIds = new ArrayList<String>();
            // If no of array elements is not zero
            if(arr.length() != 0){
                // Loop through each array element, get JSON object which has assetId and username
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = (JSONObject) arr.get(i);
                    remoteAssetIds.add(obj.get("assetId").toString());
                    //System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                    //System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                    //System.out.println("Remote timestamp" +Double.parseDouble(obj.get("last_timestamp").toString()));
                    //server return list of assets
                    //ensure that asset is not present on the device or
                    //if it is, the version on the server is newer
                    //TODO: THIS inserts new record if it find that timestamp on server is newer -> change it to update existing record
                    //      Split into two if's one creates new record if doesnt exist, the other updates an existing record
                    //      when downloading data from server set the time stamp that is stored on the server, do not create a new one
                    if (!controller.hasAsset((String)obj.get("assetId"))){
                        // Get JSON object
                        //System.out.println(obj.get("assetId"));
                        //System.out.println(obj.get("name"));
                        //System.out.println("Asset exist: " + controller.hasAsset(obj.get("assetId").toString()));
                        //System.out.println("Local timestamp: "+controller.getAssetTimestamp(obj.get("assetId").toString()));
                        //System.out.println("Remote timestamp" +Integer.parseInt(obj.get("last_timestamp").toString()));

                        // DB QueryValues Object to insert into SQLite
                        queryValues = new HashMap<String, String>();
                        // Add assetID extracted from Object
                        queryValues.put("assetId", obj.get("assetId").toString());
                        // Add userName extracted from Object
                        queryValues.put("name", obj.get("name").toString());
                        queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                        queryValues.put("needsSync", "0");
                        queryValues.put("deleted", obj.get("deleted").toString());
                        // Insert User into SQLite DB
                        controller.insertAsset(queryValues);
                        HashMap<String, String> map = new HashMap<String, String>();
                        // Add status for each User in Hashmap
                        map.put("id", obj.get("assetId").toString());
                        //map.put("last_timestamp", String.valueOf(time_stamp));
                        map.put("last_timestamp", obj.get("last_timestamp").toString());
                        map.put("needsSync", "0");
                        map.put("deleted", obj.get("deleted").toString());
                        assetsynclist.add(map);
                    }
                    else if((controller.hasAsset(obj.get("assetId").toString()) &&
                            controller.getAssetTimestamp(obj.get("assetId").toString())<Integer.parseInt(obj.get("last_timestamp").toString()))
                            || controller.isAssetDeleted(obj.get("assetId").toString())){

                        // DB QueryValues Object to insert into SQLite
                        queryValues = new HashMap<String, String>();
                        // Add assetID extracted from Object
                        queryValues.put("assetId", obj.get("assetId").toString());
                        // Add userName extracted from Object
                        queryValues.put("name", obj.get("name").toString());
                        queryValues.put("last_timestamp", obj.get("last_timestamp").toString());
                        queryValues.put("needsSync", "0");
                        queryValues.put("deleted", obj.get("deleted").toString());
                        // Insert User into SQLite DB
                        controller.updateAsset(queryValues);
                        HashMap<String, String> map = new HashMap<String, String>();
                        // Add status for each User in Hashmap
                        map.put("id", obj.get("assetId").toString());
                        //map.put("last_timestamp", String.valueOf(time_stamp));
                        map.put("last_timestamp", obj.get("last_timestamp").toString());
                        map.put("needsSync", "0");
                        map.put("deleted", obj.get("deleted").toString());
                        assetsynclist.add(map);
                    }
                    //else if (controller.remoteAssetDeleted(arr)){
                    //    Toast.makeText(getApplicationContext(), "Nothing to update", Toast.LENGTH_LONG).show();
                    //}
                }
                //controller.remoteAssetDeleted(remoteAssetIds);
                //notify the listeners
                setChanged();
                notifyObservers();
                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                //updateMySQLSyncSts(gson.toJson(usersynclist));
                // Reload the Main Activity
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
