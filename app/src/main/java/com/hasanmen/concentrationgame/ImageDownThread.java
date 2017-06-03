package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.widget.Button;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by hmenn on 23.05.2017.
 */

public class ImageDownThread extends Thread {

    private static final String LOG_KEY = "DownloadWorker";

    private PixabayImage image;
    private URL url;
    private HttpURLConnection httpURLConnection = null;
    private ArrayList<PixabayImage> randomList;
    private static Object lock = new Object();
    private StringBuilder sb = new StringBuilder();
    private long execTime;

    public ImageDownThread(PixabayImage image, ArrayList<PixabayImage> randomList) {
        this.randomList = randomList;
        this.image = image;
    }

    public void run() {

        try {
            execTime = System.currentTimeMillis();
            sb.append(" Start:").append(new SimpleDateFormat("HH:mm:ss:SSS ").format(Calendar.getInstance().getTime()));
            // open connection
            url = new URL(image.getPreviewURL());
            //Log.d(LOG_KEY,"D:"+image.getPreviewURL());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // download image and convert to bitmap object
            InputStream inputStream = httpURLConnection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            synchronized (lock) {
                image.setBitmap(bitmap);
                randomList.add(image);
            }

        } catch (Exception e) {

        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();

            execTime = System.currentTimeMillis() -execTime;
            sb.append("-- End:").append(new SimpleDateFormat("HH:mm:ss:SSS ").format(Calendar.getInstance().getTime()));
            sb.append("-- Execution Time(ms):").append(String.valueOf(execTime));
            Log.d(LOG_KEY,sb.toString());
        }

    }
}
