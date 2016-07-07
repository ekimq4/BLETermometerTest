package com.q4tech.bletermometertest.Model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ekim on 7/6/16.
 */
public class BleDevice {
    // Data
    private BluetoothDevice mBtDevice;
    private int mRssi;

    public BleDevice(BluetoothDevice device, int rssi) {
        mBtDevice = device;
        mRssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBtDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void updateRssi(int rssiValue) {
        mRssi = rssiValue;
    }

}
