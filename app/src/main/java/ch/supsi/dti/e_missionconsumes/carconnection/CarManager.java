package ch.supsi.dti.e_missionconsumes.carconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import ch.supsi.dti.e_missionconsumes.obd_commands.CheckObdCommands;
import ch.supsi.dti.e_missionconsumes.obd_commands.FuelEconomyObdCommand;
import obd.commands.SpeedCommand;
import obd.commands.engine.RPMCommand;
import obd.commands.engine.ThrottlePositionCommand;
import obd.commands.fuel.FindFuelTypeCommand;
import obd.commands.fuel.FuelLevelCommand;
import obd.commands.protocol.LineFeedOffCommand;
import obd.commands.protocol.SelectProtocolCommand;
import obd.commands.protocol.TimeoutCommand;
import obd.enums.ObdProtocols;
import obd.exceptions.NoDataException;
import obd.exceptions.UnsupportedCommandException;

/**
 * Created by Alan on 18/09/15.
 */
public class CarManager {
    /*HASH KEYS */
    public static String FUEL_TYPE = "FT";
    public static String RPM = "RPM";
    public static String SPEED = "SP";
    public static String FUEL = "FUEL";
    public static String TANK = "TK";
    public static String ODOMETER = "OD";
    /*EOK*/
    public boolean fineMode = true;
    long previousTime = 0;
    long previousSpeed = 0;
    float previousFlow = 0;
    float kmODO = 0.f;
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
    private boolean MAFSupport = false;
    private boolean FuelRateSupport = false;
    private boolean MAPSupport = false;
    private boolean FuelLevelSupport = false;

    public String getFuelType() {
        return this.fuelType;
    }

