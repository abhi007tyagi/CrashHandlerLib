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
                .email("abhi007tyagi@gmail.com")
                .alertMessage("Please help us improve by reporting the issue.");
    }
}
