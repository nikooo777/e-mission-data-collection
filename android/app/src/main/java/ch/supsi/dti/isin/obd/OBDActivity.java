package ch.supsi.dti.isin.obd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;

import edu.berkeley.eecs.cfc_tracker.R;
import edu.berkeley.eecs.cfc_tracker.log.Log;

public class OBDActivity extends Activity {


    static OBDMainService mService;
    static boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odb);
        Log.d(this, this.getClass().getName(), "OBDActivity started");


        if(mBound == false){
            Intent intent = new Intent(this, OBDMainService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }




    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            OBDMainService.LocalBinder binder = (OBDMainService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

           // serviceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
