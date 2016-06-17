package ch.supsi.dti.e_missionconsumes;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int DELAY = 100; //minimum of 100ms between each polling
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float lastUpdate = -1;
    private TextView accelerationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(this.getClass().getName(), "OBD Start");
        Intent intent = new Intent(this, OBDActivity.class);
        startActivity(intent);
        accelerationLabel = (TextView) findViewById(R.id.textViewAcceleration);
        this.senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.senAccelerometer = this.senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.senSensorManager.registerListener(this, this.senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float curTime = System.currentTimeMillis();
            //  if (this.lastUpdate + DELAY < curTime) {
            float[] acceleration = new float[3];
            acceleration[0] = event.values[0];
            acceleration[1] = event.values[1];
            acceleration[2] = event.values[2];

            final String ac = Math.sqrt(Math.pow(acceleration[0], 2) + Math.pow(acceleration[1], 2) + Math.pow(acceleration[2], 2)) + " m/ss";
            Toast.makeText(getApplicationContext(), "ac: "+ ac, Toast.LENGTH_LONG).show();
            accelerationLabel.setText(ac);  //it doesn't work for some reasons...
            this.lastUpdate = curTime;
            //    }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
