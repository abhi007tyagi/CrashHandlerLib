package com.tyagiabhinav.crashhandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.tyagiabhinav.crashhandler.CrashHandler.ERROR_REPORT_FILE_PATH;
import static com.tyagiabhinav.crashhandler.CrashHandler.REPORT_TO_URL;

public class ExceptionActivity extends AppCompatActivity {

    private static final String TAG = "CrashHandler." + ExceptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        // get intent payloads
        final String errorMsg = getIntent().getStringExtra(CrashHandler.ERROR_REPORT);
        final String appName = getIntent().getStringExtra(CrashHandler.APP_NAME);
        final String alertMsg = getIntent().getStringExtra(CrashHandler.ALERT_MESSAGE);
        final String email = getIntent().getStringExtra(CrashHandler.EMAIL_ADD);
        final boolean isHTMLMessage = getIntent().getBooleanExtra(CrashHandler.IS_HTML_MESSAGE, false);
        final boolean showStackTrace = getIntent().getBooleanExtra(CrashHandler.SHOW_STACKTRACE, false);
        final String reportToURL = getIntent().getStringExtra(REPORT_TO_URL);
        final String errorFile = getIntent().getStringExtra(ERROR_REPORT_FILE_PATH);
        final String callbackActivity = getIntent().getStringExtra(CrashHandler.CALLBACK_ACTIVITY);
        final String callbackBtnText = getIntent().getStringExtra(CrashHandler.CALLBACK_BUTTON_TEXT);
        final int backgroundDrawable = getIntent().getIntExtra(CrashHandler.BACKGROUND_DRAWABLE, -1);

        Log.e(TAG, errorMsg);

        // set UI

        // set background image
        if (backgroundDrawable != -1) {
            ((RelativeLayout) findViewById(R.id.container)).setBackgroundResource(backgroundDrawable);
        }

        // set alert Message
        if (isHTMLMessage) {
            ((TextView) findViewById(R.id.alertMessage)).setText(Html.fromHtml(alertMsg));
        } else {
            ((TextView) findViewById(R.id.alertMessage)).setText(alertMsg);
        }

        // show stacktrace report
        if (showStackTrace) {
            ((TextView) findViewById(R.id.stackTrace)).setText(errorMsg);
        } else {
            findViewById(R.id.scrollView).setVisibility(View.GONE);
        }

        // set callback button properties and listener
        if (callbackActivity != null && !callbackActivity.trim().isEmpty()) {
            Button callbackBtn = (Button) findViewById(R.id.callback);

            // set button text from Intent
            callbackBtn.setText(callbackBtnText);

            // set button click listener
            callbackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick.. Callback");
                    try {
                        Class<? extends Activity> callbackActivityClass = (Class<? extends Activity>) Class.forName(callbackActivity);
                        Intent callbackIntent = new Intent(ExceptionActivity.this, callbackActivityClass);
                        callbackIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        callbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        callbackIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        callbackIntent.putExtras(ExceptionActivity.this.getIntent());
                        startActivity(callbackIntent);
                        ExceptionActivity.this.finish();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(ExceptionActivity.this, "Class Error", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            });
        } else {
            // hide callback button
            findViewById(R.id.callback).setVisibility(View.GONE);
        }


        // set email button properties and listener
        if (email == null || email.trim().isEmpty()) {
            // hide email button
            findViewById(R.id.reportEmail).setVisibility(View.GONE);
        } else {
            Button emailBtn = (Button) findViewById(R.id.reportEmail);
            emailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick.. Email");
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

        // set report button properties and listener
        Button reportBtn = (Button) findViewById(R.id.report);
        // if email address not present, show OK button
        if (reportToURL == null || reportToURL.trim().isEmpty()) {
            reportBtn.setText(android.R.string.ok);
            reportBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick.. OK");
                    finish();
                }
            });
        } else {
            // else show Report button
            reportBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick.. Report");
                    // start background service to upload report to server
                    Intent uploadIntent = new Intent(ExceptionActivity.this, UploadReportIntentService.class);
                    uploadIntent.putExtra(REPORT_TO_URL, reportToURL);
                    uploadIntent.putExtra(ERROR_REPORT_FILE_PATH, (errorFile == null) ? null : errorFile);

                    Log.d(TAG, "starting upload service...");
                    startService(uploadIntent);
                    finish();
                }
            });
        }

    }
}
