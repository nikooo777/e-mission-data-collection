package ch.supsi.dti.e_missionconsumes;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int DELAY = 100; //minimum of 100ms between each polling
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float lastUpdate = -1;
    private TextView accelerationLabel;
    private static String ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(this.getClass().getName(), "OBD Start");
        Intent intent = new Intent(this, OBDActivity.class);
        startActivity(intent);
    }
}
