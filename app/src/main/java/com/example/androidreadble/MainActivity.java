package com.example.androidreadble;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import static java.lang.Long.valueOf;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BLEControllerListener {
    private TextView logView;
    private TextView activityText1;
    private TextView activityText2;
    private TextView activityText3;
    private Button connectButton;
    private Button disconnectButton;
    private Button switchLEDButton;


    private ProgressBar progressBar;

    private BLEController bleController;
    private String deviceAddress;

    private RemoteControl remoteControl;


    //private double BLEfullday = 0.00;


    static ArrayList<Float> data_list = new ArrayList<Float>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        this.bleController = BLEController.getInstance(this);
        this.remoteControl = new RemoteControl(this.bleController);

        this.logView = findViewById(R.id.logView);
        this.logView.setMovementMethod(new ScrollingMovementMethod());


        this.activityText1 = findViewById(R.id.activitytext1);
        this.activityText2 = findViewById(R.id.activitytext2);
        this.activityText3 = findViewById(R.id.activitytext3);

        this.progressBar = findViewById(R.id.progressBar);

        initConnectButton();
        initDisconnectButton();

        checkBLESupport();
        checkPermissions();

        disableButtons();

        initSwitchLEDButton();



    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION},
                    42);
        }
    }

    private void checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        42);
                return;
            }
            startActivityForResult(enableBTIntent, 1);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //log("[BLE]\tSearching for Bluetooth device...");
            this.bleController.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.bleController.removeBLEControllerListener(this);

    }

    public void log(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.setText(logView.getText() + "\n" + text);
            }
        });
    }


    public void logdata(final int[] arrOfStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //value BLEvalue is the value which is converted from the string "text"
                //"text" represents the data receivet from the device via BLE (Bluetooth Low Energy)
                //String BLEvalue = text;
                //String[] arrOfStr = BLEvalue.split("\t");


                if (arrOfStr.length > 1) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Long unixtime = valueOf(arrOfStr[0])* 1000;
                    activityText2.setText(dateFormat.format(unixtime));

                    DecimalFormat dfrmt = new DecimalFormat("#.#");
                    double VUV =0.05*Float.valueOf(arrOfStr[1]).floatValue()-0.0;
                    activityText1.setText(dfrmt.format(VUV));

                    //Double batteryLevel = (100/3.8)*3.8*Float.valueOf(arrOfStr[3]).floatValue()/1023;
                    //activityText2.setText(dfrmt.format(batteryLevel));

                    double Vout = 3.3*((Float.valueOf(arrOfStr[2]).floatValue())/1023.0);
                    Double Rout=(10000*Vout/(3.3-Vout));
                    Double TempC=(3600/Math.log(Rout/0.057)-272.15);
                    activityText3.setText(dfrmt.format(Vout));
                    System.out.println(dfrmt.format(VUV));
                } else {
                    // Handle the case when the split doesn't occur as expected
                    //System.out.println("Unexpected format: " + BLEvalue);
                    System.out.println("Unexpected format: ");
                }
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                //Long unixtime = valueOf(arrOfStr[0])* 1000;
                //activityText2.setText(dateFormat.format(unixtime));

                //DecimalFormat dfrmt = new DecimalFormat("#.#");
                //double VUV =0.05*Float.valueOf(arrOfStr[1]).floatValue()-0.0;
                //activityText1.setText(dfrmt.format(VUV));

                //Double batteryLevel = (100/3.8)*3.8*Float.valueOf(arrOfStr[3]).floatValue()/1023;
                //activityText2.setText(dfrmt.format(batteryLevel));

                //double Vout = 3.3*((Float.valueOf(arrOfStr[2]).floatValue())/1023.0);
                //Double Rout=(10000*Vout/(3.3-Vout));
                //Double TempC=(3600/Math.log(Rout/0.057)-272.15);
                //activityText3.setText(dfrmt.format(Vout));

                //Calendar calendar = Calendar.getInstance();
                //Calendar calendar = arrOfStr[3];
                //SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
                //String currentTime = timeFormat.format(calendar.getTime());
                //activityText2.setText(currentTime);

                //String calendar = arrOfStr[3];
                //activityText2.setText(calendar);


                //System.out.println(arrOfStr[1]);

            }
        });
    }


    public double sum()
    {
        double sum = 0;
        for(int i = 0; i < data_list.size(); i++)
        {
            sum = (sum + data_list.get(i));
        }
        return sum;
    }

    @Override
    public void BLEControllerConnected() {
        //log("[BLE]\tConnected");
        runOnUiThread(() -> {
            disconnectButton.setEnabled(true);
        });
    }

    @Override
    public void BLEControllerDisconnected() {
        //log("[BLE]\tDisconnected");
        runOnUiThread(() -> {
            connectButton.setEnabled(true);
        });
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        //log("Device " + name + " found with address " + address);
        this.deviceAddress = address;
        this.connectButton.setEnabled(true);
        System.out.println("Device " + name + " found with address " + address);
    }


    public void showdata(int[] data) {
        logdata(data);
    }

    private void initConnectButton() {
        this.connectButton = findViewById(R.id.connectButton);
        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButton.setEnabled(false);
                //log("Connecting...");
                bleController.connectToDevice(deviceAddress);
                // Add delay before sending Unix time data
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        remoteControl.sendUnixTime();
                    }
                }, 1000);

            }
        });
    }

    private void initDisconnectButton() {
        this.disconnectButton = findViewById(R.id.disconnectButton);
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remoteControl.sendUnixTime();
                disconnectButton.setEnabled(false);
                //log("Disconnecting...");
                bleController.disconnect();
            }
        });

    }

    private void disableButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(false);
            }
        });
    }

    private void initSwitchLEDButton() {
        this.switchLEDButton = findViewById(R.id.switchButton);
        this.switchLEDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remoteControl.sendUnixTime();
                System.out.println(System.currentTimeMillis() / 1000L);
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }



}
