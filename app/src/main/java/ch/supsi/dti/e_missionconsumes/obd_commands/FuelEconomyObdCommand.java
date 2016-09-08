package ch.supsi.dti.e_missionconsumes.obd_commands;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.supsi.dti.e_missionconsumes.FuelType;
import obd.commands.ObdCommand;
import obd.commands.SpeedCommand;
import obd.commands.engine.MassAirFlowCommand;
import obd.commands.engine.RPMCommand;
import obd.commands.fuel.ConsumptionRateCommand;
import obd.commands.pressure.IntakeManifoldPressureCommand;
import obd.commands.temperature.AirIntakeTemperatureCommand;
import obd.enums.AvailableCommandNames;

/**
 * Created by Patrick on 17/06/2015.
 * Rewritten by Niko
 */
public class FuelEconomyObdCommand extends ObdCommand {
    private final float b = 0.6635f;
    private final float a = 0.0023f;
    private final FuelType fuelType;
    private final boolean mafSupport;
    private final boolean fuelRateSupport;
    private final boolean mapSupport;
    private final boolean fuelLevelSupport;
    protected float kml = -1.0f;
    private float mpg = -1.f;
    private final float AIR_FUEL_RATIO = 14.7f;
    private final float AIR_FUEL_RATIO_DIESEL = 14.6f;
    private final float DIESEL_DENSITY = 7.30f;
    private final float GRAMS_POUND_CONVERSION = 454.0f;
    private final float KMH_MPH_CONSTANT = 0.621371f;
    private final float SECONDS_HOUR = 3600.0f;
    private final float MPG_KML = 235.2145833f;
    private final float GASOLINE_DENSITY = 737.22f;
    String TAG = "OBD Response";
    protected float flow = 0.f;


    public float getFlow() {
        return this.flow;
    }

    public void setFlow(float flow) {
        this.flow = flow;
    }

    public FuelEconomyObdCommand(FuelType fuelType, boolean mafSupport, boolean fuelRateSupport, boolean mapSupport, boolean fuelLevelSupport) {
        super("");

        this.fuelType = fuelType;
        this.mafSupport = mafSupport;
        this.fuelRateSupport = fuelRateSupport;
        this.mapSupport = mapSupport;
        this.fuelLevelSupport = fuelLevelSupport;
    }

    @Override
    protected void performCalculations() {

    }

    @Override
    public void run(InputStream in, OutputStream out) throws IOException, InterruptedException {
        float MAF;
        // get metric speed
        final SpeedCommand speedCommand = new SpeedCommand();
        speedCommand.run(in, out);
        float speed = speedCommand.getMetricSpeed();
        if (this.fuelRateSupport) {
            Log.i(this.TAG, "Fuel rate supported:");
            ConsumptionRateCommand rateCommand = new ConsumptionRateCommand();
            rateCommand.run(in, out);
            while (!rateCommand.isReady()) {
                Thread.sleep(1);
            }
            this.flow = rateCommand.getLitersPerHour();
            this.kml = 100 / (speed / this.flow);
            this.mpg = this.MPG_KML / this.kml;
        }
        else if (this.mafSupport) {
            //get MAF
            final MassAirFlowCommand mafCommand = new MassAirFlowCommand();
            mafCommand.run(in, out);
            MAF = (float) mafCommand.getMAF();
            Log.i(this.TAG, "MAF: " + MAF);
            if (this.fuelType == FuelType.DIESEL) {
                Log.i(this.TAG, "Diesel engine + MAF:");
                //get engine load
                final EngineLoadObdCommand loadObdCommand = new EngineLoadObdCommand();
                loadObdCommand.run(in, out);
                float engineLoad = loadObdCommand.getPercentage();
                Log.i("ENGINELOAD", "engine load: " + engineLoad);
                // compute fuel flow L/h
                this.flow = this.a * (MAF * engineLoad) + this.b;

                //compute fuel economy L/100km
                this.kml = 100 / (speed / this.flow);
                this.mpg = this.MPG_KML / this.kml;
            }
            else if (this.fuelType == FuelType.GAS) {
                Log.i(this.TAG, "Gasoline engine + MAF:");
                // get l/100km
                this.flow = (MAF * this.SECONDS_HOUR) / (this.GASOLINE_DENSITY * this.AIR_FUEL_RATIO);
                this.kml = 100 / (speed / this.flow);
                this.mpg = this.MPG_KML / this.kml;
            }
        }
        else if (this.mapSupport && this.fuelType == FuelType.GAS) {
            Log.i(this.TAG, "Alternative MAF + Gasoline:");
            //get alternative MAF
            final RPMCommand engineRpmCommand = new RPMCommand();
            engineRpmCommand.run(in, out);

            float RPM = (float) engineRpmCommand.getRPM();
            //System.err.println(RPM);
            //get manifold pressure
            final IntakeManifoldPressureCommand pressureCommand = new IntakeManifoldPressureCommand();
            pressureCommand.run(in, out);

            float MAP = (float) pressureCommand.getMetricUnit();
            Log.i("MAP:", "val: " + MAP);
            //get intake temperature
            final AirIntakeTemperatureCommand temperatureCommand = new AirIntakeTemperatureCommand();
            temperatureCommand.run(in, out);


            float INTAKE_TEMP = temperatureCommand.getKelvin();
            Log.i("ITEMP:", "val: " + INTAKE_TEMP);
            MAF = RPM * MAP / INTAKE_TEMP;
            // get l/100km

            this.flow = (MAF * this.SECONDS_HOUR) / (this.GASOLINE_DENSITY * this.AIR_FUEL_RATIO) / 26; //26 is the magic number
            //setFlow(fuelFlow);
            this.kml = 100 / (speed / this.flow);
            this.mpg = this.MPG_KML / this.kml;
        }
        super.setReady(true);
    }

    @Override
    public String getFormattedResult() {
        return this.useImperialUnits ? String.format("%.1f %s", getMilesPerUKGallon(), "mpg") : String.format("%.1f %s", this.kml, "l/100km");
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.kml);
    }

    /**
     * @return a float.
     */
    public float getLitersPer100Km() {
        return this.kml;
    }

    /**
     * @return a float.
     */
    public float getMilesPerUSGallon() {
        return 235.2f / this.kml;
    }

    /**
     * @return a float.
     */
    public float getMilesPerUKGallon() {
        return 282.5f / this.kml;
    }

    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_CONSUMPTION_RATE.getValue();
    }

}
