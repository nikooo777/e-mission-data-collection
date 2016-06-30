package ch.supsi.dti.e_missionconsumes;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.HashMap;

import ch.supsi.dti.e_missionconsumes.carconnection.CarManager;
import ch.supsi.dti.e_missionconsumes.carconnection.ConnectionException;

public class OBDMainService extends Service {
    private CarManager carManager = null;

    public OBDMainService() {
    }

    public CarManager getCarManager() {
        return this.carManager;
    }

    public void onCreate() {
        super.onCreate();
        if (this.carManager == null) {
            this.carManager = new CarManager();
        }

        // this.senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // this.senAccelerometer = this.senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // this.senSensorManager.registerListener(this, this.senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final IBinder mBinder = new LocalBinder();

    public void connectToAdapter(String dev) throws ConnectionException {
        this.carManager.connectToAdapter(dev);
        Toast.makeText(getApplicationContext(), "Connected to:" + dev, Toast.LENGTH_SHORT).show();
    }

    public void startOBDRecording() {
        // carManager.connectToAdapter(dev);
        RecordingThread.startRecording(this);
        Toast.makeText(getApplicationContext(), "Start recording", Toast.LENGTH_SHORT).show();
    }

    public void stopOBDRecording() {
        RecordingThread.stopRecording();
        this.carManager.disconnectFromCar();
        Toast.makeText(getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();
    }

    public HashMap<String, String> currentValues() throws NoValueException {
        HashMap<String, String> ct = RecordingThread.getCurrent();
        if (ct == null) {
            throw new NoValueException();
        }
        return ct;
    }

    public boolean isRecording() {
        return RecordingThread.RUN;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Service Connected");
        return this.mBinder;
    }

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float lastUpdate = -1f;
    private double currentAcceleration = -1;

    /*@Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float curTime = System.currentTimeMillis();
            //  if (this.lastUpdate + DELAY < curTime) {
            float[] acceleration = new float[3];
            acceleration[0] = event.values[0];
            acceleration[1] = event.values[1];
            acceleration[2] = event.values[2];
            this.currentAcceleration = Math.sqrt(Math.pow(acceleration[0], 2) + Math.pow(acceleration[1], 2) + Math.pow(acceleration[2], 2));
            this.lastUpdate = curTime;
        }
    }*/

    //public double getCurrentAcceleration() {
    //   return this.currentAcceleration;
    //}

    // @Override
    //public void onAccuracyChanged(Sensor sensor, int accuracy) {
    //    }

    public class LocalBinder extends Binder {
        public OBDMainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OBDMainService.this;
        }
    }
}
