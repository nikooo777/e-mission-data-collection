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

/**
 * Created by Patrick on 24/08/2015.
 */
public class CheckObdCommands extends ObdCommand {
    private Boolean supportMAF = false;
    private Boolean supportFuelRate = false;
    private Boolean supportFuelLevel = false;
    private Boolean supportMAP = false;
    private float MAF;
    private float fuelRate;
    private String VIN;

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
        try {
            final MassAirFlowCommand mafCommand = new MassAirFlowCommand();
            mafCommand.run(in, out);
            this.MAF = (float) mafCommand.getMAF();
            if (this.MAF != -1.0f) {
                this.supportMAF = true;
                Log.d("CHECK", "MAF supported");
            }
            else {
                this.supportMAF = false;
                Log.d("CHECK", "MAF not supported");
            }
        } catch (Exception e) {
            this.supportMAF = false;
            Log.d("CHECK", "MAF not supported");
        }
        try {
            final IntakeManifoldPressureCommand mapCommand = new IntakeManifoldPressureCommand();
            mapCommand.run(in, out);
            System.err.println("MAP=" + mapCommand.getFormattedResult());
            this.supportMAP = mapCommand.getMetricUnit() != 0;
            if (this.supportMAP) {
                Log.d("CHECK", "MAP supported");
            }
            else {
                Log.d("CHECK", "MAP not supported");
            }
        } catch (Exception e) {
            Log.d("CHECK", "MAP not supported");
            this.supportMAP = false;
        }
        try {
            final ConsumptionRateCommand fuelrate = new ConsumptionRateCommand();
            fuelrate.run(in, out);
            this.fuelRate = (float) fuelrate.getLitersPerHour();
            if (this.fuelRate != -1.0f) {
                this.supportFuelRate = true;
                Log.d("CHECK", "Fuel rate supported");
            }
            else {
                this.supportFuelRate = false;
                Log.d("CHECK", "Fuel rate not supported");
            }
        } catch (Exception e) {
            this.supportFuelRate = false;
            Log.d("CHECK", "Fuel rate  not supported");
        }
        try {
            final VinCommand vinCommand = new VinCommand();
            vinCommand.run(in, out);
            this.VIN = vinCommand.getFormattedResult();
            if (!this.VIN.equals("")) {
                Log.d("CHECK", "VIN : " + this.VIN);
            }
            else {
                this.VIN = getEcuName(in, out);
                Log.d("CHECK", "VIN unknown");
            }
        } catch (Exception e) {
            this.VIN = getEcuName(in, out);
            Log.d("CHECK", "VIN unknown");
        }
        try {
            final FuelLevelCommand flc = new FuelLevelCommand();
            flc.run(in, out);
            this.supportFuelLevel = Float.compare(flc.getFuelLevel(), 0f) != 0;
            if (this.supportFuelLevel) {
                Log.d("CHECK", "Fuel Level supported");
            }
            else {
                Log.d("CHECK", "Fuel Level not supported");
            }
        } catch (Exception ex) {
            Log.d("CHECK", "Fuel Level not supported");
        }
    }

    public String getEcuName(InputStream in, OutputStream out) {
        final EcuNameCommand nameCommand = new EcuNameCommand();
        String name = "NO NAME";
        try {
            nameCommand.run(in, out);
            name = nameCommand.getFormattedResult();
        } catch (Exception e) {
            Log.d("CHECK", "ECU unknown");
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
