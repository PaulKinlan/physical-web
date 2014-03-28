package com.smus.physicalweb;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by smus on 1/24/14.
 */

public class NearbyDeviceAdapter extends BaseAdapter {
  String TAG = "NearbyDeviceAdapter";

  private ArrayList<NearbyDevice> mNearbyDevices;
  private Activity mActivity;

  NearbyDeviceAdapter(Activity activity) {
    mNearbyDevices = new ArrayList<NearbyDevice>();
    mActivity = activity;
  }

  @Override
  public int getCount() {
    return mNearbyDevices.size();
  }

  @Override
  public Object getItem(int position) {
    return mNearbyDevices.get(position);
  }

  @Override
  public long getItemId(int position) {
    NearbyDevice device = mNearbyDevices.get(position);
    return System.identityHashCode(device);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = mActivity.getLayoutInflater().inflate(
        R.layout.listitem_device, null);
    NearbyDevice device = mNearbyDevices.get(position);

    DeviceMetadata deviceMetadata = device.getInfo();
    if (deviceMetadata != null) {
      TextView infoView = (TextView) view.findViewById(R.id.title);
      infoView.setText(deviceMetadata.title);

      infoView = (TextView) view.findViewById(R.id.url);
      infoView.setText(deviceMetadata.siteUrl);

      infoView = (TextView) view.findViewById(R.id.description);
      infoView.setText(deviceMetadata.description);

      ImageView iconView = (ImageView) view.findViewById(R.id.icon);
      iconView.setImageBitmap(deviceMetadata.icon);
    }
    return view;
  }

  public void addDevice(final NearbyDevice device) {
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mNearbyDevices.add(device);
        device.setAdapter(NearbyDeviceAdapter.this);
        notifyDataSetChanged();
      }
    });
  }

  public NearbyDevice getNearbyDevice(BluetoothDevice bluetoothDevice) {
    for (NearbyDevice device : mNearbyDevices) {
      if (device.equalsBluetooth(bluetoothDevice)) {
        return device;
      }
    }
    return null;
  }

  public ArrayList<NearbyDevice> removeExpiredDevices() {
    // Get a list of devices that we need to remove.
    ArrayList<NearbyDevice> toRemove = new ArrayList<NearbyDevice>();
    for (NearbyDevice device : mNearbyDevices) {
      if (device.isLastSeenAfter(NearbyDeviceManager.MAX_INACTIVE_TIME)) {
        toRemove.add(device);
      }
    }

    // Remove those devices from the list and notify the listener.
    for (final NearbyDevice device : toRemove) {
      mActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mNearbyDevices.remove(device);
          notifyDataSetChanged();
        }
      });
    }
    return toRemove;
  }

  public void updateListUI() {
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }
}