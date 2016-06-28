package ch.supsi.dti.e_missionconsumes.obd_commands;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileUtilities {
    private Writer writer;
    private String absolutePath;
    private final Context context;

    public FileUtilities(Context context) {
        super();
        this.context = context;
    }

    public void write(String fileName, String data) {
        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(root.getAbsolutePath() + File.separator + "EMission_tracker");
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory EMission_tracker. Maybe the SD card is mounted?");
            }
            File outputFile = new File(outDir, fileName);
            this.writer = new BufferedWriter(new FileWriter(outputFile, true));

            this.writer.append(data);
            this.writer.close();
        } catch (IOException e) {
            Log.i("error", e.getMessage(), e);
            Toast.makeText(this.context, e.getMessage() + " Unable to write to external storage.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public Writer getWriter() {
        return this.writer;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

}