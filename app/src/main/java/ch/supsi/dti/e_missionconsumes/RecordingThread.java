package ch.supsi.dti.e_missionconsumes;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ch.supsi.dti.e_missionconsumes.carconnection.ConnectionException;
import ch.supsi.dti.e_missionconsumes.output.OutputFile;

/**
 * Created by Alan on 17/09/15.
 * Modified by Niko
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
        RUN = false;
        OutputFile.closeFile();
    }

    @Override
    public void run() {
        RUN = true;
        Log.i(this.getClass().getName(), "Start Recording");
        while (RUN) {
            if (!this.service.getCarManager().isConnected()) {
                // Toast.makeText(service.getApplicationContext(), "Car is not connected", Toast.LENGTH_LONG).show();
                Log.i("ODBRECLOC", "Car is not connected");
            }
            else {
                try {
                    JSONObject jo = new JSONObject();
                    HashMap<String, String> carInfo = this.service.getCarManager().queryForParameters();
                    //StringBuilder res = new StringBuilder();

                    for (String key : carInfo.keySet()) {
                        jo.put(key, carInfo.get(key));
                        //res.append(key + "=" + carInfo.get(key) + ", ");
                    }
                    jo.put("dev-ac", PhoneSensors.getInstance().getAcceleration());
                    jo.put("dev-speed", PhoneSensors.getInstance().getSpeed());
                    jo.put("dev-press", PhoneSensors.getInstance().getPressure());
                    jo.put("dev-alt", PhoneSensors.getInstance().getAltitude());
                    jo.put("dev-lat", PhoneSensors.getInstance().getLatitude());
                    jo.put("dev-long", PhoneSensors.getInstance().getLongitude());

                    //res.append(PhoneSensors.getInstance().toString() + ", ");
                    storeCurrentValues(carInfo);
                    OutputFile.saveData(jo.toString());
                    Log.i(this.getClass().getName(), "Storing car data: " + jo.toString());
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    Toast.makeText(this.service.getApplicationContext(), "ConnectionException: Car is not connected", Toast.LENGTH_LONG).show();
                    Log.i(this.getClass().getName(), "ConnectionException: Car is not connected");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(Constants.RECORD_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(this.getClass().getName(), "Stop Recording");
    }
}