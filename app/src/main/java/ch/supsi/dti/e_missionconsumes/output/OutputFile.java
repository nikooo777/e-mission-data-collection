package ch.supsi.dti.e_missionconsumes.output;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.supsi.dti.e_missionconsumes.Constants;


/**
 * Created by Alan on 18/09/15.
 */
public class OutputFile {

    public static BufferedWriter output = null;
    public static String currentFile = "";
    public static boolean RECORDING = false;

    public static void openFile() {
        currentFile = Constants.FILE_DEF_DIR + System.currentTimeMillis() + "_sensor.txt";
        File d = new File(Constants.FILE_DEF_DIR);
        if (!d.exists()) {
            d.mkdir();
            Log.d("SensorFile", "Directory " + d.getAbsolutePath() + " created");
        }

        try {
            if (output != null) output.close();
            output = new BufferedWriter(new FileWriter(currentFile));
            RECORDING = true;
            Log.d("OutputFile", "File " + currentFile + " created");
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


    public static void saveData(String value) {

        if (RECORDING) {
            try {
                output.write(System.currentTimeMillis() + "\t" + value + "\n");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
