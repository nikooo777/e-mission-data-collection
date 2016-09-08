package ch.supsi.dti.e_missionconsumes.carconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

import ch.supsi.dti.e_missionconsumes.FuelType;
import ch.supsi.dti.e_missionconsumes.obd_commands.CheckObdCommands;
import ch.supsi.dti.e_missionconsumes.obd_commands.FuelEconomyObdCommand;
import obd.commands.SpeedCommand;
import obd.commands.engine.RPMCommand;
import obd.commands.engine.ThrottlePositionCommand;
import obd.commands.fuel.FindFuelTypeCommand;
import obd.commands.protocol.LineFeedOffCommand;
import obd.commands.protocol.SelectProtocolCommand;
import obd.commands.protocol.TimeoutCommand;
import obd.enums.ObdProtocols;
import obd.exceptions.NoDataException;
import obd.exceptions.UnsupportedCommandException;


public class CarManager {
    //HASH KEYS
    public static final String RPM = "RPM";
    public static final String SPEED = "SP";
    public static final String FUEL = "FUEL";
    public static final String ECONOMY = "FEC";
    public static final String FUELCONSUMED = "FC";
    public static final String ODOMETER = "OD";

    long previousTime = 0;
    long previousSpeed = 0;
    float kmODO = 0.f;
    private String devAddress;
    private BluetoothSocket sock;
    private FuelType fuelType = null;
    private boolean connected = false;
    private RPMCommand engineRpmCommand = new RPMCommand();
    private SpeedCommand speedCommand = new SpeedCommand();
    private FuelEconomyObdCommand fuelEconomy;
    private ThrottlePositionCommand throttlePositionObdCommand;
    private boolean mafSupport = false;
    private boolean fuelRateSupport = false;
    private boolean mapSupport = false;
    private boolean fuelLevelSupport = false;
    private double consumedFuel = 0;
    private double fuelAvgEconomy = 0;
    private boolean throttleWorking = true;
    private String vehicleIdentificationNumber = "UNDEFINED";
    private Boolean throttleSupport = false;

    public FuelType getFuelType() {
        return this.fuelType;
    }

    public String getVIN() {
        return this.vehicleIdentificationNumber;
    }

