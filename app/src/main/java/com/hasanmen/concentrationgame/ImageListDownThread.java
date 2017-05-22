package com.hasanmen.concentrationgame;

import android.util.Log;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by hmenn on 22.05.2017.
 */

public class ImageListDownThread extends Thread {

    private final static String LOG_KEY = "ImageListDownThread";
    private final static String PIXABAY_API_KEY = "5429432-e78aeb946ac4de95966734b5c";
    private final static String REQUEST = "https://pixabay.com/api/?key=5429432-e78aeb946ac4de95966734b5c";

    private URL url = null;
    private HttpURLConnection urlConnection = null;
    private ArrayList<PixabayImage> imageList = new ArrayList<>();

    private int imageNumber; // default different image number

    //https://pixabay.com/api/?key=5429432-e78aeb946ac4de95966734b5c&q=yellow+flowers&image_type=photo&pretty=true

    public ImageListDownThread() {
        imageNumber = 4;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    public void run() {

        StringBuilder sb = new StringBuilder(REQUEST);

        try {
            // prepare request
            sb.append("&per_page=" + imageNumber);
            sb.append("&q=yellow+cars+red");
            sb.append("&image_type=photo");

            url = new URL(sb.toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            Log.d(LOG_KEY, "Request url:" + sb.toString());

            StringBuilder jsonMsg = new StringBuilder();

            BufferedReader bfr = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            while ((line = bfr.readLine()) != null) {
                jsonMsg.append(line);
            }
            //Log.d(LOG_KEY, "Response JSON:"+jsonMsg.toString());

            // parse json string and get image list
            imageList = parsePixaJSON(jsonMsg.toString());


        } catch (Exception e) {
            Log.e(LOG_KEY, e.getMessage());
        } finally {
            // close url connection
            urlConnection.disconnect();
        }

    }

    private ArrayList<PixabayImage> parsePixaJSON(String json) {
        ArrayList<PixabayImage> images = new ArrayList<>();

        if (json.isEmpty()) {
            Log.e(LOG_KEY, "EMPTY JSON STRING");
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonImageArray = jsonObject.getJSONArray("hits");
            for (int i = 0; i < jsonImageArray.length(); ++i) {
                PixabayImage image = new PixabayImage();
                image.setPreviewURL(jsonImageArray.getJSONObject(i).getString("previewURL"));
                image.setImageHeight(jsonImageArray.getJSONObject(i).getInt("imageHeight"));
                image.setImageWidth(jsonImageArray.getJSONObject(i).getInt("imageWidth"));
                imageList.add(image);
                Log.d(LOG_KEY, "Image:" + image.toString());
            }
        } catch (Exception e) {
            Log.e(LOG_KEY, e.getMessage());
        }

        return images;
    }
}
