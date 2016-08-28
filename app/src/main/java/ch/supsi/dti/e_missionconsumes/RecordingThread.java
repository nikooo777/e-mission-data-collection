package ch.supsi.dti.e_missionconsumes;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
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
    private static HashMap<String, String> currentValues = null;
    public OBDMainService service;
    public static boolean RUN = false;
    public static RecordingThread thread;
    private boolean tripInfoStored = false;
    private static int RECORD_INTERVAL = 200;

    public RecordingThread(OBDMainService service) {
        this.service = service;
    }

    public static synchronized HashMap<String, String> getCurrentValues() {
        return currentValues;
    }

    private synchronized void storeCurrentValues(HashMap<String, String> currentValues) {
        RecordingThread.currentValues = currentValues;
    }

    public static void startRecording(OBDMainService service) {
        OutputFile.openFile();
        thread = new RecordingThread(service);
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
                Log.i("ODBRECLOC", "Car is not connected");
            }
            else {
                try {
                    JSONObject jo = new JSONObject();
                    JSONArray jaa = new JSONArray();
                    JSONArray jac = new JSONArray();
                    HashMap<String, String> readParameters = this.service.getCarManager().queryForParameters();
                    //StringBuilder res = new StringBuilder();

                    if (!this.tripInfoStored) {
                        this.tripInfoStored = true;
                        JSONObject tripInfoData = new JSONObject();
                        tripInfoData.put("VIN", this.service.getCarManager().getVIN());
                        tripInfoData.put("timestamp", System.currentTimeMillis());
                        tripInfoData.put("fuelType", this.service.getCarManager().getFuelType().name());
                        jo.put("trip-info", tripInfoData);
                        OutputFile.saveData(jo.toString().replace("\\/", "/"));
                        //empty the json object in order to free up space for the
                        jo.remove("trip-info");
                    }
                    jo.put("timestamp", System.currentTimeMillis());
                    for (String key : readParameters.keySet()) {
                        jo.put(key, readParameters.get(key));
                    }

                    //add phone parameters to the map
                    readParameters.put("dev-ac", PhoneSensors.getInstance().getAccelerationAsFormattedString());
                    readParameters.put("dev-speed", PhoneSensors.getInstance().getSpeedAsFormattedString());
                    readParameters.put("dev-press", PhoneSensors.getInstance().getPressureAsFormattedString());
                    readParameters.put("dev-alt", PhoneSensors.getInstance().getAltitudeAsFormattedString());
                    readParameters.put("dev-coords", PhoneSensors.getInstance().getCoordinatesAsFormattedString());

                    for (double a : PhoneSensors.getInstance().getAcceleration()) {
                        jaa.put(a);
                    }

                    jo.put("dev-ac", jaa);
                    jo.put("dev-speed", PhoneSensors.getInstance().getSpeed());
                    jo.put("dev-press", PhoneSensors.getInstance().getPressure());
                    jo.put("dev-alt", PhoneSensors.getInstance().getAltitude());
                    jac.put(PhoneSensors.getInstance().getLatitude());
                    jac.put(PhoneSensors.getInstance().getLongitude());
                    jo.put("dev-coords", jac);

                    //store all parameters
                    storeCurrentValues(readParameters);

                    //write the data to permanent storage
                    OutputFile.saveData(jo.toString().replace("\\/", "/"));
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
                Thread.sleep(RECORD_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(this.getClass().getName(), "Stopped Recording");
        this.tripInfoStored = false;
    }
}