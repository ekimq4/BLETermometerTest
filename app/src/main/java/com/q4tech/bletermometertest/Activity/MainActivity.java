package com.q4tech.bletermometertest.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.q4tech.bletermometertest.Adapter.MainAdapter;
import com.q4tech.bletermometertest.Model.BleDevice;
import com.q4tech.bletermometertest.Model.BleDeviceInfo;
import com.q4tech.bletermometertest.Model.BleDeviceValues;
import com.q4tech.bletermometertest.R;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 5000;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothLeScannerCompat scanner;
    private ArrayList<BleDevice> mDeviceInfoList;
    private MainAdapter adapter;
    private ImageButton btnRefresh;
    private BleDeviceValues deviceValues;
    private BleDeviceInfo deviceInfo;
    private ArrayList<String> characteristicsToRead;

    private static final byte[] READ_TEMPERATURE_FALSE = {0x00};
    private static final byte[] READ_TEMPERATURE_TRUE = {0x01};
    private BluetoothGattService primaryService;
    private BluetoothGattService deviceInfoService;
    private static final String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private TextView textTemp;
    private TextView textCircuitTemp;
    private TextView textVoltage;
    private Spinner spinnerDevices;
    private TextView textSerialNum;
    private TextView textFirmwareV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();
        initializeUI();

        if (bluetoothDisabled()) {
            enableBluetooth();
        } else {
            getScannerAndScan();
        }
    }

    private void initializeUI() {
        textTemp = (TextView) findViewById(R.id.textTemp);
        textCircuitTemp = (TextView) findViewById(R.id.textCircuitTemp);
        textVoltage = (TextView) findViewById(R.id.textVoltage);
        textSerialNum = (TextView) findViewById(R.id.textSerialNum);
        textFirmwareV = (TextView) findViewById(R.id.textFirmwareV);
        spinnerDevices = (Spinner) findViewById(R.id.spinnerDevices);
        adapter = new MainAdapter(this, R.layout.adapter_main_list, mDeviceInfoList, getResources());
        spinnerDevices.setAdapter(adapter);

        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                if (position != 0) {
                    BleDevice deviceInfo = mDeviceInfoList.get(position);
                    connectToDevice(deviceInfo.getBluetoothDevice());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        btnRefresh = (ImageButton)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scanLeDevice(true);
            }
        });
    }

    private void initialize() {
        mHandler = new Handler();
        mDeviceInfoList = new ArrayList<BleDevice>();
        characteristicsToRead = new ArrayList<String>();
        clearDeviceInfoList();
        deviceValues = new BleDeviceValues(0, 0, 0);
        deviceInfo = new BleDeviceInfo();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private Boolean bluetoothDisabled() {
        return (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled());
    }

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void getScannerAndScan() {
        if (Build.VERSION.SDK_INT >= 21) {
            scanner = BluetoothLeScannerCompat.getScanner();
//                    settings = new ScanSettings.Builder()
//                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
//                            .setUseHardwareBatchingIfSupported(false).build();
//                    filters = new ArrayList<ScanFilter>();
        }
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            } else {
                getScannerAndScan();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (bluetoothDisabled()) {
                enableBluetooth();
            } else {
                disconnectGatt();
                spinnerDevices.setEnabled(false);
                btnRefresh.setEnabled(false);
                btnRefresh.setBackgroundResource(android.R.drawable.ic_delete);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanLeDevice(false);
                        adapter.notifyDataSetChanged();
                    }
                }, SCAN_PERIOD);
                clearDeviceInfoList();
                adapter.notifyDataSetChanged();
                scanner.startScan(mScanCallback);
                //scanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            spinnerDevices.setEnabled(true);
            btnRefresh.setEnabled(true);
            btnRefresh.setBackgroundResource(android.R.drawable.ic_input_add);
            scanner.stopScan(mScanCallback);
        }

    }

    private void clearDeviceInfoList() {
        mDeviceInfoList.clear();
        BleDevice deviceInfo = new BleDevice(null, 0);
        mDeviceInfoList.add(deviceInfo);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BleDevice deviceInfo = new BleDevice(result.getDevice(), result.getRssi());
            if (!deviceInfoExists(deviceInfo.getBluetoothDevice().getAddress())) {
                mDeviceInfoList.add(deviceInfo);
            } else {
                BleDevice foundDeviceInfo = findDeviceInfo(deviceInfo.getBluetoothDevice());
                if (foundDeviceInfo != null) foundDeviceInfo.updateRssi(deviceInfo.getRssi());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private boolean deviceInfoExists(String address) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice() != null && mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
                    .equals(address)) {
                return true;
            }
        }
        return false;
    }

    private BleDevice findDeviceInfo(BluetoothDevice device) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice() != null && mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
                    .equals(device.getAddress())) {
                return mDeviceInfoList.get(i);
            }
        }
        return null;
    }

    public void connectToDevice(BluetoothDevice device) {
        disconnectGatt();
        characteristicsToRead.clear();
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_SYSTEM_ID);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_MODEL_NUM);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_SERIAL_NUM);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_FIRMWARE_REV);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_HARDWARE_REV);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_SOFTWARE_REV);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_MANUFACTURER_NAME);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_REGULATORY_CERTIF);
        characteristicsToRead.add(BleDeviceInfo.UUID_CHARACTERISTIC_PNP_ID);
        mGatt = device.connectGatt(this, false, gattCallback);
    }

    private void readNextCharacteristic() {
        if (characteristicsToRead != null && characteristicsToRead.size() != 0) {
            mGatt.readCharacteristic(deviceInfoService.getCharacteristic(UUID.fromString(characteristicsToRead.get(0))));
        } else {
            refreshInfoUI();
            setReadTemp(true);
        }
    }

    private void characteristicRead() {
        characteristicsToRead.remove(0);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    mGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    mGatt = null;
                    deviceValues.setTemperature(0);
                    deviceValues.setCircuitTemp(0);
                    deviceValues.setVoltage(0);
                    deviceInfo.refreshValues();
                    refreshValuesUI();
                    refreshInfoUI();

                    Handler mainHandler = new Handler(getBaseContext().getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            spinnerDevices.setSelection(0);
                        }
                    };
                    mainHandler.post(runnable);

                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered", mGatt.getServices().toString());

            primaryService = mGatt.getService(UUID.fromString(BleDeviceValues.UUID_SERVICE_PRIMARY));
            deviceInfoService = mGatt.getService(UUID.fromString(BleDeviceInfo.UUID_SERVICE_DEVICE_INFO));

            if (primaryService != null && deviceInfoService != null) {
                readNextCharacteristic();
            } else {
                disconnectGatt();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            switch (characteristic.getUuid().toString().toUpperCase()) {
                case BleDeviceValues.UUID_CHARACTERISTIC_READ_VOLTAGE:
                    Log.e("onCharacteristicRead", String.valueOf(characteristic.getValue()[0]));
                    deviceValues.setVoltage(characteristic.getValue()[0]);
                    refreshValuesUI();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_SYSTEM_ID:
                    Log.e("onCharacteristicRead", String.valueOf(byteArrayToInt(characteristic.getValue())));
                    deviceInfo.setSystemId(String.valueOf(byteArrayToInt(characteristic.getValue())));
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_MODEL_NUM:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setModelNum(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_SERIAL_NUM:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setSerialNum(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_FIRMWARE_REV:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setFirmwareRev(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_HARDWARE_REV:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setHardwareRev(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_SOFTWARE_REV:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setSoftwareRev(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_MANUFACTURER_NAME:
                    try {
                        Log.e("onCharacteristicRead", new String(characteristic.getValue(), "UTF-8"));
                        deviceInfo.setManufacturerName(new String(characteristic.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_REGULATORY_CERTIF:
                    Log.e("onCharacteristicRead", String.valueOf(byteArrayToInt(characteristic.getValue())));
                    deviceInfo.setRegulatoryCertif(String.valueOf(byteArrayToInt(characteristic.getValue())));
                    characteristicRead();
                    readNextCharacteristic();
                    break;
                case BleDeviceInfo.UUID_CHARACTERISTIC_PNP_ID:
                    Log.e("onCharacteristicRead", String.valueOf(byteArrayToInt(characteristic.getValue())));
                    deviceInfo.setPnpId(String.valueOf(byteArrayToInt(characteristic.getValue())));
                    characteristicRead();
                    readNextCharacteristic();
                    break;
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", characteristic.toString());

            if (characteristic.getUuid().equals(UUID.fromString(BleDeviceValues.UUID_CHARACTERISTIC_SET_READ))
                    && Arrays.equals(characteristic.getValue(), READ_TEMPERATURE_TRUE)) {
                BluetoothGattCharacteristic c = primaryService.getCharacteristic(UUID.fromString(BleDeviceValues.UUID_CHARACTERISTIC_READ_TEMP));
                mGatt.setCharacteristicNotification(c, true);
                BluetoothGattDescriptor descriptor = c.getDescriptor(UUID.fromString(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("onCharacteristicChanged", characteristic.toString());

            if (characteristic.getUuid().equals(UUID.fromString(BleDeviceValues.UUID_CHARACTERISTIC_READ_TEMP))) {
                byte[] value = characteristic.getValue();
                Log.e("onCharacteristicChanged", Arrays.toString(value));
                deviceValues.setTemperature(value[1]);
                deviceValues.setCircuitTemp(value[3]);
                refreshValuesUI();
                mGatt.readCharacteristic(primaryService.getCharacteristic(UUID.fromString(BleDeviceValues.UUID_CHARACTERISTIC_READ_VOLTAGE)));
            }
        }

    };

    private void disconnectGatt() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
        }
    }

    private void refreshValuesUI() {
        Handler mainHandler = new Handler(getBaseContext().getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textTemp.setText(String.valueOf(deviceValues.getTemperature()));
                textCircuitTemp.setText(String.valueOf(deviceValues.getCircuitTemp() + " °C"));
                textVoltage.setText(String.valueOf(deviceValues.getVoltage() + "V"));
            }
        };
        mainHandler.post(runnable);
    }
    private void refreshInfoUI() {
        Handler mainHandler = new Handler(getBaseContext().getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textSerialNum.setText(deviceInfo.getSerialNum());
                textFirmwareV.setText(deviceInfo.getFirmwareRev());
            }
        };
        mainHandler.post(runnable);
    }

    public static int byteArrayToInt(byte[] b) {
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        return wrapped.getInt();
    }


    private void setReadTemp(Boolean read) {
        BluetoothGattCharacteristic c = primaryService.getCharacteristic(UUID.fromString(BleDeviceValues.UUID_CHARACTERISTIC_SET_READ));
        c.setValue(read ? READ_TEMPERATURE_TRUE : READ_TEMPERATURE_FALSE);
        mGatt.writeCharacteristic(c);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
