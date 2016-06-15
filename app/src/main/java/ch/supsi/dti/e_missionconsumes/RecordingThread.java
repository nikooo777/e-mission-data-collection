package ch.supsi.dti.e_missionconsumes;

import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import ch.supsi.dti.e_missionconsumes.carconnection.ConnectionException;
import ch.supsi.dti.e_missionconsumes.output.OutputFile;

/**
 * Created by Alan on 17/09/15.
 */
public class RecordingThread implements Runnable {
    private static HashMap<String, String> current = null;
    public OBDMainService serv;
    public static boolean RUN = false;
    public static RecordingThread thread;

    public RecordingThread(OBDMainService _serv) {
        this.serv = _serv;
    }

    //current
    public static synchronized HashMap<String, String> getCurrent() {
        return current;
    }

    private synchronized void storeCurrentValues(HashMap<String, String> _currenet) {
        current = _currenet;
    }

    public static void startRecording(OBDMainService _serv) {
        OutputFile.openFile();
        thread = new RecordingThread(_serv);
        new Thread(thread).start();
    }

    public static void stopRecording() {
        thread.RUN = false;
        OutputFile.closeFile();
    }

    @Override
    public void run() {
        RUN = true;
        Log.d(this.getClass().getName(), "Start Recording");
        while (RUN) {
            if (!this.serv.getCarManager().isConnected()) {
                // Toast.makeText(serv.getApplicationContext(), "Car is not connected", Toast.LENGTH_LONG).show();
                Log.d("ODBRECLOC", "Car is not connected");
            }
            else {
                try {
                    HashMap<String, String> carInfo = this.serv.getCarManager().queryForParameters();
                    String res = "";
                    for (String key : carInfo.keySet()) {
                        res += key + "=" + carInfo.get(key) + ", ";
                    }
                    storeCurrentValues(carInfo);
                    OutputFile.saveData(res);
                    Log.d(this.getClass().getName(), "Storing car data: " + res);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    Toast.makeText(this.serv.getApplicationContext(), "ConnectionException: Car is not connected", Toast.LENGTH_LONG).show();
                    Log.d(this.getClass().getName(), "ConnectionException: Car is not connected");
                }
            }
            try {
                Thread.sleep(Constants.RECORD_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(this.getClass().getName(), "Stop Recording");
    }
}
