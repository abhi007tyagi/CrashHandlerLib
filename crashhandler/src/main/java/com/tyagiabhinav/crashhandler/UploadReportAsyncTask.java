package com.tyagiabhinav.crashhandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by abhinavtyagi on 15/10/16.
 */

public class UploadReportAsyncTask extends AsyncTask<Void, Integer, String> {

    private static final String TAG = UploadReportAsyncTask.class.getSimpleName();

    private Context context;
    private String errorFile;
    private String serverURL;

    UploadReportAsyncTask(Context ctx, String file, String url){
        this.context = ctx;
        this.errorFile = file;
        this.serverURL = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        Log.d(TAG, "doInBackground..... starting upload service...");
//        try {
//            MultipartUtil multipart = new MultipartUtil(serverURL, "UTF-8");
//            multipart.addFilePart("file", new File(errorFile));
//
//            List<String> response = multipart.finish();
//            Log.d(TAG, "SERVER REPLIED:");
//            for (String line : response) {
//                Log.d(TAG, "Upload Files Response:::" + line);
//                // get your server response here.
//                Log.d(TAG, line);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        uploadFile(errorFile, serverURL);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.d(TAG, "Progress... "+values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


    private int uploadFile(final String selectedFilePath, String serverURL) {
        Log.d(TAG, "uploadFile.... File->"+selectedFilePath+"   to   Server->"+serverURL);
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
            Log.i(TAG, selectedFile+" not exists");
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(serverURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy            s
                conn.setRequestMethod("POST");
                 conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file", fileName);
                conn.setRequestProperty("connection", "close");
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
                    Log.i(TAG,"while..");
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                conn.connect();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage().toString();

                Log.i(TAG, "HTTP Response is : "  + serverResponseMessage + ": " + serverResponseCode);

                DataInputStream inStream;
                String str="";
                String response="";
                try {
                    inStream = new DataInputStream(conn.getInputStream());

                    while ((str = inStream.readLine()) != null) {
                        Log.e(TAG, "SOF Server Response" + str);
                        response=str;
                    }
                    inStream.close();
                }
                catch (IOException ioex) {
                    Log.e(TAG, "SOF error: " + ioex.getMessage(), ioex);
                }
                conn.disconnect();
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                if(serverResponseCode == 201){
                    Log.e(TAG,"*** SERVER RESPONSE: 201"+response);
                }
            }
            catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e(TAG, "UL error: " + ex.getMessage(), ex);
            }

            catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception : "+ e.getMessage());
            }

            return serverResponseCode;
        }
    }

}
