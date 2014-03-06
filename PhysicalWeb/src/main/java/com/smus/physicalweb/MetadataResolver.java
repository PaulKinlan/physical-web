package com.smus.physicalweb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Patterns;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    mDeviceUrlMap.put("OLP425-ECB5", "http://z3.ca/1");
  }

  public String getURLForDevice(NearbyDevice device) {
    // If the device name is already a URL, use it.
    String deviceName = device.getName();
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

  public String getIconUrl(Document doc) {
    // If there's an explicit favicon referenced, get it.
    Element element = doc.head().select("link[href~=.*\\.(ico|png)]").first();
    String url = null;
    if (element != null) {
      url = element.attr("href");
      // The icon might be a relative URL.
      if (!url.startsWith("http")) {
        // Prepend the URL of the doc.
        url = doc.baseUri() + url;
      }
    } else {
      url = doc.baseUri() + "favicon.ico";
    }
    return url;
  }

  private class GetMetadataTask extends AsyncTask<String, Void, DeviceMetadata> {

    @Override
    protected DeviceMetadata doInBackground(String... urlArgs) {
      String url = urlArgs[0];
      if (url == null) {
        return null;
      }
      DeviceMetadata deviceMetadata = new DeviceMetadata();
      try {
        Document doc = Jsoup.connect(url).get();
        deviceMetadata.title = doc.title();
        deviceMetadata.siteUrl = url;
        Elements descriptions = doc.select("meta[name=description]");
        if (descriptions.size() > 0) {
          deviceMetadata.description = descriptions.get(0).attr("content");
        }

        deviceMetadata.iconUrl = getIconUrl(doc);
        deviceMetadata.icon = downloadIcon(deviceMetadata.iconUrl);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return deviceMetadata;
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

  public Bitmap downloadIcon(String url) throws IOException {
    InputStream is = (InputStream) new URL(url).getContent();
    Bitmap d = BitmapFactory.decodeStream(is);
    return d;
  }

  public interface OnMetadataListener {
    public void onDeviceInfo(DeviceMetadata deviceMetadata);
  }
}
