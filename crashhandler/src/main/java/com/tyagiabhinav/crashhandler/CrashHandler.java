package com.tyagiabhinav.crashhandler;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by abhinavtyagi on 12/10/16.
 */

public class CrashHandler implements UncaughtExceptionHandler {

    private final static String TAG = "CrashHandler";

    /* Default log out time, 7days. */
    private final static long EXPIRY_TIME = TimeUnit.DAYS.toMillis(7);

    /* get DateFormatter for current locale */
    private final static DateFormat FORMATTER = DateFormat.getDateInstance();

    private static final String LINE_SEPARATOR = "\n";
    public static final String ERROR_REPORT = "error_report";
    public static final String APP_NAME = "app_name";
    public static final String ERROR = "error";
    public static final String ALERT_MESSAGE = "alert_msg";
    public static final String EMAIL_ADD = "email_add";

    private volatile boolean isCrashing = false;
    private Context context;
    private Class exceptionActivity;
    private String alertMessage;
    private String emailAdd;


    public static CrashHandler init(Application application) {
        return new CrashHandler(application);
    }

    public CrashHandler exceptionActivity(Class activity) {
        this.exceptionActivity = activity;
        return this;
    }

    public CrashHandler alertMessage(String msg) {
        this.alertMessage = msg;
        return this;
    }

    public CrashHandler email(String email) {
        this.emailAdd = email;
        return this;
    }


    private CrashHandler(Context context) {
        // set default values
        this.context = context;
        this.exceptionActivity = ExceptionActivity.class;
        this.alertMessage = context.getString(R.string.exceptionLabel);

        //delete old logs
        deleteLogs(EXPIRY_TIME);

        UncaughtExceptionHandler originHandler = Thread.currentThread().getUncaughtExceptionHandler();
        // check to prevent set again
        if (this != originHandler) {
            Thread.currentThread().setUncaughtExceptionHandler(this);
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        if (isCrashing) {
            return;
        }
        isCrashing = true;

        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Manufacturer: ");
        errorReport.append(Build.MANUFACTURER);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Board: ");
        errorReport.append(Build.BOARD);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK_INT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        saveToFile(errorReport.toString());

        Intent errorIntent = new Intent(context, exceptionActivity);
        errorIntent.putExtra(ERROR_REPORT, errorReport.toString());
        errorIntent.putExtra(APP_NAME, getApplicationName(context));
        errorIntent.putExtra(ALERT_MESSAGE, alertMessage);
        errorIntent.putExtra(EMAIL_ADD, emailAdd);
        errorIntent.putExtra(ERROR, throwable);

        PendingIntent myActivity = PendingIntent.getActivity(context, 30006, errorIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 7000, myActivity);
        System.exit(2);

        // re-throw critical exception further to the os (important)
        this.uncaughtException(thread, throwable);

    }


    /**
     * Delete outdated logs.
     *
     * @param timeout outdated time
     */
    public void deleteLogs(final long timeout) {
        final File logDir = new File(getCrashDir());
        try {
            final long currTime = System.currentTimeMillis();
            File[] files = logDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File f = new File(dir, filename);
                    return currTime - f.lastModified() > timeout;
                }
            });
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        } catch (Exception e) {
            Log.v(TAG, "exception occurs when deleting outmoded logs", e);
        }
    }


    private String getCrashDir() {
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return rootPath + "/CrashHandler/";
    }


    private boolean saveToFile(String errorLog) {
        String time = FORMATTER.format(new Date());
        String fileName = "Error--" + time + ".log";

        String crashDir = getCrashDir();
        String crashPath = crashDir + fileName;

        File file = new File(crashPath);
        if (file.exists()) {
            file.delete();
        } else {
            try {
                new File(crashDir).mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            return false;
        }
        writer.write(errorLog);
        writer.close();

        return true;
    }


    /**
     * Delete corresponding path, file or directory.
     *
     * @param file path to delete.
     */
    public void delete(File file) {
        delete(file, false);
    }

    /**
     * Delete corresponding path, file or directory.
     *
     * @param file      path to delete.
     * @param ignoreDir whether ignore directory. If true, all files will be deleted while directories is reserved.
     */
    private void delete(File file, boolean ignoreDir) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }

        File[] fileList = file.listFiles();
        if (fileList == null) {
            return;
        }

        for (File f : fileList) {
            delete(f, ignoreDir);
        }
        // delete the folder if need.
        if (!ignoreDir)
            file.delete();
    }

    public String getApplicationName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        String appName = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
            appName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (final PackageManager.NameNotFoundException e) {
            String[] packages = context.getPackageName().split(".");
            appName = packages[packages.length - 1];
        }
        return appName;
    }


}