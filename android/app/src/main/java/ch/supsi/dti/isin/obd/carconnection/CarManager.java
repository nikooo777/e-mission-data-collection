package ch.supsi.dti.isin.obd.carconnection;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;


import ch.supsi.dti.isin.obd.obd_commands.CheckObdCommands;
import ch.supsi.dti.isin.obd.obd_commands.FuelEconomyObdCommand;
import obd.commands.SpeedCommand;
import obd.commands.engine.RPMCommand;
import obd.commands.engine.ThrottlePositionCommand;
import obd.commands.fuel.FuelLevelCommand;
import obd.commands.protocol.LineFeedOffCommand;
import obd.commands.protocol.SelectProtocolCommand;
import obd.commands.protocol.TimeoutCommand;
import obd.enums.ObdProtocols;

/**
 * Created by Alan on 18/09/15.
 */
public class CarManager {


    /*HASH KEYS */


    public static String FUEL_TYPE = "FT";
    private static String RPM = "RPM";
    private static String SPEED = "SP";
    private static String FUEL = "FUEL";
    private static String TANK = "TK";

    /*EOK*/


    private String devAddress;
    private BluetoothSocket sock;
    private String fuelType;
    private String commands;
    private boolean connected = false;

    private FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();
    private RPMCommand engineRpmCommand = new RPMCommand();
    private SpeedCommand speedCommand = new SpeedCommand();
    private FuelEconomyObdCommand fuelEconomy;
    private ThrottlePositionCommand throttlePositionObdCommand;


    public boolean isConnected(){
        return connected;
    }


     public void connectToAdapter(String _devAddress) throws ConnectionException {
           devAddress = _devAddress;

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = btAdapter.getRemoteDevice(devAddress);

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // creation and connection of a bluetooth socket
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
                 sock = socket;


                new obd.commands.protocol.EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
              new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                new TimeoutCommand(200).run(sock.getInputStream(), sock.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());


                CheckObdCommands check = new CheckObdCommands();
                check.run(sock.getInputStream(), sock.getOutputStream());
                String checked = check.toString();
                commands = checked;
                Log.d(this.getClass().getName(), "commands supported: " + checked);
                //determine fuel type
                //retrieveFuelType();
                /*FindFuelTypeCommand fuelTypeObdCommand = new FindFuelTypeCommand();
                fuelTypeObdCommand.run(sock.getInputStream(), sock.getOutputStream());
                String type = fuelTypeObdCommand.getFormattedResult();
                fuelType = type;
*/
                fuelType  = "Gasoline";
                 fuelEconomy = new FuelEconomyObdCommand(fuelType, commands);
                 throttlePositionObdCommand = new ThrottlePositionCommand();


                connected = true;
           } catch (Exception e) {
                Log.d("Exception", "Bluetooth IO Exception c");
                throw new ConnectionException();
            }

        }






    public HashMap<String, String> queryForParameters() throws ConnectionException {
        String rpmResult = null;
        String speedResult = null;
        String fuelResult = "" + -1 + " L/100km";
        String fuelFlow = "" + -1 + " L/h";
        String odometer = "0 Km";
        String consumption = "0 L";
        float tankLevel = 0.f;
        float finalTankLevel = 0.f;

        if (!connected) throw new ConnectionException();
        HashMap<String, String> query = new HashMap<String, String>();
        query.put(FUEL_TYPE, fuelType);

        try {
            engineRpmCommand.run(sock.getInputStream(), sock.getOutputStream());
            speedCommand.run(sock.getInputStream(), sock.getOutputStream());

            rpmResult = engineRpmCommand.getFormattedResult();
            speedResult = speedCommand.getFormattedResult();



        //managing fuel flow rate = 0 when the car is moving but you are not accelerating
        if (engineRpmCommand.getRPM() >= 1200) {
            throttlePositionObdCommand.run(sock.getInputStream(), sock.getOutputStream());
            if (((int) throttlePositionObdCommand.getPercentage()) == 0) {
                fuelFlow = "" + 0 + " L/h";
                fuelResult = "" + 0 + " L/100km";
                //cut off
                fuelEconomy.setFlow(0.f);
            } else {
                fuelEconomy.run(sock.getInputStream(), sock.getOutputStream());
                fuelFlow = "" + String.format("%.3f", fuelEconomy.getFlow()) + " L/h";
                fuelResult = fuelEconomy.getFormattedResult();
            }
        } else {
            fuelEconomy.run(sock.getInputStream(), sock.getOutputStream());
            //fuelFlow = "" + fuelEconomy.getFlow() + " L/h";
            fuelFlow = "" + String.format("%.3f", fuelEconomy.getFlow()) + " L/h";
            if (speedCommand.getMetricSpeed() > 1) {
                fuelResult = fuelEconomy.getFormattedResult();
            } else {
                fuelResult = "---";
            }
        }
            fuelLevelCommand.run(sock.getInputStream(), sock.getOutputStream());
            if (fuelLevelCommand.getFuelLevel() > 1.0f && tankLevel != -1.0f) {
                finalTankLevel = tankLevel - fuelLevelCommand.getFuelLevel();
            }



        }catch(Exception e){
                e.printStackTrace();
        }

        query.put(RPM, rpmResult);
        query.put(SPEED, speedResult);
        query.put(FUEL, fuelResult);
        query.put(TANK, String.valueOf(finalTankLevel));
        return query;
    }


    public void disconnectToCar(){
        if(connected){
            try {
                sock.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            connected = false;

        }
    }
}
