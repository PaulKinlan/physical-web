package com.smus.physicalweb;

import android.bluetooth.BluetoothDevice;

/**
 * Represents a nearby device.
 *
 * Created by smus on 1/24/14.
 */
public class NearbyDevice implements MetadataResolver.OnMetadataListener {

  String TAG = "NearbyDevice";

  private BluetoothDevice mBluetoothDevice;
  private long mLastSeen;
  private int mLastRSSI;
  private DeviceMetadata mDeviceMetadata;
  private String mUrl;
  private NearbyDeviceAdapter mAdapter;

  public NearbyDevice(BluetoothDevice bluetoothDevice, int RSSI) {
    mBluetoothDevice = bluetoothDevice;
    mLastRSSI = RSSI;
    mLastSeen = System.nanoTime();

    MetadataResolver resolver = new MetadataResolver();
    mUrl = resolver.getURLForDevice(this);
  }

  // Constructor for testing purposes only.
  public NearbyDevice(String url) {
    mUrl = url;
    mLastSeen = System.nanoTime();
  }

  public void setAdapter(NearbyDeviceAdapter adapter) {
    mAdapter = adapter;
  }

  public int getLastRSSI() { return mLastRSSI; }
  public DeviceMetadata getInfo() { return mDeviceMetadata; }
  public String getUrl() { return mUrl; }
  public String getName() {
    if (mBluetoothDevice != null) {
      return mBluetoothDevice.getName();
    } else {
      return mUrl;
    }
  }

  public void updateLastSeen(int RSSI) {
    mLastSeen = System.nanoTime();
    mLastRSSI = RSSI;
  }

  public boolean isLastSeenAfter(long threshold) {
    long notSeenMs = (System.nanoTime() - mLastSeen) / 1000000;
    return notSeenMs > threshold;
  }

  public boolean downloadMetadata() {
    MetadataResolver resolver = new MetadataResolver();
    if (mUrl == null) {
      return false;
    }
    resolver.getMetadata(mUrl, this);
    return true;
  }

  @Override
  public void onDeviceInfo(DeviceMetadata deviceMetadata) {
    mDeviceMetadata = deviceMetadata;
    if (mAdapter != null) {
      mAdapter.updateListUI();
    }
  }

  public boolean isBroadcastingUrl() {
    return mUrl != null;
  }

  public boolean equalsBluetooth(BluetoothDevice bluetoothDevice) {
    return mBluetoothDevice.equals(bluetoothDevice);
  }
}
