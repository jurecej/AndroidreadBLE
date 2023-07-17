package com.example.androidreadble;


public interface BLEControllerListener {

    public void BLEControllerConnected();
    public void BLEControllerDisconnected();
    public void BLEDeviceFound(String name, String address);
    public void showdata(int[] data);
}