package com.smus.physicalweb;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements NearbyDeviceManager.OnNearbyDeviceChangeListener {

  private String TAG = "MainActivity";

  private NearbyDeviceManager mDeviceManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, new PlaceholderFragment())
          .commit();
    }

    mDeviceManager = new NearbyDeviceManager(this);
    mDeviceManager.setOnNearbyDeviceChangeListener(this);
    mDeviceManager.startSearchingForDevices();
  }

  @Override
  protected void onDestroy() {
    mDeviceManager.stopSearchingForDevices();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_settings:
        return true;
      case R.id.action_scan:
        mDeviceManager.scanDebug();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDeviceFound(NearbyDevice device) {
    Log.i(TAG, "Found a device: " + device.getBluetoothDevice().getName());
    mDeviceManager.getAdapter().updateListUI();
  }

  @Override
  public void onDeviceLost(NearbyDevice device) {
    Log.i(TAG, "Lost a device: " + device.getBluetoothDevice().getName());
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      MainActivity parentActivity = (MainActivity) getActivity();
      ListView list = (ListView) rootView.findViewById(R.id.devices);
      list.setAdapter(parentActivity.mDeviceManager.getAdapter());
      list.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
          NearbyDevice device = (NearbyDevice) parent.getAdapter().getItem(position);
          String url = device.getUrl();
          if (url != null) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
          } else {
            Toast.makeText(getActivity(), "No URL found.", Toast.LENGTH_SHORT).show();
          }
        }
      });
      return rootView;
    }
  }

}
