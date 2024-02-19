package com.citisend.citiwastelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class CitiConnect {

    private static final String TAG = "CitiConnect";
    private final Handler handler;
    Activity activity;
    private BeaconManager beaconManager;
    private Region region;
    private int timer_lock_opened = 3;
    private int timer_door_opened = 10;
    private final BeaconTransmitter beaconTransmitter;
    private boolean isMonitoring = false;
    private Beacon beacon;
    private boolean isOpenedLock = false;
    private boolean isOpenedDoor = false;
    private boolean discoverd = false;
    Beacon beaconSelected;

    public CitiConnect(Activity activity, @Nullable Integer timer_lock_opened, @Nullable Integer timer_door_opened) {
        this.activity = activity;
        if (timer_lock_opened != null)
            this.timer_lock_opened = timer_lock_opened;
        if (timer_door_opened != null)
            this.timer_door_opened = timer_door_opened;
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(this.activity, beaconParser);
        handler = new Handler(Looper.getMainLooper());
    }

    public interface OnDiscoverWaste {
        void onDiscover(String name, int state);
    }

    public interface OnErrorWaste {
        void onError(int error);
    }

    private boolean grantedPermissions() {
        return this.activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    public void discover(OnDiscoverWaste onDiscoverWaste, OnErrorWaste onErrorWaste) {
        destroy();
        isMonitoring = true;
        beaconManager = BeaconManager.getInstanceForApplication(this.activity);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.addRangeNotifier((beacons, region) -> {
            for (Beacon beacon : beacons) {
                String name = beacon.getId1().toHexString().substring(2, 8);
                if (name.equals("434954")) {
                    boolean continueFirstTime = false;
                    if (beaconSelected == null) {
                        continueFirstTime = true;
                    } else {
                        if (!Objects.equals(beacon.getBluetoothAddress(), beaconSelected.getBluetoothAddress())) {
                            return;
                        }
                    }

                    String binary = Utils.HexToBinary(beacon.getId1().toHexString().
                            substring(beacon.getId1().toHexString().length() - 2));

                    Log.i(TAG, "BEACONS" + beacons.size());
                    Log.i(TAG, "Binario" + binary);

                    if (continueFirstTime) {
                        if (binary.charAt(1) == 49) {
                            if (binary.charAt(2) == 49) {
                                beaconSelected = beacon;
                                isOpenedLock = false;
                                handler.postDelayed(() -> {
                                    if (!isOpenedLock) {
                                        onErrorWaste.onError(Error.TIME_OUT_LOCK);
                                        this.destroy();
                                    }
                                }, timer_lock_opened * 1000L);
                                this.sendOpenSignal();
                                onDiscoverWaste.onDiscover(name, State.EVENT_LOCK_CLOSED_PRESENCE);
                                return;
                            }
                        }
                    } else {
                        if (binary.charAt(0) == 49) {
                            this.closeOpenSignal();
                            isOpenedLock = true;
                            isOpenedDoor = false;
                            handler.postDelayed(() -> {
                                if (!isOpenedDoor) {
                                    onErrorWaste.onError(Error.TIME_OUT_DOOR);
                                    this.destroy();
                                }
                            }, timer_door_opened * 1000L);
                            onDiscoverWaste.onDiscover(name, State.EVENT_LOCK_OPENED);
                            return;
                        }
                        if (binary.charAt(1) == 49) {
                            if (binary.charAt(3) == 49) {
                                isOpenedDoor = true;
                                onDiscoverWaste.onDiscover(name, State.EVENT_LOCK_CLOSED_DOOR_OPENED);
                                beaconSelected = null;
                                this.destroy();
                            }
                        }
                    }
                }
            }
        });
        region = new Region("com.citisend.citiconectregion", null, null, null);
        beaconManager.startRangingBeacons(region);
    }

    public void destroy() {
        beaconSelected = null;
        handler.removeCallbacksAndMessages(null);
        if (beaconManager != null) {
            beaconManager.stopRangingBeacons(region);
            beaconManager = null;
            isMonitoring = false;
        }
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
        }
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public void sendOpenSignal() {
        if (!grantedPermissions()) {
            Log.d("CITICONNECT", "Nor permissions granted");
            return;
        }
        if (beaconTransmitter.isStarted()) return;

        beacon = new Beacon.Builder()
                .setId1("434d4400-1008-0000-0000-000000000100")
                .setId2("98")
                .setId3("42478")
                .setManufacturer(0x65535)
                .setTxPower(-69)
                .setDataFields(Arrays.asList(0L))
                .build();
        beaconTransmitter.startAdvertising(beacon);
    }

    public void simulateWaste() {
        if (!grantedPermissions()) {
            Log.d("CITICONNECT", "Nor permissions granted");
            return;
        }
        if (beaconTransmitter.isStarted()) return;

        beacon = new Beacon.Builder()
                .setId1("43495400-1008-0000-0000-000000000100")
                .setId2("98")
                .setId3("42478")
                .setManufacturer(0x65535)
                .setTxPower(-69)
                .setDataFields(Arrays.asList(0L))
                .build();
        beaconTransmitter.startAdvertising(beacon);
    }

    public void closeOpenSignal() {
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
        }
    }
}
