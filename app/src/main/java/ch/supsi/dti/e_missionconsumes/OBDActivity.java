package ch.supsi.dti.e_missionconsumes;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ch.supsi.dti.e_missionconsumes.carconnection.CarInfo;
import ch.supsi.dti.e_missionconsumes.carconnection.CarManager;
import ch.supsi.dti.e_missionconsumes.carconnection.ConnectionException;
import ch.supsi.dti.e_missionconsumes.output.Tools;

/**
 * TODO: fix fine/coarse mode input
 */
public class OBDActivity extends Activity {
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 112;
    //private static final int REQUEST_ACCESS_FINE_LOCATION = 113;
    public static int REQUEST_ENABLE_BT = 1;
    public static String DEV_NAME = "";
    public FuelType fuelType = FuelType.GAS;
    public UpdateParametersThread updateThread = null;
    static OBDMainService mService;
    static boolean mBound = false;
    private TextView accelerationLabel;
    private TextView pressureLabel;
    private TextView gpsSpeedLabel;
    private TextView gpsAltitudeLabel;
    private TextView coordinatesLabel;
    private EditText tokenText;

    public static String token = null;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(this.getClass().getName(), "OBDActivity started");
        token = Tools.shortMd5(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
        this.tokenText = (EditText) findViewById(R.id.textToken);
        this.tokenText.setText(token, TextView.BufferType.EDITABLE);
        this.tokenText.setTextIsSelectable(true);
        this.tokenText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(token, token);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(), "Token copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
        if (!mBound) {
            Intent intent = new Intent(this, OBDMainService.class);
            bindService(intent, this.mConnection, Context.BIND_AUTO_CREATE);
        }
        this.accelerationLabel = (TextView) findViewById(R.id.textViewAcceleration);
        this.pressureLabel = (TextView) findViewById(R.id.textViewPressure);
        this.gpsSpeedLabel = (TextView) findViewById(R.id.textViewGPSSpeed);
        this.gpsAltitudeLabel = (TextView) findViewById(R.id.textViewAltitude);
        this.coordinatesLabel = (TextView) findViewById(R.id.textViewCoordinates);
        boolean hasPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET}, REQUEST_MULTIPLE_PERMISSIONS);
        }
        else {
            PhoneSensors.init(this);
        }
        CarInfo.init(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MULTIPLE_PERMISSIONS) {
            for (int result : grantResults) {

                //abort this action if we don't have permission to proceed
                if (PackageManager.PERMISSION_DENIED == result) {
                    return;
                }
            }
            PhoneSensors.init(this);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            OBDMainService.LocalBinder binder = (OBDMainService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            serviceConnected();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void postBTselection() {
        try {
            if (DEV_NAME.length() == 0) {
                return;
            }
            Log.i(this.getClass().getName(), DEV_NAME);
            mService.connectToAdapter(DEV_NAME);
            //check fuel type
            FuelType fuelType = mService.getCarManager().getFuelType();

            //check supported operations
            updateSupportedSensors();

            //let the user select his car type


            if (fuelType == null) {
                fuelDialogType(OBDActivity.this);
            }
            else {
                TextView ftv = (TextView) findViewById(R.id.textViewFuelType);
                ftv.setTextColor(Color.GREEN);
                if (fuelType == FuelType.GAS) {
                    ftv.setTextColor(Color.YELLOW);
                }
                ftv.setText(fuelType.name());
                mService.startOBDRecording();
                this.updateThread = new UpdateParametersThread();
                new Thread(this.updateThread).start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button b = (Button) findViewById(R.id.button);
                        b.setText("Disconnect");
                    }
                });
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            Toast.makeText(OBDActivity.this, "Unable to start recording", Toast.LENGTH_LONG).show();
        }
    }

    public void serviceConnected() {
        final Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mService.isRecording()) {
                    selectBTDevice();
                }
                else {
                    mService.stopOBDRecording();
                    ch.supsi.dti.e_missionconsumes.OBDActivity.this.updateThread.RUN = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            b.setText("Connect");
                        }
                    });
                }
            }
        });
    }

    public void selectBTDevice() {
        DEV_NAME = "";
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        // checking and enabling bluetooth
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
            }
            // show a list of paired devices to connect with
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OBDActivity.this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(OBDActivity.this, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));
            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String deviceAddress = devices.get(position);
                    DEV_NAME = deviceAddress;
                    Log.i(this.getClass().getName(), "BT=" + deviceAddress);
                    postBTselection();
                }
            });
            alertDialog.setTitle("Choose BT");
            alertDialog.show();
        }
    }


    class UpdateParametersThread implements Runnable {
        public boolean RUN = false;

        @Override
        public void run() {
            this.RUN = true;
            while (this.RUN) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateActParameters();
                    }
                });
            }
        }
    }

    public void updateActParameters() {
        try {
            HashMap<String, String> pm = mService.currentValues();
            TextView rpm = (TextView) findViewById(R.id.textViewRpm);
            TextView speed = (TextView) findViewById(R.id.textViewSpeed);
            TextView fuelFlow = (TextView) findViewById(R.id.textViewFlow);
            TextView fuelEconomy = (TextView) findViewById(R.id.textViewFuelEco);
            TextView odometer = (TextView) findViewById(R.id.textViewODO);
            TextView consumedFuel = (TextView) findViewById(R.id.textViewConsumed);
            rpm.setText(pm.get(CarManager.RPM));
            speed.setText(pm.get(CarManager.SPEED));
            fuelEconomy.setText(pm.get(CarManager.ECONOMY));
            fuelFlow.setText(pm.get(CarManager.FUEL));
            consumedFuel.setText(pm.get(CarManager.FUELCONSUMED));

            odometer.setText(pm.get(CarManager.ODOMETER));
            this.accelerationLabel.setText(PhoneSensors.getInstance().getAccelerationAsFormattedString());
            this.pressureLabel.setText(PhoneSensors.getInstance().getPressureAsFormattedString());
            this.gpsSpeedLabel.setText(PhoneSensors.getInstance().getSpeedAsFormattedString());
            this.gpsAltitudeLabel.setText(PhoneSensors.getInstance().getAltitudeAsFormattedString());
            this.coordinatesLabel.setText(PhoneSensors.getInstance().getCoordinatesAsFormattedString());

        } catch (NoValueException e) {
            e.printStackTrace();
        }
    }

    public void updateSupportedSensors() {
        boolean maf = mService.getCarManager().hasMAF();
        boolean map = mService.getCarManager().hasMAP();
        boolean flvl = mService.getCarManager().hasFuelLvl();
        boolean fr = mService.getCarManager().hasFuelRate();

        CheckBox mafcb = (CheckBox) findViewById(R.id.MAFCheckBox);
        CheckBox mapcb = (CheckBox) findViewById(R.id.MAPCheckBox);
        CheckBox flvlcb = (CheckBox) findViewById(R.id.FuelLevelCheckBox);
        CheckBox frcb = (CheckBox) findViewById(R.id.FuelRateSupport);

        mafcb.setChecked(maf);
        mapcb.setChecked(map);
        flvlcb.setChecked(flvl);
        frcb.setChecked(fr);
    }

    public synchronized FuelType fuelDialogType(final Context c) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder adb = new AlertDialog.Builder(c);
                CharSequence items[] = new CharSequence[]{"Gasoline", "Diesel"};
                adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int n) {
                        if (n != 0) {
                            ch.supsi.dti.e_missionconsumes.OBDActivity.this.fuelType = FuelType.DIESEL;//"Diesel";
                        }
                    }
                }).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if selected item was no GAS....
                        if (((AlertDialog) dialog).getListView().getCheckedItemPosition() != 0) {
                            ch.supsi.dti.e_missionconsumes.OBDActivity.this.fuelType = FuelType.DIESEL;
                        }

                        //update the fuel type in the car manager as it was previously null
                        mService.getCarManager().setFuelType(ch.supsi.dti.e_missionconsumes.OBDActivity.this.fuelType);
                        TextView fuelTypeTextView = (TextView) findViewById(R.id.textViewFuelType);
                        fuelTypeTextView.setTextColor(Color.GREEN);
                        if (ch.supsi.dti.e_missionconsumes.OBDActivity.this.fuelType == FuelType.GAS) {
                            fuelTypeTextView.setTextColor(Color.YELLOW);
                        }
                        fuelTypeTextView.setText(OBDActivity.this.fuelType.name());
                        mService.startOBDRecording();
                        ch.supsi.dti.e_missionconsumes.OBDActivity.this.updateThread = new UpdateParametersThread();
                        new Thread(ch.supsi.dti.e_missionconsumes.OBDActivity.this.updateThread).start();

                        Button b = (Button) findViewById(R.id.button);
                        b.setText("Disconnect");
                    }
                });
                adb.setTitle(R.string.BeginQuestion);
                adb.show();
            }
        });
        return this.fuelType;
    }
}
