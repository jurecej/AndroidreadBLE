package com.example.androidreadble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEController extends MainActivity{
    private static BLEController instance;

    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private BluetoothManager bluetoothManager;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic btGattChar = null;

    private ArrayList<BLEControllerListener> listeners = new ArrayList<>();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();



    public BLEController(Context ctx) {
        this.bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
    }


    public static BLEController getInstance(Context ctx) {
        if (null == instance)
            instance = new BLEController((ctx));

        return instance;
    }

    public void addBLEControllerListener(BLEControllerListener l) {
        if (!this.listeners.contains(l))
            this.listeners.add(l);
    }

    public void removeBLEControllerListener(BLEControllerListener l) {
        this.listeners.remove(l);
    }

    @SuppressLint("MissingPermission")
    public void init() {
        this.devices.clear();
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        scanner.startScan(bleCallback);
    }

    private void fireDisconnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerDisconnected();

        this.device = null;
    }

    private void fireConnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerConnected();
    }


    @SuppressLint("MissingPermission")
    private void fireDeviceFound(BluetoothDevice device) {
        for (BLEControllerListener l : this.listeners)
            l.BLEDeviceFound(device.getName().trim(), device.getAddress());
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                deviceFound(device);
            }
        }

        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                BluetoothDevice device = sr.getDevice();
                if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                    deviceFound(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("[BLE]", "scan failed with errorcode: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    private boolean isThisTheDevice(BluetoothDevice device) {
        return null != device.getName() && device.getName().startsWith("BT05");
    }

    private void deviceFound(BluetoothDevice device) {
        this.devices.put(device.getAddress(), device);
        fireDeviceFound(device);
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(String address) {
        this.device = this.devices.get(address);
        this.scanner.stopScan(this.bleCallback);
        Log.i("[BLE]", "connect to device " + device.getAddress());
        this.bluetoothGatt = device.connectGatt(null, false, this.bleConnectCallback);
    }



    final BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                btGattChar = null;
                fireDisconnected();
            }
        }


        @SuppressLint("MissingPermission")
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic bgc) {

            super.onCharacteristicChanged(gatt, bgc);
;
            byte[] newValue = bgc.getValue();

            if (newValue != null && newValue.length > 0) {
                //Log.i("byte", String.valueOf((newValue)));
                final StringBuilder stringBuilder = new StringBuilder(newValue.length);
                for (byte byteChar : newValue)
                    stringBuilder.append(String.format("%02X", byteChar));

                Log.i("[BLE]", (stringBuilder.toString()));

                StringBuilder output = new StringBuilder("");


                /*for (int i = 0; i < stringBuilder.toString().length(); i += 2) {
                    String str = stringBuilder.substring(i, i + 2);
                    output.append((char) Integer.parseInt(str, 16));
                }*/

                for (int i = 0; i < 8; i += 2) {
                    String str1 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str1); // Insert at the beginning to maintain the order

                }

                int value1 = Integer.parseInt(output.toString(), 16);

                output = new StringBuilder("");

                for (int i = 8; i < 12; i += 2) {
                    String str2 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str2); // Insert at the beginning to maintain the order

                }

                int value2 = Integer.parseInt(output.toString(), 16);


                output = new StringBuilder("");

                for (int i = 12; i < 16; i += 2) {
                    String str3 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str3); // Insert at the beginning to maintain the order

                }

                int value3 = Integer.parseInt(output.toString(), 16);

                output = new StringBuilder("");

                for (int i = 16; i < 20; i += 2) {
                    String str4 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str4); // Insert at the beginning to maintain the order

                }

                int value4 = Integer.parseInt(output.toString(), 16);

                int[] valuearray;

                valuearray = new int[4];

                valuearray[0] = value1;
                valuearray[1] = value2;
                valuearray[2] = value3;
                valuearray[3] = value4;

                Log.i("[BLE]", String.valueOf(value1));
                Log.i("[BLE]", "Decimal value: " + value1);

                for (BLEControllerListener l : listeners)
                    l.showdata(valuearray);


            }

        }



        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (null == btGattChar) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().toUpperCase().startsWith("0000FFE0")) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic bgc : gattCharacteristics) {
                            Log.i("[BLE]", bgc.getUuid().toString());
                            if (bgc.getUuid().toString().toUpperCase().startsWith("0000FFE1")) {
                                int chprop = bgc.getProperties();
                                if ((chprop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    btGattChar = bgc;
                                    gatt.setCharacteristicNotification(bgc, true);
                                    BluetoothGattDescriptor descriptor = bgc.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor);

                                    Log.i("[BLE]", "CONNECTED and ready to read");
                                    fireConnected();
                                }
                            }
                        }
                    }
                }
            }

        }


    };




    @SuppressLint("MissingPermission")
    public void sendData(byte [] data) {
        this.btGattChar.setValue(data);
        bluetoothGatt.writeCharacteristic(this.btGattChar);
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        this.bluetoothGatt.disconnect();
    }

}
