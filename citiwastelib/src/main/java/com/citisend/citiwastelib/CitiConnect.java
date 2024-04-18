package com.citisend.citiwastelib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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
    private String id2Signal;
    private String id3Signal;
    private String project_id;
    private String user_id;

    public CitiConnect(Activity activity, int project_id, int user_id, @Nullable Integer timer_lock_opened, @Nullable Integer timer_door_opened) {
        this.activity = activity;
        if (timer_lock_opened != null)
            this.timer_lock_opened = timer_lock_opened;
        if (timer_door_opened != null)
            this.timer_door_opened = timer_door_opened;
        this.project_id = Integer.toHexString(project_id);
        String value = String.format("%8s", Integer.toHexString(user_id)).replace(' ', '0');
        this.user_id = value.substring(0, 4) + "-" + value.substring(4, value.length());
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
                        id2Signal = String.valueOf(beacon.getId2().toInt());
                        id3Signal = String.valueOf(beacon.getId3().toInt());
                    } else {
                        if (!Objects.equals(beacon.getBluetoothAddress(), beaconSelected.getBluetoothAddress())) {
                            return;
                        }
                    }

                    String binary = Utils.HexToBinary(beacon.getId1().toHexString().
                            substring(beacon.getId1().toHexString().length() - 2));
                    Log.i(TAG, "BEACON FOUND id1: " + beacon.getId1());
                    Log.i(TAG, "BEACON FOUND test lib id2: " + beacon.getId2());
                    Log.i(TAG, "BEACON FOUND test lib id3: " + beacon.getId3());
                    Log.i(TAG, "BEACON FOUND size " + beacons.size());
                    Log.i(TAG, "BEACON FOUND Binario ev1 " + binary);
                    Log.i(TAG, "BEACON FOUND Binario ev1 hex " + beacon.getId1().toHexString().
                            substring(beacon.getId1().toHexString().length() - 2));

                    if (!String.valueOf(beacon.getId2().toInt()).equalsIgnoreCase(id2Signal)) {
                        Log.i(TAG, "BEACON EV2 MESSAGE: " + beacon.getId2().toHexString());
                        String binary_ev2 = Utils.HexToBinary(beacon.getId2().toHexString().
                                substring(2, 4));
                        Log.i(TAG, "BEACON EV2 BINARIO: " + binary_ev2);
                        switch (binary_ev2.substring(0, 2)) {
                            case "00":
                                Toast.makeText(activity, "No event", Toast.LENGTH_SHORT).show();
                                break;
                            case "01":
                                Toast.makeText(activity, "Identification suceeded", Toast.LENGTH_SHORT).show();
                                break;
                            case "10":
                                Toast.makeText(activity, "Id rejected, acces politics or device mode", Toast.LENGTH_SHORT).show();
                                break;
                            case "11":
                                Toast.makeText(activity, "Id rejected, wrong project", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }

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
                            if (binary.charAt(3) == 49) {
                                isOpenedDoor = true;
                                onDiscoverWaste.onDiscover(name, State.EVENT_LOCK_CLOSED_DOOR_OPENED);
                                //beaconSelected = null;
                                //this.destroy();
                                return;
                            }
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

    private void sendOpenSignal() {
        if (!grantedPermissions()) {
            Log.d("CITICONNECT", "Nor permissions granted");
            return;
        }
        if (beaconTransmitter.isStarted()) return;
              //  .setId1("43495400-1008-0000-0000-000000000100")

        beacon = new Beacon.Builder()
                .setId1("434d4400-1008-"+ this.project_id +"-" + this.user_id + "00000001")
                .setId2(id2Signal)
                .setId3(id3Signal)
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
                .setId1("43495400-0008-2836-0000-000000005160")
                .setId2("79")
                .setId3("55714")
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
