package com.imihov.tamssql;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by imihov on 8/26/15.
 */
public class MainActivity extends AppCompatActivity implements Observer{
    // DB Class to perform DB related operations
    private DBController controller;
    private DBSync dbSync;

    // Progress Dialog Object
    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = DBController.getInstance(this);
        this.getApplication();
        dbSync = new DBSync(this);
        dbSync.addObserver(this);

        // Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllAssets();
        System.out.println("LOCAL:");
        System.out.println(userList);
        // If users exists in SQLite DB
        if(userList.size()!=0){
            //Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter( MainActivity.this,userList, R.layout.view_user_entry, new String[] {
                    Variables._ASSETS_COLUMN_ASSET_ID, Variables._ASSETS_COLUMN_ASSET_NAME}, new int[] {R.id.assetId, R.id.name});
            ListView myList=(ListView)findViewById(android.R.id.list);
            myList.setAdapter(adapter);
            //Display Sync status of SQLite DB
            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();

            // on seleting single product
            // launching Edit Product Screen
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // getting values from selected ListItem
                    String aid = ((TextView) view.findViewById(R.id.assetId)).getText()
                            .toString();

                    // Starting new intent
                    Intent in = new Intent(getApplicationContext(), EditAsset.class);
                    Bundle b = new Bundle();

                    b.putString(Variables._ASSETS_COLUMN_ASSET_ID, aid);
                    // sending id to next activity
                    in.putExtras(b);

                    // starting new activity and expecting some response back
                    //startActivityForResult(in, 100);
                    startActivity(in);
                }
            });
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

    @Override
    public void update(Observable observable, Object data) {
        // This method is notified after data changes.
       reloadActivity();
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
            dbSync.sync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Add User method getting called on clicking (+) button
    public void addUser(View view) {
        Intent objIntent = new Intent(getApplicationContext(), NewAsset.class);
        startActivity(objIntent);
    }

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(objIntent);
    }
}