package com.imihov.tamssql;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class EditAsset extends Activity {
    EditText name;
    private DBController controller;

    String assetId;

    /**
     * Called on activity create
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_asset);
        Bundle b = getIntent().getExtras();
        name = (EditText) findViewById(R.id.name);
        assetId = b.getString(Variables._COLUMN_ASSETID);
        controller = DBController.getInstance(this);
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
    public void cancelEditAsset(View view) {
        this.callHomeActivity(view);
    }

    /**
     * Called when Delete button is clicked
     * @param view
     */
    public void deleteAsset(View view) {
        controller.deleteAsset(assetId);
        this.cancelEditAsset(view);
    }

    /**
     * Called when the Save button is clicked
     * @param view
     */
    public void editAsset(View view) {
        HashMap<String, String> queryValues = new HashMap<>();
        queryValues.put(Variables._COLUMN_ASSETID, assetId);
        queryValues.put(Variables._COLUMN_ASSETNAME, name.getText().toString());

        if (name.getText().toString() != null
                && name.getText().toString().trim().length() != 0) {
            controller.updateAsset(queryValues);
            this.callHomeActivity(view);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter User name",
                    Toast.LENGTH_LONG).show();
        }
    }

}