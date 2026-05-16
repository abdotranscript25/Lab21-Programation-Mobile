package ensa.ma.sensors.utils;

import android.hardware.Sensor;

public class SensorFormatter {

    public static String format(Sensor sensor) {
        return "Id : " + sensor.getId() + "\n"
                + "Name : " + sensor.getName() + "\n"
                + "Vendor : " + sensor.getVendor() + "\n"
                + "Version : " + sensor.getVersion() + "\n"
                + "Type : " + sensor.getStringType() + "\n"
                + "Int Type : " + sensor.getType() + "\n"
                + "Resolution : " + sensor.getResolution() + "\n"
                + "Power : " + sensor.getPower() + " mA\n"
                + "Maximum Range : " + sensor.getMaximumRange() + "\n"
                + "Min Delay : " + sensor.getMinDelay() + " µs\n";
    }
}