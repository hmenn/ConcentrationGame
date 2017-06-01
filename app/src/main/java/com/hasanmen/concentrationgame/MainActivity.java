package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_KEY = "MainActivity";

    private Button btnStart = null;
    private ArrayList<ImageButton> buttons = new ArrayList<>();
    private ArrayList<PixabayImage> pixabayImages = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private TableLayout tableLayout = null;
    private ProgressBar progressBar = null;

    private int clickNum = 0;
    private ImageButton click2Btn;
    private ImageButton click1Btn;
    private ArrayList<PixabayImage> order = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btn_start);
        tableLayout = (TableLayout) findViewById(R.id.btnArea);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ImageListDownTask imageListDownTask = new ImageListDownTask();
                    imageListDownTask.execute(4);
                } catch (Exception e) {
                    Log.d(LOG_KEY, "ImageListDownThread " + e.getMessage());
                }

            }
        });

        // init 4*4 board
        //initBtnArea(4);

    }

    private void initBtnArea(int n) {

        tableLayout.removeAllViews();
        for (int i = 0; i < n; ++i) {
            TableRow tableRow = new TableRow(MainActivity.this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            tableLayout.addView(tableRow);

            for (int j = 0; j < n; ++j) {
                ImageButton btn = new ImageButton(MainActivity.this);
                btn.setId(i * n + j);
                btn.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f
                ));

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this,String.valueOf(clickedBtn.getId()),Toast.LENGTH_SHORT).show();
                        ImageButton clickedBtn = (ImageButton) v;
                        gameLogic(clickedBtn);
                    }
                });

                buttons.add(btn);
                tableRow.addView(btn);
            }
        }
    }

    private void gameLogic(ImageButton imgBtn) {

        if (clickNum == 0) {
            ++clickNum;
            click1Btn = imgBtn;
            click1Btn.setImageBitmap(order.get(imgBtn.getId()).getBitmap());
        } else if(clickNum==1){
            click2Btn = imgBtn;
            //imgBtn.setImageBitmap(bitmaps.get(imgBtn.getId()));
            final PixabayImage image1 = order.get(imgBtn.getId());
            final PixabayImage image2 = order.get(click1Btn.getId());

            imgBtn.setImageBitmap(image1.getBitmap());

            if (image1.equals(image2)) { // if images are equal, open two button
                click1Btn.setEnabled(false);
                click2Btn.setEnabled(false);
            }else{
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        click1Btn.setImageBitmap(null);
                        click2Btn.setImageBitmap(null);
                    }
                },300);
            }
            --clickNum;
        }

    }

    private class ImageListDownTask extends AsyncTask<Integer, Void, Integer> {
        private final static String LOG_KEY = "ImageListDownThread";
        private final static String PIXABAY_API_KEY = "5429432-e78aeb946ac4de95966734b5c";
        private final static String REQUEST = "https://pixabay.com/api/?key=5429432-e78aeb946ac4de95966734b5c";

        private URL url = null;
        private HttpURLConnection urlConnection = null;
        private ArrayList<ImageDownThread> imageDownThreads = new ArrayList<>();

        private int imageNumber; // default different image number

        public ImageListDownTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        protected void onPostExecute(Integer imageNumber) {
            super.onPostExecute(imageNumber);
            progressBar.setVisibility(View.GONE);

            initBtnArea(imageNumber);
           /* for (int i = 0; i < imageNumber * imageNumber; ++i) {
                buttons.get(i).setImageBitmap(order.get(i).getBitmap());
            }*/
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(10);
        }

        @Override
        protected void onCancelled(Integer aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            StringBuilder sb = new StringBuilder(REQUEST);

            try {

                if (params.length == 0)
                    throw new InvalidParameterException();

                imageNumber = params[0] * params[0] / 2;
                // prepare request
                sb.append("&per_page=" + imageNumber);
                sb.append("&q=flowers");
                //sb.append("&image_type=photo");
                sb.append("&order=latest");

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
                Log.d(LOG_KEY, "Download :" + imageNumber + " image address");


                for (int i = 0; i < imageNumber * 2; ++i) {
                    ImageDownThread imageDownThread = new ImageDownThread(pixabayImages.get(i / 2),order);
                    Log.d(LOG_KEY, pixabayImages.get(i / 2).toString());
                    imageDownThreads.add(imageDownThread);
                    imageDownThread.start();
                }

                for (int i = 0; i < imageNumber * 2; ++i) {
                    imageDownThreads.get(i).join();
                    Log.d(LOG_KEY, "Thread " + i + " joined");
                }

            } catch (Exception e) {
                Log.e(LOG_KEY, e.getMessage());
            } finally {
                // close url connection
                urlConnection.disconnect();
            }
            return params[0];
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
                    //Log.d(LOG_KEY, "Image:" + image.toString());
                }
            } catch (Exception e) {
                Log.e(LOG_KEY, e.getMessage());
            }

            return images;
        }
    }
}
