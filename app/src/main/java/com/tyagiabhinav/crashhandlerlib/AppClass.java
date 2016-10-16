package com.tyagiabhinav.crashhandlerlib;

import android.app.Application;

import com.tyagiabhinav.crashhandler.CrashHandler;

/**
 * Created by abhinavtyagi on 13/10/16.
 */

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(this)
                .alertMessage("Please help us improve by reporting the issue.", false)
                .showStackTraceReport(false)
                .reportToURL("http://10.0.2.2:8080/FileUpload/UploadServlet")
                .background(R.mipmap.ic_launcher);

    }
}
