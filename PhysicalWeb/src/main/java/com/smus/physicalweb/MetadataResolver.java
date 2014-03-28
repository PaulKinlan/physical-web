package com.smus.physicalweb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.util.ArrayList;
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
  String METADATA_URL = "http://url-caster.appspot.com/resolve-scan";

  Map<String, String> mDeviceUrlMap;
  private RequestQueue queue;
    private String url;

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

  public void getBatchMetadata(ArrayList<NearbyDevice> mDeviceBatchList) {
      JSONObject jsonObj = createRequestObject(mDeviceBatchList);

      HashMap<String, NearbyDevice> deviceMap = new HashMap<String, NearbyDevice>();

      for(int dIdx = 0; dIdx < mDeviceBatchList.size(); dIdx++) {
          NearbyDevice nearbyDevice = mDeviceBatchList.get(dIdx);
          deviceMap.put(nearbyDevice.getUrl(), nearbyDevice);
      }

      JsonObjectRequest jsObjRequest = createMetadataRequest(jsonObj, deviceMap);

      // Queue the request
      queue.add(jsObjRequest);

  }

  public void getMetadata(NearbyDevice device) {
    ArrayList<NearbyDevice> devices = new ArrayList<NearbyDevice>();
    devices.add(device);

    getBatchMetadata(devices);
  }

    private JsonObjectRequest createMetadataRequest(JSONObject jsonObj, final HashMap<String, NearbyDevice> deviceMap) {
        return new JsonObjectRequest(
                METADATA_URL,
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
                                String description = "Unknown description";
                                String iconUrl = "/favicon.ico";
                                String id = deviceData.getString("id");


                                if(deviceData.has("title")) {
                                   title = deviceData.getString("title");
                                }

                                if(deviceData.has("url")) {
                                    url = deviceData.getString("url");
                                }

                                if(deviceData.has("description")) {
                                   description = deviceData.getString("description");
                                }

                                if(deviceData.has("favicon_url")) {
                                    // We might need to do some magic here.
                                    Uri fullUri = Uri.parse(url);
                                    iconUrl = deviceData.getString("favicon_url");

                                    if(!iconUrl.startsWith("http")) {
                                        // Lets just assume we are dealing with a path

                                        Uri.Builder builder = fullUri.buildUpon();
                                        // just change the path
                                        builder.path(iconUrl);

                                        iconUrl = builder.toString();
                                    }

                                }

                                DeviceMetadata deviceMetadata = new DeviceMetadata();
                                deviceMetadata.title = title;
                                deviceMetadata.description = description;
                                deviceMetadata.siteUrl = url;
                                deviceMetadata.iconUrl = iconUrl;
                                //deviceMetadata.icon = downloadIcon(iconUrl);

                                // Look up the device from the input and update the data
                                deviceMap.get(id).onDeviceInfo(deviceMetadata);
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
    }

    private JSONObject createRequestObject(ArrayList<NearbyDevice> devices) {
        JSONObject jsonObj = new JSONObject();

        try {
            JSONArray urlArray = new JSONArray();

            for(int dIdx = 0; dIdx < devices.size(); dIdx++) {
                NearbyDevice device = devices.get(dIdx);

                JSONObject urlObject = new JSONObject();

                urlObject.put("url", device.getUrl());
                urlObject.put("rssi", device.getLastRSSI());
                urlArray.put(urlObject);
            }


            JSONObject location = new JSONObject();

            location.put("lat", 49.129837);
            location.put("lon", 120.38142);

            jsonObj.put("location",  location);
            jsonObj.put("objects", (Object) urlArray);

        }
        catch(JSONException ex) {

        }
        return jsonObj;
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