    public void setFuelType(String ft) {
        this.fuelEconomy = new FuelEconomyObdCommand(this.fuelType, this.commands);
        this.fuelType = ft;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void connectToAdapter(String _devAddress, boolean coarseMode) throws ConnectionException {
        this.devAddress = _devAddress;
        this.fineMode = !coarseMode;
        connectToAdapter(_devAddress);
    }

    public void connectToAdapter(String _devAddress) throws ConnectionException {
        this.devAddress = _devAddress;
        this.kmODO = 0.f;
        Log.d(getClass().getName(), "entering connectToAdapter");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(this.devAddress);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        // creation and connection of a bluetooth socket
        try {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            this.sock = socket;
            new obd.commands.protocol.EchoOffCommand().run(this.sock.getInputStream(), this.sock.getOutputStream());
            new LineFeedOffCommand().run(this.sock.getInputStream(), this.sock.getOutputStream());
            new TimeoutCommand(200).run(this.sock.getInputStream(), this.sock.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(this.sock.getInputStream(), this.sock.getOutputStream());
            CheckObdCommands check = new CheckObdCommands();
            check.run(this.sock.getInputStream(), this.sock.getOutputStream());
            this.MAFSupport = check.getSupportMAF();
            this.FuelRateSupport = check.getSupportFuelRate();
            this.MAPSupport = check.getSupportMAP();
            this.FuelLevelSupport = check.getSupportFuelLevel();
            String checked = check.toString();
            this.commands = checked;
            Log.d(this.getClass().getName(), "commands supported: " + checked);
            //determine fuel type
                /*FindFuelTypeCommand fuelTypeObdCommand = new FindFuelTypeCommand();
                fuelTypeObdCommand.run(sock.getInputStream(), sock.getOutputStream());
                String type = fuelTypeObdCommand.getFormattedResult();
                fuelType = type;
*/
            this.throttlePositionObdCommand = new ThrottlePositionCommand();
            this.connected = true;
            this.fuelType = checkFuelType();
            if (this.fuelType != null) {
                this.fuelEconomy = new FuelEconomyObdCommand(this.fuelType, this.commands);
            }
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
        if (!this.connected) {
            throw new ConnectionException();
        }
        HashMap<String, String> query = new HashMap<>();
        query.put(FUEL_TYPE, this.fuelType);
        try {
            this.engineRpmCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            this.speedCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            rpmResult = this.engineRpmCommand.getFormattedResult();
            speedResult = this.speedCommand.getFormattedResult();
            if (this.engineRpmCommand.getRPM() >= 1200) {
                this.throttlePositionObdCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
                if (((int) this.throttlePositionObdCommand.getPercentage()) == 0) {
                    fuelFlow = "" + 0 + " L/h";
                    fuelResult = "" + 0 + " LHK";
                    //cut off
                    this.fuelEconomy.setFlow(0.f);
                }
                else {
                    this.fuelEconomy.run(this.sock.getInputStream(), this.sock.getOutputStream());
                    fuelFlow = "" + String.format("%.3f", this.fuelEconomy.getFlow()) + " L/h";
                    fuelResult = this.fuelEconomy.getFormattedResult();
                }
            }
            //managing fuel flow rate = 0 when the car is moving but you are not accelerating
  /*      if (engineRpmCommand.getRPM() >= 1200) {
            throttlePositionObdCommand.run(sock.getInputStream(), sock.getOutputStream());
            if (((int) throttlePositionObdCommand.getPercentage()) == 0) {
                fuelFlow = "" + 0 + " L/h";
                fuelResult = "" + 0 + " LHK";
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
            }*/
            float fuelCons = 0.f;
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.previousTime;
            this.previousTime = currentTime;
            Log.d(this.getClass().getName(), deltaTime + " " + currentTime + " " + this.previousTime);
            this.fineMode = false;
            if (this.fineMode) {
                this.kmODO += ((float) this.speedCommand.getMetricSpeed()) * ((float) deltaTime) / 1000 / 3600;
                odometer = "" + String.format("%.3f", this.kmODO) + " Km";
            }
            else {
                long currSpeed = this.speedCommand.getMetricSpeed();
                long avgSpeed = (currSpeed + this.previousSpeed) / 2;
                this.previousSpeed = currSpeed;
                this.kmODO += ((double) avgSpeed) * ((double) deltaTime) / 1000 / 3600;
                odometer = "" + String.format("%.3f", this.kmODO) + " Km";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        query.put(RPM, rpmResult);
        query.put(SPEED, speedResult);
        query.put(FUEL, fuelResult);
        query.put(TANK, String.valueOf(finalTankLevel));
        query.put(ODOMETER, odometer);
        return query;
    }

    public void disconnectFromCar() {
        if (this.connected) {
            try {
                this.sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.connected = false;
        }
    }

    private String checkFuelType() {
        //new instance of the command fuelType
        FindFuelTypeCommand fuelTypeObdCommand = new FindFuelTypeCommand();
        String ft = "";
        try {
            fuelTypeObdCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            String type = fuelTypeObdCommand.getFormattedResult();
            if (type.equals("Gasoline")) {
                return "Gasoline";
            }
            else if (type.equals("Diesel")) {
                return "Diesel";
            }
        } catch (IOException e) {
            Log.d(this.getClass().getName(), "Fuel type: " + "unknown, IOException occurred");
        } catch (NoDataException e) {
            Log.d(this.getClass().getName(), "Fuel type: " + "unknown, NoDataException occurred");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            Log.d(this.getClass().getName(), "Fuel type: " + "unknown, IndexOutOfBoundsException occurred");
        } catch (UnsupportedCommandException e) {
            Log.d(this.getClass().getName(), "Fuel type: " + "unknown, UnsupportedCommandException occurred");
        }
        return "";
    }

    public boolean hasMAF() {
        return this.MAFSupport;
    }

    public boolean hasMAP() {
        return this.MAPSupport;
    }

    public boolean hasFuelLvl() {
        return this.FuelLevelSupport;
    }

    public boolean hasFuelRate() {
        return this.FuelRateSupport;
    }
}