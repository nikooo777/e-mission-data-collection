package ch.supsi.dti.isin.obd;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ch.supsi.dti.isin.obd.carconnection.CarManager;
import ch.supsi.dti.isin.obd.carconnection.ConnectionException;
import edu.berkeley.eecs.cfc_tracker.R;
import edu.berkeley.eecs.cfc_tracker.log.Log;


/**
 * TODO: fix fine/coars mode input
 */
public class OBDActivity extends Activity {
    public static int REQUEST_ENABLE_BT = 1;
    public static String DEV_NAME = "";

    public  String fuelType = "Gasoline";

    public UpdateParametersThread updateThread = null;
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
                    serviceConnected();

            }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    public void serviceConnected(){
        final Button b = (Button)findViewById(R.id.button);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mService.isRecording()) {
                    selectBTDevice();
                    try {
                        if(DEV_NAME.length() == 0) return;

                        mService.connectToAdapter(DEV_NAME);
                        //check fuel type
                        String fuelType = mService.getCarManager().getFuelType();

                        if(fuelType.length() == 0){
                            fuelType = fuelDialogType(OBDActivity.this);
                            mService.getCarManager().setFuelType(fuelType);
                        }

                        TextView ftv = (TextView) findViewById(R.id.textViewFuelType);
                        ftv.setTextColor(Color.GREEN);
                        if(fuelType.compareTo("Gasoline") == 0) ftv.setTextColor(Color.YELLOW);
                        ftv.setText(fuelType);


                        mService.startOBDRecording();


                        updateThread = new UpdateParametersThread();
                        new Thread(updateThread).start();
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               b.setText("Disconnect");
                           }
                       });




                    } catch (ConnectionException e) {
                        e.printStackTrace();
                        Toast.makeText(OBDActivity.this, "Unable to start recording", Toast.LENGTH_LONG).show();

                    }
                }else{

                    mService.stopOBDRecording();
                    updateThread.RUN = false;
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









    public void selectBTDevice(){
        DEV_NAME = "";
        ArrayList deviceStrs = new ArrayList();
        final ArrayList<String> devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        // checking and enabling bluetooth
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
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
            ArrayAdapter adapter = new ArrayAdapter(OBDActivity.this, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));
            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String deviceAddress = devices.get(position);

                    DEV_NAME = deviceAddress;
                    Log.d(OBDActivity.this, this.getClass().getName(), "BT="+deviceAddress);



                }
            });

            alertDialog.setTitle("Choose BT");
            alertDialog.show();
        }
    }




    class UpdateParametersThread implements Runnable{
        public boolean RUN = false;

        @Override
        public void run() {
            RUN = true;

            while(RUN) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

                    updateActParameters();
            }
        }


    }



    public void updateActParameters() {
        try {
            HashMap<String, String> pm = mService.currentValues();

            TextView rpm = (TextView)findViewById(R.id.textViewRpm);
            rpm.setText(pm.get(CarManager.RPM));

            TextView speed = (TextView)findViewById(R.id.textViewSpeed);
            rpm.setText(pm.get(CarManager.SPEED));

            TextView fuelFlow = (TextView)findViewById(R.id.textViewFlow);
            rpm.setText(pm.get(CarManager.FUEL));


            TextView odometer = (TextView)findViewById(R.id.textViewODO);
            rpm.setText(pm.get(CarManager.ODOMETER));


        } catch (NoValueException e) {
            e.printStackTrace();
        }

    }




    public synchronized String fuelDialogType(final Context c) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder adb = new AlertDialog.Builder(c);
                CharSequence items[] = new CharSequence[]{"Gasoline", "Diesel"};
                adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int n) {
                        if (n != 0) {
                            fuelType = "Diesel";
                        }

                    }

                }).setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int sel = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                if (sel != 0) { fuelType = "Diesel"; }
                            }
                        });

                adb.setTitle(R.string.BeginQuestion);
                adb.show();
            }
        });


        return fuelType;

    }
}





