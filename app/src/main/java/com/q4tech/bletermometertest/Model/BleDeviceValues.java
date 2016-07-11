package com.q4tech.bletermometertest.Model;

/**
 * Created by ekim on 7/7/16.
 */
public class BleDeviceValues {

    public static final String UUID_SERVICE_PRIMARY = "F000AA00-0451-4000-B000-000000000000";
    public static final String UUID_CHARACTERISTIC_READ_TEMP = "F000AA01-0451-4000-B000-000000000000";
    public static final String UUID_CHARACTERISTIC_SET_READ = "F000AA02-0451-4000-B000-000000000000";
    public static final String UUID_CHARACTERISTIC_READ_VOLTAGE = "F000AA03-0451-4000-B000-000000000000";

    private int temperature;
    private int circuitTemp;
    private int voltage;

    public BleDeviceValues(int temperature, int circuitTemp, int voltage) {
        this.temperature = temperature;
        this.circuitTemp = circuitTemp;
        this.voltage = voltage;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getCircuitTemp() {
        return this.circuitTemp;
    }

    public void setCircuitTemp(int circuitTemp) {
        this.circuitTemp = circuitTemp;
    }

    public int getVoltage() {
        return this.voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

}
