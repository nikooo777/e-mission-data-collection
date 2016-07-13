package ch.supsi.dti.e_missionconsumes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.Arrays;

/**
 * Created by Niko on 6/30/2016.
 */
public class PhoneSensors implements SensorEventListener {
    private static PhoneSensors instance = null;
    private final Context context;
    //private double acceleration = -1;
    private double speed = -1;
    private double pressure = -1; //hPa
    private double altitude = Double.MIN_VALUE; //NL wouldn't like -1
    private double[] coordinates = new double[2];
    private double[] acceleration = new double[3];
    // Acquire a reference to the system Location Manager
    private final LocationManager locationManager;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private final Sensor senBarometer;


    private PhoneSensors(Context context) {
        this.context = context;

        // Register the listener with the Location Manager to receive location updates
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 114);
        }
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locationListener);

        //accelerometer sensor
        this.senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.senAccelerometer = this.senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (this.senAccelerometer != null) {
            this.senSensorManager.registerListener(this, this.senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        this.senBarometer = this.senSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (this.senBarometer != null) {
            this.senSensorManager.registerListener(this, this.senBarometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public static PhoneSensors init(Context context) {
        if (instance == null) {
            instance = new PhoneSensors(context);
        }
        return instance;
    }


    public static PhoneSensors getInstance() {
        if (instance == null) {
            throw new RuntimeException("Phone sensors manager hasn't been initialized yet");
        }
        return instance;
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            PhoneSensors.this.speed = location.getSpeed() * 3.6; //speed returned in m/s so we need to convert it
            PhoneSensors.this.altitude = location.getAltitude();
            PhoneSensors.this.coordinates[0] = location.getLatitude();
            PhoneSensors.this.coordinates[1] = location.getLongitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            PhoneSensors.this.speed = -1;
        }
    };

    public double[] getAcceleration() {
        return this.acceleration;
    }

    public double getSpeed() {
        return this.speed;
    }

    public double getPressure() {
        return this.pressure;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public double getLatitude() {
        return this.coordinates[0];
    }

    public double getLongitude() {
        return this.coordinates[1];
    }

    public double[] getCurrentAcceleration() {
        return this.acceleration;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            this.acceleration[0] = event.values[0];
            this.acceleration[1] = event.values[1];
            this.acceleration[2] = event.values[2];
            //this.acceleration = Math.sqrt(Math.pow(this.acceleration[0], 2) + Math.pow(this.acceleration[1], 2) + Math.pow(this.acceleration[2], 2));
        }
        else if (mySensor.getType() == Sensor.TYPE_PRESSURE) {
            this.pressure = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public String toString() {
        return "dev-ac=" + Arrays.toString(this.acceleration) + ", dev-speed=" + this.speed + ", dev-press=" + this.pressure + ", dev-alt=" + this.altitude + ", dev-lat=" + getLatitude() + ", dev-long=" + getLongitude();
    }

    public String getAccelerationAsFormattedString() {
        return "x: " + ((double) Math.round(PhoneSensors.getInstance().getAcceleration()[0] * 100) / 100) + " m/ss\n" +
                "y: " + ((double) Math.round(PhoneSensors.getInstance().getAcceleration()[1] * 100)) / 100 + " m/ss\n" +
                "z: " + ((double) Math.round(PhoneSensors.getInstance().getAcceleration()[2] * 100)) / 100 + " m/ss";
    }

    public String getPressureAsFormattedString() {
        return getPressure() + " mBar";
    }

    public String getSpeedAsFormattedString() {
        return getSpeed() + " km/h";
    }

    public String getAltitudeAsFormattedString() {
        return getAltitude() + " msl";
    }

    public String getCoordinatesAsFormattedString() {
        return String.format("%.4f/%.4f", getLatitude(), getLongitude());
    }
}
