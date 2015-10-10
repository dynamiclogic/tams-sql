package com.imihov.tamssql;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewAsset extends Activity {
    EditText name2;
    DBController controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_asset);
        name2 = (EditText) findViewById(R.id.name2);
        controller = DBController.getInstance(this);
    }

    /**
     * Called when Save button is clicked 
     * @param view
     */
    public void addNewAsset(View view) {
        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put(Variables._COLUMN_ASSETNAME, name2.getText().toString());

        if (name2.getText().toString() != null
                && name2.getText().toString().trim().length() != 0) {
            controller.insertAsset(queryValues);
            this.callHomeActivity(view);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter User name",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navigate to Home Screen 
     * @param view
     */
    public void callHomeActivity(View view) {
        Intent objIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(objIntent);
    }

    /**
     * Called when Cancel button is clicked
     * @param view
     */
    public void cancelAddAsset(View view) {
        this.callHomeActivity(view);
    }
}