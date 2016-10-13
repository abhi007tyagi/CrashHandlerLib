package com.tyagiabhinav.crashhandler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExceptionActivity extends AppCompatActivity {

    private static final String TAG = ExceptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        final String errorMsg = getIntent().getStringExtra(CrashHandler.ERROR_REPORT);
        final String appName = getIntent().getStringExtra(CrashHandler.APP_NAME);
        final String alertMsg = getIntent().getStringExtra(CrashHandler.ALERT_MESSAGE);
        final String email = getIntent().getStringExtra(CrashHandler.EMAIL_ADD);
        Log.e(TAG, errorMsg);

        ((TextView) findViewById(R.id.alertMessage)).setText(Html.fromHtml(alertMsg));

        Button reportBtn = (Button) findViewById(R.id.report);

        if (reportBtn != null) {
            if (email == null) {
                reportBtn.setText(android.R.string.ok);
                reportBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick.. OK");
                        finish();
                    }
                });
            } else {
                reportBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick.. Report");
                        Toast.makeText(ExceptionActivity.this, "Select email client to send report", Toast.LENGTH_SHORT).show();
                        String[] TO = {email};

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Error Report from " + appName + "\n\n\n" + errorMsg);
                        emailIntent.setType("*/*");
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, appName + " Error Report");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        finish();
                    }
                });
            }
        }
    }
}
