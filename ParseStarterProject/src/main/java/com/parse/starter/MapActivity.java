package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by samee on 2016-01-31.
 */
public class MapActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
    }

    public void gobackMainActivity(View view)
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
