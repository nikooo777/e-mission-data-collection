package ch.supsi.dti.e_missionconsumes.obd_commands;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import obd.commands.ObdCommand;
import obd.commands.control.VinCommand;
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
        Log.i("SUPPORT-PIDS: ", "checking pid support");
        final AvailablePidsCommand_01_20 pids0120 = new AvailablePidsCommand_01_20();
        final AvailablePidsCommand_21_40 pids2140 = new AvailablePidsCommand_21_40();
        final AvailablePidsCommand_41_60 pids4160 = new AvailablePidsCommand_41_60();
        pids0120.run(in, out);
        pids2140.run(in, out);
        pids4160.run(in, out);
        try {
            //masks used to compare available PIDs with the ones we're interested in
            final long MAFPID = 1 << 16; //10
            final long MAPPID = 1 << 21; //0B
            final long FLSPID = 1 << 17; //2F
            final long FRSPID = 1 << 2;  //5E
            long pids0129_val = Long.parseLong(pids0120.getCalculatedResult().substring(0, 8), 16);
            long pids2140_val = Long.parseLong(pids2140.getCalculatedResult().substring(0, 8), 16);
            long pids4160_val = Long.parseLong(pids4160.getCalculatedResult().substring(0, 8), 16);
            Log.i("SUPPORT-MAF: ", "val: " + (MAFPID & pids0129_val));
            Log.i("SUPPORT-MAP: ", "val: " + (MAPPID & pids0129_val));
            Log.i("SUPPORT-FL: ", "val: " + (FLSPID & pids2140_val));
            Log.i("SUPPORT-FR: ", "val: " + (FRSPID & pids4160_val));
            this.supportMAF = (MAFPID & pids0129_val) > 0;
            this.supportMAP = (MAPPID & pids0129_val) > 0;
            this.supportFuelLevel = (FLSPID & pids2140_val) > 0;
            this.supportFuelRate = (FRSPID & pids4160_val) > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
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
