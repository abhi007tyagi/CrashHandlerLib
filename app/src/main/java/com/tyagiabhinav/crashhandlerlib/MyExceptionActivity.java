package com.tyagiabhinav.crashhandlerlib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.tyagiabhinav.crashhandler.CrashHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MyExceptionActivity extends AppCompatActivity {

    private static final String TAG = MyExceptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_exception);

        final String errorMsg = getIntent().getStringExtra(CrashHandler.ERROR_REPORT);
        final String appName = getIntent().getStringExtra(CrashHandler.APP_NAME);
        final String alrtMsg = getIntent().getStringExtra(CrashHandler.ALERT_MESSAGE);
        final Throwable error = (Throwable) getIntent().getExtras().get(CrashHandler.ERROR);

        Log.d(TAG, alrtMsg);

        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        ((TextView)findViewById(R.id.tv)).setText(errorMsg);
    }
}
