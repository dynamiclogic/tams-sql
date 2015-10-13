package com.imihov.tamssql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by imihov on 8/26/15.
 */
public class SampleBC extends BroadcastReceiver {
    private DBSync dbSync;
    static int noOfTimes = 0;

    // Method gets called when Broad Case is issued from MainActivity for every 10 seconds
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        noOfTimes++;
        Toast.makeText(context, "BC Service Running for " + noOfTimes + " times", Toast.LENGTH_SHORT).show();

        dbSync = new DBSync(context);
        dbSync.sync();
    }
}