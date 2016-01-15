package ch.supsi.dti.isin.obd;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OBDMainService extends Service {
    public OBDMainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
