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
import com.q4tech.bletermometertest.Model.BleDeviceValues;
import com.q4tech.bletermometertest.R;

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
    private static final long SCAN_PERIOD = 10000;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothLeScannerCompat scanner;
    private ArrayList<BleDevice> mDeviceInfoList;
    private MainAdapter adapter;
    private ImageButton btnRefresh;
    private BleDeviceValues deviceValues;

    private static byte[] READ_TEMPERATURE_FALSE = {0x00};
    private static byte[] READ_TEMPERATURE_TRUE = {0x01};
    private BluetoothGattService primaryService;
    private BluetoothGattService deviceInfoService;
    private static String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static String UUID_PRIMARY_SERVICE = "F000AA00-0451-4000-B000-000000000000";
    private static String UUID_DEVICE_INFO_SERVICE = "0000180A-0000-1000-8000-00805F9B34FB";
    private static String UUID_CHARACTERISTIC_READ_TEMP = "F000AA01-0451-4000-B000-000000000000";
    private static String UUID_CHARACTERISTIC_SET_READ = "F000AA02-0451-4000-B000-000000000000";
    private static String UUID_CHARACTERISTIC_READ_VOLTAGE = "F000AA03-0451-4000-B000-000000000000";

    private TextView textTemp;
    private TextView textCircuitTemp;
    private TextView textVoltage;
    private Spinner spinnerDevices;

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
        clearDeviceInfoList();
        deviceValues = new BleDeviceValues(0, 0, 0);

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
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
        }
        mGatt = device.connectGatt(this, false, gattCallback);
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
                    mGatt.disconnect();
                    mGatt = null;
                    deviceValues.setTemperature(0);
                    deviceValues.setCircuitTemp(0);
                    deviceValues.setVoltage(0);
                    refreshValuesUI();

                    Handler mainHandler = new Handler(getBaseContext().getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            spinnerDevices.setSelection(0);
                        }
                    };
                    mainHandler.post(myRunnable);

                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered", mGatt.getServices().toString());

            primaryService = mGatt.getService(UUID.fromString(UUID_PRIMARY_SERVICE));
            deviceInfoService = mGatt.getService(UUID.fromString(UUID_DEVICE_INFO_SERVICE));

            if (primaryService != null && deviceInfoService != null) {
                setReadTemp(true);
            } else {
                mGatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_READ_VOLTAGE))) {
                byte[] value = characteristic.getValue();
                Log.e("onCharacteristicRead", String.valueOf(byteArrayToInt(value)));
                deviceValues.setVoltage(byteArrayToInt(value));
                refreshValuesUI();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", characteristic.toString());

            if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_SET_READ))
                    && Arrays.equals(characteristic.getValue(), READ_TEMPERATURE_TRUE)) {
                BluetoothGattCharacteristic c = primaryService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_READ_TEMP));
                mGatt.setCharacteristicNotification(c, true);
                BluetoothGattDescriptor descriptor = c.getDescriptor(UUID.fromString(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("onCharacteristicChanged", characteristic.toString());

            if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_READ_TEMP))) {
                byte[] value = characteristic.getValue();
                Log.e("onCharacteristicChanged", Arrays.toString(value));
                deviceValues.setTemperature(value[1]);
                deviceValues.setCircuitTemp(value[3]);
                refreshValuesUI();
                mGatt.readCharacteristic(primaryService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_READ_VOLTAGE)));
            }
        }

    };

    private void refreshValuesUI() {
        Handler mainHandler = new Handler(getBaseContext().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                textTemp.setText(String.valueOf(deviceValues.getTemperature()));
                textCircuitTemp.setText(String.valueOf(deviceValues.getCircuitTemp()));
                textVoltage.setText(String.valueOf(deviceValues.getVoltage()));
            }
        };
        mainHandler.post(myRunnable);
    }

    public static int byteArrayToInt(byte[] b) {
        if (b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                    | (b[3] & 0xff);
        else if (b.length == 2)
            return (b[0] & 0xff) << 8 | (b[1] & 0xff);

        return 0;
    }

    private void setReadTemp(Boolean read) {
        BluetoothGattCharacteristic c = primaryService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_SET_READ));
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
