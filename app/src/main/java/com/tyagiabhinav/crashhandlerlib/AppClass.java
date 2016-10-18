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
        CrashHandler.init(new CrashHandler.Configuration(this)
                .alertMessage("Opps! Something went wrong. Help us to improve this application.", false)
                .reportToURL("http://10.0.2.2:8080/FileUpload/UploadServlet"));
    }

}
