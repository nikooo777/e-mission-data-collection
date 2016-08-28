package ch.supsi.dti.e_missionconsumes.output;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Alan on 18/09/15.
 * Modified by Niko
 */
public class OutputFile {

    public static BufferedWriter output = null;
    public static String currentFile = "";
    public static boolean RECORDING = false;
    public static String FILE_DEF_DIR = "/sdcard/odbres/";

    public static void openFile() {
        currentFile = FILE_DEF_DIR + System.currentTimeMillis() + "_sensor.txt";
        File d = new File(FILE_DEF_DIR);
        if (!d.exists()) {
            d.mkdir();
            Log.i("SensorFile", "Directory " + d.getAbsolutePath() + " created");
        }

        try {
            if (output != null) {
                output.close();
            }
            output = new BufferedWriter(new FileWriter(currentFile));
            RECORDING = true;
            Log.i("OutputFile", "File " + currentFile + " created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void closeFile() {
        RECORDING = false;
        try {
            output.flush();
            output.close();
            output = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void saveData(String value) {
        if (RECORDING) {
            try {
                output.write(value + System.lineSeparator());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
