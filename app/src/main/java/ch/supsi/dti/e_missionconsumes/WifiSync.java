package ch.supsi.dti.e_missionconsumes;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ch.supsi.dti.e_missionconsumes.output.Tools;

/**
 * Created by Niko on 6/27/2016.
 */
public class WifiSync extends BroadcastReceiver {
    private String token = null;
    private static volatile long lastUpdated = 0;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(intent);
        synchronized (WifiSync.class) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            boolean isWifiConnected = false;
            Network[] networks = connectivityManager.getAllNetworks();
            this.token = Tools.shortMd5(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            if (networks == null) {
                isWifiConnected = false;
            }
            else {
                for (Network network : networks) {
                    NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
                    if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (netInfo.isAvailable() && netInfo.isConnected()) {
                            isWifiConnected = true;
                            break;
                        }
                    }
                }
            }
            if (!isWifiConnected) {
                return;
            }
            if (System.currentTimeMillis() - lastUpdated > 30000) // android presents an issue where the state change is fired multiple times, we can filter this behavior
            {
                lastUpdated = System.currentTimeMillis();
            }
            else {
                return;
            }
            //if (info != null && info.isConnected()) {
            File d = new File(Constants.FILE_DEF_DIR);
            String[] list = d.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("_sensor.txt");
                }
            });
            if (list == null) {
                Log.i(this.getClass().getName(), "No files to upload");
                return;
            }
            for (final String f : list) {
                //inner-for
                final String fullPath = d.getPath() + "/" + f;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int responseCode = -1;
                        while (responseCode != 200) {
                            responseCode = uploadFile(fullPath);
                            //rename the file on success
                        }
                        if (responseCode == 200) {
                            File file = new File(fullPath);
                            file.renameTo(new File(fullPath + ".stored"));
                        }
                    }
                }).start();

                Log.i(this.getClass().getName(), "uploading: " + fullPath);
            }
        }
    }

    /*
    * original implementation
    * http://www.coderefer.com/android-upload-file-to-server/
    **/
    public int uploadFile(final String selectedFilePath) {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];
        if (!selectedFile.isFile()) {
            Log.i(this.getClass().getName(), "File to upload is NOT a file");
            Log.e(this.getClass().getName(), selectedFile.getPath());
            return 0;
        }
        else {
            try {
                Log.i(this.getClass().getName(), "File is upload-able");
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                //
                URL url = new URL("http://emission.storni.info/emissionupload.php?token=" + this.token);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(this.getClass().getName(), "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    Log.i(this.getClass().getName(), "Upload completed");
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(this.getClass().getName(), "File Not Found");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i(this.getClass().getName(), "URL error!");

            } catch (IOException e) {
                e.printStackTrace();
                Log.i(this.getClass().getName(), "Cannot Read/Write File!");
            }
            return serverResponseCode;
        }
    }
}