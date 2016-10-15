package com.tyagiabhinav.crashhandler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
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

public class CrashHandler extends ActivityCompat implements UncaughtExceptionHandler {

    private final static String TAG = "CrashHandler";

    /* Default log out time, 7days. */
    private final static long EXPIRY_TIME = TimeUnit.DAYS.toMillis(7);

    /* get DateFormatter for current locale */
    private final static DateFormat FORMATTER = DateFormat.getDateTimeInstance();

    private static final String LINE_SEPARATOR = "\n";
    public static final String ERROR_REPORT = "error_report";
    public static final String APP_NAME = "app_name";
    public static final String ERROR = "error";
    public static final String ALERT_MESSAGE = "alert_msg";
    public static final String IS_HTML_MESSAGE = "alert_html_msg";
    public static final String EMAIL_ADD = "email_add";
    public static final String REPORT_TO_URL = "report_to_url";
    public static final String CALLBACK_ACTIVITY = "callback_activity";
    public static final String CALLBACK_BUTTON_TEXT = "callback_btn_text";
    public static final String BACKGROUND_DRAWABLE = "background";
    public static final String SHOW_STACKTRACE = "show_stacktrace";
    public static final String ERROR_REPORT_FILE_PATH = "err_report_file_path";

    private volatile boolean isCrashing = false;
    private Context context;
    private Class<? extends Activity> exceptionActivity;
    private Class<? extends Activity> callbackActivity;
    private String callbackActivityBtnText;
    private int background;
    private String alertMessage;
    private boolean isHTMLMessage;
    private boolean showStackTrace;
    private String emailAdd;
    private String reportToURL;


    public static CrashHandler init(Application application) {
        return new CrashHandler(application);
    }

    public CrashHandler exceptionActivity(Class<? extends Activity> activity) {
        this.exceptionActivity = activity;
        return this;
    }

    public CrashHandler callbackActivity(Class<? extends Activity> activity, String btnText) {
        this.callbackActivity = activity;
        this.callbackActivityBtnText = btnText;
        return this;
    }

    public CrashHandler background(int drawable) {
        this.background = drawable;
        return this;
    }

    public CrashHandler alertMessage(String msg, boolean htmlMSG) {
        this.alertMessage = msg;
        this.isHTMLMessage = htmlMSG;
        return this;
    }

    public CrashHandler showStackTraceReport(boolean show) {
        this.showStackTrace = show;
        return this;
    }

    public CrashHandler emailTo(String email) {
        this.emailAdd = email;
        return this;
    }

    private CrashHandler reportToURL(String url) {
        this.reportToURL = url;
        return this;
    }


    private CrashHandler(Context context) {
        // set default values
        this.context = context;
        this.exceptionActivity = ExceptionActivity.class;
        this.background = -1;
        this.alertMessage = context.getString(R.string.alertMsg);

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

        String errorFile = saveToFile(errorReport.toString());


        Intent errorIntent = new Intent(context, exceptionActivity);
        errorIntent.putExtra(ERROR_REPORT, errorReport.toString());
        errorIntent.putExtra(APP_NAME, getApplicationName(context));
        errorIntent.putExtra(ALERT_MESSAGE, alertMessage);
        errorIntent.putExtra(IS_HTML_MESSAGE, isHTMLMessage);
        errorIntent.putExtra(EMAIL_ADD, emailAdd);
        errorIntent.putExtra(REPORT_TO_URL, reportToURL);
        errorIntent.putExtra(CALLBACK_ACTIVITY, (callbackActivity == null) ? null : callbackActivity.getName());
        errorIntent.putExtra(CALLBACK_BUTTON_TEXT, callbackActivityBtnText);
        errorIntent.putExtra(BACKGROUND_DRAWABLE, background);
        errorIntent.putExtra(SHOW_STACKTRACE, showStackTrace);
        errorIntent.putExtra(ERROR_REPORT_FILE_PATH, (errorFile == null) ? null : errorFile);
        errorIntent.putExtra(ERROR, throwable);

        PendingIntent pendingActivityIntent = PendingIntent.getActivity(context, 30006, errorIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmActivityManager;
        alarmActivityManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmActivityManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 7000, pendingActivityIntent);

        System.exit(0);

        // re-throw critical exception further to the os (important)
        this.uncaughtException(thread, throwable);

    }


    /**
     * Delete outdated logs.
     *
     * @param timeout outdated time
     */
    private void deleteLogs(final long timeout) {
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
        String rootPath = context.getFilesDir().getPath();
        return rootPath + "/CrashHandler/";
    }


    private String saveToFile(String errorLog) {
        String time = FORMATTER.format(new Date());
        String fileName = "Error_" + time + ".log";

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
                return null;
            }
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        writer.write(errorLog);
        writer.close();

        return file.toString();
    }


    /**
     * Delete corresponding path, file or directory.
     *
     * @param file path to delete.
     */
    private void delete(File file) {
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