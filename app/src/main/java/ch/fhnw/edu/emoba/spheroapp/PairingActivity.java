package ch.fhnw.edu.emoba.spheroapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import ch.fhnw.edu.emoba.spherolib.SpheroRobotDiscoveryListener;
import ch.fhnw.edu.emoba.spherolib.SpheroRobotFactory;
import ch.fhnw.edu.emoba.spherolib.SpheroRobotProxy;

import static ch.fhnw.edu.emoba.spherolib.SpheroRobotDiscoveryListener.SpheroRobotBluetoothNotification.Online;

public class PairingActivity extends AppCompatActivity implements SpheroRobotDiscoveryListener {

    public static final boolean DEBUG = false;

    private ProgressDialog progress;
    private TextView infoText;
    private SpheroRobotProxy spheroRobotProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        infoText = (TextView) findViewById(R.id.messageText);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            progress = ProgressDialog.show(this, "Pairing", "Wait while pairing...");
            progress.setCancelable(true);
            Log.d("Sphero.Main", "requesting bluetooth access");
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog alert = builder.setTitle("Bluetooth not enabled!").setMessage("Bluetooth must be enabled!").create();
            alert.show();
            infoText.setText("Can't use Bluetooth");
            return;
        }
        spheroRobotProxy = SpheroRobotFactory.createRobot(DEBUG);
        spheroRobotProxy.setDiscoveryListener(this);
        spheroRobotProxy.startDiscovering(getApplicationContext());
        if (DEBUG) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    showMessageAndSwitchToMain();
                }
            }, 3000);
        }
    }

    public void showMessageAndSwitchToMain() {
        progress.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(PairingActivity.this);
        AlertDialog alert = builder.setTitle("Successfully Connected").setMessage("The connection to the robot was established.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(PairingActivity.this, MainActivity.class));
                    }
                }).create();
        alert.show();
    }

    @Override
    public void handleRobotChangedState(final SpheroRobotBluetoothNotification spheroRobotBluetoothNotification) {
        Log.d("Sphero.Main", "robostate changed " + spheroRobotBluetoothNotification.name());
        if (spheroRobotBluetoothNotification.equals(Online)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessageAndSwitchToMain();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoText.setText(spheroRobotBluetoothNotification.name());
                }
            });
        }
    }
}
