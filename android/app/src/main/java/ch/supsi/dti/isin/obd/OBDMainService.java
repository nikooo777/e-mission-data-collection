package ch.supsi.dti.isin.obd;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class OBDMainService extends Service {
    public OBDMainService() {
    }



    private final IBinder mBinder = new LocalBinder();



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
