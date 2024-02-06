package com.citisend.citiwastelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class CitiConnect {

    Activity activity;

    public CitiConnect(Activity activity) {
        this.activity = activity;
    }

    private boolean grantedPermissions() {
        if (this.activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (this.activity.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return false;

            } else {

                return true;

            }
        } else {
            return false;
        }
    }

    public void createBeacon() {
        if (!grantedPermissions()) {
            Log.d("CITICONNECT", "Nor permissions granted");
            return;
        }
        Beacon beacon = new Beacon.Builder()
                .setId1("434d4400-1008-0000-0000-000000000100")
                .setId2("98")
                .setId3("42478")
                .setManufacturer(0x65535)
                .setTxPower(-69)
                .setDataFields(Arrays.asList(new Long[]{0l}))
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");

        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(this.activity, beaconParser);
        beaconTransmitter.startAdvertising(beacon);
    }

}
