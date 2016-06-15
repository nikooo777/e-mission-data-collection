package ch.supsi.dti.e_missionconsumes.obd_commands;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 */
public class FuelEconomyObdCommand extends ObdCommand {
    private final float b=0.6635f;
    private final float a=0.0023f;
    protected float kml = -1.0f;
    private final float AIR_FUEL_RATIO=14.7f;
    private final float AIR_FUEL_RATIO_DIESEL=14.6f;
    private final float GASOLINE_DENSITY=6.17f;
    private final float DIESEL_DENSITY=7.30f;
    private final float GRAMS_POUND_CONVERSION=454.0f;
    private final float KMH_MPH_CONSTANT=0.621371f;
    private final float SECONDS_HOUR=3600.0f;
    private final float MPG_KML=235.2145833f;
    private final float GASOLINE_DENSITY2=739.329063f;
    String TAG= "OBD Response";
    protected float flow=0.f;
    public static String fuelType ="Gasoline";
    String commands;
    public float getFlow() {
        return flow;
    }

    public void setFlow(float flow) {
        this.flow = flow;
    }

    public FuelEconomyObdCommand(String fuelType_ext, String commands) {
        super("");
        fuelType=fuelType_ext;
        this.commands=commands;
    }

    @Override
    protected void performCalculations() {

    }

    @Override
    public void run(InputStream in, OutputStream out) throws IOException,
            InterruptedException {

        // get consumption liters per hour
        /*final FuelConsumptionRateObdCommand2 fuelConsumptionCommand = new FuelConsumptionRateObdCommand2();
        fuelConsumptionCommand.run(in, out);*/
        String[] parts=commands.split("\n");
        boolean supMaf= Boolean.parseBoolean(parts[0]);
        boolean supRate= Boolean.parseBoolean(parts[1]);

        float MAF=-1.0f;
        // get metric speed
        final SpeedCommand speedCommand = new SpeedCommand();
        speedCommand.run(in, out);
        float speed=speedCommand.getMetricSpeed();

        fuelType="Gasoline";

       System.err.println("STATE="+supMaf+" "+fuelType);


        //TODO: FIX MAF

        if(supRate){
            Log.d(TAG, "Fuel rate supported:");
            ConsumptionRateCommand rateCommand = new ConsumptionRateCommand();
            rateCommand.run(in, out);
            float fuelFlow=rateCommand.getLitersPerHour();
            setFlow(fuelFlow);
            kml=100/(speed/fuelFlow);
        }else if (supMaf){
            //get MAF
            final MassAirFlowCommand mafCommand= new MassAirFlowCommand();
            mafCommand.run(in, out);
            MAF= (float) mafCommand.getMAF();
            Log.d(TAG, "MAF: " + MAF);
            if (fuelType.equals("Diesel")){
                Log.d(TAG, "Diesel engine + MAF:");
                //get engine load
                final EngineLoadObdCommand loadObdCommand=new EngineLoadObdCommand();
                loadObdCommand.run(in, out);
                float engineLoad=loadObdCommand.getPercentage();

                // compute fuel flow L/h
                float fuelFlow=a*(MAF*engineLoad)+b;
                setFlow(fuelFlow);

                //compute fuel economy L/100km
                kml=100/(speed/fuelFlow);
            }else if(fuelType.equals("Gasoline")){
                Log.d(TAG, "Gasoline engine + MAF:");
                // get l/100km
                //float MPG= (float) (AIR_FUEL_RATIO*GASOLINE_DENSITY*GRAMS_POUND_CONVERSION*speed*KMH_MPH_CONSTANT)/(SECONDS_HOUR*MAF/*/100*/);
                // float MPG= (float) (710.7 * speedCommand.getMetricSpeed() / MAF);
                float fuelFlow=(float)(MAF*SECONDS_HOUR)/(GASOLINE_DENSITY2*AIR_FUEL_RATIO);
                setFlow(fuelFlow);
                kml=100/(speed/fuelFlow);
                float MPG= MPG_KML/kml;


            }}
       else if (!supMaf && fuelType.equals("Gasoline"))

       {
            Log.d(TAG, "Alternative MAF + Gasoline:");
            //get alternative MAF
            final RPMCommand engineRpmCommand = new RPMCommand();
            System.err.println("Here");
            engineRpmCommand.run(in, out);
            float RPM=(float)engineRpmCommand.getRPM();
            System.err.println(RPM);
            //get manifold pressure
            final IntakeManifoldPressureCommand pressureCommand = new IntakeManifoldPressureCommand();
            System.err.println(pressureCommand);
            pressureCommand.run(in, out);
            float MAP=(float)pressureCommand.getMetricUnit();
            //get intake temperature
            final AirIntakeTemperatureCommand temperatureCommand= new AirIntakeTemperatureCommand();
            temperatureCommand.run(in, out);
            float INTAKE_TEMP= temperatureCommand.getTemperature();
            MAF=RPM * (MAP / INTAKE_TEMP);
            // get l/100km

            float fuelFlow=(float)(MAF*SECONDS_HOUR)/(GASOLINE_DENSITY2*AIR_FUEL_RATIO);
            System.err.println(fuelFlow);

            setFlow(fuelFlow);
            kml=100/(speed/fuelFlow);
            float MPG= MPG_KML/kml;


        }


    }

    @Override
    public String getFormattedResult() {
        return useImperialUnits ? String.format("%.1f %s", getMilesPerUKGallon(),
                "mpg") : String.format("%.1f %s", kml, "l/100km");
    }

    @Override
    public String getCalculatedResult() {
        return  String.valueOf(kml);
    }

    /**
     * @return a float.
     */
    public float getLitersPer100Km() {
        return kml;
    }

    /**
     * @return a float.
     */
    public float getMilesPerUSGallon() {
        return 235.2f / kml;
    }

    /**
     * @return a float.
     */
    public float getMilesPerUKGallon() {
        return 282.5f / kml;
    }

    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_CONSUMPTION_RATE.getValue();
    }

}
