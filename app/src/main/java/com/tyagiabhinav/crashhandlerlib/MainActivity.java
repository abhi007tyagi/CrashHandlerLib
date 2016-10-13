package com.tyagiabhinav.crashhandlerlib;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createError(View view) {
        Toast.makeText(this, "Testing", Toast.LENGTH_LONG).show();
        String a = null;
        String b = a.split(",")[0];
    }
}
