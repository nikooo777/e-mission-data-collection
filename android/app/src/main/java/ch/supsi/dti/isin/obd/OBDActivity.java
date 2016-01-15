package ch.supsi.dti.isin.obd;

import android.os.Bundle;
import android.app.Activity;

import edu.berkeley.eecs.cfc_tracker.R;
import edu.berkeley.eecs.cfc_tracker.log.Log;

public class OBDActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odb);
        Log.d(this, this.getClass().getName(), "OBDActivity started");
    }

}
