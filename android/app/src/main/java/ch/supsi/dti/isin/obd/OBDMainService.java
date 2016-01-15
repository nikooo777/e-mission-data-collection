package ch.supsi.dti.isin.obd;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.HashMap;

import ch.supsi.dti.isin.obd.carconnection.CarManager;
import ch.supsi.dti.isin.obd.carconnection.ConnectionException;

public class OBDMainService extends Service {

    private CarManager carManager = null;


    public OBDMainService() {
    }


    public CarManager getCarManager(){
        return carManager;
    }

    public void onCreate() {
        super.onCreate();
        if(carManager == null) carManager = new CarManager();
    }







    private final IBinder mBinder = new LocalBinder();




    public void startOBDRecording(String dev) throws ConnectionException {
        // carManager.connectToAdapter(dev);

        carManager.connectToAdapter(dev);

        Toast.makeText(getApplicationContext(), "Connected to:" + dev, Toast.LENGTH_SHORT).show();
        RecordingThread.startRecording(this);
        Toast.makeText(getApplicationContext(), "Start recording", Toast.LENGTH_SHORT).show();

    }


    public void stopOBDRecording(){
       /* RecordingThread.stopRecording();
        SensorFile.closeFile(); */
        carManager.disconnectToCar();
        Toast.makeText(getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();

    }




    public HashMap<String, String> currentValues() throws NoValueException {
        HashMap<String, String> ct = RecordingThread.getCurrent();
        if(ct == null) throw new NoValueException();
        return ct;
    }

    public boolean isRecording(){
        return RecordingThread.RUN;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Service Connected");
        return mBinder;
    }

    public class LocalBinder extends Binder {

        public OBDMainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OBDMainService.this;
        }
    }

}
