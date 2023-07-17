package com.example.androidreadble;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class RemoteControl {

    // add this constant for Unix time
    private final static byte UNIX_TIME_COMMAND = 0x8;
    private final static byte START = 0x1;
    private final static byte HEARTBEAT = 0x2;
    private final static byte LED_COMMAND = 0x4;

    private final static byte VALUE_OFF = 0x0;
    private final static byte VALUE_ON = (byte)0xFF;

    private BLEController bleController;

    public RemoteControl(BLEController bleController) {
        this.bleController = bleController;
    }

    // add this method to get the current Unix time in seconds
    private long getCurrentUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    //private String getCurrentUnixTimeString() {
    //    long unixTime = System.currentTimeMillis() / 1000L;
    //    return String.valueOf(unixTime);
    //}

    private byte [] createControlWord(byte type, byte ... args) {
        byte [] command = new byte[args.length + 3];
        command[0] = START;
        command[1] = type;
        command[2] = (byte)args.length;
        for(int i=0; i<args.length; i++)
            command[i+3] = args[i];
        return command;

    }

    public void switchLED(boolean on) {
        this.bleController.sendData(createControlWord(LED_COMMAND, on?VALUE_ON:VALUE_OFF));
    }

    // add this method to send current Unix time via Bluetooth
   // public void sendUnixTime() {
     //   long unixTime = getCurrentUnixTime();
        //String unixTimeString = getCurrentUnixTimeString();
        //byte[] unixTimeBytes = ByteBuffer.allocate(Long.BYTES).putLong(unixTime).array();
        //byte[] unixTimeBytes = unixTimeString.getBytes(StandardCharsets.US_ASCII);
     //   byte[] unixTimeBytes = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array();
     //   this.bleController.sendData(createControlWord(UNIX_TIME_COMMAND, unixTimeBytes));
    //}

    public void sendUnixTime() {
        long unixTime = getCurrentUnixTime();
        byte[] unixTimeBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt((int) unixTime).array();
        this.bleController.sendData(createControlWord(UNIX_TIME_COMMAND, unixTimeBytes));
    }

}
