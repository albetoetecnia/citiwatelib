package com.citisend.citiwastelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.Collection;

public class CitiConnect {

    private static final String TAG = "CitiConnect";

    Activity activity;
    private BeaconManager beaconManager;
    private Region region;

    public CitiConnect(Activity activity) {
        this.activity = activity;
    }


    public interface OnDiscoverWaste {
        void onDiscover(String response);
    }

    private boolean grantedPermissions() {
        if (this.activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void discover(OnDiscoverWaste onDiscoverWaste) {
        beaconManager = BeaconManager.getInstanceForApplication(this.activity);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.addRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {
                Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                Log.i(TAG, "UUID" + beacons.iterator().next().getId1() + " meters away.");
                onDiscoverWaste.onDiscover(beacons.iterator().next().getBluetoothAddress());
            }
        });
        region = new Region("myRegion", null, null, null);
        beaconManager.startRangingBeacons(region);
    }

    public void destroy() {
        if (beaconManager != null) {
            beaconManager.stopMonitoring(region);
            beaconManager.stopRangingBeacons(region);
            beaconManager = null;
        }
    }

    public void sendOpenSignal() {
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
