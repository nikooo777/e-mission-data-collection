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
    public OBDMainService service;
    public static boolean RUN = false;
    public static RecordingThread thread;

    public RecordingThread(OBDMainService _service) {
        this.service = _service;
    }

    //current
    public static synchronized HashMap<String, String> getCurrent() {
        return current;
    }

    private synchronized void storeCurrentValues(HashMap<String, String> _current) {
        current = _current;
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
            if (!this.service.getCarManager().isConnected()) {
                // Toast.makeText(service.getApplicationContext(), "Car is not connected", Toast.LENGTH_LONG).show();
                Log.d("ODBRECLOC", "Car is not connected");
            }
            else {
                try {
                    HashMap<String, String> carInfo = this.service.getCarManager().queryForParameters();
                    //String res = "";
                    StringBuilder res = new StringBuilder();
                    for (String key : carInfo.keySet()) {
                        //res += key + "=" + carInfo.get(key) + ", ";
                        res.append(key+"="+carInfo.get(key)+", ");
                    }
                    res.append("ACC="+service.getCurrentAcceleration()+", ");
                    storeCurrentValues(carInfo);
                    OutputFile.saveData(res.toString());
                    Log.d(this.getClass().getName(), "Storing car data: " + res);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    Toast.makeText(this.service.getApplicationContext(), "ConnectionException: Car is not connected", Toast.LENGTH_LONG).show();
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
