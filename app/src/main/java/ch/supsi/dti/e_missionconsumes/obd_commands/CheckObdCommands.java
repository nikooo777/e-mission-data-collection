package ch.supsi.dti.e_missionconsumes.obd_commands;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import obd.commands.ObdCommand;
import obd.commands.control.VinCommand;
import obd.commands.engine.MassAirFlowCommand;
import obd.commands.fuel.ConsumptionRateCommand;
import obd.commands.fuel.FuelLevelCommand;
import obd.commands.pressure.IntakeManifoldPressureCommand;
import obd.commands.protocol.AvailablePidsCommand_01_20;
import obd.commands.protocol.AvailablePidsCommand_21_40;
import obd.commands.protocol.AvailablePidsCommand_41_60;

/**
 * Created by Patrick on 24/08/2015.
 * Rewritten by Niko
 */
public class CheckObdCommands extends ObdCommand {
    private Boolean supportMAF = false;
    private Boolean supportFuelRate = false;
    private Boolean supportFuelLevel = false;
    private Boolean supportMAP = false;
    private float MAF;
    private float fuelRate;
    private String VIN;
    private boolean ready = false;

    public Boolean getSupportMAF() {
        return this.supportMAF;
    }

    public Boolean getSupportFuelRate() {
        return this.supportFuelRate;
    }

    public String getVIN() {
        return this.VIN;
    }

    public CheckObdCommands() {
        super("");
    }

    @Override
    protected void performCalculations() {
    }

    @Override
    public void run(InputStream in, OutputStream out) throws IOException, InterruptedException {
        //check MAF
        final AvailablePidsCommand_01_20 pids0120 = new AvailablePidsCommand_01_20();
        final AvailablePidsCommand_21_40 pids2140 = new AvailablePidsCommand_21_40();
        final AvailablePidsCommand_41_60 pids4160 = new AvailablePidsCommand_41_60();
        try {
            Log.i("SUPPORT-0120: ", pids0120.getCalculatedResult());
            Log.i("SUPPORT-2140: ", pids2140.getCalculatedResult());
            Log.i("SUPPORT-4160: ", pids4160.getCalculatedResult());
        } catch (Exception ex) {
            ex.printStackTrace(); //TODO: remove
        }
        //check MAF
        try {
            final MassAirFlowCommand mafCommand = new MassAirFlowCommand();
            mafCommand.run(in, out);
            while (!mafCommand.isReady()) {
                Thread.sleep(1);
            }
            this.MAF = (float) mafCommand.getMAF();
            if (this.MAF != -1.0f) {
                this.supportMAF = true;
                Log.i("CHECK", "MAF supported");
            }
            else {
                this.supportMAF = false;
                Log.i("CHECK", "MAF not supported");
            }
        } catch (Exception e) {
            this.supportMAF = false;
            Log.i("CHECK", "MAF not supported");
        }
        try {
            final IntakeManifoldPressureCommand mapCommand = new IntakeManifoldPressureCommand();
            mapCommand.run(in, out);
            while (!mapCommand.isReady()) {
                Thread.sleep(1);
            }
            System.err.println("MAP=" + mapCommand.getFormattedResult());
            this.supportMAP = mapCommand.getMetricUnit() != 0;
            if (this.supportMAP) {
                Log.i("CHECK", "MAP supported");
            }
            else {
                Log.i("CHECK", "MAP not supported");
            }
        } catch (Exception e) {
            Log.i("CHECK", "MAP not supported");
            this.supportMAP = false;
        }
        try {
            final ConsumptionRateCommand fuelRate = new ConsumptionRateCommand();
            fuelRate.run(in, out);
            this.fuelRate = fuelRate.getLitersPerHour();
            if (this.fuelRate != -1.0f) {
                this.supportFuelRate = true;
                Log.i("CHECK", "Fuel rate supported");
            }
            else {
                this.supportFuelRate = false;
                Log.i("CHECK", "Fuel rate not supported");
            }
        } catch (Exception e) {
            this.supportFuelRate = false;
            Log.i("CHECK", "Fuel rate  not supported");
        }
        try {
            final VinCommand vinCommand = new VinCommand();
            vinCommand.run(in, out);
            this.VIN = vinCommand.getFormattedResult();
            if (!this.VIN.equals("")) {
                Log.i("CHECK", "VIN : " + this.VIN);
            }
            else {
                this.VIN = getEcuName(in, out);
                Log.i("CHECK", "VIN unknown");
            }
        } catch (Exception e) {
            this.VIN = getEcuName(in, out);
            Log.i("CHECK", "VIN unknown");
        }
        try {
            final FuelLevelCommand flc = new FuelLevelCommand();
            flc.run(in, out);
            this.supportFuelLevel = Float.compare(flc.getFuelLevel(), 0f) != 0;
            if (this.supportFuelLevel) {
                Log.i("CHECK", "Fuel Level supported");
            }
            else {
                Log.i("CHECK", "Fuel Level not supported");
            }
        } catch (Exception ex) {
            Log.i("CHECK", "Fuel Level not supported");
        }
        this.ready = true;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    public String getEcuName(InputStream in, OutputStream out) {
        final EcuNameCommand nameCommand = new EcuNameCommand();
        String name = "NO NAME";
        try {
            nameCommand.run(in, out);
            while (!nameCommand.isReady()) {
                Thread.sleep(1);
            }
            name = nameCommand.getFormattedResult();
        } catch (Exception e) {
            Log.i("CHECK", "ECU unknown");
            return "NO NAME";
        }
        return name;
    }

    @Override
    public String toString() {
        return "" + this.supportMAF + "\n" + this.supportFuelRate;
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(toString());
    }

    @Override
    public String getFormattedResult() {
        return String.valueOf(toString());
    }

    @Override
    public String getName() {
        return null;
    }

    public Boolean getSupportMAP() {
        return this.supportMAP;
    }

    public Boolean getSupportFuelLevel() {
        return this.supportFuelLevel;
    }
}
