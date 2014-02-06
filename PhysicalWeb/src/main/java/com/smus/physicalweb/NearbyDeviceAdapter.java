package com.smus.physicalweb;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    BluetoothDevice device = mNearbyDevices.get(position).getBluetoothDevice();
    String address = device.getAddress();
    long id = 0;
    int index = 0;
    for (String token: address.split(":")) {
      long decode = Long.parseLong(token, 16);
      id |= (decode << (index * 8));
      index += 1;
    }
//    Log.i(TAG, String.format("Device Mac %s is %d", address, id));
    return id;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = mActivity.getLayoutInflater().inflate(
        R.layout.listitem_device, null);
    NearbyDevice device = mNearbyDevices.get(position);
    TextView textView = (TextView) view.findViewById(R.id.url);
    textView.setText(device.getUrl());

    DeviceMetadata deviceMetadata = device.getInfo();
    if (deviceMetadata != null) {
      TextView infoView = (TextView) view.findViewById(R.id.title);
      infoView.setText(deviceMetadata.title);
    }
    return view;
  }

  public void addDevice(final NearbyDevice device) {
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mNearbyDevices.add(device);
        notifyDataSetChanged();
      }
    });
  }

  public NearbyDevice getNearbyDevice(BluetoothDevice bluetoothDevice) {
    for (NearbyDevice device : mNearbyDevices) {
      if (device.getBluetoothDevice().equals(bluetoothDevice)) {
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