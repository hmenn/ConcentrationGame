package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by hmenn on 23.05.2017.
 */

public class ImageDownThread extends Thread {

    private static final String LOG_KEY = "ImageDownThread";

    private PixabayImage image;
    private URL url;
    private HttpURLConnection httpURLConnection;
    private ArrayList<Bitmap> list;
    private Object lock = new Object();

    public ImageDownThread(PixabayImage image, ArrayList<Bitmap> list) {
        this.list = list;
        this.image = image;
    }

    public void run() {

        try {

            // open connection
            url = new URL(image.getPreviewURL());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // download image and convert to bitmap object
            InputStream inputStream = httpURLConnection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            synchronized (lock) {
                list.add(bitmap);
                Log.d(LOG_KEY, "Bitmap added to sysnchronized list from "+getName());
            }

        } catch (Exception e) {

        } finally {
            httpURLConnection.disconnect();
        }

    }
}
