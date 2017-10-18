package com.ksyun.media.streamer.demo;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

/**
 * This is a utility to detect bluetooth headset connection and establish audio connection.
 */
public class BluetoothHeadsetUtils {
    private static final String TAG = "BluetoothHeadsetUtils";

    private Context mContext;
    private AudioManager mAudioManager;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHeadset mBluetoothHeadset;

    private boolean mIsOnHeadsetSco;
    private boolean mIsStarted;

    /**
     * Constructor
     *
     * @param context context
     */
    public BluetoothHeadsetUtils(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Call this to start BluetoothHeadsetUtils functionalities.
     */
    public void start() {
        if (mIsStarted) {
            return;
        }
        if (mBluetoothAdapter == null || !mAudioManager.isBluetoothScoAvailableOffCall()) {
            return;
        }

        mContext.registerReceiver(mHeadsetBroadcastReceiver,
                new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
        mContext.registerReceiver(mHeadsetBroadcastReceiver,
                new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener,
                    BluetoothProfile.HEADSET);
        }
        mIsStarted = true;
    }

    /**
     * Should call this on onResume or onDestroy.
     * Unregister broadcast receivers and stop Sco audio connection
     */
    public void stop() {
        if (!mIsStarted) {
            return;
        }

        if (mBluetoothHeadset != null) {
            stopBluetoothSco();
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            mBluetoothHeadset = null;
        }
        mContext.unregisterReceiver(mHeadsetBroadcastReceiver);
        mIsStarted = false;
    }

    /**
     * @return true if audio is connected through headset.
     */
    public boolean isOnHeadsetSco() {
        return mIsOnHeadsetSco;
    }

    private void startBluetoothSco() {
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.startBluetoothSco();
        mAudioManager.setBluetoothScoOn(true);
    }

    private void stopBluetoothSco() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setBluetoothScoOn(false);
    }

    /**
     * Check for already connected headset and if so start audio connection.
     * Register for broadcast of headset and Sco audio connection states.
     */
    private BluetoothProfile.ServiceListener mHeadsetProfileListener =
            new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG, "Profile listener onServiceDisconnected");
            if (mBluetoothHeadset != null) {
                stopBluetoothSco();
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG, "Profile listener onServiceConnected");

            // mBluetoothHeadset is just a headset profile,
            // it does not represent a headset device.
            mBluetoothHeadset = (BluetoothHeadset) proxy;

            // If a headset is connected before this application starts,
            // ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
            // So we need to check for already connected headset.
            List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
            if (devices.size() > 0) {
                startBluetoothSco();
            }
        }
    };

    /**
     * Handle headset and Sco audio connection states.
     */
    private BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver() {

        @SuppressWarnings("synthetic-access")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state;
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_DISCONNECTED);
                Log.d(TAG, "Action = " + action + " State = " + state);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    Log.d(TAG, "Headset connected");
                    if (mBluetoothHeadset != null) {
                        startBluetoothSco();
                    } else {
                        mBluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener,
                                BluetoothProfile.HEADSET);
                    }
                } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    Log.d(TAG, "Headset disconnected");
                    if (mBluetoothHeadset != null) {
                        stopBluetoothSco();
                    }
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    Log.d(TAG, "Headset audio connected");
                    mIsOnHeadsetSco = true;
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Log.d(TAG, "Headset audio disconnected");
                    mIsOnHeadsetSco = false;
                }
            }
        }
    };
}