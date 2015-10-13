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

import java.util.HashMap;
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
            params.put(Variables._ASSETS_JSON_POST, updatedAssets);
            params.put(Variables._API_AUTH_POST, Variables._API_PASSWORD);

            // Make Http call to push.php
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
                    String isNew, needsSync, purgeAsset, assetId;
                    isNew = needsSync = purgeAsset = assetId = "";
                    HashMap<String, String> queryValues = new HashMap<>();

                    try {
                        JSONArray arr = new JSONArray(response);
                        //System.out.println(arr.length());
                        for(int i=0; i<arr.length();i++){
                            JSONObject obj = (JSONObject)arr.get(i);
                            //System.out.println("RESPONSE:");
                            //System.out.println(obj);

                            //get the returned values
                            if (obj.get(Variables._ASSETS_COLUMN_ISNEW).toString().trim() != "")
                                isNew = obj.get(Variables._ASSETS_COLUMN_ISNEW).toString().trim();
                            assetId = obj.get(Variables._ASSETS_COLUMN_ASSET_ID).toString().trim();
                            needsSync = obj.get(Variables._ASSETS_COLUMN_NEEDSSYNC).toString().trim();

                            //if asset was successfully deleted on server, purge it from sqllite
                            purgeAsset = obj.get("purgeAsset").toString().trim();
                            if(purgeAsset.equals("1")) {
                                controller.purgeAsset(assetId);
                            } else {
                                queryValues.put(Variables._ASSETS_COLUMN_ASSET_ID, assetId);
                                queryValues.put(Variables._ASSETS_COLUMN_ISNEW, isNew);
                                queryValues.put(Variables._ASSETS_COLUMN_NEEDSSYNC, needsSync);
                                controller.updateAsset(queryValues);
                            }
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
        // Create AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        String allAssets = controller.assetsToJSON(false);
        // Show ProgressBar
        //prgDialog.show(); //crashes

        //params.put("assetsJSON", allAssets);
        params.put(Variables._API_AUTH_POST, Variables._API_PASSWORD);

        // Make Http call to pull.php
        client.post(Variables._IPADDRESS + Variables._PULL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                //prgDialog.hide(); //crashes
                if (statusCode == 404) {
                    Toast.makeText(applicationContext.getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(applicationContext.getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationContext.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                // Create GSON object
                Gson gson = new GsonBuilder().create();

                try {
                    // Extract JSON array from the response
                    JSONArray arr = new JSONArray(response);
                    // If no of array elements is not zero
                    if (arr.length() != 0) {
                        // Loop through each array element, get JSON object which has assetId and username
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            System.out.println("RESPONSE:");
                            System.out.println(obj);

                            // DB QueryValues Object to insert into SQLite
                            queryValues = new HashMap<>();
                            queryValues.put(Variables._ASSETS_COLUMN_ASSET_ID, obj.get(Variables._ASSETS_COLUMN_ASSET_ID).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_ASSET_NAME, obj.get(Variables._ASSETS_COLUMN_ASSET_NAME).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_CREATED_AT, obj.get(Variables._ASSETS_COLUMN_CREATED_AT).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_UPDATED_AT, obj.get(Variables._ASSETS_COLUMN_UPDATED_AT).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_NEEDSSYNC, obj.get(Variables._ASSETS_COLUMN_NEEDSSYNC).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_DELETED, obj.get(Variables._ASSETS_COLUMN_DELETED).toString());
                            queryValues.put(Variables._ASSETS_COLUMN_ISNEW, obj.get(Variables._ASSETS_COLUMN_ISNEW).toString());

                            /**
                             * Server returns assets
                             * if asset is not present locally create it
                             * If asset exist locally and the server timestamp is newer, update it
                             * Or if the asset has been market as deleted locally and the server sends it again, update it **commented out
                             */
                            if (!controller.hasAsset(obj.get(Variables._ASSETS_COLUMN_ASSET_ID).toString())) {
                                // Insert asset into SQLite DB
                                controller.insertAsset(queryValues);
                            } else if ((controller.hasAsset(obj.get(Variables._ASSETS_COLUMN_ASSET_ID).toString()) &&
                                    controller.getAssetUpdatedTimestamp(obj.get(Variables._ASSETS_COLUMN_ASSET_ID).toString()) < Integer.parseInt(obj.get(Variables._ASSETS_COLUMN_UPDATED_AT).toString()))
                                    /*|| controller.isAssetDeleted(obj.get("assetId").toString())*/) {

                                // update local asset
                                controller.updateAsset(queryValues);
                            }
                        }
                        //notify the listeners
                        setChanged();
                        notifyObservers();
                    }
                } catch (JSONException e) {
                    Toast.makeText(applicationContext.getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        });
        return finished;
    }
}
