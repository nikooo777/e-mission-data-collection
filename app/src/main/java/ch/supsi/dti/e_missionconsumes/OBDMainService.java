package ch.supsi.dti.e_missionconsumes;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import ch.supsi.dti.e_missionconsumes.carconnection.CarManager;
import ch.supsi.dti.e_missionconsumes.carconnection.ConnectionException;

public class OBDMainService extends Service {
    private CarManager carManager = null;

    public OBDMainService() {
        Log.d("SHIT", "nothing works");
    }

    public CarManager getCarManager() {
        return this.carManager;
    }

    public void onCreate() {
        super.onCreate();
        if (this.carManager == null) {
            this.carManager = new CarManager();
        }
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

    public class LocalBinder extends Binder {
        public OBDMainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OBDMainService.this;
        }
    }
}
