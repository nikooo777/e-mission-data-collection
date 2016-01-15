package ch.supsi.dti.isin.obd;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import edu.berkeley.eecs.cfc_tracker.R;
import edu.berkeley.eecs.cfc_tracker.log.Log;

public class OBDActivity extends Activity {
    public static int REQUEST_ENABLE_BT = 1;

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



    public void serviceConnected(){
        final Button b = (Button)findViewById(R.id.button);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectBTDevice();
            }
        });
    }









    public void selectBTDevice(){
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
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));
            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String deviceAddress = devices.get(position);

                    Log.d(OBDActivity.this, this.getClass().getName(), "BT="+deviceAddress);

                }
            });

            alertDialog.setTitle("Choose BT");
            alertDialog.show();
        }
    }





}





