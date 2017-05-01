package de.till_s.spotifyshuffleradio.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

/**
 * [Description]
 *
 * @author Till
 * @package de.till_s.spotifyshuffleradio.receiver
 * @date 01.05.2017 - 13:52
 */

public class BluetoothHeadsetManager {

    interface onBluetoothHeadSetListener {

        public void connectedToProxy(BluetoothHeadset aBluetoothHeadset);

        public void disconnectedToProxy();

    }

    private BluetoothHeadset mBluetoothHeadset;

    private BluetoothAdapter mBluetoothAdapter;

    private Context mContext;

    private onBluetoothHeadSetListener mCallBackListener;

    public BluetoothHeadsetManager(Context mContext) {
        this.mContext = mContext;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void addListener(onBluetoothHeadSetListener aListener) {
        this.mCallBackListener = aListener;
    }

    public boolean hasEnableBluetooth() {
        return (mBluetoothAdapter.isEnabled()) ? true : false;
    }

    public void connectionToProxy() {
        mBluetoothAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.HEADSET);

    }

    public void disconnectProxy() {
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
    }

    private BluetoothHeadset getBTHandSetInstance() {
        return mBluetoothHeadset;
    }

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
                mCallBackListener.connectedToProxy(getBTHandSetInstance());
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
                mCallBackListener.disconnectedToProxy();
            }
        }
    };

}
