package com.q4tech.bletermometertest.Model;

/**
 * Created by ekim on 7/7/16.
 */
public class BleDeviceValues {

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
