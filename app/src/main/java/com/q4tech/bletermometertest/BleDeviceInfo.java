package com.q4tech.bletermometertest;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ekim on 7/6/16.
 */
public class BleDeviceInfo {
    // Data
    private BluetoothDevice mBtDevice;
    private int mRssi;

    public BleDeviceInfo(BluetoothDevice device, int rssi) {
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
