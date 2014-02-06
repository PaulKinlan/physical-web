package com.smus.physicalweb;

import android.os.AsyncTask;
import android.util.Patterns;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smus on 1/24/14.
 */
public class MetadataResolver {
  String TAG = "MetadataResolver";

  Map<String, String> mDeviceUrlMap;
  private OnMetadataListener mMetadataListener;

  MetadataResolver() {
    mDeviceUrlMap = new HashMap<String, String>();
    mDeviceUrlMap.put("OLP425-ECF5", "http://z3.ca/light");
  }

  public String getURLForDevice(NearbyDevice device) {
    // If the device name is already a URL, use it.
    String deviceName = device.getBluetoothDevice().getName();
    String url = deviceName;
    if (Patterns.WEB_URL.matcher(deviceName).matches()) {
      // TODO(smus): Fix this hack.
      // For now, if there's no scheme present, add a default http:// scheme.
      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://" + url;
      }
    } else {
      // Otherwise, try doing the lookup.
      url = mDeviceUrlMap.get(deviceName);
    }
    return url;
  }

  public void getMetadata(String urlString, OnMetadataListener listener) {
    GetMetadataTask t = new GetMetadataTask();
    t.execute(urlString);
    mMetadataListener = listener;
  }

  private class GetMetadataTask extends AsyncTask<String, Void, DeviceMetadata> {

    @Override
    protected DeviceMetadata doInBackground(String... urlArgs) {
      String url = urlArgs[0];
      if (url == null) {
        return null;
      }
      try {
        Document doc = Jsoup.connect(url).get();
        DeviceMetadata deviceMetadata = new DeviceMetadata();
        deviceMetadata.title = doc.title();
        return deviceMetadata;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }


    @Override
    protected void onPostExecute(DeviceMetadata result) {
      if (result == null) {
        result = new DeviceMetadata();
        result.title = "Error.";
      }
      mMetadataListener.onDeviceInfo(result);
    }

  }

  public interface OnMetadataListener {
    public void onDeviceInfo(DeviceMetadata deviceMetadata);
  }
}