    //this method is called if the user has to manually specify the fuel type
    public void setFuelType(final FuelType fuelType) {
        if (fuelType != null) {
            this.fuelType = fuelType;
            this.fuelEconomy = new FuelEconomyObdCommand(this.fuelType, this.mafSupport, this.fuelRateSupport, this.mapSupport, this.fuelLevelSupport);
        }
        //if it wasn't detected automatically and null was supplied (why?) we throw an ex
        else if (this.fuelType == null) {
            throw new RuntimeException("Fuel type must be specified!");
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    //this method is useful to check whether or not the- throttle sensor is working as intended
    private void checkThrottle(int rpm, float speed, int throttlePosition) {
        //if the RPM is below 1200, we're standing still and the throttle position is at > 90% then the sensor is either broken or doesn't work as intended
        if (rpm < 1200 && speed < 1 && this.throttleWorking) {
            if (throttlePosition > 90) {
                this.throttleWorking = false;
            }
        }
    }

    private void resetVals() {
        this.kmODO = 0.f;
        this.previousTime = 0;
        this.previousSpeed = 0;
        this.fuelType = null;
        this.mafSupport = false;
        this.fuelRateSupport = false;
        this.mapSupport = false;
        this.fuelLevelSupport = false;
        this.consumedFuel = 0;
        this.fuelAvgEconomy = 0;
        this.throttleWorking = true;
        this.vehicleIdentificationNumber = "UNDEFINED";
    }

    public void connectToAdapter(String devAddress) throws ConnectionException {
        resetVals();
        this.devAddress = devAddress;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(this.devAddress);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        // creation and connection of a bluetooth socket
        try {
            this.sock = device.createInsecureRfcommSocketToServiceRecord(uuid);
            this.sock.connect();
            InputStream in = this.sock.getInputStream();
            OutputStream out = this.sock.getOutputStream();
            new obd.commands.protocol.EchoOffCommand().run(in, out);
            new LineFeedOffCommand().run(in, out);
            new TimeoutCommand(200).run(in, out);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(in, out);
            checkSupportedSensors();

            String carModel = CarInfo.getInstance().getCarModel(this.vehicleIdentificationNumber);
            if (carModel.isEmpty()) {
                CarInfo.getInstance().promptCarModel(this.vehicleIdentificationNumber);
            }

            if (this.throttleSupport) {
                this.throttlePositionObdCommand = new ThrottlePositionCommand();
            }
            this.connected = true;
            this.fuelType = checkFuelType();
            if (this.fuelType != null) {
                this.fuelEconomy = new FuelEconomyObdCommand(this.fuelType, this.mafSupport, this.fuelRateSupport, this.mapSupport, this.fuelLevelSupport);
            }
        } catch (Exception e) {
            Log.i("Exception", "Bluetooth IO Exception c");
            try {
                disconnectFromCar();
            } catch (Exception ex) {

            }
            throw new ConnectionException(e.getMessage());
        }
    }

    private void checkSupportedSensors() throws IOException, InterruptedException {
        CheckObdCommands check = new CheckObdCommands();
        check.run(this.sock.getInputStream(), this.sock.getOutputStream());
        while (!check.isReady()) {
            Thread.sleep(1);
        }
        this.mafSupport = check.getSupportMAF();
        this.fuelRateSupport = check.getSupportFuelRate();
        this.mapSupport = check.getSupportMAP();
        this.fuelLevelSupport = check.getSupportFuelLevel();
        this.throttleSupport = check.getSupportThrottle();
        this.vehicleIdentificationNumber = check.getVIN();

        Log.i(this.getClass().getName(), "Sensors supported: maf=" + this.mafSupport + ", fuelrate=" + this.fuelRateSupport + ", map=" + this.mapSupport + ",fuellevel=" + this.fuelLevelSupport);
    }

    public HashMap<String, String> queryForParameters() throws ConnectionException {
        String rpmResult = null;
        String speedResult = null;
        String fuelResult = "" + -1 + " L/100km";
        String odometer = "0 Km";

        if (!this.connected) {
            throw new ConnectionException("Service not connected!");
        }

        HashMap<String, String> query = new HashMap<>();
        try {
            InputStream in = this.sock.getInputStream();
            OutputStream out = this.sock.getOutputStream();
            this.engineRpmCommand.run(in, out);
            this.speedCommand.run(in, out);

            this.fuelEconomy.run(in, out);
            rpmResult = this.engineRpmCommand.getRPM() + "";
            speedResult = this.speedCommand.getFormattedResult();
            int throttlePosition = -1;
            if (this.throttleSupport) {
                this.throttlePositionObdCommand.run(in, out);
                throttlePosition = ((int) this.throttlePositionObdCommand.getPercentage());
                checkThrottle(this.engineRpmCommand.getRPM(), this.speedCommand.getMetricSpeed(), throttlePosition);
            }

            //check if we're idling
            if (this.speedCommand.getMetricSpeed() < 1) {
                //we're idling!
                fuelResult = 2 + " L/h";
                this.fuelEconomy.setFlow(2f);
            }
            //check if we're on cutoff state
            else if (this.throttleSupport && this.throttleWorking && this.engineRpmCommand.getRPM() >= 1200 && throttlePosition == 0) {
                fuelResult = "" + 0 + " l/100km";
                this.fuelEconomy.setFlow(0);
            }
            else {
                fuelResult = this.fuelEconomy.getFormattedResult();
            }

            long currentTime = System.currentTimeMillis();
            if (this.previousTime == 0) {
                this.previousTime = currentTime;
            }
            long deltaTime = currentTime - this.previousTime;
            this.previousTime = currentTime;

            Log.i(this.getClass().getName(), deltaTime + " " + currentTime + " " + this.previousTime);

            long currSpeed = this.speedCommand.getMetricSpeed();
            long avgSpeed = (currSpeed + this.previousSpeed) / 2;
            this.previousSpeed = currSpeed;
            //calculate the travelled distance
            this.kmODO += ((double) avgSpeed) * ((double) deltaTime) / 1000 / 3600;
            odometer = "" + String.format("%.3f", this.kmODO) + " Km";

            //calculate consumed fuel and avg fuel economy:
            this.consumedFuel += (this.fuelEconomy.getFlow() / 3600.) * (deltaTime / 1000.);
            //consumed fuel divided by distance traveled multiplied by 100 to get liters/100km
            this.fuelAvgEconomy = (this.consumedFuel / this.kmODO) * 100;
            Log.i("FuelFlow", this.fuelEconomy.getFlow() + " l/h");
            Log.i("deltaTime", deltaTime + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }

        query.put(RPM, rpmResult);
        query.put(SPEED, speedResult);
        query.put(FUEL, fuelResult);
        //query.put(TANK, String.valueOf(finalTankLevel));
        query.put(ODOMETER, odometer);
        query.put(ECONOMY, String.format("%.3f l/100km", this.fuelAvgEconomy));
        query.put(FUELCONSUMED, String.format("%.3f L", this.consumedFuel));
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

    private FuelType checkFuelType() {
        //new instance of the command fuelType
        FindFuelTypeCommand fuelTypeObdCommand = new FindFuelTypeCommand();
        try {
            fuelTypeObdCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            while (!fuelTypeObdCommand.isReady()) {
                Thread.sleep(1);
            }
            String type = fuelTypeObdCommand.getFormattedResult();
            if (type.equals("GAS")) {
                return FuelType.GAS;
            }
            else if (type.equals("DIESEL")) {
                return FuelType.DIESEL;
            }
        } catch (IOException e) {
            Log.i(this.getClass().getName(), "Fuel type: " + "unknown, IOException occurred");
        } catch (NoDataException e) {
            Log.i(this.getClass().getName(), "Fuel type: " + "unknown, NoDataException occurred");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            Log.i(this.getClass().getName(), "Fuel type: " + "unknown, IndexOutOfBoundsException occurred");
        } catch (UnsupportedCommandException e) {
            Log.i(this.getClass().getName(), "Fuel type: " + "unknown, UnsupportedCommandException occurred");
        }
        return null;
    }

    public boolean hasMAF() {
        return this.mafSupport;
    }

    public boolean hasMAP() {
        return this.mapSupport;
    }

    public boolean hasFuelLvl() {
        return this.fuelLevelSupport;
    }

    public boolean hasFuelRate() {
        return this.fuelRateSupport;
    }
}