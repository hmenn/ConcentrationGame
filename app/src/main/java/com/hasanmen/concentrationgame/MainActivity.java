package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_KEY = "MainActivity";

    private ImageButton btn_init = null;
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<PixabayImage> pixabayImages = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private TableLayout tableLayout = null;
    private ProgressBar progressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_init = (ImageButton) findViewById(R.id.imgBtn_init);
        tableLayout = (TableLayout) findViewById(R.id.btnArea);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);


        btn_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    ImageListDownTask imageListDownTask = new ImageListDownTask(8);
                    imageListDownTask.execute();

                   /* ImageListDownThread imageListDownThread = new ImageListDownThread(pixabayImages); // run thread to download list
                    imageListDownThread.start();

                    // TODO: ADD LOADING IMAGE

                    imageListDownThread.join(); // wait until image list downloaded

                    ImageDownThread imageDownThread = new ImageDownThread(pixabayImages.get(0),bitmaps);
                    imageDownThread.start();

                    imageDownThread.join();
                    Log.d(LOG_KEY,"Downloaded "+bitmaps.size());

                    btn_init.setImageBitmap(bitmaps.get(0));*/

                } catch (Exception e) {
                    Log.d(LOG_KEY, "ImageListDownThread " + e.getMessage());
                }

            }
        });

    }


    private class ImageListDownTask extends AsyncTask<Void, Void, Void> {
        private final static String LOG_KEY = "ImageListDownThread";
        private final static String PIXABAY_API_KEY = "5429432-e78aeb946ac4de95966734b5c";
        private final static String REQUEST = "https://pixabay.com/api/?key=5429432-e78aeb946ac4de95966734b5c";

        private URL url = null;
        private HttpURLConnection urlConnection = null;

        private int imageNumber; // default different image number

        public ImageListDownTask(int imageNumber) {
            super();
            this.imageNumber = imageNumber;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            initBtnArea(4);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(10);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... params) {

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
                pixabayImages = parsePixaJSON(jsonMsg.toString());
                Log.d(LOG_KEY, "Size:" + pixabayImages.size());
            } catch (Exception e) {
                Log.e(LOG_KEY, e.getMessage());
            } finally {
                // close url connection
                urlConnection.disconnect();
            }
            return null;
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
                    image.setWebformatURL(jsonImageArray.getJSONObject(i).getString("webformatURL"));
                    images.add(image);
                    Log.d(LOG_KEY, "Image:" + image.toString());
                }
            } catch (Exception e) {
                Log.e(LOG_KEY, e.getMessage());
            }

            return images;
        }

        private void initBtnArea(int n) {

            for (int i = 0; i < n; ++i) {
                TableRow tableRow = new TableRow(MainActivity.this);
                tableLayout.addView(tableRow);

                for (int j = 0; j < n; ++j) {
                    Button btn = new Button(MainActivity.this);
                    btn.setText("Btn" + j * i + j);

                    //buttons.add(btn);
                    tableRow.addView(btn);
                }
            }

        }
    }
}
