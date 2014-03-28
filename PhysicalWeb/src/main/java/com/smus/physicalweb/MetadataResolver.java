package com.smus.physicalweb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by smus on 1/24/14.
 */
public class MetadataResolver {
  String TAG = "MetadataResolver";

  Map<String, String> mDeviceUrlMap;
  private OnMetadataListener mMetadataListener;
  private RequestQueue queue;

  MetadataResolver(Context context) {
    mDeviceUrlMap = new HashMap<String, String>();
    mDeviceUrlMap.put("OLP425-ECF5", "http://z3.ca/light");
    mDeviceUrlMap.put("OLP425-ECB5", "http://z3.ca/1");

    queue = Volley.newRequestQueue(context);
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

  public void getMetadata(String urlString, int mLastRSSI, OnMetadataListener listener) {

    // Request.
    String url = "http://url-caster.appspot.com/resolve-scan";

    JSONObject jsonObj = new JSONObject();

    try {
        JSONArray urlArray = new JSONArray();
        JSONObject urlObject = new JSONObject();
        urlObject.put("url", urlString);
        urlObject.put("rssi", mLastRSSI);
        urlArray.put(0, urlObject );

        JSONObject location = new JSONObject();

        location.put("lat", 49.129837);
        location.put("lon", 120.38142);

        jsonObj.put("location",  location);
        jsonObj.put("objects", (Object) urlArray);

    }
    catch(JSONException ex) {

    }

    JsonObjectRequest jsObjRequest = new JsonObjectRequest(
            url,
            jsonObj,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonResponse) {

                    try {
                        JSONArray foundMetaData = jsonResponse.getJSONArray("metadata");

                        int deviceCount = foundMetaData.length();
                        for(int i = 0; i < deviceCount; i++) {

                            JSONObject deviceData = foundMetaData.getJSONObject(i);

                            String title = "Unknown name";
                            String url = "Unknown url";
                            if(deviceData.has("title")) {
                               title = deviceData.getString("title");
                            }

                            if(deviceData.has("url")) {
                                url = deviceData.getString("url");
                            }

                            DeviceMetadata deviceMetadata = new DeviceMetadata();
                            deviceMetadata.title = title;
                            deviceMetadata.description = "TEST DESCRIPTION";
                            deviceMetadata.siteUrl = url;

                            mMetadataListener.onDeviceInfo(deviceMetadata);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    return;
                }
            },
            new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    return;
                }
            }
        );



    mMetadataListener = listener;
    queue.add(jsObjRequest);

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
