package com.tyagiabhinav.crashhandler;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by abhinavtyagi on 14/10/16.
 */

public class UploadReportIntentService extends IntentService {

    private static final String TAG = "CrashHandler." + UploadReportIntentService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadReportIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        uploadFile(intent.getStringExtra(CrashHandler.ERROR_REPORT_FILE_PATH), intent.getStringExtra(CrashHandler.REPORT_TO_URL));
    }

    private int uploadFile(final String selectedFilePath, String serverURL) {
        Log.d(TAG, "uploadFile.... File->" + selectedFilePath + "   to   Server->" + serverURL);
        int serverResponseCode = 0;

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];
        Log.d(TAG, fileName);
        if (!selectedFile.isFile()) {
            // TODO no file exists
            Log.i(TAG, selectedFile + " not exists!");
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(serverURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("filename", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    Log.i(TAG, "while..");
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                conn.connect();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage().toString();

                Log.d(TAG, "HTTP Response : " + serverResponseMessage + ": " + serverResponseCode);

//                DataInputStream inStream;
                BufferedReader bufferReader;

                String str = "";
                String response = "";
                try {
//                    inStream = new DataInputStream();
                    bufferReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((str = bufferReader.readLine()) != null) {
                        Log.d(TAG, "Server Response -> " + str);
                        response = str;
                    }
//                    inStream.close();
                    bufferReader.close();
                } catch (IOException ioex) {
                    Log.e(TAG, "Error: " + ioex.getMessage(), ioex);
                }
                conn.disconnect();
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                if (serverResponseCode == 201) {
                    Log.e(TAG, "*** SERVER RESPONSE: 201" + response);
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e(TAG, "UL error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception : " + e.getMessage());
            }

            return serverResponseCode;
        }
    }


}
