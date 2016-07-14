package ch.supsi.dti.e_missionconsumes.carconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import ch.supsi.dti.e_missionconsumes.FuelType;
import ch.supsi.dti.e_missionconsumes.obd_commands.CheckObdCommands;
import ch.supsi.dti.e_missionconsumes.obd_commands.FuelEconomyObdCommand;
import obd.commands.SpeedCommand;
import obd.commands.control.VinCommand;
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


public class CarManager {
    /*HASH KEYS */
    public static final String FUEL_TYPE = "FT";
    public static final String RPM = "RPM";
    public static final String SPEED = "SP";
    public static final String FUEL = "FUEL";
    public static final String ECONOMY = "FEC";
    public static final String FUELCONSUMED = "FC";
    public static final String TANK = "TK";
    public static final String ODOMETER = "OD";
    /*EOK*/
    public boolean fineMode = true;
    long previousTime = 0;
    long previousSpeed = 0;
    float previousFlow = 0;
    float kmODO = 0.f;
    private String devAddress;
    private BluetoothSocket sock;
    // private String fuelType;
    private FuelType fuelType = null;
    private boolean connected = false;
    private FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();
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

    public FuelType getFuelType() {
        return this.fuelType;
    }


    //this method is called if the use has to manually specify the fuel type
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

    public void connectToAdapter(String _devAddress, boolean coarseMode) throws ConnectionException {
        this.devAddress = _devAddress;
        this.fineMode = !coarseMode;
        connectToAdapter(_devAddress);
    }

    private void checkThrottle(int rpm, float speed, int throttlePosition) {
        if (rpm < 1200 && speed < 1 && throttleWorking) {
            if (throttlePosition > 90) {
                throttleWorking = false;
            }
        }
    }

    public void connectToAdapter(String _devAddress) throws ConnectionException {
        this.devAddress = _devAddress;
        this.kmODO = 0.f;
        Log.i(getClass().getName(), "entering connectToAdapter");
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
            checkSupportedSensors();

            VinCommand vinCommand = new VinCommand();
            vinCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            String vin = "";
            while (!vinCommand.isReady()) {
                Thread.sleep(1);
            }
            vin = vinCommand.getCalculatedResult();
            String carModel = CarInfo.getInstance().getCarModel(vin);
            if (carModel.isEmpty()) {
                CarInfo.getInstance().promptCarModel(vin);
            }

            this.throttlePositionObdCommand = new ThrottlePositionCommand();
            this.connected = true;
            this.fuelType = checkFuelType();
            if (this.fuelType != null) {
                this.fuelEconomy = new FuelEconomyObdCommand(this.fuelType, this.mafSupport, this.fuelRateSupport, this.mapSupport, this.fuelLevelSupport);
            }
        } catch (Exception e) {
            Log.i("Exception", "Bluetooth IO Exception c");
            throw new ConnectionException();
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

        Log.i(this.getClass().getName(), "Sensors supported: maf=" + this.mafSupport + ", fuelrate=" + this.fuelRateSupport + ", map=" + this.mapSupport + ",fuellevel=" + this.fuelLevelSupport);
    }

    public HashMap<String, String> queryForParameters() throws ConnectionException {
        String rpmResult = null;
        String speedResult = null;
        String fuelResult = "" + -1 + " L/100km";
        //String fuelFlow = "" + -1 + " L/h";
        String odometer = "0 Km";
        // String consumption = "0 L";
        // float tankLevel = 0.f;
        float finalTankLevel = 0.f;
        if (!this.connected) {
            throw new ConnectionException();
        }
        HashMap<String, String> query = new HashMap<>();
        query.put(FUEL_TYPE, this.fuelType.name());
        try {
            this.engineRpmCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            this.speedCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            this.throttlePositionObdCommand.run(this.sock.getInputStream(), this.sock.getOutputStream());
            this.fuelEconomy.run(this.sock.getInputStream(), this.sock.getOutputStream());
            rpmResult = this.engineRpmCommand.getFormattedResult();
            speedResult = this.speedCommand.getFormattedResult();
            int throttlePosition = ((int) this.throttlePositionObdCommand.getPercentage());
            checkThrottle(engineRpmCommand.getRPM(), speedCommand.getMetricSpeed(), throttlePosition);
            //check if we're idling
            if (this.speedCommand.getMetricSpeed() < 1) {
                //we're idling!
                fuelResult = 2 + " L/h";
                this.fuelEconomy.setFlow(2f);
            }
            else if (throttleWorking && this.engineRpmCommand.getRPM() >= 1200 && throttlePosition == 0) {
                fuelResult = "" + 0 + " l/100km";
                fuelEconomy.setFlow(0);
            }
            else {
                fuelResult = fuelEconomy.getFormattedResult();
            }

            //fuelFlow = "" + String.format("%.3f", this.fuelEconomy.getFlow()) + " L/h";
            fuelResult = this.fuelEconomy.getFormattedResult();
            //  }
            // }

            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - this.previousTime;
            this.previousTime = currentTime;

            Log.i(this.getClass().getName(), deltaTime + " " + currentTime + " " + this.previousTime);
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

            //calculate consumed fuel and avg fuel economy:
            this.consumedFuel += (this.fuelEconomy.getFlow() / 3600.) * (deltaTime / 1000.);
            //consumed fuel divided by distance traveled multiplied by 100 to get liters/100km
            this.fuelAvgEconomy = (this.consumedFuel / this.kmODO) * 100;
            Log.i("FuelFlow",fuelEconomy.getFlow()+" l/h");

        } catch (Exception e) {
            e.printStackTrace();
        }

        query.put(RPM, rpmResult);
        query.put(SPEED, speedResult);
        query.put(FUEL, fuelResult);
        query.put(TANK, String.valueOf(finalTankLevel));
        query.put(ODOMETER, odometer);
        query.put(ECONOMY, this.fuelAvgEconomy + " l/100km");
        query.put(FUELCONSUMED, this.consumedFuel + " L");
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
            if (type.equals("Gasoline")) {
                return FuelType.GAS;
            }
            else if (type.equals("Diesel")) {
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